package com.acxiom.crm.controller.api;

import com.acxiom.crm.dto.OpportunityDto;
import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.model.OpportunityStage;
import com.acxiom.crm.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
@Tag(name = "Opportunity API", description = "Endpoints for Opportunity & Sales Pipeline Management")
public class OpportunityApiController {

    private final OpportunityService opportunityService;

    public OpportunityApiController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    @Operation(summary = "Get all opportunities with optional stage/status filtering and pagination")
    public ResponseEntity<List<OpportunityDto>> getAllOpportunities(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "stage", required = false) OpportunityStage stage,
            @RequestParam(value = "status", required = false, defaultValue = "") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<Opportunity> pageResult = opportunityService.getOpportunities(query, stage, status, PageRequest.of(page, size));
        List<OpportunityDto> dtos = pageResult.getContent().stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get opportunity by ID")
    public ResponseEntity<OpportunityDto> getOpportunityById(@PathVariable("id") Long id) {
        Opportunity opp = opportunityService.getOpportunityById(id);
        return ResponseEntity.ok(toDto(opp));
    }

    @PostMapping
    @Operation(summary = "Create a new opportunity")
    public ResponseEntity<OpportunityDto> createOpportunity(@Valid @RequestBody OpportunityDto dto) {
        Opportunity created = opportunityService.createOpportunity(dto);
        return new ResponseEntity<>(toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing opportunity")
    public ResponseEntity<OpportunityDto> updateOpportunity(@PathVariable("id") Long id, @Valid @RequestBody OpportunityDto dto) {
        Opportunity updated = opportunityService.updateOpportunity(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete opportunity by ID")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable("id") Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.noContent().build();
    }

    private OpportunityDto toDto(Opportunity o) {
        OpportunityDto dto = new OpportunityDto();
        dto.setOpportunityId(o.getOpportunityId());
        dto.setOpportunityName(o.getOpportunityName());
        dto.setCustomerId(o.getCustomerId());
        dto.setLeadId(o.getLeadId());
        dto.setAmount(o.getAmount());
        dto.setStage(o.getStage());
        dto.setProbability(o.getProbability());
        dto.setExpectedCloseDate(o.getExpectedCloseDate());
        dto.setStatus(o.getStatus());
        dto.setAssignedTo(o.getAssignedTo());
        dto.setWeightedValue(o.getWeightedValue());
        return dto;
    }
}
