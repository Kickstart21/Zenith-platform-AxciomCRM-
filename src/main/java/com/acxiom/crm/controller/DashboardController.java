package com.acxiom.crm.controller;

import com.acxiom.crm.dto.DashboardMetricsDto;
import com.acxiom.crm.service.CustomerService;
import com.acxiom.crm.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final CustomerService customerService;

    public DashboardController(DashboardService dashboardService, CustomerService customerService) {
        this.dashboardService = dashboardService;
        this.customerService = customerService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardMetricsDto metrics = dashboardService.getMetrics();
        model.addAttribute("metrics", metrics);
        model.addAttribute("recentCustomers", 
                customerService.getCustomers("", "", PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdDate"))).getContent());
        return "dashboard/index";
    }
}
