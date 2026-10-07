package com.acxiom.crm.repository;

import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.model.OpportunityStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    @Query("SELECT o FROM Opportunity o WHERE " +
           "(:query IS NULL OR :query = '' OR LOWER(o.opportunityName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:stage IS NULL OR o.stage = :stage) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(o.status) = LOWER(:status)) AND " +
           "(:assignedTo IS NULL OR :assignedTo = '' OR LOWER(o.assignedTo) = LOWER(:assignedTo))")
    Page<Opportunity> searchOpportunities(@Param("query") String query,
                                          @Param("stage") OpportunityStage stage,
                                          @Param("status") String status,
                                          @Param("assignedTo") String assignedTo,
                                          Pageable pageable);

    long countByStatus(String status);

    long countByAssignedToAndStatus(String assignedTo, String status);

    long countByStage(OpportunityStage stage);

    long countByAssignedToAndStage(String assignedTo, OpportunityStage stage);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Opportunity o WHERE o.status = 'Open'")
    BigDecimal calculateTotalPipelineValue();

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Opportunity o WHERE o.status = 'Open' AND LOWER(o.assignedTo) = LOWER(:assignedTo)")
    BigDecimal calculateTotalPipelineValueByAssignedTo(@Param("assignedTo") String assignedTo);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Opportunity o WHERE o.stage = com.acxiom.crm.model.OpportunityStage.WON")
    BigDecimal calculateTotalWonValue();

    List<Opportunity> findByAssignedTo(String assignedTo);
}
