package com.kmp.Triply.domain.game.dto.response;

import com.kmp.Triply.domain.game.entity.TeamMember;
import com.kmp.Triply.domain.game.entity.TeamRole;
import com.kmp.Triply.domain.user.entity.UserTravelProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class TeamMemberResponse {

    private Long userId;
    private String nickname;
    private String profileImg;
    private TeamRole role;
    private List<String> personaTags;
    private LocalDateTime joinedAt;

    public static TeamMemberResponse from(TeamMember teamMember, Optional<UserTravelProfile> travelProfile) {
        return TeamMemberResponse.builder()
                .userId(teamMember.getUser().getId())
                .nickname(teamMember.getUser().getNickname())
                .profileImg(teamMember.getUser().getProfileImg())
                .role(teamMember.getRole())
                .personaTags(toPersonaTags(travelProfile))
                .joinedAt(teamMember.getJoinedAt())
                .build();
    }

    private static List<String> toPersonaTags(Optional<UserTravelProfile> travelProfile) {
        return travelProfile
                .map(profile -> List.of(
                        profile.getStyleType().name(),
                        profile.getMoveType().name(),
                        profile.getCompanionType().name()
                ))
                .orElseGet(List::of);
    }
}
