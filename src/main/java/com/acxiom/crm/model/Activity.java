package com.acxiom.crm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityType activityType = ActivityType.CALL;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime activityDate;

    @Column
    private Long customerId;

    @Column
    private Long leadId;

    @Column(length = 120)
    private String assignedTo;

    @Column(nullable = false, length = 30)
    private String status = "Completed";

    public Activity() {
    }

    public Activity(ActivityType activityType, String subject, String description,
                    LocalDateTime activityDate, Long customerId, Long leadId, String assignedTo, String status) {
        this.activityType = activityType != null ? activityType : ActivityType.CALL;
        this.subject = subject;
        this.description = description;
        this.activityDate = activityDate != null ? activityDate : LocalDateTime.now();
        this.customerId = customerId;
        this.leadId = leadId;
        this.assignedTo = assignedTo;
        this.status = status != null ? status : "Completed";
    }

    @PrePersist
    protected void onCreate() {
        if (activityDate == null) {
            activityDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "Completed";
        }
        if (activityType == null) {
            activityType = ActivityType.CALL;
        }
    }

    // Getters and Setters
    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDateTime activityDate) {
        this.activityDate = activityDate;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
