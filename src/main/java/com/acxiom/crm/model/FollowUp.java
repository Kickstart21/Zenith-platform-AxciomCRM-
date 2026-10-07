package com.acxiom.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow_ups")
public class FollowUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followUpId;

    @Column
    private Long customerId;

    @Column
    private Long leadId;

    @Column(nullable = false)
    private LocalDateTime followUpDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FollowUpType followUpType = FollowUpType.CALL;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FollowUpStatus status = FollowUpStatus.PLANNED;

    @Column(length = 120)
    private String assignedTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public FollowUp() {
    }

    public FollowUp(Long customerId, Long leadId, LocalDateTime followUpDate,
                    FollowUpType followUpType, String remarks, FollowUpStatus status, String assignedTo) {
        this.customerId = customerId;
        this.leadId = leadId;
        this.followUpDate = followUpDate;
        this.followUpType = followUpType != null ? followUpType : FollowUpType.CALL;
        this.remarks = remarks;
        this.status = status != null ? status : FollowUpStatus.PLANNED;
        this.assignedTo = assignedTo;
        this.createdDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (status == null) {
            status = FollowUpStatus.PLANNED;
        }
        if (followUpType == null) {
            followUpType = FollowUpType.CALL;
        }
    }

    public boolean isOverdue() {
        return status == FollowUpStatus.PLANNED && followUpDate != null && followUpDate.isBefore(LocalDateTime.now());
    }

    // Getters and Setters
    public Long getFollowUpId() {
        return followUpId;
    }

    public void setFollowUpId(Long followUpId) {
        this.followUpId = followUpId;
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

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public FollowUpType getFollowUpType() {
        return followUpType;
    }

    public void setFollowUpType(FollowUpType followUpType) {
        this.followUpType = followUpType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public FollowUpStatus getStatus() {
        return status;
    }

    public void setStatus(FollowUpStatus status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
