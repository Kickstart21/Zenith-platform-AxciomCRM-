package com.acxiom.crm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LeadConversionDto {

    @NotNull(message = "Lead ID is required.")
    private Long leadId;

    @NotBlank(message = "Opportunity Name is required.")
    private String opportunityName;

    @NotNull(message = "Opportunity Amount is required.")
    @DecimalMin(value = "0.01", message = "Opportunity Amount must be greater than 0.")
    private BigDecimal amount;

    @NotNull(message = "Expected Close Date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedCloseDate;

    public LeadConversionDto() {
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getOpportunityName() {
        return opportunityName;
    }

    public void setOpportunityName(String opportunityName) {
        this.opportunityName = opportunityName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }
}
