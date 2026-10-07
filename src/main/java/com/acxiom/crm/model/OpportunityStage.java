package com.acxiom.crm.model;

public enum OpportunityStage {
    QUALIFICATION("Qualification"),
    PROPOSAL("Proposal"),
    NEGOTIATION("Negotiation"),
    WON("Won"),
    LOST("Lost");

    private final String label;

    OpportunityStage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
