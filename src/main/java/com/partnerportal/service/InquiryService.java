package com.partnerportal.service;

import com.partnerportal.domain.Consult;
import com.partnerportal.domain.Hotline;
import com.partnerportal.domain.InquiryStatus;
import com.partnerportal.repository.ConsultRepository;
import com.partnerportal.repository.HotlineRepository;
import com.partnerportal.web.form.AnswerForm;
import com.partnerportal.web.form.ConsultForm;
import com.partnerportal.web.form.HotlineForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * 핫라인 제보와 입점상담 신청의 접수·조회·답변 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String LOOKUP_FAIL = "접수번호 또는 조회 비밀번호가 일치하지 않습니다.";

    private final HotlineRepository hotlineRepository;
    private final ConsultRepository consultRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- 핫라인 ----

    @Transactional
    public Hotline registerHotline(HotlineForm form) {
        Hotline hotline = new Hotline();
        hotline.setReceiptNo(newReceiptNo("H", hotlineRepository::existsByReceiptNo));
        hotline.setCategory(form.getCategory());
        hotline.setTitle(form.getTitle().trim());
        hotline.setContent(form.getContent().trim());
        hotline.setReporterName(blankToNull(form.getReporterName()));
        hotline.setContact(blankToNull(form.getContact()));
        hotline.setLookupPassword(passwordEncoder.encode(form.getLookupPassword()));
        Hotline saved = hotlineRepository.save(hotline);
        log.info("핫라인 접수: receiptNo={}", saved.getReceiptNo());
        return saved;
    }

    public Hotline lookupHotline(String receiptNo, String password) {
        return hotlineRepository.findByReceiptNo(normalize(receiptNo))
                .filter(h -> passwordEncoder.matches(password, h.getLookupPassword()))
                .orElseThrow(() -> new PortalException(LOOKUP_FAIL));
    }

    public Page<Hotline> listHotlines(Pageable pageable) {
        return hotlineRepository.findAll(pageable);
    }

    public Hotline getHotline(Long id) {
        return hotlineRepository.findById(id).orElseThrow(() -> new PortalException("접수 건을 찾을 수 없습니다."));
    }

    @Transactional
    public void answerHotline(Long id, AnswerForm form) {
        Hotline hotline = getHotline(id);
        hotline.setStatus(form.getStatus());
        hotline.setAnswer(blankToNull(form.getAnswer()));
        hotline.setAnsweredAt(StringUtils.hasText(form.getAnswer()) ? LocalDateTime.now() : null);
    }

    // ---- 입점상담 ----

    @Transactional
    public Consult applyConsult(ConsultForm form) {
        Consult consult = new Consult();
        consult.setReceiptNo(newReceiptNo("C", consultRepository::existsByReceiptNo));
        consult.setCompanyName(form.getCompanyName().trim());
        consult.setBizNo(form.getBizNo().replaceAll("\\D", ""));
        consult.setManagerName(form.getManagerName().trim());
        consult.setEmail(form.getEmail().trim());
        consult.setPhone(form.getPhone().trim());
        consult.setCategory(form.getCategory());
        consult.setHomepage(blankToNull(form.getHomepage()));
        consult.setContent(form.getContent().trim());
        consult.setLookupPassword(passwordEncoder.encode(form.getLookupPassword()));
        Consult saved = consultRepository.save(consult);
        log.info("입점상담 신청: receiptNo={}, company={}", saved.getReceiptNo(), saved.getCompanyName());
        return saved;
    }

    public Consult lookupConsult(String receiptNo, String password) {
        return consultRepository.findByReceiptNo(normalize(receiptNo))
                .filter(c -> passwordEncoder.matches(password, c.getLookupPassword()))
                .orElseThrow(() -> new PortalException(LOOKUP_FAIL));
    }

    public Page<Consult> listConsults(Pageable pageable) {
        return consultRepository.findAll(pageable);
    }

    public Consult getConsult(Long id) {
        return consultRepository.findById(id).orElseThrow(() -> new PortalException("상담 건을 찾을 수 없습니다."));
    }

    @Transactional
    public void answerConsult(Long id, AnswerForm form) {
        Consult consult = getConsult(id);
        consult.setStatus(form.getStatus());
        consult.setAnswer(blankToNull(form.getAnswer()));
        consult.setAnsweredAt(StringUtils.hasText(form.getAnswer()) ? LocalDateTime.now() : null);
    }

    public long countOpenHotlines() {
        return hotlineRepository.countByStatusNot(InquiryStatus.DONE);
    }

    public long countOpenConsults() {
        return consultRepository.countByStatusNot(InquiryStatus.DONE);
    }

    /** 접수번호: 접두어 + yyyyMMdd + 6자리 난수 (예: H20261109123456) */
    private static String newReceiptNo(String prefix, Predicate<String> exists) {
        String date = LocalDate.now().format(DATE);
        for (int i = 0; i < 20; i++) {
            String candidate = prefix + date + String.format("%06d", RANDOM.nextInt(1_000_000));
            if (!exists.test(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("접수번호 생성 실패");
    }

    private static String normalize(String receiptNo) {
        return receiptNo == null ? "" : receiptNo.trim().toUpperCase();
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
