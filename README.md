# Zenith — Enterprise CRM Application

A production-grade, full-stack Customer Relationship Management (CRM) application engineered for enterprise assessment. Built on **Java 17+ / 21 LTS**, **Spring Boot 3**, **Spring Security 6**, **Spring Data JPA**, **Thymeleaf**, and styled with modern **AdminOne Bulma Dashboard** with **Chart.js**.

---

## 1. Quick Start & Execution

### Prerequisites
* **Java 17+** (Verified on Java 17, 21, and 25 LTS)
* **Maven Wrapper** (`mvnw.cmd` / `./mvnw` bundled in repository)

### Launch the Application
Run the following command in the project directory:

```bash
# On Windows
.\mvnw.cmd spring-boot:run

# On Linux / macOS
./mvnw spring-boot:run
```

Once started, open your web browser at:
* **Web Portal:** [http://localhost:8080](http://localhost:8080)
* **Interactive Swagger / OpenAPI UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Embedded H2 Database Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * JDBC URL: `jdbc:h2:file:./data/acxiom_crm_db`
  * Username: `sa`
  * Password: `password`

---

## 2. Demo Credentials

The system automatically initializes and seeds 3 pre-configured demo users across all security tiers, with 15+ customers, 20+ leads, 15+ opportunities, follow-ups, and audit logs:

| Role | Email | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@acxiom.com` | `Password@123` | Full system control, user unlocking, role configuration, immutable audit trail |
| **Manager** | `manager@acxiom.com` | `Password@123` | Team pipeline analytics, management dashboards, CSV data reports |
| **Sales Executive** | `sales@acxiom.com` | `Password@123` | Assigned customers, leads, opportunities, follow-ups, and activity logs |

> **Self-Registration:** New users registering via `/register` are automatically assigned the `SALES_EXECUTIVE` role and bound by the password policy.

---

## 3. Architecture & Layers

The system strictly follows an enterprise layered architecture:

```text
Presentation Layer (Controllers & Views)
   │   ├── Web MVC Controllers (Thymeleaf + AdminOne Bulma UI)
   │   └── REST API Controllers (/api/* with DTOs & Swagger OpenAPI)
   ▼
Application Layer (Services & DTOs)
   │   ├── CustomerService, LeadService, OpportunityService
   │   ├── FollowUpService, ActivityService, UserService, AuditService
   │   └── DTO Mapping & Business Rule Validations
   ▼
Domain Layer (Entities & Rules)
   │   ├── Customer, Lead, Opportunity, FollowUp, Activity, User, AuditLog
   │   └── Lifecycle hooks (@PrePersist, @PreUpdate)
   ▼
Data Access Layer (Repositories)
   │   └── Spring Data JPA interfaces with parameterized JPQL
   ▼
Persistence / Database
       └── Embedded persistent H2 Engine (File-based: ./data/acxiom_crm_db)
```

---

## 4. Key Modules & Business Workflows

1. **Authentication & Security (Spring Security 6):**
   * BCrypt adaptive password hashing (never stored in plain-text).
   * Password policy: min 8 characters with uppercase, lowercase, digit, and special character.
   * Lockout security: 5 consecutive failed login attempts trigger an automatic 15-minute lockout.
   * Anti-forgery (CSRF) protection on all state-changing web forms.
   * Immutable audit logging for all authentication, lockout, and password change events.

2. **Dashboard & Analytics:**
   * 8 Role-scoped KPI cards: Total Customers, Total Leads, Open Leads, Total Opportunities, Open Deals, Won Deals, Lost Deals, Total Pipeline Value, and Overdue Follow-ups.
   * Dynamic Chart.js visualizations: Lead Status Funnel, Opportunity Pipeline Stage Breakdown, and Monthly Revenue Trends.

3. **Customer Management:**
   * Auto-generated identifiers: `CUST-1001`, `CUST-1002`, etc.
   * Strict uniqueness checks on Email and Indian Mobile Phone numbers.
   * Soft-delete capability (status updated to `Inactive`).

4. **Lead Progression & Conversion:**
   * Workflow state machine: `NEW` &rarr; `CONTACTED` &rarr; `QUALIFIED` &rarr; `CONVERTED` or `LOST`.
   * Invalid transitions strictly blocked (e.g., `LOST` &rarr; `NEW` is forbidden).
   * **Atomic Conversion:** Only `QUALIFIED` leads can convert. Conversion atomically produces a new `Customer`, opens a new `Opportunity`, transitions the lead to `CONVERTED`, and logs an audit record.

5. **Opportunity Management:**
   * Stages: `QUALIFICATION`, `PROPOSAL`, `NEGOTIATION`, `WON`, `LOST`.
   * Business Rules: Amount > 0, Probability 0–100%, Expected Close Date cannot be in the past for active deals.
   * Formula: **Weighted Value** = $\frac{\text{Amount} \times \text{Probability}}{100}$.
   * Interactive stage-grouped Kanban view.

6. **Follow-Ups & Activities:**
   * Follow-up date validation: Cannot schedule before today.
   * Overdue indicators on missed/pending follow-ups.
   * Complete action workflow.
   * Activity tracking for Calls, Meetings, Emails, and Tasks.

7. **Audit Logging:**
   * Append-only ledger recording: `UserId`, `Action`, `EntityName`, `RecordId`, `OldValue` (JSON), `NewValue` (JSON), `CreatedDate`, `IpAddress`.

8. **REST API Suite:**
   * `GET /api/customers`, `GET /api/customers/{id}`, `POST /api/customers`, `PUT /api/customers/{id}`, `DELETE /api/customers/{id}`
   * `GET /api/leads`, `GET /api/leads/{id}`, `POST /api/leads`, `PUT /api/leads/{id}`, `DELETE /api/leads/{id}`
   * `GET /api/opportunities`, `GET /api/opportunities/{id}`, `POST /api/opportunities`, `PUT /api/opportunities/{id}`, `DELETE /api/opportunities/{id}`
   * `GET /api/followups`, `GET /api/followups/{id}`, `POST /api/followups`, `PUT /api/followups/{id}`, `DELETE /api/followups/{id}`
   * `GET /api/reports/pipeline` (Stage breakdown, counts, total value, and weighted pipeline)

---

## 5. Verification Commands

```bash
# Run automated acceptance test suite (15/15 tests passing)
.\mvnw.cmd test

# Compile and package production JAR
.\mvnw.cmd package -DskipTests
```
