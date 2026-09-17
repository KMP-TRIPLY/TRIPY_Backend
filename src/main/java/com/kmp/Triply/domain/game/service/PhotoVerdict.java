package com.kmp.Triply.domain.game.service;

/**
 * 사진 판정 결과. observed·reason 은 플레이어가 이의를 제기했을 때 근거로 남긴다.
 *
 * @param passed     통과 여부
 * @param confidence 0.0 ~ 1.0
 * @param observed   모델이 사진에서 봤다고 말한 것
 * @param reason     판정 근거 (한 줄)
 */
public record PhotoVerdict(boolean passed, double confidence, String observed, String reason) {

    /** verification_note 컬럼 길이. 넘치면 잘라 담는다. */
    private static final int NOTE_LIMIT = 500;

    public static PhotoVerdict skipped() {
        return new PhotoVerdict(true, 1.0, "", "AI 판정 비활성화 — 업로드만 확인");
    }

    public String note() {
        String note = "passed=%s confidence=%.2f %s | 사진: %s".formatted(passed, confidence, reason, observed);
        return note.length() <= NOTE_LIMIT ? note : note.substring(0, NOTE_LIMIT);
    }
}
