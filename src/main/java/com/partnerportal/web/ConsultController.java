package com.partnerportal.web;

import com.partnerportal.domain.Consult;
import com.partnerportal.service.Categories;
import com.partnerportal.service.InquiryService;
import com.partnerportal.service.PortalException;
import com.partnerportal.web.form.ConsultForm;
import com.partnerportal.web.form.LookupForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final InquiryService inquiryService;

    @ModelAttribute("categories")
    public List<String> categories() {
        return Categories.CONSULT;
    }

    @GetMapping("/apply")
    public String applyForm(Model model) {
        model.addAttribute("form", new ConsultForm());
        return "consult/apply";
    }

    @PostMapping("/apply")
    public String apply(@Valid @ModelAttribute("form") ConsultForm form, BindingResult bindingResult,
                        RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "consult/apply";
        }
        Consult saved = inquiryService.applyConsult(form);
        redirect.addFlashAttribute("receiptNo", saved.getReceiptNo());
        return "redirect:/consult/done";
    }

    @GetMapping("/done")
    public String done(Model model) {
        if (!model.containsAttribute("receiptNo")) {
            return "redirect:/consult/apply";
        }
        model.addAttribute("lookupUrl", "/consult/status");
        model.addAttribute("title", "입점상담 신청이 접수되었습니다");
        return "common/receipt-done";
    }

    @GetMapping("/status")
    public String lookupForm(Model model) {
        model.addAttribute("form", new LookupForm());
        return "consult/lookup";
    }

    @PostMapping("/status")
    public String lookup(@Valid @ModelAttribute("form") LookupForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "consult/lookup";
        }
        try {
            Consult consult = inquiryService.lookupConsult(form.getReceiptNo(), form.getLookupPassword());
            model.addAttribute("consult", consult);
        } catch (PortalException e) {
            bindingResult.reject("portal", e.getMessage());
            return "consult/lookup";
        }
        return "consult/detail";
    }
}
