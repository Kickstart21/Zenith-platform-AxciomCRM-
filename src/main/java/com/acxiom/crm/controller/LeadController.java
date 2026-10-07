package com.acxiom.crm.controller;

import com.acxiom.crm.dto.LeadConversionDto;
import com.acxiom.crm.dto.LeadDto;
import com.acxiom.crm.model.Lead;
import com.acxiom.crm.model.LeadStatus;
import com.acxiom.crm.service.LeadService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public String listLeads(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                            @RequestParam(value = "status", required = false) LeadStatus status,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Page<Lead> leadPage = leadService.getLeads(
                query, status, PageRequest.of(page, size, Sort.by("createdDate").descending())
        );

        model.addAttribute("leads", leadPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", leadPage.getTotalPages());
        model.addAttribute("totalElements", leadPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", LeadStatus.values());
        return "leads/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("leadDto", new LeadDto());
        model.addAttribute("statuses", LeadStatus.values());
        return "leads/form";
    }

    @PostMapping("/create")
    public String createLead(@Valid @ModelAttribute("leadDto") LeadDto leadDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", LeadStatus.values());
            return "leads/form";
        }
        try {
            Lead created = leadService.createLead(leadDto);
            redirectAttributes.addFlashAttribute("successMessage", "Lead " + created.getLeadCode() + " created successfully!");
            return "redirect:/leads";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("statuses", LeadStatus.values());
            return "leads/form";
        }
    }

    @GetMapping("/{id}")
    public String viewDetails(@PathVariable("id") Long id, Model model) {
        Lead lead = leadService.getLeadById(id);
        model.addAttribute("lead", lead);
        return "leads/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Lead lead = leadService.getLeadById(id);
        LeadDto dto = new LeadDto();
        dto.setLeadId(lead.getLeadId());
        dto.setLeadCode(lead.getLeadCode());
        dto.setLeadName(lead.getLeadName());
        dto.setEmail(lead.getEmail());
        dto.setPhone(lead.getPhone());
        dto.setCompanyName(lead.getCompanyName());
        dto.setSource(lead.getSource());
        dto.setStatus(lead.getStatus());
        dto.setExpectedValue(lead.getExpectedValue());
        dto.setAssignedTo(lead.getAssignedTo());

        model.addAttribute("leadDto", dto);
        model.addAttribute("statuses", LeadStatus.values());
        return "leads/form";
    }

    @PostMapping("/{id}/edit")
    public String updateLead(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("leadDto") LeadDto leadDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", LeadStatus.values());
            return "leads/form";
        }
        try {
            leadService.updateLead(id, leadDto);
            redirectAttributes.addFlashAttribute("successMessage", "Lead updated successfully!");
            return "redirect:/leads";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("statuses", LeadStatus.values());
            return "leads/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteLead(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            leadService.deleteLead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lead deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/leads";
    }

    @GetMapping("/{id}/convert")
    public String convertForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Lead lead = leadService.getLeadById(id);
        if (lead.getStatus() != LeadStatus.QUALIFIED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only Qualified leads can be converted.");
            return "redirect:/leads/" + id;
        }

        LeadConversionDto dto = new LeadConversionDto();
        dto.setLeadId(lead.getLeadId());
        dto.setOpportunityName(lead.getLeadName() + " Deal");
        dto.setAmount(lead.getExpectedValue());
        dto.setExpectedCloseDate(LocalDate.now().plusMonths(1));

        model.addAttribute("lead", lead);
        model.addAttribute("conversionDto", dto);
        return "leads/convert";
    }

    @PostMapping("/{id}/convert")
    public String processConversion(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("conversionDto") LeadConversionDto conversionDto,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Lead lead = leadService.getLeadById(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("lead", lead);
            return "leads/convert";
        }
        try {
            conversionDto.setLeadId(id);
            leadService.convertLead(conversionDto);
            redirectAttributes.addFlashAttribute("successMessage", "Lead successfully converted into Customer and Opportunity!");
            return "redirect:/opportunities";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("lead", lead);
            return "leads/convert";
        }
    }
}
