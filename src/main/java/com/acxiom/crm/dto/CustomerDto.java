package com.acxiom.crm.dto;

import com.acxiom.crm.validator.ValidIndianPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerDto {

    private Long customerId;

    private String customerCode;

    @NotBlank(message = "Customer Name is required.")
    @Size(max = 120, message = "Customer Name cannot exceed 120 characters.")
    private String customerName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @ValidIndianPhone(message = "Enter a valid phone number.")
    private String phone;

    @Size(max = 150, message = "Company Name cannot exceed 150 characters.")
    private String companyName;

    @Size(max = 255, message = "Address cannot exceed 255 characters.")
    private String address;

    @Size(max = 80, message = "City cannot exceed 80 characters.")
    private String city;

    @Size(max = 80, message = "State cannot exceed 80 characters.")
    private String state;

    private String status = "Active";

    private String createdBy;

    public CustomerDto() {
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
