package com.partnerportal.web;

import com.partnerportal.domain.MemberStatus;
import com.partnerportal.security.PortalUserDetails;
import com.partnerportal.service.BoardService;
import com.partnerportal.service.Categories;
import com.partnerportal.service.InquiryService;
import com.partnerportal.service.MemberService;
import com.partnerportal.web.form.AnswerForm;
import com.partnerportal.web.form.FaqForm;
import com.partnerportal.web.form.NoticeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;
    private final InquiryService inquiryService;
    private final BoardService boardService;

    @GetMapping
    public String home(Model model) {
        model.addAttribute("pendingMembers", memberService.list(MemberStatus.PENDING,
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("openHotlines", inquiryService.countOpenHotlines());
        model.addAttribute("openConsults", inquiryService.countOpenConsults());
        return "admin/home";
    }

    // ---- 회원 관리 ----

    @GetMapping("/members")
    public String members(@RequestParam(required = false) MemberStatus status,
                          @PageableDefault(size = 15) Pageable pageable, Model model) {
        model.addAttribute("page", memberService.list(status, pageable));
        model.addAttribute("statuses", MemberStatus.values());
        model.addAttribute("selected", status);
        return "admin/members";
    }

    @PostMapping("/members/{id}/status")
    public String changeMemberStatus(@PathVariable Long id, @RequestParam MemberStatus status,
                                     @AuthenticationPrincipal PortalUserDetails user,
                                     RedirectAttributes redirect) {
        memberService.changeStatus(id, status, user.getUsername());
        redirect.addFlashAttribute("infoMessage", "회원 상태를 변경했습니다.");
        return "redirect:/admin/members";
    }

    // ---- 핫라인 ----

    @GetMapping("/hotlines")
    public String hotlines(@PageableDefault(size = 15) Pageable pageable, Model model) {
        model.addAttribute("page", inquiryService.listHotlines(pageable));
        return "admin/hotline-list";
    }

    @GetMapping("/hotlines/{id}")
    public String hotline(@PathVariable Long id, Model model) {
        var hotline = inquiryService.getHotline(id);
        model.addAttribute("hotline", hotline);
        model.addAttribute("form", answerForm(hotline.getStatus(), hotline.getAnswer()));
        return "admin/hotline-detail";
    }

    @PostMapping("/hotlines/{id}")
    public String answerHotline(@PathVariable Long id, @Valid @ModelAttribute("form") AnswerForm form,
                                BindingResult bindingResult, Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("hotline", inquiryService.getHotline(id));
            return "admin/hotline-detail";
        }
        inquiryService.answerHotline(id, form);
        redirect.addFlashAttribute("infoMessage", "처리 내용을 저장했습니다.");
        return "redirect:/admin/hotlines/" + id;
    }

    // ---- 입점상담 ----

    @GetMapping("/consults")
    public String consults(@PageableDefault(size = 15) Pageable pageable, Model model) {
        model.addAttribute("page", inquiryService.listConsults(pageable));
        return "admin/consult-list";
    }

    @GetMapping("/consults/{id}")
    public String consult(@PathVariable Long id, Model model) {
        var consult = inquiryService.getConsult(id);
        model.addAttribute("consult", consult);
        model.addAttribute("form", answerForm(consult.getStatus(), consult.getAnswer()));
        return "admin/consult-detail";
    }

    @PostMapping("/consults/{id}")
    public String answerConsult(@PathVariable Long id, @Valid @ModelAttribute("form") AnswerForm form,
                                BindingResult bindingResult, Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("consult", inquiryService.getConsult(id));
            return "admin/consult-detail";
        }
        inquiryService.answerConsult(id, form);
        redirect.addFlashAttribute("infoMessage", "처리 내용을 저장했습니다.");
        return "redirect:/admin/consults/" + id;
    }

    // ---- 공지사항 ----

    @GetMapping("/notices")
    public String notices(@PageableDefault(size = 15) Pageable pageable, Model model) {
        model.addAttribute("page", boardService.notices(null, pageable));
        return "admin/notice-list";
    }

    @GetMapping("/notices/new")
    public String newNotice(Model model) {
        model.addAttribute("form", new NoticeForm());
        model.addAttribute("mode", "new");
        return "admin/notice-form";
    }

    @GetMapping("/notices/{id}/edit")
    public String editNotice(@PathVariable Long id, Model model) {
        var notice = boardService.getNotice(id);
        NoticeForm form = new NoticeForm();
        form.setTitle(notice.getTitle());
        form.setContent(notice.getContent());
        form.setPinned(notice.isPinned());
        model.addAttribute("form", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("id", id);
        return "admin/notice-form";
    }

    @PostMapping("/notices")
    public String saveNotice(@RequestParam(required = false) Long id,
                             @Valid @ModelAttribute("form") NoticeForm form, BindingResult bindingResult,
                             @AuthenticationPrincipal PortalUserDetails user, Model model,
                             RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", id == null ? "new" : "edit");
            model.addAttribute("id", id);
            return "admin/notice-form";
        }
        boardService.saveNotice(id, form, user.getUsername());
        redirect.addFlashAttribute("infoMessage", "공지사항을 저장했습니다.");
        return "redirect:/admin/notices";
    }

    @PostMapping("/notices/{id}/delete")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirect) {
        boardService.deleteNotice(id);
        redirect.addFlashAttribute("infoMessage", "공지사항을 삭제했습니다.");
        return "redirect:/admin/notices";
    }

    // ---- FAQ ----

    @GetMapping("/faqs")
    public String faqs(Model model) {
        model.addAttribute("faqs", boardService.faqs(null));
        return "admin/faq-list";
    }

    @GetMapping("/faqs/new")
    public String newFaq(Model model) {
        model.addAttribute("form", new FaqForm());
        model.addAttribute("categories", Categories.FAQ);
        model.addAttribute("mode", "new");
        return "admin/faq-form";
    }

    @GetMapping("/faqs/{id}/edit")
    public String editFaq(@PathVariable Long id, Model model) {
        var faq = boardService.getFaq(id);
        FaqForm form = new FaqForm();
        form.setCategory(faq.getCategory());
        form.setQuestion(faq.getQuestion());
        form.setAnswer(faq.getAnswer());
        form.setSortOrder(faq.getSortOrder());
        model.addAttribute("form", form);
        model.addAttribute("categories", Categories.FAQ);
        model.addAttribute("mode", "edit");
        model.addAttribute("id", id);
        return "admin/faq-form";
    }

    @PostMapping("/faqs")
    public String saveFaq(@RequestParam(required = false) Long id,
                          @Valid @ModelAttribute("form") FaqForm form, BindingResult bindingResult,
                          Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Categories.FAQ);
            model.addAttribute("mode", id == null ? "new" : "edit");
            model.addAttribute("id", id);
            return "admin/faq-form";
        }
        boardService.saveFaq(id, form);
        redirect.addFlashAttribute("infoMessage", "FAQ를 저장했습니다.");
        return "redirect:/admin/faqs";
    }

    @PostMapping("/faqs/{id}/delete")
    public String deleteFaq(@PathVariable Long id, RedirectAttributes redirect) {
        boardService.deleteFaq(id);
        redirect.addFlashAttribute("infoMessage", "FAQ를 삭제했습니다.");
        return "redirect:/admin/faqs";
    }

    private static AnswerForm answerForm(com.partnerportal.domain.InquiryStatus status, String answer) {
        AnswerForm form = new AnswerForm();
        form.setStatus(status);
        form.setAnswer(answer);
        return form;
    }
}
