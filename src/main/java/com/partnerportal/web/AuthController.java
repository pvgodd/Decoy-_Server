package com.partnerportal.web;

import com.partnerportal.service.MemberService;
import com.partnerportal.service.PasswordResetService;
import com.partnerportal.service.PortalException;
import com.partnerportal.web.form.FindIdForm;
import com.partnerportal.web.form.JoinForm;
import com.partnerportal.web.form.PasswordRequestForm;
import com.partnerportal.web.form.PasswordResetForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/")
    public String home(Authentication authentication) {
        return isLoggedIn(authentication) ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Authentication authentication,
                        @RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("errorMessage", switch (error) {
                case "locked" -> "비밀번호 오류가 반복되어 계정이 잠겼습니다. 비밀번호 초기화 후 이용해 주세요.";
                case "pending" -> "가입 승인 대기 중입니다. 승인 완료 후 로그인할 수 있습니다.";
                case "rejected" -> "가입이 반려된 계정입니다. 관리자에게 문의해 주세요.";
                default -> "아이디 또는 비밀번호가 올바르지 않습니다.";
            });
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "로그아웃되었습니다.");
        }
        return "auth/login";
    }

    // ---- 회원가입 ----

    @GetMapping("/member/join")
    public String joinForm(Model model) {
        model.addAttribute("form", new JoinForm());
        return "auth/join";
    }

    @PostMapping("/member/join")
    public String join(@Valid @ModelAttribute("form") JoinForm form, BindingResult bindingResult,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "auth/join";
        }
        try {
            memberService.join(form);
        } catch (PortalException e) {
            FormErrors.apply(bindingResult, e);
            return "auth/join";
        }
        redirect.addFlashAttribute("companyName", form.getCompanyName());
        return "redirect:/member/join/done";
    }

    @GetMapping("/member/join/done")
    public String joinDone(Model model) {
        if (!model.containsAttribute("companyName")) {
            return "redirect:/login";
        }
        return "auth/join-done";
    }

    @GetMapping("/member/check-id")
    @ResponseBody
    public Map<String, Boolean> checkId(@RequestParam String loginId) {
        return Map.of("available", memberService.isLoginIdAvailable(loginId));
    }

    // ---- 아이디 찾기 ----

    @GetMapping("/support/find-id")
    public String findIdForm(Model model) {
        model.addAttribute("form", new FindIdForm());
        return "auth/find-id";
    }

    @PostMapping("/support/find-id")
    public String findId(@Valid @ModelAttribute("form") FindIdForm form, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/find-id";
        }
        List<String> ids = memberService.findMaskedLoginIds(form.getCompanyName(), form.getEmail());
        model.addAttribute("searched", true);
        model.addAttribute("loginIds", ids);
        return "auth/find-id";
    }

    // ---- 비밀번호 초기화 ----

    @GetMapping("/support/password")
    public String passwordRequestForm(Model model) {
        model.addAttribute("form", new PasswordRequestForm());
        return "auth/password-request";
    }

    @PostMapping("/support/password")
    public String passwordRequest(@Valid @ModelAttribute("form") PasswordRequestForm form,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/password-request";
        }
        passwordResetService.request(form.getLoginId(), form.getEmail());
        model.addAttribute("requested", true);
        return "auth/password-request";
    }

    @GetMapping("/support/reset")
    public String resetForm(@RequestParam(required = false) String token, Model model) {
        if (!passwordResetService.isValid(token)) {
            model.addAttribute("message", "링크가 만료되었거나 이미 사용되었습니다. 비밀번호 초기화를 다시 요청해 주세요.");
            return "error/message";
        }
        PasswordResetForm form = new PasswordResetForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "auth/password-reset";
    }

    @PostMapping("/support/reset")
    public String reset(@Valid @ModelAttribute("form") PasswordResetForm form, BindingResult bindingResult,
                        RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "auth/password-reset";
        }
        try {
            passwordResetService.reset(form.getToken(), form.getPassword(), form.getPasswordConfirm());
        } catch (PortalException e) {
            FormErrors.apply(bindingResult, e);
            return "auth/password-reset";
        }
        redirect.addFlashAttribute("infoMessage", "비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.");
        return "redirect:/login";
    }

    private static boolean isLoggedIn(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
