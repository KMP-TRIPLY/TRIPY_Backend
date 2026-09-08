package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.Team;
import com.kmp.Triply.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionClearNotificationResponse {

    private Long missionId;
    private Long spotId;
    private Long teamId;
    private String teamName;
    private Long userId;
    private String nickname;
    private int scoreEarned;
    private int teamTotalScore;
    private String message;

    public static MissionClearNotificationResponse of(Long missionId, Long spotId, Team team,
                                                      User user, int scoreEarned) {
        String message = user.getNickname() + "님이 미션을 클리어했습니다. "
                + scoreEarned + "포인트를 획득했습니다.";

        return MissionClearNotificationResponse.builder()
                .missionId(missionId)
                .spotId(spotId)
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .userId(user.getId())
                .nickname(user.getNickname())
                .scoreEarned(scoreEarned)
                .teamTotalScore(team.getTotalScore())
                .message(message)
                .build();
    }
}
