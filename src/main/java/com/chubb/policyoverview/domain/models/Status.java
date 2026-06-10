package com.chubb.policyoverview.domain.models;

public enum Status {
    ACTIVE("Active"),
    EXPIRED("Expired"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
