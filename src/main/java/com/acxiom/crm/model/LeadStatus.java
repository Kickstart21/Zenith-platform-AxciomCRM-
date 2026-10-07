package com.acxiom.crm.model;

public enum LeadStatus {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    UNQUALIFIED("Unqualified"),
    CONVERTED("Converted"),
    LOST("Lost");

    private final String label;

    LeadStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
