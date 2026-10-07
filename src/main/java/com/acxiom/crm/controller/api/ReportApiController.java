package com.acxiom.crm.controller.api;

import com.acxiom.crm.model.Opportunity;
import com.acxiom.crm.model.OpportunityStage;
import com.acxiom.crm.service.OpportunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Report API", description = "Endpoints for CRM Reports and Analytics")
public class ReportApiController {

    private final OpportunityService opportunityService;

    public ReportApiController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping("/pipeline")
    @Operation(summary = "Get sales pipeline report data with stage breakdown and metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getPipelineReport() {
        List<Opportunity> opportunities = opportunityService.getAllOpportunitiesForPipeline();

        BigDecimal totalPipelineValue = BigDecimal.ZERO;
        BigDecimal totalWeightedValue = BigDecimal.ZERO;

        Map<String, Map<String, Object>> stageBreakdown = new LinkedHashMap<>();
        for (OpportunityStage stage : OpportunityStage.values()) {
            Map<String, Object> stageData = new HashMap<>();
            stageData.put("stage", stage.name());
            stageData.put("label", stage.getLabel());
            stageData.put("count", 0);
            stageData.put("totalAmount", BigDecimal.ZERO);
            stageBreakdown.put(stage.name(), stageData);
        }

        for (Opportunity opp : opportunities) {
            if ("Open".equalsIgnoreCase(opp.getStatus())) {
                totalPipelineValue = totalPipelineValue.add(opp.getAmount() != null ? opp.getAmount() : BigDecimal.ZERO);
                totalWeightedValue = totalWeightedValue.add(opp.getWeightedValue() != null ? opp.getWeightedValue() : BigDecimal.ZERO);
            }

            Map<String, Object> sData = stageBreakdown.get(opp.getStage().name());
            if (sData != null) {
                int count = (int) sData.get("count") + 1;
                BigDecimal sum = ((BigDecimal) sData.get("totalAmount")).add(opp.getAmount() != null ? opp.getAmount() : BigDecimal.ZERO);
                sData.put("count", count);
                sData.put("totalAmount", sum);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalOpportunities", opportunities.size());
        response.put("totalPipelineValue", totalPipelineValue);
        response.put("totalWeightedPipelineValue", totalWeightedValue);
        response.put("stageBreakdown", stageBreakdown);

        return ResponseEntity.ok(response);
    }
}
