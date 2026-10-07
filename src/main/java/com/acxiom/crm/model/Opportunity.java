package com.acxiom.crm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long opportunityId;

    @Column(nullable = false, length = 150)
    private String opportunityName;

    @Column(nullable = false)
    private Long customerId;

    @Column
    private Long leadId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpportunityStage stage = OpportunityStage.QUALIFICATION;

    @Column(nullable = false)
    private Integer probability = 10;

    @Column(nullable = false)
    private LocalDate expectedCloseDate;

    @Column(nullable = false, length = 30)
    private String status = "Open";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(length = 120)
    private String assignedTo;

    public Opportunity() {
    }

    public Opportunity(String opportunityName, Long customerId, Long leadId, BigDecimal amount,
                       OpportunityStage stage, Integer probability, LocalDate expectedCloseDate,
                       String status, String assignedTo) {
        this.opportunityName = opportunityName;
        this.customerId = customerId;
        this.leadId = leadId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.stage = stage != null ? stage : OpportunityStage.QUALIFICATION;
        this.probability = probability != null ? probability : 10;
        this.expectedCloseDate = expectedCloseDate;
        this.status = status != null ? status : "Open";
        this.assignedTo = assignedTo;
        this.createdDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "Open";
        }
        syncStatusWithStage();
    }

    @PreUpdate
    protected void onUpdate() {
        syncStatusWithStage();
    }

    private void syncStatusWithStage() {
        if (stage == OpportunityStage.WON) {
            status = "Won";
        } else if (stage == OpportunityStage.LOST) {
            status = "Lost";
        } else {
            status = "Open";
        }
    }

    public BigDecimal getWeightedValue() {
        if (amount == null || probability == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(BigDecimal.valueOf(probability))
                     .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    // Getters and Setters
    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getOpportunityName() {
        return opportunityName;
    }

    public void setOpportunityName(String opportunityName) {
        this.opportunityName = opportunityName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OpportunityStage getStage() {
        return stage;
    }

    public void setStage(OpportunityStage stage) {
        this.stage = stage;
        syncStatusWithStage();
    }

    public Integer getProbability() {
        return probability;
    }

    public void setProbability(Integer probability) {
        this.probability = probability;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
