package com.acxiom.crm.model;

public enum FollowUpStatus {
    PLANNED("Planned"),
    COMPLETED("Completed"),
    MISSED("Missed"),
    CANCELLED("Cancelled");

    private final String label;

    FollowUpStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
