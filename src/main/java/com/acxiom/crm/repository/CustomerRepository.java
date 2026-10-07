package com.acxiom.crm.repository;

import com.acxiom.crm.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerCode(String customerCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmailIgnoreCaseAndCustomerIdNot(String email, Long customerId);

    boolean existsByPhoneAndCustomerIdNot(String phone, Long customerId);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "c.phone LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Customer> searchCustomers(@Param("query") String query,
                                  @Param("status") String status,
                                  Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.createdBy = :createdBy AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "c.phone LIKE CONCAT('%', :query, '%') OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Customer> searchCustomersByCreatedBy(@Param("createdBy") String createdBy,
                                              @Param("query") String query,
                                              @Param("status") String status,
                                              Pageable pageable);

    long countByStatus(String status);

    long countByCreatedByAndStatus(String createdBy, String status);
}
