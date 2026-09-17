package com.kmp.Triply.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        UriComponentsBuilder target = UriComponentsBuilder.fromUriString(redirectUri);

        if (exception instanceof WithdrawnUserException withdrawn) {
            // 언제 돌아올 수 있는지까지 알려준다. 막힌 이유를 모르면 계속 다시 시도하게 된다.
            target.queryParam("error", WithdrawnUserException.ERROR_CODE)
                    .queryParam("retry_after_days", withdrawn.getRetryAfterDays());
        } else {
            target.queryParam("error", exception.getMessage());
        }

        getRedirectStrategy().sendRedirect(request, response, target.encode().build().toUriString());
    }
}
