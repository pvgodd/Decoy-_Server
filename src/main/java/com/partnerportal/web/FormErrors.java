package com.partnerportal.web;

import com.partnerportal.service.PortalException;
import org.springframework.validation.BindingResult;

final class FormErrors {

    private FormErrors() {
    }

    /** 업무 오류를 폼 필드(또는 폼 전체) 오류로 옮긴다. */
    static void apply(BindingResult bindingResult, PortalException e) {
        if (e.getField() != null) {
            bindingResult.rejectValue(e.getField(), "portal", e.getMessage());
        } else {
            bindingResult.reject("portal", e.getMessage());
        }
    }
}
