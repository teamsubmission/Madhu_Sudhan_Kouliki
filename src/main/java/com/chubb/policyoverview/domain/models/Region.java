package com.chubb.policyoverview.domain.models;

public enum Region {
    SINGAPORE("Singapore"),
    HONG_KONG("Hong Kong"),
    AUSTRALIA("Australia"),
    JAPAN("Japan"),
    THAILAND("Thailand"),
    INDONESIA("Indonesia"),
    MALAYSIA("Malaysia"),
    PHILIPPINES("Philippines");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
