package com.kmp.Triply.domain.game.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 팀이 스팟의 미션을 모두 끝냈을 때 방 채널로 알릴 내용.
 *
 * <p>다음 스팟 번호를 함께 싣는다. 예전에는 완료된 스팟 번호만 보내서,
 * 마지막 미션을 제출한 사람만 HTTP 응답으로 다음 스팟을 받고 나머지 팀원은
 * 어디로 가야 하는지 몰랐다 — 실시간으로 맞춰 놓고 이 한 건만 다시 조회해야 했다.
 */
@Getter
@Builder
public class SpotCompletedEventResponse {

    /** 방금 끝낸 스팟 */
    private Long spotId;

    /** 다음으로 가야 할 스팟. 마지막 스팟이었으면 비어 있다. */
    private Long nextSpotId;

    /** 코스의 마지막 스팟까지 끝냈는지. true 면 다음 스팟이 없다. */
    private boolean courseCompleted;

    public static SpotCompletedEventResponse of(Long spotId, Long nextSpotId) {
        return SpotCompletedEventResponse.builder()
                .spotId(spotId)
                .nextSpotId(nextSpotId)
                .courseCompleted(nextSpotId == null)
                .build();
    }
}
