package com.acxiom.crm.controller;

import com.acxiom.crm.dto.OpportunityDto;
import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.model.OpportunityStage;
import com.acxiom.crm.service.CustomerService;
import com.acxiom.crm.service.OpportunityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final CustomerService customerService;

    public OpportunityController(OpportunityService opportunityService,
                                 CustomerService customerService) {
        this.opportunityService = opportunityService;
        this.customerService = customerService;
    }

    @GetMapping
    public String listOpportunities(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                                    @RequestParam(value = "stage", required = false) OpportunityStage stage,
                                    @RequestParam(value = "status", required = false, defaultValue = "") String status,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size,
                                    Model model) {
        Page<Opportunity> oppPage = opportunityService.getOpportunities(
                query, stage, status, PageRequest.of(page, size, Sort.by("createdDate").descending())
        );

        model.addAttribute("opportunities", oppPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", oppPage.getTotalPages());
        model.addAttribute("totalElements", oppPage.getTotalElements());
        model.addAttribute("query", query);
        model.addAttribute("selectedStage", stage);
        model.addAttribute("status", status);
        model.addAttribute("stages", OpportunityStage.values());
        return "opportunities/list";
    }

    @GetMapping("/pipeline")
    public String pipelineView(Model model) {
        Map<OpportunityStage, List<Opportunity>> pipelineMap = opportunityService.getPipelineByStages();
        model.addAttribute("pipelineMap", pipelineMap);
        model.addAttribute("stages", OpportunityStage.values());
        return "opportunities/pipeline";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("opportunityDto", new OpportunityDto());
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("stages", OpportunityStage.values());
        return "opportunities/form";
    }

    @PostMapping("/create")
    public String createOpportunity(@Valid @ModelAttribute("opportunityDto") OpportunityDto opportunityDto,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("stages", OpportunityStage.values());
            return "opportunities/form";
        }
        try {
            Opportunity created = opportunityService.createOpportunity(opportunityDto);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity '" + created.getOpportunityName() + "' created successfully!");
            return "redirect:/opportunities";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("stages", OpportunityStage.values());
            return "opportunities/form";
        }
    }

    @GetMapping("/{id}")
    public String viewDetails(@PathVariable("id") Long id, Model model) {
        Opportunity opp = opportunityService.getOpportunityById(id);
        model.addAttribute("opportunity", opp);
        try {
            model.addAttribute("customer", customerService.getCustomerById(opp.getCustomerId()));
        } catch (Exception ignored) {
        }
        return "opportunities/details";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Opportunity opp = opportunityService.getOpportunityById(id);
        OpportunityDto dto = new OpportunityDto();
        dto.setOpportunityId(opp.getOpportunityId());
        dto.setOpportunityName(opp.getOpportunityName());
        dto.setCustomerId(opp.getCustomerId());
        dto.setLeadId(opp.getLeadId());
        dto.setAmount(opp.getAmount());
        dto.setStage(opp.getStage());
        dto.setProbability(opp.getProbability());
        dto.setExpectedCloseDate(opp.getExpectedCloseDate());
        dto.setStatus(opp.getStatus());
        dto.setAssignedTo(opp.getAssignedTo());

        model.addAttribute("opportunityDto", dto);
        model.addAttribute("customers", customerService.getAllActiveCustomers());
        model.addAttribute("stages", OpportunityStage.values());
        return "opportunities/form";
    }

    @PostMapping("/{id}/edit")
    public String updateOpportunity(@PathVariable("id") Long id,
                                    @Valid @ModelAttribute("opportunityDto") OpportunityDto opportunityDto,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("stages", OpportunityStage.values());
            return "opportunities/form";
        }
        try {
            opportunityService.updateOpportunity(id, opportunityDto);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity updated successfully!");
            return "redirect:/opportunities";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customers", customerService.getAllActiveCustomers());
            model.addAttribute("stages", OpportunityStage.values());
            return "opportunities/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteOpportunity(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            opportunityService.deleteOpportunity(id);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/opportunities";
    }
}
