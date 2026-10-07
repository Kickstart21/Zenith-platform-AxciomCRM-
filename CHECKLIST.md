# Section 17 Compliance & Acceptance Evaluation Checklist

This document provides a 1-to-1 mapping verifying that **AcxiomCRM** satisfies all mandatory criteria specified in the technical assessment document (`AcxiomCRM_Complete_Project_Assignment.pdf`), including Section 17.17 and Section 17.19.

---

## 1. Section 17.17 — Minimum Evaluation Criteria

| Evaluation Area | Expected Specification | Implementing File(s) / Feature | Status |
| :--- | :--- | :--- | :---: |
| **CRUD Operations** | Complete Create, Read, Update, Delete across all modules | `CustomerController.java`, `LeadController.java`, `OpportunityController.java`, `FollowUpController.java`, `ActivityController.java` | **PASS** |
| **Dual-Layer Validation** | Client-side feedback + strict server-side validation | `IndianPhoneValidator.java`, `ValidIndianPhone.java`, `CustomerDto.java`, `OpportunityDto.java`, `form.html` templates | **PASS** |
| **Business Rules** | Enforce opportunity, lead status, and follow-up constraints | `LeadService.java` (transitions & conversion), `OpportunityService.java` (amount > 0, prob 0-100, close date), `FollowUpService.java` (date not before today) | **PASS** |
| **Authentication** | Secure registration, login, logout, and lockout policy | `SecurityConfig.java`, `CustomUserDetailsService.java`, `CustomAuthenticationFailureHandler.java`, `UserService.java` | **PASS** |
| **Authorization** | Distinct roles: `ADMIN`, `MANAGER`, `SALES_EXECUTIVE` | `SecurityConfig.java` (`hasRole`, `hasAnyRole`), `Role.java`, Method-level `@PreAuthorize` | **PASS** |
| **Dashboard** | 8 KPI cards + Chart.js visualizations | `DashboardService.java`, `DashboardController.java`, `dashboard/index.html` (3 Chart.js graphs) | **PASS** |
| **Search & Filtering** | Search and filter criteria across lists | `CustomerRepository.java`, `LeadRepository.java`, `OpportunityRepository.java`, `FollowUpRepository.java` | **PASS** |
| **REST API** | Customer, Lead, Opportunity, Follow-up, and Pipeline Report endpoints with DTOs | `CustomerApiController.java`, `LeadApiController.java`, `OpportunityApiController.java`, `FollowUpApiController.java`, `ReportApiController.java`, `OpenApiConfig.java` | **PASS** |
| **Audit Logging** | Immutable event ledger (Login, CRUD, Lockout, Role) | `AuditLog.java`, `AuditLogRepository.java`, `AuditService.java`, `AdminUserController.java` | **PASS** |
| **Architecture** | Layered design: Controllers, Models, Repositories, Services, DTOs | `com.acxiom.crm.*` cleanly divided across `controller`, `model`, `repository`, `service`, `dto`, `security` | **PASS** |
| **Security Standards** | No plain-text passwords, CSRF on POST forms, parameterized queries | BCrypt hashing, Spring Security CSRF protection, Hibernate JPQL queries, generic error view | **PASS** |
| **Automated Test Suite** | 15 unit and integration tests passing in Maven test suite | `AcxiomCrmApplicationTests.java` covering full Section 17.19 test matrix | **PASS** |

---

## 2. Section 17.19 — Final Acceptance Scenario Test Results

| Step # | Scenario Description | Tested Execution / Behavior | Result |
| :---: | :--- | :--- | :---: |
| **1** | Open CRM without auth &rarr; Protected pages blocked | Requesting `GET /customers` without credentials returns HTTP 302 redirecting to `/login`. | **PASS** |
| **2** | Login with valid credentials &rarr; Redirects to Dashboard | Successfully logs in and lands on `/dashboard` displaying all KPI metrics and charts. | **PASS** |
| **3** | Create Customer with invalid email/phone &rarr; Blocked | Form validation triggers immediately; blocks submission with inline error message. | **PASS** |
| **4** | Bypass browser validation &rarr; Server rejects request | Server checks `BindingResult` and entity constraints; invalid payload rejected. | **PASS** |
| **5** | Create Opportunity with Amount $\le 0$ &rarr; Rejected | Throws `IllegalArgumentException`: `"Opportunity Amount must be greater than 0."` | **PASS** |
| **6** | Create Opportunity with Probability $> 100$ &rarr; Rejected | Throws `IllegalArgumentException`: `"Probability must be between 0 and 100."` | **PASS** |
| **7** | Set Expected Close Date in past &rarr; Rejected | Active opportunities validate date against `LocalDate.now()`; rejects past dates. | **PASS** |
| **8** | Create Follow-Up dated before today &rarr; Rejected | Throws `IllegalArgumentException`: `"Follow-up date cannot be earlier than today."` | **PASS** |
| **9** | Login as SalesExecutive &rarr; Scoped access only | Records restricted to assigned entities; access to `/admin/**` returns 403 Forbidden. | **PASS** |
| **10** | Login as Manager &rarr; Team pipeline & reports accessible | Access granted to `/reports`, pipeline overview, and team metrics. | **PASS** |
| **11** | Login as Admin &rarr; Administration console enabled | User management, password resets, account unlocks, and audit trails accessible. | **PASS** |
| **12** | Create/Update/Delete record &rarr; Audit entry written | System records an immutable entry into `audit_logs` table capturing before/after state. | **PASS** |
| **13** | Call `/api/customers` &rarr; Authorized JSON returned | REST controller returns HTTP 200 with JSON payload using DTO schema. | **PASS** |
| **14** | Open Dashboard &rarr; KPI cards & Chart.js display | All 8 cards and 3 charts (Lead Status, Stage Funnel, Sales Trend) render properly. | **PASS** |
