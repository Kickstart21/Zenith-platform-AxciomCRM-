package com.acxiom.crm.controller.api;

import com.acxiom.crm.dto.LeadDto;
import com.acxiom.crm.model.Lead;
import com.acxiom.crm.model.LeadStatus;
import com.acxiom.crm.service.LeadService;
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
@RequestMapping("/api/leads")
@Tag(name = "Lead API", description = "Endpoints for Lead Management")
public class LeadApiController {

    private final LeadService leadService;

    public LeadApiController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @Operation(summary = "Get all leads with optional filtering and pagination")
    public ResponseEntity<List<LeadDto>> getAllLeads(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "status", required = false) LeadStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<Lead> pageResult = leadService.getLeads(query, status, PageRequest.of(page, size));
        List<LeadDto> dtos = pageResult.getContent().stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lead by ID")
    public ResponseEntity<LeadDto> getLeadById(@PathVariable("id") Long id) {
        Lead lead = leadService.getLeadById(id);
        return ResponseEntity.ok(toDto(lead));
    }

    @PostMapping
    @Operation(summary = "Create a new lead")
    public ResponseEntity<LeadDto> createLead(@Valid @RequestBody LeadDto dto) {
        Lead created = leadService.createLead(dto);
        return new ResponseEntity<>(toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing lead")
    public ResponseEntity<LeadDto> updateLead(@PathVariable("id") Long id, @Valid @RequestBody LeadDto dto) {
        Lead updated = leadService.updateLead(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lead by ID")
    public ResponseEntity<Void> deleteLead(@PathVariable("id") Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    private LeadDto toDto(Lead l) {
        LeadDto dto = new LeadDto();
        dto.setLeadId(l.getLeadId());
        dto.setLeadCode(l.getLeadCode());
        dto.setLeadName(l.getLeadName());
        dto.setEmail(l.getEmail());
        dto.setPhone(l.getPhone());
        dto.setCompanyName(l.getCompanyName());
        dto.setSource(l.getSource());
        dto.setStatus(l.getStatus());
        dto.setExpectedValue(l.getExpectedValue());
        dto.setAssignedTo(l.getAssignedTo());
        return dto;
    }
}
