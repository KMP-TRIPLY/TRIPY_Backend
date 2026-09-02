package com.kmp.Triply.domain.tourism.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.tourism.dto.response.NearbyTourismSpotResponse;
import com.kmp.Triply.domain.tourism.dto.response.RecommendationResponse;
import com.kmp.Triply.global.common.PageResponse;
import com.kmp.Triply.domain.tourism.entity.ApiType;
import com.kmp.Triply.domain.tourism.entity.SpotCategory;
import com.kmp.Triply.domain.tourism.entity.TourismApiCache;
import com.kmp.Triply.domain.tourism.entity.TourismSpot;
import com.kmp.Triply.domain.tourism.repository.TourismApiCacheRepository;
import com.kmp.Triply.domain.tourism.repository.TourismSpotRepository;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourismApiServiceImpl implements TourismApiService {

    // areaCd(국가행정코드) + signguCd(5자리 시군구코드) 쌍
    private static final List<SigunguKey> CHUNGCHEONG_SIGUNGUS = List.of(
            new SigunguKey("43", "43111"),  // 충청북도 청주시 상당구
            new SigunguKey("43", "43130"),  // 충청북도 충주시
            new SigunguKey("43", "43150"),  // 충청북도 제천시
            new SigunguKey("44", "44131"),  // 충청남도 천안시 동남구
            new SigunguKey("44", "44150"),  // 충청남도 공주시
            new SigunguKey("44", "44200"),  // 충청남도 아산시
            new SigunguKey("30", "30110"),  // 대전광역시 동구
            new SigunguKey("30", "30170"),  // 대전광역시 서구
            new SigunguKey("30", "30200"),  // 대전광역시 유성구
            new SigunguKey("36", "36110")   // 세종특별자치시
    );

    private static final int EXTERNAL_PAGE_SIZE = 100;

    // 사진 매칭용 조회 범위. 좌표가 조금 어긋나도 잡히게 넉넉히 두고, 이름으로 걸러낸다.
    private static final int IMAGE_SEARCH_RADIUS_METERS = 2000;
    private static final int IMAGE_SEARCH_ROWS = 30;
    // 목록 한 요청에 새로 찾을 사진 개수. 직렬 호출이라 응답 시간과 일일 한도를 함께 막는다.
    private static final int IMAGE_LOOKUP_PER_REQUEST = 5;
    // 접두 일치를 허용할 최소 이름 길이. 짧으면 아무 데나 걸린다.
    private static final int MIN_PREFIX_MATCH_LENGTH = 3;

    // 데이터랩 통계 후행 공개 대비. 이 개월 수까지 거슬러 올라가며 데이터가 있는 달을 찾는다.
    private static final int BASE_YM_LOOKBACK_MONTHS = 4;

    private final TourismApiCacheRepository tourismApiCacheRepository;
    private final TourismSpotRepository tourismSpotRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${tourism.api.key}")
    private String apiKey;

    @Value("${tourism.api.base-url}")
    private String baseUrl;

    @Value("${tourism.api.kor-service-base-url:https://apis.data.go.kr/B551011/KorService2}")
    private String korServiceBaseUrl;

    @Value("${tourism.api.cache-hours:24}")
    private int cacheHours;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RecommendationResponse> getChungcheongRecommendations() {
        List<RecommendationResponse> results = new ArrayList<>();
        for (SigunguKey key : CHUNGCHEONG_SIGUNGUS) {
            results.addAll(fetchBySigungu(key));
        }
        return results;
    }

    /**
     * 스팟 사진 URL. 한 번 찾으면 tourism_spots 에 저장해 다시 부르지 않는다
     * (관광공사 API 는 일일 호출 한도가 있다).
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<String> findThumbnailUrl(String openApiContentId) {
        TourismSpot spot = tourismSpotRepository.findByOpenApiContentId(openApiContentId).orElse(null);
        if (spot == null) {
            return Optional.empty();
        }
        if (StringUtils.hasText(spot.getThumbnailUrl())) {
            return Optional.of(spot.getThumbnailUrl());
        }

        try {
            String image = searchImageByName(spot.getName(), spot.getLng(), spot.getLat());
            if (!StringUtils.hasText(image)) {
                return Optional.empty();
            }
            spot.updateThumbnailUrl(image);
            return Optional.of(image);
        } catch (Exception e) {
            log.warn("스팟 사진 조회 실패. contentId={}, name={}", openApiContentId, spot.getName(), e);
            return Optional.empty();
        }
    }

    /**
     * 좌표 주변에서 이름이 같은 곳의 사진을 찾는다.
     *
     * <p>거리순 1 위를 쓰면 안 된다. 국립공주박물관 좌표에서 222m 지점이 선화당이라
     * 다른 장소 사진이 붙는다. 이름으로 걸러내는 것이 유일한 방어선이다.
     *
     * <p>완전일치를 먼저 보고, 없으면 접두 일치를 쓴다. 실측(rank 1~3 스팟 12곳)에서
     * 완전일치만 2곳, 접두까지 허용하면 4곳이 붙었다. 나머지는 역·백화점·시장처럼
     * 관광정보에 관광지로 없는 곳이라 사진이 없는 게 맞다.
     */
    private String searchImageByName(String name, BigDecimal lng, BigDecimal lat) {
        if (name == null || lng == null || lat == null) {
            return null;
        }

        String rawJson = callLocationBasedList(lng, lat, IMAGE_SEARCH_RADIUS_METERS, null, 1, IMAGE_SEARCH_ROWS);
        String wanted = normalizeSpotName(name);
        String prefixMatch = null;  // 응답이 거리순이라 첫 접두 일치가 가장 가깝다

        for (JsonNode item : parseItemNodes(rawJson)) {
            String image = item.path("firstimage").asText("");
            if (image.isBlank()) {
                continue;
            }
            String title = normalizeSpotName(item.path("title").asText(""));
            if (wanted.equals(title)) {
                return image;
            }
            if (prefixMatch == null && isPrefixMatch(wanted, title)) {
                prefixMatch = image;
            }
        }
        return prefixMatch;
    }

    /**
     * 관광정보 이름이 스팟 이름으로 시작하면 같은 곳으로 본다.
     * ("청남대" → "청남대 가을축제", "대전역" → "대전역 동광장")
     *
     * <p>포함 관계까지 넓히면 안 된다. "공산성" 이 "공주공산성게스트하우스" 에,
     * "대전역" 이 "올리브영 대전역점" 에 붙어 다른 장소 사진이 나간다.
     * 접두로 제한하면 그런 경우가 걸러진다. 짧은 이름은 아무 데나 걸리므로
     * {@value #MIN_PREFIX_MATCH_LENGTH} 자 이상만 본다.
     */
    static boolean isPrefixMatch(String wanted, String title) {
        return wanted.length() >= MIN_PREFIX_MATCH_LENGTH && title.startsWith(wanted);
    }

    /** 괄호 안 부가설명("선화당(공주)")과 공백을 떼고 비교한다. */
    static String normalizeSpotName(String name) {
        return name == null ? "" : name
                .replaceAll("\\(.*?\\)", "")
                .replaceAll("\\[.*?\\]", "")
                .replaceAll("\\s", "");
    }

    private List<JsonNode> parseItemNodes(String rawJson) {
        List<JsonNode> nodes = new ArrayList<>();
        try {
            JsonNode items = objectMapper.readTree(rawJson).path("response").path("body").path("items").path("item");
            if (items.isArray()) {
                items.forEach(nodes::add);
            } else if (items.isObject()) {
                nodes.add(items);
            }
        } catch (Exception e) {
            log.warn("관광정보 응답 파싱 실패");
        }
        return nodes;
    }

    private List<RecommendationResponse> fetchBySigungu(SigunguKey key) {
        String cacheKey = "SIGUNGU_" + key.signguCd();
        TourismApiCache cache = tourismApiCacheRepository.findByContentId(cacheKey).orElse(null);

        if (cache != null && !cache.isExpired()) {
            return parseItemsFromArray(cache.getRawJson());
        }

        List<RecommendationResponse> items = callApiAllPages(key);

        // 빈 결과는 캐싱하지 않는다. 잘못된 서비스키나 잠깐의 장애로 한 번 비어 있으면
        // 그 빈 목록이 cache-hours(기본 24시간) 동안 굳어, 원인을 고쳐도 계속 비어 보인다.
        if (items.isEmpty()) {
            log.warn("관광공사 API 결과가 비어 캐시하지 않는다. signguCd={}", key.signguCd());
            return items;
        }

        String cachedJson = serializeItems(items);

        if (cache == null) {
            tourismApiCacheRepository.save(TourismApiCache.builder()
                    .apiType(ApiType.SPOT)
                    .contentId(cacheKey)
                    .rawJson(cachedJson)
                    .regionCode(key.areaCd())
                    .expiresAt(LocalDateTime.now().plusHours(cacheHours))
                    .build());
        } else {
            cache.refresh(cachedJson, LocalDateTime.now().plusHours(cacheHours));
        }

        upsertSpots(items);
        return items;
    }

    /**
     * 데이터랩 통계는 후행 공개인데 후행 기간이 일정하지 않다.
     * (2026-09-02 확인: 전월 202608 은 totalCount 0, 202607 부터 100 건)
     * 그래서 전월을 고정하지 않고 데이터가 있는 달까지 거슬러 올라간다.
     *
     * <p>ponytail: 최대 {@value #BASE_YM_LOOKBACK_MONTHS} 개월까지만 본다.
     * 후행이 더 길어지면 이 값만 올리면 된다.
     */
    private List<RecommendationResponse> callApiAllPages(SigunguKey key) {
        for (int monthsAgo = 1; monthsAgo <= BASE_YM_LOOKBACK_MONTHS; monthsAgo++) {
            String baseYm = baseYm(monthsAgo);
            List<RecommendationResponse> items = callApiAllPages(key, baseYm);
            if (!items.isEmpty()) {
                return items;
            }
            log.info("관광공사 통계가 아직 없는 기준월. signguCd={}, baseYm={}", key.signguCd(), baseYm);
        }
        return new ArrayList<>();
    }

    static String baseYm(int monthsAgo) {
        LocalDate month = LocalDate.now().minusMonths(monthsAgo);
        return String.format("%04d%02d", month.getYear(), month.getMonthValue());
    }

    private List<RecommendationResponse> callApiAllPages(SigunguKey key, String baseYm) {
        List<RecommendationResponse> all = new ArrayList<>();
        int pageNo = 1;
        int totalCount = Integer.MAX_VALUE;

        while ((long) (pageNo - 1) * EXTERNAL_PAGE_SIZE < totalCount) {
            String rawJson = callApi(key, baseYm, pageNo);
            try {
                JsonNode body = objectMapper.readTree(rawJson).path("response").path("body");
                totalCount = body.path("totalCount").asInt(0);
            } catch (Exception e) {
                log.warn("totalCount 파싱 실패. signguCd={}", key.signguCd());
                break;
            }
            List<RecommendationResponse> page = parseItems(rawJson);
            if (page.isEmpty()) break;
            all.addAll(page);
            pageNo++;
        }
        return all;
    }

    private String callApi(SigunguKey key, String baseYm, int pageNo) {
        String uri = UriComponentsBuilder.fromUriString(baseUrl + "/areaBasedList1")
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", EXTERNAL_PAGE_SIZE)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Triply")
                .queryParam("_type", "json")
                .queryParam("baseYm", baseYm)
                .queryParam("areaCd", key.areaCd())
                .queryParam("signguCd", key.signguCd())
                .build(true)
                .toUriString();

        try {
            String response = restClient.get().uri(uri).retrieve().body(String.class);
            if (response == null) throw new CustomException(ErrorCode.TOURISM_API_ERROR);
            return response;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("관광공사 API 호출 실패. signguCd={}, page={}", key.signguCd(), pageNo, e);
            throw new CustomException(ErrorCode.TOURISM_API_ERROR);
        }
    }

    private String serializeItems(List<RecommendationResponse> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<RecommendationResponse> parseItemsFromArray(String json) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RecommendationResponse.class));
        } catch (Exception e) {
            log.warn("캐시 JSON 배열 파싱 실패, 빈 목록 반환");
            return new ArrayList<>();
        }
    }

    private List<RecommendationResponse> parseItems(String rawJson) {
        List<RecommendationResponse> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || items.isNull() || !items.isArray()) {
                return result;
            }

            for (JsonNode item : items) {
                result.add(RecommendationResponse.builder()
                        .contentId(item.path("hubTatsCd").asText(""))
                        .title(item.path("hubTatsNm").asText(""))
                        .mapX(item.path("mapX").asText(""))
                        .mapY(item.path("mapY").asText(""))
                        .areaCd(item.path("areaCd").asText(""))
                        .areaNm(item.path("areaNm").asText(""))
                        .signguCd(item.path("signguCd").asText(""))
                        .signguNm(item.path("signguNm").asText(""))
                        .categoryLarge(item.path("hubCtgryLclsNm").asText(""))
                        .categoryMiddle(item.path("hubCtgryMclsNm").asText(""))
                        .rank(item.path("hubRank").asInt(0))
                        .build());
            }
        } catch (JsonProcessingException e) {
            log.error("관광 API 응답 JSON 파싱 실패", e);
        }
        return result;
    }

    private void upsertSpots(List<RecommendationResponse> items) {
        for (RecommendationResponse item : items) {
            if (item.getContentId().isBlank()) continue;
            try {
                BigDecimal lat = parseBigDecimal(item.getMapY());
                BigDecimal lng = parseBigDecimal(item.getMapX());
                SpotCategory category = mapCategory(item.getCategoryMiddle());
                String address = item.getSignguNm();

                tourismSpotRepository.findByOpenApiContentId(item.getContentId())
                        .ifPresentOrElse(
                                // 사진 URL 은 통계 API 가 주지 않는다. 별도로 찾아 넣은 값을
                                // null 로 덮으면 갱신마다 사라지므로 있던 값을 그대로 넘긴다.
                                spot -> spot.update(item.getTitle(), category,
                                        item.getCategoryLarge(), item.getCategoryMiddle(), address,
                                        lat, lng, spot.getThumbnailUrl(), item.getAreaCd(), item.getRank()),
                                () -> tourismSpotRepository.save(TourismSpot.builder()
                                        .openApiContentId(item.getContentId())
                                        .name(item.getTitle())
                                        .category(category)
                                        .categoryLarge(item.getCategoryLarge())
                                        .categoryMiddle(item.getCategoryMiddle())
                                        .address(address)
                                        .lat(lat)
                                        .lng(lng)
                                        .thumbnailUrl(null)
                                        .areaCode(item.getAreaCd())
                                        .rank(item.getRank())
                                        .build())
                        );
            } catch (Exception e) {
                log.warn("TourismSpot 저장 실패. contentId={}", item.getContentId(), e);
            }
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return (value == null || value.isBlank()) ? BigDecimal.ZERO : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private SpotCategory mapCategory(String categoryMiddle) {
        if (categoryMiddle == null) return SpotCategory.NATURE;
        return switch (categoryMiddle) {
            case "문화시설" -> SpotCategory.HERITAGE;
            case "축제/행사" -> SpotCategory.FESTIVAL;
            case "쇼핑" -> SpotCategory.SHOP;
            case "음식" -> SpotCategory.FOOD;
            default -> SpotCategory.NATURE;
        };
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PageResponse<RecommendationResponse> getChungcheongRecommendations(String region, int page, int size) {
        String areaCd = resolveAreaCd(region);
        List<RecommendationResponse> all = getChungcheongRecommendations();
        List<RecommendationResponse> filtered = areaCd == null ? all
                : all.stream().filter(r -> areaCd.equals(r.getAreaCd())).toList();

        PageResponse<RecommendationResponse> paged = new PageResponse<>(filtered, page, size);
        fillImageUrls(paged.getContent());
        return paged;
    }

    /**
     * 이 페이지에 담긴 곳들의 사진을 채운다.
     *
     * <p>이미 {@code tourism_spots.thumbnail_url} 에 있는 값은 외부 호출 없이 그대로 쓴다.
     * 없는 것만 국문 관광정보에서 찾고, 찾으면 DB 에 저장해 다음부터는 공짜로 읽는다.
     *
     * <p>ponytail: 한 요청에 새로 찾는 개수를 {@value #IMAGE_LOOKUP_PER_REQUEST} 개로 막는다.
     * 한 페이지(20 개)를 전부 직렬로 부르면 응답이 몇 초씩 걸리고 일일 호출 한도도 빨리 닳는다.
     * 목록을 넘겨 볼수록 채워지고 채운 값은 남으므로 시간이 지나면 다 채워진다.
     * 한 번에 전부 채우려면 배치 워밍업 작업이 필요하다.
     */
    private void fillImageUrls(List<RecommendationResponse> spots) {
        int lookupBudget = IMAGE_LOOKUP_PER_REQUEST;

        for (RecommendationResponse spot : spots) {
            TourismSpot stored = tourismSpotRepository.findByOpenApiContentId(spot.getContentId()).orElse(null);
            if (stored == null) {
                continue;
            }
            if (StringUtils.hasText(stored.getThumbnailUrl())) {
                spot.applyImageUrl(stored.getThumbnailUrl());
                continue;
            }
            if (lookupBudget <= 0) {
                continue;
            }

            lookupBudget--;
            try {
                String image = searchImageByName(stored.getName(), stored.getLng(), stored.getLat());
                if (StringUtils.hasText(image)) {
                    stored.updateThumbnailUrl(image);
                    spot.applyImageUrl(image);
                }
            } catch (Exception e) {
                log.warn("스팟 사진 조회 실패. contentId={}, name={}", spot.getContentId(), stored.getName(), e);
            }
        }
    }

    @Override
    public PageResponse<NearbyTourismSpotResponse> getNearbyTourismSpots(
            BigDecimal mapX,
            BigDecimal mapY,
            int radius,
            String contentTypeId,
            int page,
            int size
    ) {
        String rawJson = callLocationBasedList(mapX, mapY, radius, contentTypeId, page + 1, size);
        return new PageResponse<>(parseNearbyItems(rawJson), page, size, parseTotalCount(rawJson));
    }

    private String callLocationBasedList(BigDecimal mapX, BigDecimal mapY, int radius,
                                         String contentTypeId, int pageNo, int numOfRows) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(korServiceBaseUrl + "/locationBasedList2")
                .queryParam("serviceKey", apiKey)
                .queryParam("numOfRows", numOfRows)
                .queryParam("pageNo", pageNo)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Triply")
                .queryParam("_type", "json")
                .queryParam("arrange", "E")
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", radius);

        if (StringUtils.hasText(contentTypeId)) {
            builder.queryParam("contentTypeId", contentTypeId);
        }

        try {
            String response = restClient.get().uri(builder.build(true).toUriString()).retrieve().body(String.class);
            if (response == null) throw new CustomException(ErrorCode.TOURISM_API_ERROR);
            return response;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("위치 기반 관광공사 API 호출 실패. mapX={}, mapY={}, radius={}", mapX, mapY, radius, e);
            throw new CustomException(ErrorCode.TOURISM_API_ERROR);
        }
    }

    private List<NearbyTourismSpotResponse> parseNearbyItems(String rawJson) {
        List<NearbyTourismSpotResponse> result = new ArrayList<>();
        try {
            JsonNode items = objectMapper.readTree(rawJson)
                    .path("response").path("body").path("items").path("item");

            if (items.isMissingNode() || items.isNull()) {
                return result;
            }

            if (items.isArray()) {
                for (JsonNode item : items) {
                    result.add(toNearbyTourismSpotResponse(item));
                }
                return result;
            }

            result.add(toNearbyTourismSpotResponse(items));
        } catch (JsonProcessingException e) {
            log.error("위치 기반 관광 API 응답 JSON 파싱 실패", e);
        }
        return result;
    }

    private int parseTotalCount(String rawJson) {
        try {
            return objectMapper.readTree(rawJson)
                    .path("response").path("body").path("totalCount")
                    .asInt(0);
        } catch (JsonProcessingException e) {
            log.warn("위치 기반 관광 API totalCount 파싱 실패");
            return 0;
        }
    }

    private NearbyTourismSpotResponse toNearbyTourismSpotResponse(JsonNode item) {
        return NearbyTourismSpotResponse.builder()
                .contentId(item.path("contentid").asText(""))
                .contentTypeId(item.path("contenttypeid").asText(""))
                .title(item.path("title").asText(""))
                .address(item.path("addr1").asText(""))
                .addressDetail(item.path("addr2").asText(""))
                .firstImage(item.path("firstimage").asText(""))
                .firstImageSmall(item.path("firstimage2").asText(""))
                .mapX(item.path("mapx").asText(""))
                .mapY(item.path("mapy").asText(""))
                .distanceMeters(parseDistanceMeters(item.path("dist").asText()))
                .areaCode(item.path("areacode").asText(""))
                .sigunguCode(item.path("sigungucode").asText(""))
                .category1(item.path("cat1").asText(""))
                .category2(item.path("cat2").asText(""))
                .category3(item.path("cat3").asText(""))
                .build();
    }

    private int parseDistanceMeters(String value) {
        try {
            if (!StringUtils.hasText(value)) {
                return 0;
            }
            return new BigDecimal(value).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String resolveAreaCd(String region) {
        if (region == null) return null;
        return switch (region.trim()) {
            case "충북", "충청북도" -> "43";
            case "충남", "충청남도" -> "44";
            case "대전", "대전광역시" -> "30";
            case "세종", "세종시", "세종특별자치시" -> "36";
            default -> null;
        };
    }

    private record SigunguKey(String areaCd, String signguCd) {}
}
