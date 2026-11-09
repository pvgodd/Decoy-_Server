package com.partnerportal.service;

/** 사용자에게 그대로 보여줄 수 있는 업무 오류 */
public class PortalException extends RuntimeException {

    private final String field;

    public PortalException(String message) {
        this(null, message);
    }

    public PortalException(String field, String message) {
        super(message);
        this.field = field;
    }

    /** 오류를 표시할 폼 필드명 (없으면 폼 전체 오류) */
    public String getField() {
        return field;
    }
}
