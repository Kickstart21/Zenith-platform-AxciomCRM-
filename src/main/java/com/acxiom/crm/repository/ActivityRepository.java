package com.acxiom.crm.repository;

import com.acxiom.crm.model.Activity;
import com.acxiom.crm.model.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT a FROM Activity a WHERE " +
           "(:type IS NULL OR a.activityType = :type) AND " +
           "(:status IS NULL OR :status = '' OR LOWER(a.status) = LOWER(:status)) AND " +
           "(:assignedTo IS NULL OR :assignedTo = '' OR LOWER(a.assignedTo) = LOWER(:assignedTo))")
    Page<Activity> searchActivities(@Param("type") ActivityType type,
                                    @Param("status") String status,
                                    @Param("assignedTo") String assignedTo,
                                    Pageable pageable);

    List<Activity> findByCustomerId(Long customerId);

    List<Activity> findByLeadId(Long leadId);
}
