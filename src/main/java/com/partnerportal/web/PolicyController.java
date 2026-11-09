package com.partnerportal.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {

    @GetMapping("/policy/privacy")
    public String privacy() {
        return "policy/privacy";
    }
}
