package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.request.HintRequest;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.request.SpotArriveRequest;
import com.kmp.Triply.domain.game.dto.response.HintResponse;
import com.kmp.Triply.domain.game.dto.response.MissionSubmitResponse;
import com.kmp.Triply.domain.game.dto.response.PlayMissionResponse;
import com.kmp.Triply.domain.game.dto.response.SpotArriveResponse;
import com.kmp.Triply.domain.game.dto.response.RoomProgressResponse;

import java.util.List;

public interface GamePlayService {

    RoomProgressResponse getRoomProgress(Long userId, Long roomId);

    SpotArriveResponse arriveSpot(Long userId, Long roomId, Long spotId, SpotArriveRequest request);

    List<PlayMissionResponse> getSpotMissions(Long userId, Long roomId, Long spotId);

    /** 사진 계열 미션 제출. 이미지 자체를 받아 저장하고 판정한다. */
    MissionSubmitResponse submitPhotoMission(Long userId, Long missionId, Long roomId,
                                             byte[] image, String contentType);

    HintResponse requestHint(Long userId, Long missionId, HintRequest request);

    MissionSubmitResponse submitMission(Long userId, Long missionId, MissionSubmitRequest request);
}