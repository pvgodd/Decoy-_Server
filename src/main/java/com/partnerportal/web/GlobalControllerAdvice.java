package com.partnerportal.web;

import com.partnerportal.config.AppProperties;
import com.partnerportal.service.PortalException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AppProperties appProperties;

    @ModelAttribute("app")
    public AppProperties app() {
        return appProperties;
    }

    @ExceptionHandler(PortalException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlePortalException(PortalException e, Model model) {
        model.addAttribute("app", appProperties);
        model.addAttribute("message", e.getMessage());
        return "error/message";
    }
}
