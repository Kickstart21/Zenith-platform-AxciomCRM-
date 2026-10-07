package com.acxiom.crm.model;

public enum ActivityType {
    CALL("Phone Call"),
    MEETING("Client Meeting"),
    EMAIL("Email Sent"),
    TASK("CRM Task");

    private final String label;

    ActivityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
