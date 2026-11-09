package com.partnerportal.web;

import com.partnerportal.security.PortalUserDetails;
import com.partnerportal.service.MemberService;
import com.partnerportal.service.PortalException;
import com.partnerportal.web.form.PasswordChangeForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberService memberService;

    @GetMapping
    public String myPage(@AuthenticationPrincipal PortalUserDetails user, Model model) {
        model.addAttribute("member", memberService.get(user.getUsername()));
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PasswordChangeForm());
        }
        return "mypage/index";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal PortalUserDetails user,
                                 @Valid @ModelAttribute("form") PasswordChangeForm form,
                                 BindingResult bindingResult, Model model, RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("member", memberService.get(user.getUsername()));
            return "mypage/index";
        }
        try {
            memberService.changePassword(user.getUsername(), form.getCurrentPassword(),
                    form.getNewPassword(), form.getNewPasswordConfirm());
        } catch (PortalException e) {
            FormErrors.apply(bindingResult, e);
            model.addAttribute("member", memberService.get(user.getUsername()));
            return "mypage/index";
        }
        redirect.addFlashAttribute("infoMessage", "비밀번호가 변경되었습니다.");
        return "redirect:/mypage";
    }
}
