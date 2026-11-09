package com.partnerportal.service;

import com.partnerportal.domain.Faq;
import com.partnerportal.domain.Notice;
import com.partnerportal.repository.FaqRepository;
import com.partnerportal.repository.NoticeRepository;
import com.partnerportal.web.form.FaqForm;
import com.partnerportal.web.form.NoticeForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 공지사항·FAQ */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final NoticeRepository noticeRepository;
    private final FaqRepository faqRepository;

    // ---- 공지사항 ----

    public Page<Notice> notices(String keyword, Pageable pageable) {
        return StringUtils.hasText(keyword)
                ? noticeRepository.findByTitleContainingIgnoreCase(keyword.trim(), pageable)
                : noticeRepository.findAll(pageable);
    }

    public List<Notice> recentNotices() {
        return noticeRepository.findTop5ByOrderByPinnedDescCreatedAtDesc();
    }

    @Transactional
    public Notice readNotice(Long id) {
        Notice notice = getNotice(id);
        noticeRepository.increaseViewCount(id);
        return notice;
    }

    public Notice getNotice(Long id) {
        return noticeRepository.findById(id).orElseThrow(() -> new PortalException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional
    public Notice saveNotice(Long id, NoticeForm form, String author) {
        Notice notice = id == null ? new Notice() : getNotice(id);
        notice.setTitle(form.getTitle().trim());
        notice.setContent(form.getContent());
        notice.setPinned(form.isPinned());
        if (id == null) {
            notice.setAuthor(author);
        }
        return noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }

    // ---- FAQ ----

    public List<Faq> faqs(String category) {
        return StringUtils.hasText(category)
                ? faqRepository.findByCategoryOrderBySortOrderAscIdAsc(category)
                : faqRepository.findAllByOrderByCategoryAscSortOrderAscIdAsc();
    }

    public Faq getFaq(Long id) {
        return faqRepository.findById(id).orElseThrow(() -> new PortalException("FAQ를 찾을 수 없습니다."));
    }

    @Transactional
    public Faq saveFaq(Long id, FaqForm form) {
        Faq faq = id == null ? new Faq() : getFaq(id);
        faq.setCategory(form.getCategory());
        faq.setQuestion(form.getQuestion().trim());
        faq.setAnswer(form.getAnswer());
        faq.setSortOrder(form.getSortOrder());
        return faqRepository.save(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }
}
