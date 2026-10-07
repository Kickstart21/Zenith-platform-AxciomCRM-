package com.acxiom.crm.service;

import com.acxiom.crm.dto.OpportunityDto;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.model.OpportunityStage;
import com.acxiom.crm.model.Role;
import com.acxiom.crm.model.User;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final UserService userService;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              CustomerRepository customerRepository,
                              AuditService auditService,
                              UserService userService) {
        this.opportunityRepository = opportunityRepository;
        this.customerRepository = customerRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public Page<Opportunity> getOpportunities(String query, OpportunityStage stage, String status, Pageable pageable) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return opportunityRepository.searchOpportunities(query, stage, status, currentUser, pageable);
        }
        return opportunityRepository.searchOpportunities(query, stage, status, null, pageable);
    }

    public List<Opportunity> getAllOpportunitiesForPipeline() {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return opportunityRepository.findByAssignedTo(currentUser);
        }
        return opportunityRepository.findAll();
    }

    public Map<OpportunityStage, List<Opportunity>> getPipelineByStages() {
        List<Opportunity> all = getAllOpportunitiesForPipeline();
        return all.stream().collect(Collectors.groupingBy(Opportunity::getStage));
    }

    public Opportunity getOpportunityById(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with ID: " + id));
    }

    @Transactional
    public Opportunity createOpportunity(OpportunityDto dto) {
        validateOpportunityBusinessRules(dto.getAmount(), dto.getProbability(), dto.getExpectedCloseDate(), dto.getStage());

        if (!customerRepository.existsById(dto.getCustomerId())) {
            throw new IllegalArgumentException("Selected customer does not exist.");
        }

        String currentUser = auditService.getCurrentUsername();
        String assigned = dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()
                ? dto.getAssignedTo().trim()
                : currentUser;

        Opportunity opportunity = new Opportunity(
                dto.getOpportunityName().trim(),
                dto.getCustomerId(),
                dto.getLeadId(),
                dto.getAmount(),
                dto.getStage(),
                dto.getProbability(),
                dto.getExpectedCloseDate(),
                dto.getStatus(),
                assigned
        );

        Opportunity saved = opportunityRepository.save(opportunity);
        auditService.log("CREATE", "Opportunity", saved.getOpportunityId().toString(), null, saved);
        return saved;
    }

    @Transactional
    public Opportunity updateOpportunity(Long id, OpportunityDto dto) {
        Opportunity existing = getOpportunityById(id);
        enforceAccess(existing);

        validateOpportunityBusinessRules(dto.getAmount(), dto.getProbability(), dto.getExpectedCloseDate(), dto.getStage());

        Opportunity oldCopy = copyOf(existing);

        existing.setOpportunityName(dto.getOpportunityName().trim());
        existing.setCustomerId(dto.getCustomerId());
        existing.setLeadId(dto.getLeadId());
        existing.setAmount(dto.getAmount());
        existing.setStage(dto.getStage());
        existing.setProbability(dto.getProbability());
        existing.setExpectedCloseDate(dto.getExpectedCloseDate());
        if (dto.getAssignedTo() != null && !dto.getAssignedTo().trim().isEmpty()) {
            existing.setAssignedTo(dto.getAssignedTo().trim());
        }

        Opportunity updated = opportunityRepository.save(existing);
        auditService.log("UPDATE", "Opportunity", updated.getOpportunityId().toString(), oldCopy, updated);
        return updated;
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opp = getOpportunityById(id);
        enforceAccess(opp);
        Opportunity oldCopy = copyOf(opp);
        opportunityRepository.delete(opp);
        auditService.log("DELETE", "Opportunity", id.toString(), oldCopy, "Deleted opportunity");
    }

    public void validateOpportunityBusinessRules(BigDecimal amount, Integer probability, LocalDate expectedCloseDate, OpportunityStage stage) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Opportunity Amount must be greater than 0.");
        }
        if (probability == null || probability < 0 || probability > 100) {
            throw new IllegalArgumentException("Probability must be between 0 and 100.");
        }
        // Active opportunity close date cannot be in the past
        if (stage != OpportunityStage.WON && stage != OpportunityStage.LOST) {
            if (expectedCloseDate != null && expectedCloseDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Expected Close Date cannot be in the past.");
            }
        }
    }

    public void enforceAccess(Opportunity opp) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            if (!currentUser.equalsIgnoreCase(opp.getAssignedTo())) {
                throw new IllegalArgumentException("Access Denied: You can only modify opportunities assigned to you.");
            }
        }
    }

    private Opportunity copyOf(Opportunity o) {
        Opportunity c = new Opportunity();
        c.setOpportunityId(o.getOpportunityId());
        c.setOpportunityName(o.getOpportunityName());
        c.setCustomerId(o.getCustomerId());
        c.setAmount(o.getAmount());
        c.setStage(o.getStage());
        c.setProbability(o.getProbability());
        c.setStatus(o.getStatus());
        c.setAssignedTo(o.getAssignedTo());
        return c;
    }
}
