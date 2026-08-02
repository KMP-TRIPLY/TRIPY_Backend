package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.request.HintRequest;
import com.kmp.Triply.domain.game.dto.request.MissionSubmitRequest;
import com.kmp.Triply.domain.game.dto.request.SpotArriveRequest;
import com.kmp.Triply.domain.game.dto.response.HintResponse;
import com.kmp.Triply.domain.game.dto.response.MissionSubmitResponse;
import com.kmp.Triply.domain.game.dto.response.PlayMissionResponse;
import com.kmp.Triply.domain.game.dto.response.SpotArriveResponse;
import com.kmp.Triply.domain.game.dto.response.TeamProgressResponse;

import java.util.List;

public interface GamePlayService {

    TeamProgressResponse getTeamProgress(Long userId, Long roomId, Long teamId);

    SpotArriveResponse arriveSpot(Long userId, Long roomId, Long spotId, SpotArriveRequest request);

    List<PlayMissionResponse> getSpotMissions(Long userId, Long roomId, Long spotId, Long teamId);

    HintResponse requestHint(Long userId, Long missionId, HintRequest request);

    MissionSubmitResponse submitMission(Long userId, Long missionId, MissionSubmitRequest request);
}