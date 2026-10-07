package com.acxiom.crm.controller;

import com.acxiom.crm.model.Customer;
import com.acxiom.crm.model.Lead;
import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.repository.CustomerRepository;
import com.acxiom.crm.repository.LeadRepository;
import com.acxiom.crm.repository.OpportunityRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ReportController {

    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;

    public ReportController(CustomerRepository customerRepository,
                            LeadRepository leadRepository,
                            OpportunityRepository opportunityRepository) {
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @GetMapping
    public String reportsHome(Model model) {
        model.addAttribute("totalCustomers", customerRepository.count());
        model.addAttribute("totalLeads", leadRepository.count());
        model.addAttribute("totalOpportunities", opportunityRepository.count());
        return "reports/index";
    }

    @GetMapping("/export/customers")
    public void exportCustomersCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"customers_report.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("CustomerCode,CustomerName,Email,Phone,CompanyName,City,State,Status,CreatedBy,CreatedDate");

        List<Customer> customers = customerRepository.findAll();
        for (Customer c : customers) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    c.getCustomerCode(), c.getCustomerName(), c.getEmail(), c.getPhone(),
                    c.getCompanyName() != null ? c.getCompanyName() : "",
                    c.getCity() != null ? c.getCity() : "",
                    c.getState() != null ? c.getState() : "",
                    c.getStatus(), c.getCreatedBy(), c.getCreatedDate());
        }
        writer.flush();
    }

    @GetMapping("/export/leads")
    public void exportLeadsCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"leads_report.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("LeadCode,LeadName,Email,Phone,CompanyName,Source,Status,ExpectedValue,AssignedTo,CreatedDate");

        List<Lead> leads = leadRepository.findAll();
        for (Lead l : leads) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    l.getLeadCode(), l.getLeadName(), l.getEmail(), l.getPhone(),
                    l.getCompanyName() != null ? l.getCompanyName() : "",
                    l.getSource() != null ? l.getSource() : "",
                    l.getStatus().name(), l.getExpectedValue(),
                    l.getAssignedTo() != null ? l.getAssignedTo() : "",
                    l.getCreatedDate());
        }
        writer.flush();
    }

    @GetMapping("/export/opportunities")
    public void exportOpportunitiesCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"opportunities_pipeline.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("OpportunityId,OpportunityName,CustomerId,Amount,Stage,Probability,WeightedValue,ExpectedCloseDate,Status,AssignedTo");

        List<Opportunity> opps = opportunityRepository.findAll();
        for (Opportunity o : opps) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    o.getOpportunityId(), o.getOpportunityName(), o.getCustomerId(),
                    o.getAmount(), o.getStage().name(), o.getProbability(),
                    o.getWeightedValue(), o.getExpectedCloseDate(), o.getStatus(),
                    o.getAssignedTo() != null ? o.getAssignedTo() : "");
        }
        writer.flush();
    }
}
