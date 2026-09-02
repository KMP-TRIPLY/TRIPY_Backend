package com.kmp.Triply.domain.game.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlockParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmp.Triply.domain.course.entity.CourseSpot;
import com.kmp.Triply.domain.course.entity.Mission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.List;

/**
 * 제출된 사진이 해당 스팟에서 찍은 것으로 볼 수 있는지 Claude 에게 묻는다.
 *
 * 판정은 비결정적이라 같은 사진에 다른 결과가 나올 수 있다. 그래서 (1) 임계값을 설정으로 빼고
 * (2) 통째로 끌 수 있게 했다. 끄면 업로드 성공만으로 통과하며 회귀 테스트가 결정적으로 돈다.
 * 호출이 실패하면 통과시킨다 — 외부 장애로 플레이어가 게임을 진행하지 못하는 편이 더 나쁘다.
 */
@Slf4j
@Service
public class PhotoVerificationService {

    private static final String SYSTEM_PROMPT = """
            너는 여행 게임의 사진 인증 심판이다. 플레이어가 특정 장소에 방문했다는 증거로 올린 사진을 본다.
            플레이어는 이미 GPS 로 그 장소 반경 안에 있음이 확인된 상태다. 따라서 정확히 같은 각도의
            관광 엽서 사진일 필요는 없고, 그 장소 또는 그 주변에서 찍었다고 볼 수 있으면 통과다.

            명백히 거부할 것: 실내 스크린샷, 다른 사진의 재촬영, 인터넷에서 받은 이미지,
            검은 화면이나 손가락으로 가린 사진처럼 아무 정보가 없는 사진, 전혀 무관한 장소.

            반드시 아래 JSON 하나만 출력한다. 다른 말은 쓰지 않는다.
            {"passed": true|false, "confidence": 0.0~1.0, "reason": "한 문장"}
            """;

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String model;
    private final double threshold;
    private final AnthropicClient client;

    public PhotoVerificationService(
            ObjectMapper objectMapper,
            @Value("${mission.photo.verification.enabled}") boolean enabled,
            @Value("${mission.photo.verification.api-key}") String apiKey,
            @Value("${mission.photo.verification.model}") String model,
            @Value("${mission.photo.verification.threshold}") double threshold) {
        this.objectMapper = objectMapper;
        this.model = model;
        this.threshold = threshold;
        this.enabled = enabled && StringUtils.hasText(apiKey);
        this.client = this.enabled ? AnthropicOkHttpClient.builder().apiKey(apiKey).build() : null;

        if (enabled && !this.enabled) {
            log.warn("사진 AI 판정이 켜져 있지만 ANTHROPIC_API_KEY 가 없습니다. 업로드만으로 통과시킵니다.");
        }
    }

    public PhotoVerdict verify(byte[] image, String contentType, Mission mission) {
        if (!enabled) {
            return PhotoVerdict.skipped();
        }
        try {
            return parse(callClaude(image, contentType, mission));
        } catch (Exception e) {
            // 외부 호출 실패로 게임을 막지 않는다. 대신 로그에 남겨 사후 검토가 가능하게 한다.
            log.error("사진 AI 판정 실패. missionId={}", mission.getId(), e);
            return new PhotoVerdict(true, 0.0, "판정 실패로 통과 처리: " + e.getClass().getSimpleName());
        }
    }

    private String callClaude(byte[] image, String contentType, Mission mission) {
        ImageBlockParam imageBlock = ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .mediaType(mediaType(contentType))
                        .data(Base64.getEncoder().encodeToString(image))
                        .build())
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(2000L)
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
                .system(SYSTEM_PROMPT)
                .addUserMessageOfBlockParams(List.of(
                        ContentBlockParam.ofImage(imageBlock),
                        ContentBlockParam.ofText(TextBlockParam.builder()
                                .text(question(mission))
                                .build())))
                .build();

        Message response = client.messages().create(params);
        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(text -> text.text())
                .reduce("", String::concat);
    }

    private static String question(Mission mission) {
        CourseSpot spot = mission.getCourseSpot();
        String place = spot.getTourismSpot() != null ? spot.getTourismSpot().getName() : "해당 스팟";
        return """
                장소: %s
                미션: %s
                이 사진이 위 장소에서 찍은 인증 사진으로 인정될 수 있는지 판정해라.
                """.formatted(place, mission.getQuestion() == null ? "사진 인증" : mission.getQuestion());
    }

    private PhotoVerdict parse(String raw) throws Exception {
        // 모델이 JSON 앞뒤에 말을 붙일 수 있으므로 첫 중괄호 구간만 잘라 쓴다
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("판정 응답에서 JSON 을 찾지 못했습니다: " + raw);
        }
        JsonNode node = objectMapper.readTree(raw.substring(start, end + 1));
        double confidence = node.path("confidence").asDouble(0.0);
        String reason = node.path("reason").asText("");
        boolean passed = node.path("passed").asBoolean(false) && confidence >= threshold;
        return new PhotoVerdict(passed, confidence, reason);
    }

    private static Base64ImageSource.MediaType mediaType(String contentType) {
        return "image/png".equals(contentType)
                ? Base64ImageSource.MediaType.IMAGE_PNG
                : Base64ImageSource.MediaType.IMAGE_JPEG;
    }
}
