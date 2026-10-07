package com.acxiom.crm.repository;

import com.acxiom.crm.model.FollowUp;
import com.acxiom.crm.model.FollowUpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {

    @Query("SELECT f FROM FollowUp f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:assignedTo IS NULL OR :assignedTo = '' OR LOWER(f.assignedTo) = LOWER(:assignedTo)) AND " +
           "(:customerId IS NULL OR f.customerId = :customerId) AND " +
           "(:leadId IS NULL OR f.leadId = :leadId)")
    Page<FollowUp> searchFollowUps(@Param("status") FollowUpStatus status,
                                   @Param("assignedTo") String assignedTo,
                                   @Param("customerId") Long customerId,
                                   @Param("leadId") Long leadId,
                                   Pageable pageable);

    @Query("SELECT COUNT(f) FROM FollowUp f WHERE f.status = com.acxiom.crm.model.FollowUpStatus.PLANNED AND f.followUpDate < :now")
    long countOverdueFollowUps(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(f) FROM FollowUp f WHERE f.status = com.acxiom.crm.model.FollowUpStatus.PLANNED AND f.followUpDate < :now AND LOWER(f.assignedTo) = LOWER(:assignedTo)")
    long countOverdueFollowUpsByAssignedTo(@Param("now") LocalDateTime now, @Param("assignedTo") String assignedTo);

    List<FollowUp> findByCustomerId(Long customerId);

    List<FollowUp> findByLeadId(Long leadId);
}
