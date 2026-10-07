package com.acxiom.crm.controller.api;

import com.acxiom.crm.dto.CustomerDto;
import com.acxiom.crm.model.Customer;
import com.acxiom.crm.service.CustomerService;
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
@RequestMapping("/api/customers")
@Tag(name = "Customer API", description = "Endpoints for Customer Management")
public class CustomerApiController {

    private final CustomerService customerService;

    public CustomerApiController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @Operation(summary = "Get all customers with optional search and pagination")
    public ResponseEntity<List<CustomerDto>> getAllCustomers(
            @RequestParam(value = "query", required = false, defaultValue = "") String query,
            @RequestParam(value = "status", required = false, defaultValue = "") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Page<Customer> pageResult = customerService.getCustomers(query, status, PageRequest.of(page, size));
        List<CustomerDto> dtos = pageResult.getContent().stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable("id") Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(toDto(customer));
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto dto) {
        Customer created = customerService.createCustomer(dto);
        return new ResponseEntity<>(toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable("id") Long id, @Valid @RequestBody CustomerDto dto) {
        Customer updated = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate/Delete customer by ID")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    private CustomerDto toDto(Customer c) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(c.getCustomerId());
        dto.setCustomerCode(c.getCustomerCode());
        dto.setCustomerName(c.getCustomerName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setCompanyName(c.getCompanyName());
        dto.setAddress(c.getAddress());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setStatus(c.getStatus());
        dto.setCreatedBy(c.getCreatedBy());
        return dto;
    }
}
