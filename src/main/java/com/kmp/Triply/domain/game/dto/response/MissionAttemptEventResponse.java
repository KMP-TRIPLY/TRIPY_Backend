package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.AttemptResult;
import lombok.Builder;
import lombok.Getter;

/**
 * 팀원의 미션 제출 결과를 방 채널로 알릴 때 싣는 내용.
 *
 * <p>진행상황은 팀 단위라 누가 무엇을 풀었는지 나머지 팀원 화면도 같이 움직여야 한다.
 * 예전에는 미션 번호만 보내고 정답 여부는 사람이 읽는 문장 안에만 있어서,
 * 프론트가 한글 문구를 파싱해야 구분할 수 있었다.
 */
@Getter
@Builder
public class MissionAttemptEventResponse {

    private Long missionId;
    private Long spotId;
    private AttemptResult result;
    private boolean correct;
    private int scoreEarned;
    private boolean hintUsed;
    /** 제출한 팀원. 내가 보낸 이벤트인지 구분해 화면을 다르게 그릴 수 있다. */
    private Long userId;
    private String nickname;
    private int teamTotalScore;
    /** 이 제출로 스팟의 미션을 전부 끝냈는지 */
    private boolean spotCompleted;

    public static MissionAttemptEventResponse of(Long missionId, Long spotId, AttemptResult result,
                                                 int scoreEarned, boolean hintUsed,
                                                 Long userId, String nickname,
                                                 int teamTotalScore, boolean spotCompleted) {
        return MissionAttemptEventResponse.builder()
                .missionId(missionId)
                .spotId(spotId)
                .result(result)
                .correct(result == AttemptResult.CORRECT)
                .scoreEarned(scoreEarned)
                .hintUsed(hintUsed)
                .userId(userId)
                .nickname(nickname)
                .teamTotalScore(teamTotalScore)
                .spotCompleted(spotCompleted)
                .build();
    }
}
