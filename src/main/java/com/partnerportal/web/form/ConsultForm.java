package com.partnerportal.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsultForm {

    @NotBlank(message = "회사명을 입력하세요.")
    @Size(max = 100)
    private String companyName;

    @NotBlank(message = "사업자등록번호를 입력하세요.")
    @Pattern(regexp = "^\\d{3}-?\\d{2}-?\\d{5}$", message = "사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String bizNo;

    @NotBlank(message = "담당자명을 입력하세요.")
    @Size(max = 50)
    private String managerName;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "연락처를 입력하세요.")
    @Pattern(regexp = "^[0-9\\-]{9,20}$", message = "연락처는 숫자와 -만 입력하세요.")
    private String phone;

    @NotBlank(message = "상담 분야를 선택하세요.")
    private String category;

    @Size(max = 300)
    private String homepage;

    @NotBlank(message = "상담 내용을 입력하세요.")
    @Size(max = 5000, message = "내용은 5,000자 이내로 입력하세요.")
    private String content;

    @NotBlank(message = "조회 비밀번호를 입력하세요.")
    @Size(min = 4, max = 20, message = "조회 비밀번호는 4~20자입니다.")
    private String lookupPassword;

    @AssertTrue(message = "개인정보 수집·이용에 동의해야 신청할 수 있습니다.")
    private boolean agreePrivacy;
}
