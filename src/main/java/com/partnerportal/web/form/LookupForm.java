package com.partnerportal.web.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LookupForm {

    @NotBlank(message = "접수번호를 입력하세요.")
    private String receiptNo;

    @NotBlank(message = "조회 비밀번호를 입력하세요.")
    private String lookupPassword;
}
