package com.acxiom.crm.repository;

import com.acxiom.crm.model.Lead;
import com.acxiom.crm.model.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByLeadCode(String leadCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndLeadIdNot(String email, Long leadId);

    boolean existsByPhoneAndLeadIdNot(String phone, Long leadId);

    @Query("SELECT l FROM Lead l WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(l.leadName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:assignedTo IS NULL OR :assignedTo = '' OR LOWER(l.assignedTo) = LOWER(:assignedTo))")
    Page<Lead> searchLeads(@Param("query") String query,
                           @Param("status") LeadStatus status,
                           @Param("assignedTo") String assignedTo,
                           Pageable pageable);

    long countByStatus(LeadStatus status);

    long countByAssignedToAndStatus(String assignedTo, LeadStatus status);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status NOT IN (com.acxiom.crm.model.LeadStatus.CONVERTED, com.acxiom.crm.model.LeadStatus.LOST)")
    long countOpenLeads();

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.assignedTo = :assignedTo AND l.status NOT IN (com.acxiom.crm.model.LeadStatus.CONVERTED, com.acxiom.crm.model.LeadStatus.LOST)")
    long countOpenLeadsByAssignedTo(@Param("assignedTo") String assignedTo);

    List<Lead> findByAssignedTo(String assignedTo);
}
