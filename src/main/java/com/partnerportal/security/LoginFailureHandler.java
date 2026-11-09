package com.partnerportal.security;

import com.partnerportal.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 로그인 실패 처리: 비밀번호 오류 누적 시 계정을 잠그고, 실패 사유별로 안내 메시지를 구분한다.
 */
@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MemberService memberService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String reason = "bad";
        if (exception instanceof LockedException) {
            reason = "locked";
        } else if (exception instanceof DisabledException) {
            reason = "REJECTED".equals(exception.getMessage()) ? "rejected" : "pending";
        } else if (exception instanceof BadCredentialsException) {
            String loginId = request.getParameter("loginId");
            if (loginId != null && memberService.recordLoginFailure(loginId.trim())) {
                reason = "locked";
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error=" + reason);
    }
}
