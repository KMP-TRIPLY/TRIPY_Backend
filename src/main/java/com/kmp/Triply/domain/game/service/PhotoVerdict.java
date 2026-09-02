package com.kmp.Triply.domain.game.service;

/**
 * 사진 판정 결과. reason 은 플레이어가 이의를 제기했을 때 근거로 남긴다.
 *
 * @param passed     통과 여부
 * @param confidence 0.0 ~ 1.0
 * @param reason     판정 근거 (한 줄)
 */
public record PhotoVerdict(boolean passed, double confidence, String reason) {

    public static PhotoVerdict skipped() {
        return new PhotoVerdict(true, 1.0, "AI 판정 비활성화 — 업로드만 확인");
    }

    public String note() {
        return "passed=%s confidence=%.2f %s".formatted(passed, confidence, reason);
    }
}
