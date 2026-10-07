package com.acxiom.crm.model;

public enum FollowUpType {
    CALL("Call"),
    MEETING("Meeting"),
    EMAIL("Email"),
    DEMO("Product Demo");

    private final String label;

    FollowUpType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
