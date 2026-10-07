# Zenith CRM — Technical Interview & Viva Study Guide

This document is prepared to help you understand every architectural component of the application, explain the request lifecycle with confidence, and ace the technical viva or code walkthrough.

---

## 1. Architectural Flow (Controller &rarr; Service &rarr; Repository &rarr; Database)

Every HTTP request follows a strict, decoupled layered architecture:

```text
Browser / Client Request
        │
        ▼
1. Web Filter Chain (Spring Security 6)
   - Inspects JSESSIONID cookie or credentials
   - Enforces Role-Based Access Control (@PreAuthorize / requestMatchers)
   - Verifies CSRF token on POST requests
        │
        ▼
2. Presentation Layer (Controller)
   - Receives DTO / ModelAttribute
   - Triggers Bean Validation (@Valid BindingResult)
   - Unobtrusively extracts HTTP metadata (IP, Principal)
   - Delegates business execution to Service
        │
        ▼
3. Application Layer (Service)
   - Executes domain workflows and validates business rules
   - Evaluates security boundaries (ownership / scope checks)
   - Emits asynchronous or transactional Audit Log records
   - Interacts with Spring Data Repositories
        │
        ▼
4. Data Access Layer (Repository)
   - Extends JpaRepository<Entity, Long>
   - Executes parameterized JPQL queries to avoid SQL Injection
        │
        ▼
5. Persistence Engine (H2 Database / Hibernate ORM)
   - Manages table schemas, indexes, and ACID transactional state
```

---

## 2. Deep Dive by Key Modules

### A. Authentication & Security
* **Key Files:** `SecurityConfig.java`, `CustomUserDetailsService.java`, `CustomAuthenticationFailureHandler.java`, `CustomAuthenticationSuccessHandler.java`, `UserService.java`.
* **How it works:**
  1. Passwords are saved using `BCryptPasswordEncoder` with salted cryptographic hashing.
  2. When a user submits credentials, `CustomUserDetailsService` loads their entity and checks `isActive()` and `isAccountLocked()`.
  3. If login fails, `CustomAuthenticationFailureHandler` increments `failedLoginAttempts`. On the 5th consecutive failure, it sets `lockoutEndTime` to 15 minutes in the future and generates an audit log entry.
  4. On successful login, `CustomAuthenticationSuccessHandler` clears failed attempts to 0 and redirects the user to `/dashboard`.

### B. Dual-Layer Validation
* **Key Files:** `ValidIndianPhone.java`, `IndianPhoneValidator.java`, `CustomerDto.java`, `OpportunityDto.java`, `form.html` templates.
* **How it works:**
  1. **Client-side:** Bootstrap 5 form validation with HTML5 regex patterns (`pattern="^(?:\+91|0)?[6-9]\d{9}$"`) provides immediate UI feedback.
  2. **Server-side:** Spring `@Valid` triggers Hibernate Validator annotations (`@NotBlank`, `@Email`, `@ValidIndianPhone`, `@DecimalMin`, `@Min`, `@Max`).
  3. **Service-side:** Business checks confirm that Opportunity Amount $> 0$, Probability is between 0 and 100, and Follow-Up dates are not in the past.

### C. Lead Conversion Workflow
* **Key Files:** `LeadService.java`, `LeadController.java`, `leads/convert.html`.
* **How it works:**
  1. The lead's status is validated. Only leads in `QUALIFIED` status are eligible for conversion; other states throw an `IllegalArgumentException`.
  2. In an atomic transaction (`@Transactional`), the system:
     * Generates a new `Customer` record (auto-assigning `CUST-xxxx`).
     * Creates an associated `Opportunity` in the `QUALIFICATION` stage.
     * Updates the `Lead` status to `CONVERTED` (locking it from further status changes).
     * Records a detailed audit log entry.

### D. Audit Logging Engine
* **Key Files:** `AuditLog.java`, `AuditService.java`, `AuditLogRepository.java`.
* **How it works:**
  * Uses Jackson `ObjectMapper` to serialize JSON snapshots of objects before and after mutations.
  * Records the user, action, target entity, record ID, IP address, and timestamp.
  * The audit ledger is append-only—there are no update or delete endpoints.

---

## 3. Top 10 Technical Viva / Interview Questions & Answers

### Q1: Why did you use DTOs instead of exposing JPA Entities directly in the REST API and MVC forms?
**Answer:** Exposing entities directly causes security vulnerabilities like mass assignment (over-posting), exposes sensitive database schema details, and can trigger JSON circular reference errors (LazyInitializationException). DTOs decouple the external API contract from internal database models.

### Q2: How does Spring Security prevent CSRF attacks in your application?
**Answer:** Spring Security enables CSRF tokens by default. For every state-changing POST/PUT/DELETE form, a unique cryptographically signed token is embedded as a hidden field (`_csrf`). The server validates this token with the user's session before processing the request, preventing Cross-Site Request Forgery.

### Q3: How is SQL Injection prevented in this project?
**Answer:** All database interactions use Spring Data JPA and Hibernate. Queries use parameterized JPQL (`:query`, `:status`, `:assignedTo`) rather than raw string concatenation. Hibernate binds parameters as literal values, preventing malicious SQL code injection.

### Q4: Explain the account lockout mechanism you implemented.
**Answer:** In `CustomAuthenticationFailureHandler`, each failed login increments `failedLoginAttempts` on the `User` entity. If attempts reach 5, `lockoutEndTime` is set to `LocalDateTime.now().plusMinutes(15)`. During subsequent login attempts, `CustomUserDetailsService` checks if `isAccountLocked()` is true and throws a `LockedException`, blocking access until the lockout window expires or an Admin unlocks the account.

### Q5: How is role-based data isolation enforced between an Admin and a Sales Executive?
**Answer:** In `CustomerService`, `LeadService`, and `OpportunityService`, the logged-in user's identity is retrieved from the `SecurityContext`. If the user has the `SALES_EXECUTIVE` role, queries are scoped strictly to `createdBy = :username` or `assignedTo = :username`. Attempting to modify another representative's record triggers an access control validation error.

### Q6: What is the significance of the `@Transactional` annotation during Lead Conversion?
**Answer:** Converting a lead involves three distinct database mutations: creating a Customer, creating an Opportunity, and updating the Lead status. `@Transactional` ensures atomicity (ACID). If any step fails (e.g., duplicate phone check on the Customer), the entire operation rolls back, preventing inconsistent data.

### Q7: How does your application calculate the weighted pipeline value on the Dashboard?
**Answer:** Weighted pipeline value measures risk-adjusted forecast revenue using the formula:
$$\text{Weighted Value} = \frac{\text{Amount} \times \text{Probability}}{100}$$
In `Opportunity.java`, a helper method executes this using `BigDecimal` with `RoundingMode.HALF_UP` to prevent floating-point rounding errors.

### Q8: How did you implement soft delete for Customers?
**Answer:** In `CustomerService.deleteCustomer()`, instead of executing a SQL `DELETE`, the customer's status is updated to `"Inactive"`. This preserves historical referential integrity with past opportunities, follow-ups, and audit entries while removing them from active operational workflows.

### Q9: How are Chart.js visualizations populated dynamically?
**Answer:** `DashboardService` queries the database for aggregated metric groups (counts grouped by `LeadStatus`, counts grouped by `OpportunityStage`, and monthly sales revenue). The controller injects these maps into the model, and Thymeleaf serializes them directly into JavaScript data arrays using `/*[[${metrics.leadStatusData}]]*/`.

### Q10: Where are database credentials and configuration stored?
**Answer:** Externalized in `src/main/resources/application.properties`. For production environments, these can be overridden via environment variables or external configuration files without altering compiled bytecode.
