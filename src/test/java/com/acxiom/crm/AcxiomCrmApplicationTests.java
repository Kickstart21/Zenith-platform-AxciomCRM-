package com.acxiom.crm;

import com.acxiom.crm.dto.CustomerDto;
import com.acxiom.crm.dto.FollowUpDto;
import com.acxiom.crm.dto.OpportunityDto;
import com.acxiom.crm.model.Customer;
import com.acxiom.crm.model.OpportunityStage;
import com.acxiom.crm.repository.*;
import com.acxiom.crm.service.CustomerService;
import com.acxiom.crm.service.FollowUpService;
import com.acxiom.crm.service.OpportunityService;
import com.acxiom.crm.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AcxiomCrmApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private OpportunityService opportunityService;

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    @DisplayName("Verification 1: Seed data initialized properly")
    void testSeedDataInitialized() {
        assertTrue(userRepository.count() >= 3, "Demo users must be seeded");
        assertTrue(customerRepository.count() >= 15, "At least 15 customers must be seeded");
        assertTrue(leadRepository.count() >= 20, "At least 20 leads must be seeded");
        assertTrue(opportunityRepository.count() >= 15, "At least 15 opportunities must be seeded");
        assertTrue(auditLogRepository.count() > 0, "Audit logs must exist");
    }

    @Test
    @DisplayName("Section 17.19 Step 1: Protected CRM page redirects unauthenticated request to /login")
    void testAnonymousUserRedirected() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 2: Authenticated user reaches Dashboard")
    void testAuthenticatedUserDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("metrics"));
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 5: Opportunity with Amount <= 0 is rejected")
    void testOpportunityAmountZeroRejected() {
        OpportunityDto dto = new OpportunityDto();
        dto.setOpportunityName("Invalid Amount Opp");
        dto.setCustomerId(1L);
        dto.setAmount(BigDecimal.ZERO);
        dto.setProbability(50);
        dto.setExpectedCloseDate(LocalDate.now().plusDays(10));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            opportunityService.createOpportunity(dto);
        });
        assertEquals("Opportunity Amount must be greater than 0.", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 6: Opportunity with Probability > 100 is rejected")
    void testOpportunityProbabilityOver100Rejected() {
        OpportunityDto dto = new OpportunityDto();
        dto.setOpportunityName("Invalid Probability Opp");
        dto.setCustomerId(1L);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setProbability(150);
        dto.setExpectedCloseDate(LocalDate.now().plusDays(10));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            opportunityService.createOpportunity(dto);
        });
        assertEquals("Probability must be between 0 and 100.", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 7: Opportunity with Expected Close Date in the past is rejected")
    void testOpportunityCloseDateInPastRejected() {
        OpportunityDto dto = new OpportunityDto();
        dto.setOpportunityName("Past Date Opp");
        dto.setCustomerId(1L);
        dto.setAmount(new BigDecimal("10000.00"));
        dto.setProbability(50);
        dto.setExpectedCloseDate(LocalDate.now().minusDays(5));
        dto.setStage(OpportunityStage.QUALIFICATION);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            opportunityService.createOpportunity(dto);
        });
        assertEquals("Expected Close Date cannot be in the past.", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 8: Follow-up date earlier than today is rejected")
    void testFollowUpDateBeforeTodayRejected() {
        FollowUpDto dto = new FollowUpDto();
        dto.setFollowUpDate(LocalDateTime.now().minusDays(2));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            followUpService.scheduleFollowUp(dto);
        });
        assertEquals("Follow-up date cannot be earlier than today.", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "sales@acxiom.com", roles = {"SALES_EXECUTIVE"})
    @DisplayName("Section 17.19 Step 9: SalesExecutive cannot access Admin user management")
    void testSalesExecutiveAccessAdminForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@acxiom.com", roles = {"MANAGER"})
    @DisplayName("Section 17.19 Step 10: Manager can access Reports")
    void testManagerCanAccessReports() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 11: Admin can access Users & Audit Logs")
    void testAdminCanAccessAdminConsole() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 13: /api/customers returns JSON")
    void testApiCustomers() throws Exception {
        mockMvc.perform(get("/api/customers").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 3 & 4: Duplicate customer creation prevented on server")
    void testDuplicateCustomerValidation() {
        CustomerDto dup = new CustomerDto();
        dup.setCustomerName("Duplicate Customer");
        dup.setEmail("tcs.corp@tata.com"); // exists in seed data
        dup.setPhone("9876599999");

        Exception ex = assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(dup));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 12: CRM operations create audit log entries")
    void testAuditLogCreatedOnEntityOperation() {
        long initialCount = auditLogRepository.count();

        CustomerDto dto = new CustomerDto();
        dto.setCustomerName("Audit Test Customer");
        dto.setEmail("audittest_" + System.currentTimeMillis() + "@acxiomtest.com");
        dto.setPhone("9" + (System.currentTimeMillis() % 1000000000L));
        dto.setStatus("Active");

        Customer saved = customerService.createCustomer(dto);
        assertNotNull(saved.getCustomerId());

        long afterCount = auditLogRepository.count();
        assertTrue(afterCount > initialCount, "Audit log count must increase after createCustomer");
    }

    @Test
    @WithMockUser(username = "admin@acxiom.com", roles = {"ADMIN"})
    @DisplayName("Section 17.19 Step 13 extended: /api/leads, /api/opportunities, /api/followups, /api/reports/pipeline")
    void testAllRestApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/leads").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/opportunities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/followups").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/reports/pipeline").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOpportunities").exists())
                .andExpect(jsonPath("$.totalPipelineValue").exists());
    }

    @Test
    @DisplayName("Section 17.9: Password policy enforcement")
    void testPasswordPolicyEnforcement() {
        assertThrows(IllegalArgumentException.class, () -> userService.validatePasswordPolicy("weak"));
        assertThrows(IllegalArgumentException.class, () -> userService.validatePasswordPolicy("nocapital123!"));
        assertDoesNotThrow(() -> userService.validatePasswordPolicy("ValidP@ssw0rd123"));
    }
}
