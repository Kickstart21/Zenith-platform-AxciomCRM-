package com.acxiom.crm.service;

import com.acxiom.crm.dto.CustomerDto;
import com.acxiom.crm.exception.ResourceNotFoundException;
import com.acxiom.crm.model.Customer;
import com.acxiom.crm.model.Role;
import com.acxiom.crm.model.User;
import com.acxiom.crm.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;
    private final UserService userService;

    public CustomerService(CustomerRepository customerRepository,
                           AuditService auditService,
                           UserService userService) {
        this.customerRepository = customerRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    public Page<Customer> getCustomers(String query, String status, Pageable pageable) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);

        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            return customerRepository.searchCustomersByCreatedBy(currentUser, query, status, pageable);
        }
        return customerRepository.searchCustomers(query, status, pageable);
    }

    public List<Customer> getAllActiveCustomers() {
        return customerRepository.findAll().stream()
                .filter(c -> "Active".equalsIgnoreCase(c.getStatus()))
                .toList();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Transactional
    public Customer createCustomer(CustomerDto dto) {
        // Enforce Unique constraints & business validation
        if (customerRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("A customer with this email address already exists.");
        }
        if (customerRepository.existsByPhone(dto.getPhone())) {
            throw new IllegalArgumentException("A customer with this phone number already exists.");
        }

        String code = generateCustomerCode();
        String currentUser = auditService.getCurrentUsername();

        Customer customer = new Customer(
                code,
                dto.getCustomerName().trim(),
                dto.getEmail().toLowerCase().trim(),
                dto.getPhone().trim(),
                dto.getCompanyName(),
                dto.getAddress(),
                dto.getCity(),
                dto.getState(),
                dto.getStatus() != null ? dto.getStatus() : "Active",
                currentUser
        );

        Customer saved = customerRepository.save(customer);
        auditService.log("CREATE", "Customer", saved.getCustomerId().toString(), null, saved);
        return saved;
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerDto dto) {
        Customer existing = getCustomerById(id);
        enforceAccess(existing);

        if (customerRepository.existsByEmailIgnoreCaseAndCustomerIdNot(dto.getEmail(), id)) {
            throw new IllegalArgumentException("Another customer already has this email address.");
        }
        if (customerRepository.existsByPhoneAndCustomerIdNot(dto.getPhone(), id)) {
            throw new IllegalArgumentException("Another customer already has this phone number.");
        }

        Customer oldCopy = copyOf(existing);

        existing.setCustomerName(dto.getCustomerName().trim());
        existing.setEmail(dto.getEmail().toLowerCase().trim());
        existing.setPhone(dto.getPhone().trim());
        existing.setCompanyName(dto.getCompanyName());
        existing.setAddress(dto.getAddress());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        Customer updated = customerRepository.save(existing);
        auditService.log("UPDATE", "Customer", updated.getCustomerId().toString(), oldCopy, updated);
        return updated;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        enforceAccess(customer);

        Customer oldCopy = copyOf(customer);
        customer.setStatus("Inactive");
        customerRepository.save(customer);
        auditService.log("DELETE", "Customer", customer.getCustomerId().toString(), oldCopy, "Status changed to Inactive");
    }

    public void enforceAccess(Customer customer) {
        String currentUser = auditService.getCurrentUsername();
        Optional<User> userOpt = userService.findByEmail(currentUser);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.SALES_EXECUTIVE) {
            if (!currentUser.equalsIgnoreCase(customer.getCreatedBy())) {
                throw new IllegalArgumentException("Access Denied: You can only modify customers created by you.");
            }
        }
    }

    public synchronized String generateCustomerCode() {
        long count = customerRepository.count();
        return String.format("CUST-%04d", count + 1001);
    }

    private Customer copyOf(Customer c) {
        Customer copy = new Customer();
        copy.setCustomerId(c.getCustomerId());
        copy.setCustomerCode(c.getCustomerCode());
        copy.setCustomerName(c.getCustomerName());
        copy.setEmail(c.getEmail());
        copy.setPhone(c.getPhone());
        copy.setCompanyName(c.getCompanyName());
        copy.setStatus(c.getStatus());
        copy.setCreatedBy(c.getCreatedBy());
        return copy;
    }
}
