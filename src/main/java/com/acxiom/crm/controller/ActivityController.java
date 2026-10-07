package com.acxiom.crm.controller;

import com.acxiom.crm.dto.ActivityDto;
import com.acxiom.crm.model.Activity;
import com.acxiom.crm.model.ActivityType;
import com.acxiom.crm.service.ActivityService;
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

import java.time.LocalDateTime;

@Controller
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final CustomerService customerService;

    public ActivityController(ActivityService activityService, CustomerService customerService) {
        this.activityService = activityService;
        this.customerService = customerService;
    }

    @GetMapping
    public String listActivities(@RequestParam(value = "type", required = false) ActivityType type,
                                 @RequestParam(value = "status", required = false, defaultValue = "") String status,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        Page<Activity> pageData = activityService.getActivities(
                type, status, PageRequest.of(page, size, Sort.by("activityDate").descending())
        );

        model.addAttribute("activities", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("selectedType", type);
        model.addAttribute("status", status);
        model.addAttribute("types", ActivityType.values());
        return "activities/list";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam(value = "customerId", required = false) Long customerId,
                             @RequestParam(value = "leadId", required = false) Long leadId,
                             Model model) {
        ActivityDto dto = new ActivityDto();
        dto.setCustomerId(customerId);
        dto.setLeadId(leadId);
        dto.setActivityDate(LocalDateTime.now());

        model.addAttribute("activityDto", dto);
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("types", ActivityType.values());
        return "activities/form";
    }

    @PostMapping("/create")
    public String createActivity(@Valid @ModelAttribute("activityDto") ActivityDto activityDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("types", ActivityType.values());
            return "activities/form";
        }
        try {
            activityService.createActivity(activityDto);
            redirectAttributes.addFlashAttribute("successMessage", "Activity logged successfully!");
            return "redirect:/activities";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("types", ActivityType.values());
            return "activities/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteActivity(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            activityService.deleteActivity(id);
            redirectAttributes.addFlashAttribute("successMessage", "Activity deleted.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/activities";
    }
}
