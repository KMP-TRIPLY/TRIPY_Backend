package com.kmp.Triply.domain.tourism.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private final TourismApiCacheRepository tourismApiCacheRepository;
    private final TourismSpotRepository tourismSpotRepository;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Value("${tourism.api.key}")
    private String apiKey;

    @Value("${tourism.api.base-url}")
    private String baseUrl;

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

    private List<RecommendationResponse> fetchBySigungu(SigunguKey key) {
        String cacheKey = "SIGUNGU_" + key.signguCd();
        TourismApiCache cache = tourismApiCacheRepository.findByContentId(cacheKey).orElse(null);

        if (cache != null && !cache.isExpired()) {
            return parseItemsFromArray(cache.getRawJson());
        }

        List<RecommendationResponse> items = callApiAllPages(key);
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

    private List<RecommendationResponse> callApiAllPages(SigunguKey key) {
        // DataLab 데이터는 약 1개월 후행 → 전월 기준으로 조회
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        String baseYm = String.format("%04d%02d", lastMonth.getYear(), lastMonth.getMonthValue());

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
                                spot -> spot.update(item.getTitle(), category,
                                        item.getCategoryLarge(), item.getCategoryMiddle(), address,
                                        lat, lng, null, item.getAreaCd(), item.getRank()),
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
        return new PageResponse<>(filtered, page, size);
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
