package com.partnerportal.web.form;

import com.partnerportal.domain.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerForm {

    @NotNull(message = "처리 상태를 선택하세요.")
    private InquiryStatus status;

    private String answer;
}
