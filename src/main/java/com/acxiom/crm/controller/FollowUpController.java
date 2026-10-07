package com.acxiom.crm.controller;

import com.acxiom.crm.dto.FollowUpDto;
import com.acxiom.crm.model.FollowUp;
import com.acxiom.crm.model.FollowUpStatus;
import com.acxiom.crm.model.FollowUpType;
import com.acxiom.crm.service.CustomerService;
import com.acxiom.crm.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/followups")
public class FollowUpController {

    private final FollowUpService followUpService;
    private final CustomerService customerService;

    public FollowUpController(FollowUpService followUpService, CustomerService customerService) {
        this.followUpService = followUpService;
        this.customerService = customerService;
    }

    @GetMapping
    public String listFollowUps(@RequestParam(value = "status", required = false) FollowUpStatus status,
                                @RequestParam(value = "customerId", required = false) Long customerId,
                                @RequestParam(value = "leadId", required = false) Long leadId,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                @RequestParam(value = "size", defaultValue = "10") int size,
                                Model model) {
        Page<FollowUp> pageData = followUpService.getFollowUps(
                status, customerId, leadId, PageRequest.of(page, size, Sort.by("followUpDate").ascending())
        );

        model.addAttribute("followUps", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", FollowUpStatus.values());
        model.addAttribute("types", FollowUpType.values());
        model.addAttribute("now", LocalDateTime.now());
        return "followups/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(value = "customerId", required = false) Long customerId,
                             @RequestParam(value = "leadId", required = false) Long leadId,
                             Model model) {
        FollowUpDto dto = new FollowUpDto();
        dto.setCustomerId(customerId);
        dto.setLeadId(leadId);
        dto.setFollowUpDate(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));

        model.addAttribute("followUpDto", dto);
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("types", FollowUpType.values());
        return "followups/form";
    }

    @PostMapping("/create")
    public String scheduleFollowUp(@Valid @ModelAttribute("followUpDto") FollowUpDto followUpDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("types", FollowUpType.values());
            return "followups/form";
        }
        try {
            followUpService.scheduleFollowUp(followUpDto);
            redirectAttributes.addFlashAttribute("successMessage", "Follow-up scheduled successfully!");
            return "redirect:/followups";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("types", FollowUpType.values());
            return "followups/form";
        }
    }

    @PostMapping("/{id}/complete")
    public String completeFollowUp(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            followUpService.completeFollowUp(id);
            redirectAttributes.addFlashAttribute("successMessage", "Follow-up marked as Completed.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/followups";
    }

    @PostMapping("/{id}/delete")
    public String deleteFollowUp(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            followUpService.deleteFollowUp(id);
            redirectAttributes.addFlashAttribute("successMessage", "Follow-up removed.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/followups";
    }
}
