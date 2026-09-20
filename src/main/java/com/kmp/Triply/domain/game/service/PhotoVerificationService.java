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
            너는 여행 게임의 사진 인증 심판이다. 팀이 미션을 수행했다는 증거로 올린 사진을 판정한다.

            플레이어가 그 장소 반경 안에 있다는 것은 GPS 로 이미 확인됐다. 그러니 사진으로 장소를 다시
            증명할 필요는 없다. 네가 볼 것은 하나다 — 미션 문구가 시킨 것을 했는가.

            먼저 사진에 보이는 것만 observed 에 적는다. 아는 것을 적지 말고 보이는 것을 적는다.
            그 다음 미션 문구와 맞춰본다.
              - "팀 전원 인증샷" 처럼 사람을 찍으라는 미션이면 사람이 보이면 통과다. 배경이 어디인지
                알아볼 수 없어도 된다. 실내 미션이면 실내 사진이 정상이고, 인원수는 따지지 않는다.
              - "현판이 보이도록", "OO 앞에서" 처럼 대상을 지정한 미션이면 그 대상이 보여야 한다.
                각도·밝기가 달라도 그것으로 보이면 통과다.
              - 미션이 장소만 말하면, 그 장소에서 찍었다고 해도 이상하지 않은 사진이면 통과다.

            아래만 passed=false 다.
              - 스크린샷, 화면이나 인쇄물을 다시 찍은 사진, 인터넷에서 받은 것으로 보이는 이미지
              - 검은 화면, 손으로 가린 사진처럼 아무것도 보이지 않는 사진
              - 미션이 요구한 것이 명백히 없는 사진 (사람을 찍으라는데 사람이 없다, 현판을 찍으라는데
                그 비슷한 것도 없다)

            애매하면 통과시킨다. 플레이어는 이미 현장에 있다. 확신이 없다는 이유로 떨어뜨리면 실제로
            미션을 해낸 팀이 게임을 못 한다.

            confidence 는 "미션을 수행한 사진이다" 에 대한 확신이다. 통과시킬 때는 그 확신을 적고,
            거부할 때는 0.3 이하를 준다.
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
                미션: %s
                (참고 — 팀이 있는 장소: %s. GPS 로 확인된 값이라 사진으로 다시 확인할 필요는 없다.)
                이 사진이 위 미션을 수행하고 찍은 것으로 인정될 수 있는지 판정해라.
                """.formatted(mission.getQuestion() == null ? "사진 인증" : mission.getQuestion(), place);
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
