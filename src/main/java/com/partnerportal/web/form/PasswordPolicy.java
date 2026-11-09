package com.partnerportal.web.form;

public final class PasswordPolicy {

    /** 8~64자, 영문·숫자·특수문자 각 1자 이상 */
    public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";
    public static final String MESSAGE = "비밀번호는 영문·숫자·특수문자를 포함해 8~64자로 입력하세요.";

    private PasswordPolicy() {
    }
}
