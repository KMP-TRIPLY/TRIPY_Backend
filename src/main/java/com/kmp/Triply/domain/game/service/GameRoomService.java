package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomJoinRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomStartRequest;
import com.kmp.Triply.domain.game.dto.response.GameRoomJoinResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomResponse;
import com.kmp.Triply.domain.game.dto.response.TeamMemberResponse;
import com.kmp.Triply.domain.game.dto.response.TeamRankingResponse;

import java.util.List;

public interface GameRoomService {

    GameRoomJoinResponse createRoom(Long userId, GameRoomCreateRequest request);

    GameRoomJoinResponse joinRoom(Long userId, GameRoomJoinRequest request);

    GameRoomResponse startRoom(Long userId, GameRoomStartRequest request);

    GameRoomResponse endRoom(Long userId, Long roomId);

    List<TeamRankingResponse> getRankings(Long roomId);

    List<TeamMemberResponse> getTeamMembers(Long teamId);

    void kickMember(Long hostUserId, Long roomId, Long userId);
}
