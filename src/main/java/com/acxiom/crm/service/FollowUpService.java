package com.acxiom.crm.service;

import com.acxiom.crm.dto.FollowUpDto;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.model.FollowUp;
import com.acxiom.crm.model.FollowUpStatus;
import com.acxiom.crm.model.Role;
import com.acxiom.crm.model.User;
import com.acxiom.crm.repository.FollowUpRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final AuditService auditService;
    private final UserService userService;

    public FollowUpService(FollowUpRepository followUpRepository,
                           AuditService auditService,
                           UserService userService) {
        this.followUpRepository = followUpRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public Page<FollowUp> getFollowUps(FollowUpStatus status, Long customerId, Long leadId, Pageable pageable) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return followUpRepository.searchFollowUps(status, currentUser, customerId, leadId, pageable);
        }
        return followUpRepository.searchFollowUps(status, null, customerId, leadId, pageable);
    }

    public FollowUp getFollowUpById(Long id) {
        return followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with ID: " + id));
    }

    @Transactional
    public FollowUp scheduleFollowUp(FollowUpDto dto) {
        if (dto.getFollowUpDate() == null || dto.getFollowUpDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Follow-up date cannot be earlier than today.");
        }

        String currentUser = auditService.getCurrentUsername();
        String assigned = dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()
                ? dto.getAssignedTo().trim()
                : currentUser;

        FollowUp followUp = new FollowUp(
                dto.getCustomerId(),
                dto.getLeadId(),
                dto.getFollowUpDate(),
                dto.getFollowUpType(),
                dto.getRemarks(),
                FollowUpStatus.PLANNED,
                assigned
        );

        FollowUp saved = followUpRepository.save(followUp);
        auditService.log("CREATE", "FollowUp", saved.getFollowUpId().toString(), null, saved);
        return saved;
    }

    @Transactional
    public FollowUp updateFollowUp(Long id, FollowUpDto dto) {
        FollowUp existing = getFollowUpById(id);
        enforceAccess(existing);

        if (dto.getStatus() == FollowUpStatus.PLANNED && dto.getFollowUpDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Follow-up date cannot be earlier than today.");
        }

        FollowUp oldCopy = copyOf(existing);

        existing.setFollowUpDate(dto.getFollowUpDate());
        existing.setFollowUpType(dto.getFollowUpType());
        existing.setRemarks(dto.getRemarks());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()) {
            existing.setAssignedTo(dto.getAssignedTo().trim());
        }

        FollowUp updated = followUpRepository.save(existing);
        auditService.log("UPDATE", "FollowUp", updated.getFollowUpId().toString(), oldCopy, updated);
        return updated;
    }

    @Transactional
    public void completeFollowUp(Long id) {
        FollowUp followUp = getFollowUpById(id);
        enforceAccess(followUp);
        FollowUp oldCopy = copyOf(followUp);
        followUp.setStatus(FollowUpStatus.COMPLETED);
        followUpRepository.save(followUp);
        auditService.log("UPDATE", "FollowUp", id.toString(), oldCopy, "Marked as COMPLETED");
    }

    @Transactional
    public void deleteFollowUp(Long id) {
        FollowUp followUp = getFollowUpById(id);
        enforceAccess(followUp);
        FollowUp oldCopy = copyOf(followUp);
        followUpRepository.delete(followUp);
        auditService.log("DELETE", "FollowUp", id.toString(), oldCopy, "Deleted follow-up");
    }

    public void enforceAccess(FollowUp followUp) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            if (!currentUser.equalsIgnoreCase(followUp.getAssignedTo())) {
                throw new IllegalArgumentException("Access Denied: You can only modify follow-ups assigned to you.");
            }
        }
    }

    private FollowUp copyOf(FollowUp f) {
        FollowUp c = new FollowUp();
        c.setFollowUpId(f.getFollowUpId());
        c.setCustomerId(f.getCustomerId());
        c.setLeadId(f.getLeadId());
        c.setFollowUpDate(f.getFollowUpDate());
        c.setFollowUpType(f.getFollowUpType());
        c.setStatus(f.getStatus());
        c.setAssignedTo(f.getAssignedTo());
        return c;
    }
}
