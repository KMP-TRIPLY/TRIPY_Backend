package com.kmp.Triply.global.security.oauth2;

import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * 탈퇴한 지 얼마 안 된 계정이 다시 로그인하려 할 때.
 *
 * <p>예전에는 탈퇴한 계정도 로그인 자체는 통과하고 토큰까지 받은 뒤, 이후 모든 API 가
 * 401 로 떨어졌다. 사용자에게는 "로그인은 됐는데 앱이 전부 안 되는" 상태로 보였다.
 * 로그인 단계에서 끊고 언제 돌아올 수 있는지 알려준다.
 */
@Getter
public class WithdrawnUserException extends OAuth2AuthenticationException {

    public static final String ERROR_CODE = "WITHDRAWN_USER";

    private final long retryAfterDays;

    public WithdrawnUserException(long retryAfterDays) {
        super(new OAuth2Error(ERROR_CODE, "탈퇴한 계정입니다.", null));
        this.retryAfterDays = retryAfterDays;
    }
}
