package com.partnerportal.domain;

public enum InquiryStatus {
    RECEIVED("접수"),
    IN_PROGRESS("처리중"),
    DONE("완료");

    private final String label;

    InquiryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
