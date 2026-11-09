package com.partnerportal.web;

import com.partnerportal.service.BoardService;
import com.partnerportal.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.partnerportal.security.PortalUserDetails;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final BoardService boardService;
    private final MemberService memberService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal PortalUserDetails user, Model model) {
        model.addAttribute("member", memberService.get(user.getUsername()));
        model.addAttribute("notices", boardService.recentNotices());
        return "dashboard";
    }
}
