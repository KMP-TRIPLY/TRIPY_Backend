package com.kmp.Triply.domain.game.service;

import com.kmp.Triply.domain.game.dto.request.GameRoomCourseChangeRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomCreateRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomJoinRequest;
import com.kmp.Triply.domain.game.dto.request.GameRoomStartRequest;
import com.kmp.Triply.domain.game.dto.request.TeamLeaveRequest;
import com.kmp.Triply.domain.game.dto.response.GameRoomJoinResponse;
import com.kmp.Triply.domain.game.dto.response.GameRoomResponse;
import com.kmp.Triply.domain.game.dto.response.TeamLeaveResponse;
import com.kmp.Triply.domain.game.dto.response.TeamMemberResponse;

import java.util.List;

public interface GameRoomService {

    GameRoomJoinResponse createRoom(Long userId, GameRoomCreateRequest request);

    GameRoomJoinResponse joinRoom(Long userId, GameRoomJoinRequest request);

    GameRoomResponse startRoom(Long userId, GameRoomStartRequest request);

    GameRoomResponse endRoom(Long userId, Long roomId);

    GameRoomResponse changeCourse(Long userId, Long roomId, GameRoomCourseChangeRequest request);

    TeamLeaveResponse leaveRoom(Long userId, Long roomId, TeamLeaveRequest request);


    List<TeamMemberResponse> getRoomMembers(Long roomId);

    void kickMember(Long hostUserId, Long roomId, Long userId);
}
