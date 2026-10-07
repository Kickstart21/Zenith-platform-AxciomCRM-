package com.acxiom.crm.dto;

import com.acxiom.crm.model.LeadStatus;
import com.acxiom.crm.validator.ValidIndianPhone;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class LeadDto {

    private Long leadId;

    private String leadCode;

    @NotBlank(message = "Lead name is mandatory.")
    @Size(max = 120, message = "Lead Name cannot exceed 120 characters.")
    private String leadName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @ValidIndianPhone(message = "Enter a valid phone number.")
    private String phone;

    @Size(max = 150, message = "Company Name cannot exceed 150 characters.")
    private String companyName;

    @Size(max = 80, message = "Source cannot exceed 80 characters.")
    private String source;

    @NotNull(message = "Lead status is mandatory.")
    private LeadStatus status = LeadStatus.NEW;

    @NotNull(message = "Expected value is required.")
    @DecimalMin(value = "0.0", inclusive = true, message = "Expected value must be a positive number.")
    private BigDecimal expectedValue = BigDecimal.ZERO;

    private String assignedTo;

    public LeadDto() {
    }

    // Getters and Setters
    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

    public String getLeadCode() {
        return leadCode;
    }

    public void setLeadCode(String leadCode) {
        this.leadCode = leadCode;
    }

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public BigDecimal getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(BigDecimal expectedValue) {
        this.expectedValue = expectedValue;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}
