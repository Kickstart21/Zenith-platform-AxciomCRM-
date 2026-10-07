package com.acxiom.crm.repository;

import com.acxiom.crm.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR :userId = '' OR LOWER(a.userId) LIKE LOWER(CONCAT('%', :userId, '%'))) AND " +
           "(:entityName IS NULL OR :entityName = '' OR LOWER(a.entityName) LIKE LOWER(CONCAT('%', :entityName, '%'))) AND " +
           "(:action IS NULL OR :action = '' OR LOWER(a.action) = LOWER(:action)) AND " +
           "(:startDate IS NULL OR a.createdDate >= :startDate) AND " +
           "(:endDate IS NULL OR a.createdDate <= :endDate) " +
           "ORDER BY a.createdDate DESC")
    Page<AuditLog> searchAuditLogs(@Param("userId") String userId,
                                   @Param("entityName") String entityName,
                                   @Param("action") String action,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
}
