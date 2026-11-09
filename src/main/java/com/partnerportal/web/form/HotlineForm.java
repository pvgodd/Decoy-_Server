package com.partnerportal.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotlineForm {

    @NotBlank(message = "제보 유형을 선택하세요.")
    private String category;

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력하세요.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 5000, message = "내용은 5,000자 이내로 입력하세요.")
    private String content;

    /** 비워두면 익명 */
    @Size(max = 50)
    private String reporterName;

    @Size(max = 120)
    private String contact;

    @NotBlank(message = "조회 비밀번호를 입력하세요.")
    @Size(min = 4, max = 20, message = "조회 비밀번호는 4~20자입니다.")
    private String lookupPassword;

    @AssertTrue(message = "개인정보 수집·이용에 동의해야 접수할 수 있습니다.")
    private boolean agreePrivacy;
}
