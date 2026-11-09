package com.partnerportal.domain;

public enum MemberRole {
    PARTNER("협력사"),
    ADMIN("관리자");

    private final String label;

    MemberRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
