package com.acxiom.crm.controller;

import com.acxiom.crm.dto.CustomerDto;
import com.acxiom.crm.model.Customer;
import com.acxiom.crm.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                                @RequestParam(value = "status", required = false, defaultValue = "") String status,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        Page<Customer> customerPage = customerService.getCustomers(
                query, status, PageRequest.of(page, size, Sort.by("createdDate").descending())
        );

        model.addAttribute("customers", customerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("totalElements", customerPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("status", status);
        return "customers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "customers/form";
    }

    @PostMapping("/create")
    public String createCustomer(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customers/form";
        }
        try {
            Customer created = customerService.createCustomer(customerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Customer " + created.getCustomerCode() + " created successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "customers/form";
        }
    }

    @GetMapping("/{id}")
    public String viewDetails(@PathVariable("id") Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        model.addAttribute("customer", customer);
        return "customers/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Customer customer = customerService.getCustomerById(id);
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setCustomerCode(customer.getCustomerCode());
        dto.setCustomerName(customer.getCustomerName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCompanyName(customer.getCompanyName());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setStatus(customer.getStatus());

        model.addAttribute("customerDto", dto);
        return "customers/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customers/form";
        }
        try {
            customerService.updateCustomer(id, customerDto);
            redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
            return "redirect:/customers";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "customers/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deactivated successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/customers";
    }
}
