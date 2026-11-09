package com.partnerportal.service;

import com.partnerportal.config.AppProperties;
import com.partnerportal.domain.Faq;
import com.partnerportal.domain.Member;
import com.partnerportal.domain.MemberRole;
import com.partnerportal.domain.MemberStatus;
import com.partnerportal.domain.Notice;
import com.partnerportal.repository.FaqRepository;
import com.partnerportal.repository.MemberRepository;
import com.partnerportal.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;

/**
 * 최초 기동 시 관리자 계정과 기본 FAQ·공지를 만든다. 이미 있으면 아무것도 하지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final FaqRepository faqRepository;
    private final NoticeRepository noticeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAdminIfMissing();
        seedFaqs();
        seedNotice();
    }

    private void createAdminIfMissing() {
        if (memberRepository.existsByRole(MemberRole.ADMIN)) {
            return;
        }
        String loginId = appProperties.getAdmin().getLoginId();
        String password = appProperties.getAdmin().getPassword();
        boolean generated = !StringUtils.hasText(password);
        if (generated) {
            password = randomPassword();
        }
        Member admin = new Member();
        admin.setLoginId(loginId);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setCompanyName(appProperties.getCompany());
        admin.setBizNo("0000000000");
        admin.setManagerName("관리자");
        admin.setEmail("admin@example.com");
        admin.setPhone("000-0000-0000");
        admin.setRole(MemberRole.ADMIN);
        admin.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(admin);
        if (generated) {
            log.warn("\n==============================================\n"
                    + " 관리자 계정이 생성되었습니다.\n"
                    + "   아이디  : {}\n"
                    + "   비밀번호: {}\n"
                    + " 로그인 후 [내 정보]에서 비밀번호를 꼭 변경하세요.\n"
                    + "==============================================", loginId, password);
        } else {
            log.info("관리자 계정 생성: loginId={}", loginId);
        }
    }

    private void seedFaqs() {
        if (faqRepository.count() > 0) {
            return;
        }
        faq("회원가입", "회원가입 후 바로 로그인할 수 있나요?",
                "가입 신청 후 담당자 승인이 완료되면 로그인할 수 있습니다. 승인은 영업일 기준 1~2일 소요됩니다.", 1);
        faq("로그인", "비밀번호를 여러 번 틀려 계정이 잠겼습니다.",
                "비밀번호를 5회 연속 잘못 입력하면 계정이 잠깁니다. [비밀번호 초기화]로 새 비밀번호를 설정하면 잠금이 해제됩니다.", 1);
        faq("입점상담", "입점상담 신청 결과는 어떻게 확인하나요?",
                "신청 완료 화면의 접수번호와 신청 시 입력한 조회 비밀번호로 [상담현황 조회]에서 확인할 수 있습니다.", 1);
        faq("핫라인", "핫라인 제보는 익명으로 할 수 있나요?",
                "네. 이름과 연락처를 비워두면 익명으로 접수되며, 접수번호와 조회 비밀번호로 처리 결과를 확인할 수 있습니다.", 1);
    }

    private void faq(String category, String question, String answer, int order) {
        Faq faq = new Faq();
        faq.setCategory(category);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setSortOrder(order);
        faqRepository.save(faq);
    }

    private void seedNotice() {
        if (noticeRepository.count() > 0) {
            return;
        }
        Notice notice = new Notice();
        notice.setTitle(appProperties.getBrand() + " 오픈 안내");
        notice.setContent("협력사 전용 포털을 오픈했습니다.\n회원가입 후 승인이 완료되면 포털의 모든 기능을 이용하실 수 있습니다.");
        notice.setPinned(true);
        notice.setAuthor("관리자");
        noticeRepository.save(notice);
    }

    private static String randomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb + "!9";
    }
}
