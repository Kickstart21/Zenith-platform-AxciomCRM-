package com.acxiom.crm.service;

import com.acxiom.crm.dto.CustomerDto;
import com.acxiom.crm.dto.LeadConversionDto;
import com.acxiom.crm.dto.LeadDto;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.model.*;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final OpportunityRepository opportunityRepository;
    private final AuditService auditService;
    private final UserService userService;

    public LeadService(LeadRepository leadRepository,
                       CustomerService customerService,
                       CustomerRepository customerRepository,
                       OpportunityRepository opportunityRepository,
                       AuditService auditService,
                       UserService userService) {
        this.leadRepository = leadRepository;
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.opportunityRepository = opportunityRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public Page<Lead> getLeads(String query, LeadStatus status, Pageable pageable) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return leadRepository.searchLeads(query, status, currentUser, pageable);
        }
        return leadRepository.searchLeads(query, status, null, pageable);
    }

    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + id));
    }

    @Transactional
    public Lead createLead(LeadDto dto) {
        if (leadRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("A lead with this email address already exists.");
        }
        if (leadRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("A lead with this phone number already exists.");
        }

        String leadCode = generateLeadCode();
        String currentUser = auditService.getCurrentUsername();
        String assignedTo = dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()
                ? dto.getAssignedTo().trim()
                : currentUser;

        Lead lead = new Lead(
                leadCode,
                dto.getLeadName().trim(),
                dto.getEmail().toLowerCase().trim(),
                dto.getPhone().trim(),
                dto.getCompanyName(),
                dto.getSource(),
                dto.getStatus() != null ? dto.getStatus() : LeadStatus.NEW,
                dto.getExpectedValue() != null ? dto.getExpectedValue() : BigDecimal.ZERO,
                assignedTo
        );

        Lead saved = leadRepository.save(lead);
        auditService.log("CREATE", "Lead", saved.getLeadId().toString(), null, saved);
        return saved;
    }

    @Transactional
    public Lead updateLead(Long id, LeadDto dto) {
        Lead existing = getLeadById(id);
        enforceAccess(existing);

        if (leadRepository.existsByEmailIgnoreCaseAndLeadIdNot(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Another lead already has this email address.");
        }
        if (leadRepository.existsByPhoneAndLeadIdNot(dto.getPhone(), id)) {
            throw new IllegalArgumentException("Another lead already has this phone number.");
        }

        // Validate Status Transition
        if (dto.getStatus() != null && dto.getStatus() != existing.getStatus()) {
            validateStatusTransition(existing.getStatus(), dto.getStatus());
            existing.setStatus(dto.getStatus());
        }

        Lead oldCopy = copyOf(existing);

        existing.setLeadName(dto.getLeadName().trim());
        existing.setEmail(dto.getEmail().toLowerCase().trim());
        existing.setPhone(dto.getPhone().trim());
        existing.setCompanyName(dto.getCompanyName());
        existing.setSource(dto.getSource());
        if (dto.getExpectedValue() != null) {
            existing.setExpectedValue(dto.getExpectedValue());
        }
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()) {
            existing.setAssignedTo(dto.getAssignedTo().trim());
        }

        Lead updated = leadRepository.save(existing);
        auditService.log("UPDATE", "Lead", updated.getLeadId().toString(), oldCopy, updated);
        return updated;
    }

    @Transactional
    public void deleteLead(Long id) {
        Lead lead = getLeadById(id);
        enforceAccess(lead);
        Lead oldCopy = copyOf(lead);
        leadRepository.delete(lead);
        auditService.log("DELETE", "Lead", id.toString(), oldCopy, "Deleted lead record");
    }

    @Transactional
    public void convertLead(LeadConversionDto conversionDto) {
        Lead lead = getLeadById(conversionDto.getLeadId());
        enforceAccess(lead);

        if (lead.getStatus() != LeadStatus.QUALIFIED) {
            throw new IllegalArgumentException("Only Qualified leads can be converted. Current status is: " + lead.getStatus().getLabel());
        }

        if (conversionDto.getExpectedCloseDate() != null && conversionDto.getExpectedCloseDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expected Close Date cannot be in the past.");
        }

        if (conversionDto.getAmount() == null || conversionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Opportunity Amount must be greater than 0.");
        }

        // 1. Create or Find Customer
        Customer customer;
        Optional<Customer> existingCust = customerRepository.findAll().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(lead.getEmail()))
                .findFirst();

        if (existingCust.isPresent()) {
            customer = existingCust.get();
        } else {
            CustomerDto custDto = new CustomerDto();
            custDto.setCustomerName(lead.getLeadName());
            custDto.setEmail(lead.getEmail());
            custDto.setPhone(lead.getPhone());
            custDto.setCompanyName(lead.getCompanyName());
            custDto.setStatus("Active");
            customer = customerService.createCustomer(custDto);
        }

        // 2. Create Opportunity
        Opportunity opportunity = new Opportunity(
                conversionDto.getOpportunityName(),
                customer.getCustomerId(),
                lead.getLeadId(),
                conversionDto.getAmount(),
                OpportunityStage.QUALIFICATION,
                20,
                conversionDto.getExpectedCloseDate(),
                "Open",
                lead.getAssignedTo()
        );
        Opportunity savedOpp = opportunityRepository.save(opportunity);
        auditService.log("CREATE", "Opportunity", savedOpp.getOpportunityId().toString(), null, savedOpp);

        // 3. Mark Lead as Converted
        Lead oldCopy = copyOf(lead);
        lead.setStatus(LeadStatus.CONVERTED);
        Lead updatedLead = leadRepository.save(lead);
        auditService.log("CONVERT", "Lead", updatedLead.getLeadId().toString(), oldCopy,
                "Converted into Customer ID: " + customer.getCustomerId() + " and Opportunity ID: " + savedOpp.getOpportunityId());
    }

    public void validateStatusTransition(LeadStatus current, LeadStatus target) {
        if (current == target) return;

        if (current == LeadStatus.CONVERTED) {
            throw new IllegalArgumentException("A Converted lead is closed and cannot change status.");
        }
        if (current == LeadStatus.LOST) {
            throw new IllegalArgumentException("Cannot transition a Lost lead back to " + target.getLabel() + ".");
        }

        // Transition matrix
        switch (current) {
            case NEW -> {
                if (!Set.of(LeadStatus.CONTACTED, LeadStatus.LOST).contains(target)) {
                    throw new IllegalArgumentException("Invalid transition: New lead can only transition to Contacted or Lost.");
                }
            }
            case CONTACTED -> {
                if (!Set.of(LeadStatus.QUALIFIED, LeadStatus.UNQUALIFIED, LeadStatus.LOST).contains(target)) {
                    throw new IllegalArgumentException("Invalid transition: Contacted lead can only transition to Qualified, Unqualified, or Lost.");
                }
            }
            case QUALIFIED -> {
                if (!Set.of(LeadStatus.CONVERTED, LeadStatus.LOST).contains(target)) {
                    throw new IllegalArgumentException("Invalid transition: Qualified lead can only transition to Converted or Lost.");
                }
            }
            case UNQUALIFIED -> {
                if (!Set.of(LeadStatus.CONTACTED, LeadStatus.LOST).contains(target)) {
                    throw new IllegalArgumentException("Invalid transition: Unqualified lead can only transition to Contacted or Lost.");
                }
            }
        }
    }

    public void enforceAccess(Lead lead) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            if (!currentUser.equalsIgnoreCase(lead.getAssignedTo())) {
                throw new IllegalArgumentException("Access Denied: You can only modify leads assigned to you.");
            }
        }
    }

    public synchronized String generateLeadCode() {
        long count = leadRepository.count();
        return String.format("LEAD-%04d", count + 1001);
    }

    private Lead copyOf(Lead l) {
        Lead copy = new Lead();
        copy.setLeadId(l.getLeadId());
        copy.setLeadCode(l.getLeadCode());
        copy.setLeadName(l.getLeadName());
        copy.setEmail(l.getEmail());
        copy.setPhone(l.getPhone());
        copy.setStatus(l.getStatus());
        copy.setExpectedValue(l.getExpectedValue());
        copy.setAssignedTo(l.getAssignedTo());
        return copy;
    }
}
