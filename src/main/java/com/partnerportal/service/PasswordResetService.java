package com.partnerportal.service;

import com.partnerportal.config.AppProperties;
import com.partnerportal.domain.Member;
import com.partnerportal.domain.PasswordResetToken;
import com.partnerportal.repository.MemberRepository;
import com.partnerportal.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int EXPIRE_MINUTES = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MemberService memberService;
    private final MailService mailService;
    private final AppProperties appProperties;

    /**
     * 아이디 + 이메일이 일치하면 초기화 링크를 메일로 보낸다.
     * 일치 여부는 화면에 노출하지 않는다(계정 존재 여부 유추 방지).
     */
    @Transactional
    public void request(String loginId, String email) {
        memberRepository.findByLoginIdAndEmailIgnoreCase(loginId.trim(), email.trim()).ifPresent(member -> {
            tokenRepository.invalidateAllFor(member);

            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            PasswordResetToken token = new PasswordResetToken();
            token.setMember(member);
            token.setTokenHash(sha256(rawToken));
            token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            tokenRepository.save(token);

            String link = appProperties.getBaseUrl() + "/support/reset?token=" + rawToken;
            mailService.send(member.getEmail(),
                    "[" + appProperties.getBrand() + "] 비밀번호 초기화 안내",
                    member.getManagerName() + "님, 안녕하세요.\n\n"
                            + "아래 링크에서 " + EXPIRE_MINUTES + "분 안에 새 비밀번호를 설정해 주세요.\n"
                            + link + "\n\n"
                            + "본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.");
            log.info("비밀번호 초기화 요청: loginId={}", member.getLoginId());
        });
    }

    public boolean isValid(String rawToken) {
        return rawToken != null && tokenRepository.findByTokenHash(sha256(rawToken))
                .map(PasswordResetToken::isUsable)
                .orElse(false);
    }

    @Transactional
    public void reset(String rawToken, String password, String confirm) {
        if (!password.equals(confirm)) {
            throw new PortalException("passwordConfirm", "비밀번호가 일치하지 않습니다.");
        }
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken))
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new PortalException("링크가 만료되었거나 이미 사용되었습니다. 다시 요청해 주세요."));
        Member member = token.getMember();
        if (password.contains(member.getLoginId())) {
            throw new PortalException("password", "비밀번호에 아이디를 포함할 수 없습니다.");
        }
        token.setUsed(true);
        memberService.resetPassword(member, password);
        log.info("비밀번호 초기화 완료: loginId={}", member.getLoginId());
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
