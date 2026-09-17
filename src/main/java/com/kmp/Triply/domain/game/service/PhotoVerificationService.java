package com.kmp.Triply.domain.game.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import com.kmp.Triply.global.exception.CustomException;
import com.kmp.Triply.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 제출된 사진이 해당 스팟에서 찍은 것으로 볼 수 있는지 OpenAI 비전 모델에게 묻는다.
 *
 * 판정을 못 하는 상황(키 미설정·API 장애)에서 통과시키면 아무 사진이나 다 통과한다.
 * 그렇다고 오답 처리하면 우리 쪽 장애로 플레이어가 점수를 잃는다. 그래서 둘 다 하지 않고
 * 503 으로 돌려보내 다시 제출하게 한다. 판정을 아예 끄고 싶으면 enabled=false 로 명시해야 한다.
 */
@Slf4j
@Service
public class PhotoVerificationService {

    private static final String SYSTEM_PROMPT = """
            너는 여행 게임의 사진 인증 심판이다. 플레이어가 특정 장소에 방문했다는 증거로 올린 사진을 판정한다.
            플레이어는 이미 GPS 로 그 장소 반경 안에 있음이 확인됐다. 관광 엽서 같은 각도일 필요는 없지만,
            "그 장소에서 이 미션을 하고 찍었다"고 볼 근거가 사진 안에 보여야 한다.

            먼저 사진에 실제로 보이는 것만 observed 에 적는다. 장소 이름을 알고 있다고 해서 보이지 않는 것을 적지 않는다.
            그 다음 observed 를 근거로만 판정한다. passed=true 는 아래 중 하나일 때만 준다.
              - 미션이나 장소가 요구하는 대상(건물·간판·전시물·조형물·풍경·음식 등)이 사진에 실제로 보인다
              - 그 장소 특유의 환경이 보이고, 미션이 요구하는 행동과 어긋나지 않는다

            아래는 예외 없이 passed=false 다.
              - 스크린샷, 화면이나 인쇄물을 다시 찍은 사진, 인터넷에서 받은 것으로 보이는 이미지
              - 검은 화면, 심한 흔들림, 손가락으로 가린 사진처럼 아무 단서가 없는 사진
              - 천장·바닥·벽·책상·하늘처럼 어디서든 찍을 수 있어 장소를 특정할 수 없는 사진
              - 미션·장소와 무관한 물건이나 풍경
              - 배경에 장소 단서가 없는 인물 사진(셀카 포함)

            confidence 는 "이 사진이 그 장소에서 찍혔다"에 대한 확신이다. 애매하면 낮게 준다.
            장소를 특정할 단서가 사진에 없으면 confidence 를 0.3 이하로 준다.
            reason 은 한 문장으로 쓴다.
            """;

    /** 모델이 말을 붙이거나 필드를 빠뜨리지 못하게 응답 형식을 스키마로 고정한다. */
    private static final Map<String, Object> RESPONSE_FORMAT = Map.of(
            "type", "json_schema",
            "json_schema", Map.of(
                    "name", "photo_verdict",
                    "strict", true,
                    "schema", Map.of(
                            "type", "object",
                            "additionalProperties", false,
                            "required", List.of("observed", "passed", "confidence", "reason"),
                            "properties", Map.of(
                                    "observed", Map.of("type", "string"),
                                    "passed", Map.of("type", "boolean"),
                                    "confidence", Map.of("type", "number"),
                                    "reason", Map.of("type", "string")))));

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String model;
    private final double threshold;
    private final RestClient client;

    public PhotoVerificationService(
            ObjectMapper objectMapper,
            @Value("${mission.photo.verification.enabled}") boolean enabled,
            @Value("${mission.photo.verification.api-key}") String apiKey,
            @Value("${mission.photo.verification.base-url}") String baseUrl,
            @Value("${mission.photo.verification.model}") String model,
            @Value("${mission.photo.verification.threshold}") double threshold) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.model = model;
        this.threshold = threshold;
        this.client = enabled && StringUtils.hasText(apiKey) ? build(baseUrl, apiKey) : null;

        if (!enabled) {
            log.warn("사진 AI 판정이 꺼져 있습니다. 업로드만 하면 모든 사진이 통과합니다. (MISSION_PHOTO_AI_ENABLED)");
        } else if (client == null) {
            log.error("사진 AI 판정이 켜져 있지만 OPENAI_API_KEY 가 없습니다. 사진 미션 제출은 503 으로 거부됩니다.");
        }
    }

    public PhotoVerdict verify(byte[] image, String contentType, Mission mission) {
        if (!enabled) {
            return PhotoVerdict.skipped();
        }
        if (client == null) {
            throw new CustomException(ErrorCode.PHOTO_VERIFICATION_UNAVAILABLE);
        }
        try {
            return parse(objectMapper, call(image, contentType, mission), threshold);
        } catch (Exception first) {
            // 한 번은 일시적인 오류일 수 있다. 그래도 실패하면 통과도 오답도 아닌 503 으로 돌려보낸다.
            log.warn("사진 AI 판정 실패, 재시도합니다. missionId={}", mission.getId(), first);
            try {
                return parse(objectMapper, call(image, contentType, mission), threshold);
            } catch (Exception second) {
                log.error("사진 AI 판정 재시도도 실패했습니다. missionId={}", mission.getId(), second);
                throw new CustomException(ErrorCode.PHOTO_VERIFICATION_UNAVAILABLE);
            }
        }
    }

    private String call(byte[] image, String contentType, Mission mission) {
        Map<String, Object> body = Map.of(
                "model", model,
                // gpt-5 계열은 추론 토큰도 이 한도에서 쓴다. 너무 낮으면 본문이 빈 채로 끊긴다.
                "max_completion_tokens", 2000,
                "response_format", RESPONSE_FORMAT,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", question(mission)),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", dataUrl(image, contentType)))))));

        JsonNode response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("판정 응답이 비어 있습니다.");
        }
        JsonNode choice = response.path("choices").path(0);
        String finishReason = choice.path("finish_reason").asText("");
        if ("length".equals(finishReason)) {
            throw new IllegalStateException("판정 응답이 토큰 한도에서 잘렸습니다.");
        }
        return choice.path("message").path("content").asText("");
    }

    private static String dataUrl(byte[] image, String contentType) {
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(image);
    }

    private static String question(Mission mission) {
        CourseSpot spot = mission.getCourseSpot();
        String place = spot.getTourismSpot() != null ? spot.getTourismSpot().getName() : "해당 스팟";
        return """
                장소: %s
                미션: %s
                이 사진이 위 장소에서 위 미션을 수행하고 찍은 인증 사진으로 인정될 수 있는지 판정해라.
                """.formatted(place, mission.getQuestion() == null ? "사진 인증" : mission.getQuestion());
    }

    /** 스키마를 걸어도 응답이 망가질 수 있다. 읽어내지 못한 값은 "판정 못 함"이지 통과가 아니다. */
    static PhotoVerdict parse(ObjectMapper objectMapper, String raw, double threshold) throws Exception {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("판정 응답에서 JSON 을 찾지 못했습니다: " + raw);
        }
        JsonNode node = objectMapper.readTree(raw.substring(start, end + 1));
        if (!node.hasNonNull("passed") || !node.hasNonNull("confidence")) {
            throw new IllegalStateException("판정 응답에 passed/confidence 가 없습니다: " + raw);
        }
        double confidence = node.path("confidence").asDouble(0.0);
        boolean passed = node.path("passed").asBoolean(false) && confidence >= threshold;
        return new PhotoVerdict(passed, confidence,
                node.path("observed").asText(""), node.path("reason").asText(""));
    }

    private static RestClient build(String baseUrl, String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .requestFactory(factory)
                .build();
    }
}
