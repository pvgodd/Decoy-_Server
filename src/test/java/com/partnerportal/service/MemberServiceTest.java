package com.partnerportal.service;

import com.partnerportal.domain.Member;
import com.partnerportal.domain.MemberStatus;
import com.partnerportal.repository.MemberRepository;
import com.partnerportal.web.form.JoinForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    private JoinForm sampleForm() {
        JoinForm form = new JoinForm();
        form.setLoginId("partner01");
        form.setPassword("Partner!123");
        form.setPasswordConfirm("Partner!123");
        form.setCompanyName("테스트상사");
        form.setBizNo("123-45-67890");
        form.setManagerName("홍길동");
        form.setEmail("partner01@example.com");
        form.setPhone("010-1234-5678");
        form.setAgreePrivacy(true);
        return form;
    }

    @Test
    void 회원가입은_승인대기_상태로_저장되고_비밀번호는_암호화된다() {
        Member saved = memberService.join(sampleForm());

        assertThat(saved.getStatus()).isEqualTo(MemberStatus.PENDING);
        assertThat(saved.getBizNo()).isEqualTo("1234567890");
        assertThat(saved.getPassword()).isNotEqualTo("Partner!123");
    }

    @Test
    void 비밀번호_확인이_다르면_예외() {
        JoinForm form = sampleForm();
        form.setPasswordConfirm("different!9");

        assertThatThrownBy(() -> memberService.join(form))
                .isInstanceOf(PortalException.class);
    }

    @Test
    void 아이디가_중복되면_예외() {
        memberService.join(sampleForm());

        assertThatThrownBy(() -> memberService.join(sampleForm()))
                .isInstanceOf(PortalException.class);
    }

    @Test
    void 로그인_5회_실패하면_계정이_잠긴다() {
        Member saved = memberService.join(sampleForm());
        saved.setStatus(MemberStatus.ACTIVE);
        memberRepository.saveAndFlush(saved);

        boolean locked = false;
        for (int i = 0; i < 5; i++) {
            locked = memberService.recordLoginFailure("partner01");
        }

        assertThat(locked).isTrue();
        assertThat(memberRepository.findByLoginId("partner01").orElseThrow().getStatus())
                .isEqualTo(MemberStatus.LOCKED);
    }

    @Test
    void 아이디_마스킹() {
        assertThat(MemberService.mask("partner01")).isEqualTo("pa******1");
        assertThat(MemberService.mask("abc")).isEqualTo("a**");
    }
}
