package com.partnerportal.domain;

public enum MemberStatus {
    PENDING("승인대기"),
    ACTIVE("정상"),
    LOCKED("잠금"),
    REJECTED("반려");

    private final String label;

    MemberStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
