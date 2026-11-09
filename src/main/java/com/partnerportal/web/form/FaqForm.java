package com.partnerportal.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FaqForm {

    @NotBlank(message = "분류를 선택하세요.")
    private String category;

    @NotBlank(message = "질문을 입력하세요.")
    @Size(max = 300)
    private String question;

    @NotBlank(message = "답변을 입력하세요.")
    private String answer;

    private int sortOrder;
}
