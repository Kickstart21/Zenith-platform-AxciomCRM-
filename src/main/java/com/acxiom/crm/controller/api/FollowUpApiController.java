package com.acxiom.crm.controller.api;

import com.acxiom.crm.dto.FollowUpDto;
import com.acxiom.crm.model.FollowUp;
import com.acxiom.crm.model.FollowUpStatus;
import com.acxiom.crm.service.FollowUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/followups")
@Tag(name = "Follow-Up API", description = "Endpoints for Follow-Up Activities")
public class FollowUpApiController {

    private final FollowUpService followUpService;

    public FollowUpApiController(FollowUpService followUpService) {
        this.followUpService = followUpService;
    }

    @GetMapping
    @Operation(summary = "Get all follow-ups with optional status, customer, lead filters")
    public ResponseEntity<List<FollowUpDto>> getAllFollowUps(
            @RequestParam(value = "status", required = false) FollowUpStatus status,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "leadId", required = false) Long leadId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<FollowUp> pageResult = followUpService.getFollowUps(status, customerId, leadId, PageRequest.of(page, size));
        List<FollowUpDto> dtos = pageResult.getContent().stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get follow-up by ID")
    public ResponseEntity<FollowUpDto> getFollowUpById(@PathVariable("id") Long id) {
        FollowUp followUp = followUpService.getFollowUpById(id);
        return ResponseEntity.ok(toDto(followUp));
    }

    @PostMapping
    @Operation(summary = "Create/Schedule a new follow-up")
    public ResponseEntity<FollowUpDto> createFollowUp(@Valid @RequestBody FollowUpDto dto) {
        FollowUp created = followUpService.scheduleFollowUp(dto);
        return new ResponseEntity<>(toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing follow-up")
    public ResponseEntity<FollowUpDto> updateFollowUp(@PathVariable("id") Long id, @Valid @RequestBody FollowUpDto dto) {
        FollowUp updated = followUpService.updateFollowUp(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark follow-up as completed")
    public ResponseEntity<Void> completeFollowUp(@PathVariable("id") Long id) {
        followUpService.completeFollowUp(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete follow-up by ID")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable("id") Long id) {
        followUpService.deleteFollowUp(id);
        return ResponseEntity.noContent().build();
    }

    private FollowUpDto toDto(FollowUp f) {
        FollowUpDto dto = new FollowUpDto();
        dto.setFollowUpId(f.getFollowUpId());
        dto.setCustomerId(f.getCustomerId());
        dto.setLeadId(f.getLeadId());
        dto.setFollowUpDate(f.getFollowUpDate());
        dto.setFollowUpType(f.getFollowUpType());
        dto.setRemarks(f.getRemarks());
        dto.setStatus(f.getStatus());
        dto.setAssignedTo(f.getAssignedTo());
        dto.setOverdue(f.getStatus() == FollowUpStatus.PLANNED && f.getFollowUpDate().isBefore(LocalDateTime.now()));
        return dto;
    }
}
