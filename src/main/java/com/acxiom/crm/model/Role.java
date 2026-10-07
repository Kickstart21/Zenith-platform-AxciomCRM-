package com.acxiom.crm.model;

public enum Role {
    ADMIN("Admin"),
    MANAGER("Manager"),
    SALES_EXECUTIVE("Sales Executive");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
