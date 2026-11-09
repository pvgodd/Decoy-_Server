package com.partnerportal.web;

import com.partnerportal.domain.Hotline;
import com.partnerportal.service.Categories;
import com.partnerportal.service.InquiryService;
import com.partnerportal.service.PortalException;
import com.partnerportal.web.form.HotlineForm;
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

@Controller
@RequestMapping("/hotline")
@RequiredArgsConstructor
public class HotlineController {

    private final InquiryService inquiryService;

    @ModelAttribute("categories")
    public java.util.List<String> categories() {
        return Categories.HOTLINE;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new HotlineForm());
        return "hotline/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") HotlineForm form, BindingResult bindingResult,
                           RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "hotline/register";
        }
        Hotline saved = inquiryService.registerHotline(form);
        redirect.addFlashAttribute("receiptNo", saved.getReceiptNo());
        return "redirect:/hotline/done";
    }

    @GetMapping("/done")
    public String done(Model model) {
        if (!model.containsAttribute("receiptNo")) {
            return "redirect:/hotline/register";
        }
        model.addAttribute("lookupUrl", "/hotline/list");
        model.addAttribute("title", "핫라인 제보가 접수되었습니다");
        return "common/receipt-done";
    }

    @GetMapping("/list")
    public String lookupForm(Model model) {
        model.addAttribute("form", new LookupForm());
        return "hotline/lookup";
    }

    @PostMapping("/list")
    public String lookup(@Valid @ModelAttribute("form") LookupForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "hotline/lookup";
        }
        try {
            Hotline hotline = inquiryService.lookupHotline(form.getReceiptNo(), form.getLookupPassword());
            model.addAttribute("hotline", hotline);
        } catch (PortalException e) {
            bindingResult.reject("portal", e.getMessage());
            return "hotline/lookup";
        }
        return "hotline/detail";
    }
}
