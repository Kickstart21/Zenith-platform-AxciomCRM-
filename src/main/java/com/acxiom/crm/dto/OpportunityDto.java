package com.acxiom.crm.dto;

import com.acxiom.crm.model.OpportunityStage;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OpportunityDto {

    private Long opportunityId;

    @NotBlank(message = "Opportunity Name is required.")
    @Size(max = 150, message = "Opportunity Name cannot exceed 150 characters.")
    private String opportunityName;

    @NotNull(message = "Customer is required.")
    private Long customerId;

    private Long leadId;

    @NotNull(message = "Opportunity Amount is required.")
    @DecimalMin(value = "0.01", message = "Opportunity Amount must be greater than 0.")
    private BigDecimal amount;

    @NotNull(message = "Stage is required.")
    private OpportunityStage stage = OpportunityStage.QUALIFICATION;

    @NotNull(message = "Probability is required.")
    @Min(value = 0, message = "Probability must be between 0 and 100.")
    @Max(value = 100, message = "Probability must be between 0 and 100.")
    private Integer probability = 10;

    @NotNull(message = "Expected Close Date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedCloseDate;

    private String status = "Open";

    private String assignedTo;

    private BigDecimal weightedValue;

    public OpportunityDto() {
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public BigDecimal getWeightedValue() {
        return weightedValue;
    }

    public void setWeightedValue(BigDecimal weightedValue) {
        this.weightedValue = weightedValue;
    }
}
