package com.acxiom.crm.util;

import com.acxiom.crm.model.*;
import com.acxiom.crm.repository.*;
import com.acxiom.crm.service.AuditService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final LeadRepository leadRepository;
    private final OpportunityRepository opportunityRepository;
    private final FollowUpRepository followUpRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           LeadRepository leadRepository,
                           OpportunityRepository opportunityRepository,
                           FollowUpRepository followUpRepository,
                           ActivityRepository activityRepository,
                           PasswordEncoder passwordEncoder,
                           AuditService auditService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.leadRepository = leadRepository;
        this.opportunityRepository = opportunityRepository;
        this.followUpRepository = followUpRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Already seeded
        }

        System.out.println(">>> SEEDING ACXIOMCRM INITIAL DEMO DATA <<<");

        // 1. Seed Demo Users
        String encPass = passwordEncoder.encode("Password@123");
        User admin = userRepository.save(new User("admin@acxiom.com", encPass, "System Administrator", Role.ADMIN));
        User manager = userRepository.save(new User("manager@acxiom.com", encPass, "Regional Sales Manager", Role.MANAGER));
        User sales = userRepository.save(new User("sales@acxiom.com", encPass, "Senior Sales Executive", Role.SALES_EXECUTIVE));

        auditService.logEvent("SYSTEM", "INITIALIZE", "System", "1", "Seeded system demo users", "127.0.0.1");

        // 2. Seed 15 Customers
        String[][] customersData = {
            {"Tata Consultancy Services", "tcs.corp@tata.com", "9876543210", "TCS Ltd", "IT Park Road", "Mumbai", "Maharashtra"},
            {"Infosys Technologies", "contact@infosys.com", "9876543211", "Infosys Ltd", "Electronics City", "Bengaluru", "Karnataka"},
            {"Wipro Global Enterprises", "procure@wipro.com", "9876543212", "Wipro Corp", "Sarjapur Road", "Bengaluru", "Karnataka"},
            {"HCL Technologies", "info@hcltech.com", "9876543213", "HCL Tech", "Sector 126", "Noida", "Uttar Pradesh"},
            {"Tech Mahindra Solutions", "business@techmahindra.com", "9876543214", "Tech Mahindra", "Hinjawadi", "Pune", "Maharashtra"},
            {"Larsen & Toubro Infotech", "enterprise@lti.com", "9876543215", "L&T Infotech", "Powai", "Mumbai", "Maharashtra"},
            {"Reliance Digital Services", "connect@ril.com", "9876543216", "Reliance Industries", "Navi Mumbai", "Mumbai", "Maharashtra"},
            {"Bharti Airtel Enterprise", "corporate@airtel.com", "9876543217", "Airtel B2B", "Nelson Mandela Marg", "New Delhi", "Delhi"},
            {"HDFC Bank IT Systems", "procure@hdfcbank.com", "9876543218", "HDFC Bank", "Kanjurmarg", "Mumbai", "Maharashtra"},
            {"ICICI Infotech Solutions", "vendor@icicibank.com", "9876543219", "ICICI Bank", "Bandra Kurla Complex", "Mumbai", "Maharashtra"},
            {"Bajaj Finserv Commercial", "partners@bajajfinserv.com", "9876543220", "Bajaj Finserv", "Viman Nagar", "Pune", "Maharashtra"},
            {"Adani Data Centers", "inquiry@adani.com", "9876543221", "Adani Group", "Shantigram", "Ahmedabad", "Gujarat"},
            {"Mahindra & Mahindra IT", "sales@mahindra.com", "9876543222", "M&M Group", "Worli", "Mumbai", "Maharashtra"},
            {"Maruti Suzuki India", "vendor@maruti.co.in", "9876543223", "Maruti Suzuki", "Palam Gurgaon Rd", "Gurugram", "Haryana"},
            {"Sun Pharma Healthcare", "info@sunpharma.com", "9876543224", "Sun Pharma", "Goregaon East", "Mumbai", "Maharashtra"}
        };

        for (int i = 0; i < customersData.length; i++) {
            String[] c = customersData[i];
            String code = String.format("CUST-%04d", 1001 + i);
            String creator = (i % 2 == 0) ? "sales@acxiom.com" : "manager@acxiom.com";
            customerRepository.save(new Customer(code, c[0], c[1], c[2], c[3], c[4], c[5], c[6], "Active", creator));
        }

        // 3. Seed 20 Leads across different statuses
        String[][] leadsData = {
            {"Apex Logistics Cloud", "contact@apexlogistics.com", "9811111101", "Apex Logistics", "Website", "250000.00"},
            {"Zenith Financial Analytics", "info@zenithfin.com", "9811111102", "Zenith FinTech", "LinkedIn", "450000.00"},
            {"Nova BioSciences", "sales@novabio.com", "9811111103", "Nova Bio", "Referral", "180000.00"},
            {"Beacon Supply Chain", "inquiry@beaconsupply.com", "9811111104", "Beacon Ltd", "Cold Call", "320000.00"},
            {"Starlight Media Labs", "hello@starlightmedia.com", "9811111105", "Starlight Media", "Google Ads", "120000.00"},
            {"Quantum Robotics", "procurement@quantumrobot.com", "9811111106", "Quantum Robotics", "Expo", "850000.00"},
            {"Pioneer Agro Systems", "support@pioneeragro.com", "9811111107", "Pioneer Agro", "Website", "210000.00"},
            {"Vanguard Cyber Shield", "leads@vanguardcyber.com", "9811111108", "Vanguard Security", "Partner", "620000.00"},
            {"Horizon Renewable Energy", "b2b@horizonenergy.com", "9811111109", "Horizon Green", "Trade Show", "750000.00"},
            {"Cascade Retail Systems", "retail@cascadegroup.com", "9811111110", "Cascade Retail", "Website", "300000.00"},
            {"Matrix Telecommunications", "telecom@matrixsys.com", "9811111111", "Matrix Telecom", "Direct", "540000.00"},
            {"Summit Hospitality Suite", "info@summithotels.com", "9811111112", "Summit Hotels", "Inbound Call", "190000.00"},
            {"Aura Health Diagnostics", "dr@auradiag.com", "9811111113", "Aura Health", "Referral", "280000.00"},
            {"Solaris Green Mobility", "ev@solarisauto.com", "9811111114", "Solaris Auto", "Expo", "920000.00"},
            {"Krypton Defense Technologies", "contract@kryptondefense.com", "9811111115", "Krypton Tech", "Government RFP", "1500000.00"},
            {"Omega Manufacturing ERP", "erp@omegamfg.com", "9811111116", "Omega Mfg", "Website", "430000.00"},
            {"Epsilon AI Engines", "ai@epsilonsystems.com", "9811111117", "Epsilon AI", "Webinar", "680000.00"},
            {"Delta Shipping Networks", "cargo@deltashipping.com", "9811111118", "Delta Shipping", "Cold Call", "270000.00"},
            {"Theta Smart Warehousing", "smart@thetaware.com", "9811111119", "Theta Storage", "Direct Mail", "360000.00"},
            {"Sigma Cloud Migration", "cloud@sigmasecure.com", "9811111120", "Sigma IT", "LinkedIn", "510000.00"}
        };

        LeadStatus[] statuses = {
            LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.QUALIFIED,
            LeadStatus.CONTACTED, LeadStatus.NEW, LeadStatus.QUALIFIED, LeadStatus.CONVERTED,
            LeadStatus.LOST, LeadStatus.UNQUALIFIED, LeadStatus.NEW, LeadStatus.CONTACTED,
            LeadStatus.QUALIFIED, LeadStatus.CONVERTED, LeadStatus.LOST, LeadStatus.CONTACTED,
            LeadStatus.QUALIFIED, LeadStatus.NEW, LeadStatus.NEW, LeadStatus.QUALIFIED
        };

        for (int i = 0; i < leadsData.length; i++) {
            String[] ld = leadsData[i];
            String code = String.format("LEAD-%04d", 1001 + i);
            String assigned = (i % 2 == 0) ? "sales@acxiom.com" : "manager@acxiom.com";
            leadRepository.save(new Lead(
                    code, ld[0], ld[1], ld[2], ld[3], ld[4],
                    statuses[i], new BigDecimal(ld[5]), assigned
            ));
        }

        // 4. Seed 15 Opportunities across stages
        OpportunityStage[] stages = {
            OpportunityStage.QUALIFICATION, OpportunityStage.PROPOSAL, OpportunityStage.NEGOTIATION,
            OpportunityStage.WON, OpportunityStage.LOST, OpportunityStage.PROPOSAL,
            OpportunityStage.NEGOTIATION, OpportunityStage.WON, OpportunityStage.QUALIFICATION,
            OpportunityStage.PROPOSAL, OpportunityStage.WON, OpportunityStage.NEGOTIATION,
            OpportunityStage.LOST, OpportunityStage.WON, OpportunityStage.PROPOSAL
        };

        int[] probabilities = {20, 50, 75, 100, 0, 45, 80, 100, 15, 60, 100, 70, 0, 100, 50};
        double[] amounts = {
            350000.0, 750000.0, 1200000.0, 850000.0, 400000.0,
            620000.0, 950000.0, 1500000.0, 250000.0, 480000.0,
            2100000.0, 800000.0, 310000.0, 1150000.0, 590000.0
        };

        for (int i = 0; i < 15; i++) {
            String name = "Enterprise CRM Contract #" + (i + 1);
            Long customerId = (long) (1 + (i % 15));
            String assigned = (i % 2 == 0) ? "sales@acxiom.com" : "manager@acxiom.com";
            LocalDate closeDate = LocalDate.now().plusDays(15 + i * 5);
            Opportunity opp = new Opportunity(
                    name, customerId, null, BigDecimal.valueOf(amounts[i]),
                    stages[i], probabilities[i], closeDate, "Open", assigned
            );
            opportunityRepository.save(opp);
        }

        // 5. Seed 15 Follow-Ups
        FollowUpType[] fTypes = {
            FollowUpType.CALL, FollowUpType.MEETING, FollowUpType.EMAIL, FollowUpType.DEMO
        };
        FollowUpStatus[] fStatuses = {
            FollowUpStatus.PLANNED, FollowUpStatus.COMPLETED, FollowUpStatus.MISSED, FollowUpStatus.PLANNED
        };

        for (int i = 0; i < 15; i++) {
            Long customerId = (long) (1 + (i % 15));
            LocalDateTime fDate = (i % 4 == 2)
                    ? LocalDateTime.now().minusDays(2).withHour(11).withMinute(0) // Overdue / Missed
                    : LocalDateTime.now().plusDays(1 + i).withHour(14).withMinute(30); // Planned
            String assigned = (i % 2 == 0) ? "sales@acxiom.com" : "manager@acxiom.com";

            followUpRepository.save(new FollowUp(
                    customerId, null, fDate, fTypes[i % 4],
                    "Follow up on commercial licensing proposal and stakeholder review.",
                    fStatuses[i % 4], assigned
            ));
        }

        // 6. Seed 10 Activities
        ActivityType[] actTypes = {
            ActivityType.CALL, ActivityType.MEETING, ActivityType.EMAIL, ActivityType.TASK
        };
        String[] subjects = {
            "Initial Discovery Call", "Architecture Deep Dive Meeting", "Follow-up Email with Quotation",
            "Prepare Scope of Work Document", "Security Review Meeting", "Customer Feedback Call",
            "Send Updated Master Service Agreement", "Executive Product Showcase", "Post-Sale Check-in Call",
            "Quarterly Business Review Task"
        };

        for (int i = 0; i < 10; i++) {
            Long customerId = (long) (1 + (i % 15));
            String assigned = (i % 2 == 0) ? "sales@acxiom.com" : "manager@acxiom.com";
            activityRepository.save(new Activity(
                    actTypes[i % 4], subjects[i],
                    "Conducted thorough technical evaluation and noted customer requirements.",
                    LocalDateTime.now().minusDays(i), customerId, null, assigned, "Completed"
            ));
        }

        System.out.println(">>> SEEDING COMPLETED SUCCESSFULLY <<<");
    }
}
