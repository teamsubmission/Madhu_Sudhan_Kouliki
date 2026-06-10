package com.chubb.policyoverview.domain.models;

public enum LineOfBusiness {
    PROPERTY("Property"),
    CASUALTY("Casualty"),
    ACCIDENT_AND_HEALTH("A&H"),
    MARINE("Marine");

    private final String displayName;

    LineOfBusiness(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
