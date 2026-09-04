package com.kmp.Triply.domain.game.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 방 비밀번호는 선택이다 — 넣으면 아는 사람만, 비우면 누구나 들어온다.
 * 넣을 때는 숫자 5자리다. 여럿이 입으로 공유하는 임시 비번이라
 * 불러주기 쉬워야 하고 앱이 숫자 키패드를 띄울 수 있어야 한다.
 */
class GameRoomPasswordRuleTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static GameRoomCreateRequest create(String passwordJson) throws Exception {
        return MAPPER.readValue("""
                {"courseId":1,"roomName":"공주 원정대","maxMembers":4,"password":%s}
                """.formatted(passwordJson), GameRoomCreateRequest.class);
    }

    private static boolean valid(Object request) {
        return VALIDATOR.validate(request).isEmpty();
    }

    @Test
    void 숫자_5자리를_받는다() throws Exception {
        assertThat(valid(create("\"12345\""))).isTrue();
        assertThat(valid(create("\"00000\""))).isTrue();
    }

    @Test
    void 비우면_누구나_들어오는_방이라_유효하다() throws Exception {
        // 클라이언트가 빈 입력을 어느 쪽으로 보내도 공개 방이 되어야 한다
        assertThat(valid(create("null"))).isTrue();
        assertThat(valid(create("\"\""))).isTrue();
        assertThat(valid(MAPPER.readValue(
                "{\"courseId\":1,\"roomName\":\"공주 원정대\",\"maxMembers\":4}",
                GameRoomCreateRequest.class))).isTrue();
    }

    @Test
    void 자리수가_다르면_거부한다() throws Exception {
        assertThat(valid(create("\"1234\""))).isFalse();
        assertThat(valid(create("\"123456\""))).isFalse();
    }

    @Test
    void 숫자가_아니면_거부한다() throws Exception {
        assertThat(valid(create("\"abcde\""))).isFalse();
        assertThat(valid(create("\"12ab5\""))).isFalse();
        assertThat(valid(create("\"12 45\""))).isFalse();
        assertThat(valid(create("\"1234!\""))).isFalse();
    }

    @Test
    void 참여_요청도_같은_규칙을_쓴다() throws Exception {
        assertThat(valid(MAPPER.readValue("{\"password\":\"12345\"}", GameRoomJoinRequest.class))).isTrue();
        assertThat(valid(MAPPER.readValue("{\"password\":\"1234\"}", GameRoomJoinRequest.class))).isFalse();
        assertThat(valid(MAPPER.readValue("{\"password\":\"abcde\"}", GameRoomJoinRequest.class))).isFalse();
        // 잠기지 않은 방은 본문 없이 참여한다
        assertThat(valid(MAPPER.readValue("{}", GameRoomJoinRequest.class))).isTrue();
    }
}
