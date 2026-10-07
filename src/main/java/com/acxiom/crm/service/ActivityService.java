package com.acxiom.crm.service;

import com.acxiom.crm.dto.ActivityDto;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.model.Activity;
import com.acxiom.crm.model.ActivityType;
import com.acxiom.crm.model.Role;
import com.acxiom.crm.model.User;
import com.acxiom.crm.repository.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final AuditService auditService;
    private final UserService userService;

    public ActivityService(ActivityRepository activityRepository,
                           AuditService auditService,
                           UserService userService) {
        this.activityRepository = activityRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public Page<Activity> getActivities(ActivityType type, String status, Pageable pageable) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return activityRepository.searchActivities(type, status, currentUser, pageable);
        }
        return activityRepository.searchActivities(type, status, null, pageable);
    }

    public Activity getActivityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + id));
    }

    @Transactional
    public Activity createActivity(ActivityDto dto) {
        String currentUser = auditService.getCurrentUsername();
        String assigned = dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()
                ? dto.getAssignedTo().trim()
                : currentUser;

        Activity activity = new Activity(
                dto.getActivityType(),
                dto.getSubject().trim(),
                dto.getDescription(),
                dto.getActivityDate(),
                dto.getCustomerId(),
                dto.getLeadId(),
                assigned,
                dto.getStatus() != null ? dto.getStatus() : "Completed"
        );

        Activity saved = activityRepository.save(activity);
        auditService.log("CREATE", "Activity", saved.getActivityId().toString(), null, saved);
        return saved;
    }

    @Transactional
    public Activity updateActivity(Long id, ActivityDto dto) {
        Activity existing = getActivityById(id);
        enforceAccess(existing);

        Activity oldCopy = copyOf(existing);

        existing.setActivityType(dto.getActivityType());
        existing.setSubject(dto.getSubject().trim());
        existing.setDescription(dto.getDescription());
        existing.setActivityDate(dto.getActivityDate());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()) {
            existing.setAssignedTo(dto.getAssignedTo().trim());
        }

        Activity updated = activityRepository.save(existing);
        auditService.log("UPDATE", "Activity", updated.getActivityId().toString(), oldCopy, updated);
        return updated;
    }

    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = getActivityById(id);
        enforceAccess(activity);
        Activity oldCopy = copyOf(activity);
        activityRepository.delete(activity);
        auditService.log("DELETE", "Activity", id.toString(), oldCopy, "Deleted activity");
    }

    public void enforceAccess(Activity activity) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            if (!currentUser.equalsIgnoreCase(activity.getAssignedTo())) {
                throw new IllegalArgumentException("Access Denied: You can only modify activities assigned to you.");
            }
        }
    }

    private Activity copyOf(Activity a) {
        Activity c = new Activity();
        c.setActivityId(a.getActivityId());
        c.setActivityType(a.getActivityType());
        c.setSubject(a.getSubject());
        c.setStatus(a.getStatus());
        c.setAssignedTo(a.getAssignedTo());
        return c;
    }
}
