package com.acxiom.crm.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardMetricsDto {

    private long totalCustomers;
    private long totalLeads;
    private long openLeads;
    private long totalOpportunities;
    private long openOpportunities;
    private long wonOpportunities;
    private long lostOpportunities;
    private BigDecimal totalPipelineValue = BigDecimal.ZERO;
    private long overdueFollowUps;

    // Charts data
    private Map<String, Long> leadStatusData;
    private Map<String, Long> opportunityStageData;
    private List<String> monthlySalesLabels;
    private List<BigDecimal> monthlySalesValues;

    public DashboardMetricsDto() {
    }

    // Getters and Setters
    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(long totalLeads) {
        this.totalLeads = totalLeads;
    }

    public long getOpenLeads() {
        return openLeads;
    }

    public void setOpenLeads(long openLeads) {
        this.openLeads = openLeads;
    }

    public long getTotalOpportunities() {
        return totalOpportunities;
    }

    public void setTotalOpportunities(long totalOpportunities) {
        this.totalOpportunities = totalOpportunities;
    }

    public long getOpenOpportunities() {
        return openOpportunities;
    }

    public void setOpenOpportunities(long openOpportunities) {
        this.openOpportunities = openOpportunities;
    }

    public long getWonOpportunities() {
        return wonOpportunities;
    }

    public void setWonOpportunities(long wonOpportunities) {
        this.wonOpportunities = wonOpportunities;
    }

    public long getLostOpportunities() {
        return lostOpportunities;
    }

    public void setLostOpportunities(long lostOpportunities) {
        this.lostOpportunities = lostOpportunities;
    }

    public BigDecimal getTotalPipelineValue() {
        return totalPipelineValue;
    }

    public void setTotalPipelineValue(BigDecimal totalPipelineValue) {
        this.totalPipelineValue = totalPipelineValue;
    }

    public long getOverdueFollowUps() {
        return overdueFollowUps;
    }

    public void setOverdueFollowUps(long overdueFollowUps) {
        this.overdueFollowUps = overdueFollowUps;
    }

    public Map<String, Long> getLeadStatusData() {
        return leadStatusData;
    }

    public void setLeadStatusData(Map<String, Long> leadStatusData) {
        this.leadStatusData = leadStatusData;
    }

    public Map<String, Long> getOpportunityStageData() {
        return opportunityStageData;
    }

    public void setOpportunityStageData(Map<String, Long> opportunityStageData) {
        this.opportunityStageData = opportunityStageData;
    }

    public List<String> getMonthlySalesLabels() {
        return monthlySalesLabels;
    }

    public void setMonthlySalesLabels(List<String> monthlySalesLabels) {
        this.monthlySalesLabels = monthlySalesLabels;
    }

    public List<BigDecimal> getMonthlySalesValues() {
        return monthlySalesValues;
    }

    public void setMonthlySalesValues(List<BigDecimal> monthlySalesValues) {
        this.monthlySalesValues = monthlySalesValues;
    }
}
