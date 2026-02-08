package com.maenterprise.erp.models;

public enum SalesChannel {
    DIRECT("Direct"),
    ONLINE("Online"),
    WHOLESALE("Wholesale"),
    RETAIL("Retail");

    private final String displayName;

    SalesChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
