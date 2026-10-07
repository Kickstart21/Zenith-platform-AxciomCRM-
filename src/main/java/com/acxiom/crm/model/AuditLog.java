package com.acxiom.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLogId;

    @Column(nullable = false, length = 120)
    private String userId; // Email or username of the actor

    @Column(nullable = false, length = 60)
    private String action; // LOGIN, FAILED_LOGIN, LOCKOUT, LOGOUT, CREATE, UPDATE, DELETE, ROLE_CHANGE, SECURITY

    @Column(nullable = false, length = 80)
    private String entityName; // Customer, Lead, Opportunity, FollowUp, User, Session

    @Column(length = 80)
    private String recordId;

    @Column(length = 4000)
    private String oldValue; // Old JSON value or description

    @Column(length = 4000)
    private String newValue; // New JSON value or description

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(length = 60)
    private String ipAddress;

    public AuditLog() {
    }

    public AuditLog(String userId, String action, String entityName, String recordId,
                    String oldValue, String newValue, String ipAddress) {
        this.userId = userId != null ? userId : "ANONYMOUS";
        this.action = action;
        this.entityName = entityName;
        this.recordId = recordId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";
        this.createdDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long auditLogId) {
        this.auditLogId = auditLogId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
