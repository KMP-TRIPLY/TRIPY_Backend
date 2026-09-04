package com.kmp.Triply.domain.game.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 방 비밀번호는 숫자 5자리 고정이고 필수다.
 * 여럿이 입으로 공유하는 임시 비번이라 앱이 숫자 키패드를 띄울 수 있어야 한다.
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
    void 숫자_5자리만_받는다() throws Exception {
        assertThat(valid(create("\"12345\""))).isTrue();
        assertThat(valid(create("\"00000\""))).isTrue();
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
    void 비밀번호는_필수다() throws Exception {
        // 공개 방은 없다. 비우면 만들 수 없어야 한다
        assertThat(valid(create("null"))).isFalse();
        assertThat(valid(create("\"\""))).isFalse();
    }

    @Test
    void 참여_요청도_같은_규칙을_쓴다() throws Exception {
        assertThat(valid(MAPPER.readValue("{\"password\":\"12345\"}", GameRoomJoinRequest.class))).isTrue();
        assertThat(valid(MAPPER.readValue("{\"password\":\"1234\"}", GameRoomJoinRequest.class))).isFalse();
        assertThat(valid(MAPPER.readValue("{\"password\":\"abcde\"}", GameRoomJoinRequest.class))).isFalse();
        // 참여에도 비밀번호가 필수다
        assertThat(valid(MAPPER.readValue("{}", GameRoomJoinRequest.class))).isFalse();
    }
}
