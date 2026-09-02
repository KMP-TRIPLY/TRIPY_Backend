package com.kmp.Triply.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // Auth
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 refresh token입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인 계정만 수정할 수 있습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),

    // Trip
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "여행을 찾을 수 없습니다."),
    TRIP_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 여행에 접근 권한이 없습니다."),

    // Place
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "장소를 찾을 수 없습니다."),

    // Travel Profile
    TRAVEL_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "여행 프로필을 찾을 수 없습니다."),

    // Tourism API
    TOURISM_API_ERROR(HttpStatus.BAD_GATEWAY, "한국관광공사 API 호출에 실패했습니다."),
    INVALID_COORDINATE(HttpStatus.BAD_REQUEST, "좌표가 대한민국 범위를 벗어났습니다."),

    // Course
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "코스를 찾을 수 없습니다."),
    COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 만든 코스만 삭제할 수 있습니다."),
    REGION_NOT_RESOLVED(HttpStatus.BAD_REQUEST, "도시 이름으로 지역을 찾을 수 없습니다. regionCode 를 직접 지정해 주세요."),
    COURSE_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "코스 스팟을 찾을 수 없습니다."),
    TOURISM_SPOT_NOT_FOUND(HttpStatus.NOT_FOUND, "관광지 정보를 찾을 수 없습니다."),

    // Game
    GAME_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "게임 방을 찾을 수 없습니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 멤버를 찾을 수 없습니다."),
    INVALID_GAME_ROOM_STATUS(HttpStatus.BAD_REQUEST, "현재 게임 방 상태에서 수행할 수 없는 요청입니다."),
    ROOM_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "게임 방 정원이 초과되었습니다."),
    GAME_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "게임 방에 대한 권한이 없습니다."),
    LEFT_ROOM_CANNOT_REJOIN(HttpStatus.CONFLICT, "하차한 게임 방에는 다시 참여할 수 없습니다."),
    INVALID_GAME_ROOM_PASSWORD(HttpStatus.UNAUTHORIZED, "게임 방 비밀번호가 일치하지 않습니다."),

    // Game Play (미션 대결)
    GAME_NOT_RUNNING(HttpStatus.BAD_REQUEST, "게임이 진행 중이 아닙니다."),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "해당 팀의 멤버가 아닙니다."),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "미션을 찾을 수 없습니다."),
    SPOT_LOCKED(HttpStatus.BAD_REQUEST, "아직 도착할 수 없는 스팟입니다. 이전 스팟을 먼저 완료하세요."),
    SPOT_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "스팟에 먼저 도착(인증)해야 합니다."),
    OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "스팟 반경 밖입니다. 더 가까이 이동하세요."),
    MISSION_ALREADY_SOLVED(HttpStatus.CONFLICT, "이미 해결한 미션입니다."),
    PHOTO_SUBMIT_REQUIRED(HttpStatus.BAD_REQUEST, "사진 인증 미션은 사진 업로드로 제출해야 합니다."),
    NOT_PHOTO_MISSION(HttpStatus.BAD_REQUEST, "사진으로 제출할 수 없는 미션입니다."),
    INVALID_PHOTO(HttpStatus.BAD_REQUEST, "이미지 파일이 아니거나 허용 크기를 넘었습니다."),
    PHOTO_STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "사진 저장소를 사용할 수 없습니다."),

    // Reward
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_NOT_ISSUABLE(HttpStatus.BAD_REQUEST, "발급할 수 없는 쿠폰입니다."),
    REWARD_SETTLEMENT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "게임 종료 후 보상을 정산할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
