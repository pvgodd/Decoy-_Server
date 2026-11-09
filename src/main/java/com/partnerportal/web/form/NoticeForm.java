package com.partnerportal.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeForm {

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    private String content;

    private boolean pinned;
}
