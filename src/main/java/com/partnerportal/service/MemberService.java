package com.partnerportal.service;

import com.partnerportal.config.AppProperties;
import com.partnerportal.domain.Member;
import com.partnerportal.domain.MemberStatus;
import com.partnerportal.repository.MemberRepository;
import com.partnerportal.web.form.JoinForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Transactional
    public Member join(JoinForm form) {
        String loginId = form.getLoginId().trim();
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            throw new PortalException("passwordConfirm", "비밀번호가 일치하지 않습니다.");
        }
        if (form.getPassword().contains(loginId)) {
            throw new PortalException("password", "비밀번호에 아이디를 포함할 수 없습니다.");
        }
        if (memberRepository.existsByLoginId(loginId)) {
            throw new PortalException("loginId", "이미 사용 중인 아이디입니다.");
        }
        Member member = new Member();
        member.setLoginId(loginId);
        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setCompanyName(form.getCompanyName().trim());
        member.setBizNo(digitsOnly(form.getBizNo()));
        member.setManagerName(form.getManagerName().trim());
        member.setEmail(form.getEmail().trim());
        member.setPhone(form.getPhone().trim());
        member.setStatus(MemberStatus.PENDING);
        Member saved = memberRepository.save(member);
        log.info("회원가입 신청: loginId={}, company={}", saved.getLoginId(), saved.getCompanyName());
        return saved;
    }

    public boolean isLoginIdAvailable(String loginId) {
        return loginId != null && loginId.matches("^[a-z0-9_]{4,20}$") && !memberRepository.existsByLoginId(loginId);
    }

    /**
     * 비밀번호 오류를 기록한다.
     * @return 이번 실패로 계정이 잠겼으면 true
     */
    @Transactional
    public boolean recordLoginFailure(String loginId) {
        return memberRepository.findByLoginId(loginId).map(member -> {
            if (member.getStatus() != MemberStatus.ACTIVE) {
                return false;
            }
            member.setFailedLoginCount(member.getFailedLoginCount() + 1);
            if (member.getFailedLoginCount() >= appProperties.getSecurity().getMaxLoginFailures()) {
                member.setStatus(MemberStatus.LOCKED);
                log.warn("로그인 {}회 실패로 계정 잠금: loginId={}", member.getFailedLoginCount(), loginId);
                return true;
            }
            return false;
        }).orElse(false);
    }

    @Transactional
    public void recordLoginSuccess(String loginId) {
        memberRepository.findByLoginId(loginId).ifPresent(member -> {
            member.setFailedLoginCount(0);
            member.setLastLoginAt(LocalDateTime.now());
        });
    }

    /** 회사명 + 이메일로 아이디 찾기. 아이디는 일부 마스킹해서 돌려준다. */
    public List<String> findMaskedLoginIds(String companyName, String email) {
        return memberRepository.findByCompanyNameAndEmailIgnoreCase(companyName.trim(), email.trim()).stream()
                .map(Member::getLoginId)
                .map(MemberService::mask)
                .toList();
    }

    public Member get(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new PortalException("회원 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public void changePassword(String loginId, String current, String next, String confirm) {
        Member member = get(loginId);
        if (!passwordEncoder.matches(current, member.getPassword())) {
            throw new PortalException("currentPassword", "현재 비밀번호가 올바르지 않습니다.");
        }
        if (!next.equals(confirm)) {
            throw new PortalException("newPasswordConfirm", "새 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(next, member.getPassword())) {
            throw new PortalException("newPassword", "현재 비밀번호와 다른 비밀번호를 입력하세요.");
        }
        member.setPassword(passwordEncoder.encode(next));
    }

    @Transactional
    public void resetPassword(Member member, String rawPassword) {
        member.setPassword(passwordEncoder.encode(rawPassword));
        member.setFailedLoginCount(0);
        if (member.getStatus() == MemberStatus.LOCKED) {
            member.setStatus(MemberStatus.ACTIVE);
        }
        memberRepository.save(member);
    }

    // ---- 관리자 기능 ----

    public Page<Member> list(MemberStatus status, Pageable pageable) {
        return status == null ? memberRepository.findAll(pageable) : memberRepository.findByStatus(status, pageable);
    }

    @Transactional
    public void changeStatus(Long memberId, MemberStatus status, String adminLoginId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new PortalException("회원 정보를 찾을 수 없습니다."));
        if (member.getLoginId().equals(adminLoginId)) {
            throw new PortalException("본인 계정의 상태는 변경할 수 없습니다.");
        }
        member.setStatus(status);
        if (status == MemberStatus.ACTIVE) {
            member.setFailedLoginCount(0);
        }
        log.info("회원 상태 변경: loginId={}, status={}, by={}", member.getLoginId(), status, adminLoginId);
    }

    static String mask(String loginId) {
        if (loginId.length() <= 3) {
            return loginId.charAt(0) + "*".repeat(loginId.length() - 1);
        }
        return loginId.substring(0, 2) + "*".repeat(loginId.length() - 3) + loginId.charAt(loginId.length() - 1);
    }

    static String digitsOnly(String value) {
        return value.replaceAll("\\D", "");
    }
}
