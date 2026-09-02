package com.kmp.Triply.domain.game.entity;

import com.kmp.Triply.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 나갔다 다시 들어오면 팀이 그대로여야 한다.
 * 재입장으로 팀이 바뀌면 그때까지 쌓은 점수가 따라다니게 된다.
 */
class TeamMemberRejoinTest {

    @Test
    void 재입장하면_활성화되고_팀은_유지된다() {
        Team team = Team.builder().teamName("공주 원정대").build();
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(User.builder().build())
                .build();

        member.leave();
        assertThat(member.isActive()).isFalse();
        assertThat(member.getLeftAt()).isNotNull();

        member.rejoin();
        assertThat(member.isActive()).isTrue();
        assertThat(member.getLeftAt()).isNull();
        assertThat(member.getTeam()).isSameAs(team);
    }
}
