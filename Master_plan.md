# Ticklora Enterprise ITSM Platform
## Master Product Blueprint & Migration Guide (Revised)

This blueprint outlines the complete system architecture, database design, and developmental roadmap for the Ticklora Enterprise ITSM Platform. It is designed as a **Modular Monolith** in Spring Boot, configured for **Multi-Tenancy from Day 1**, utilizing **MySQL** for data storage, **Redis** for caching, and **RabbitMQ** for asynchronous tasks.

---

## 🏛️ Platform Architecture

```
                  ┌─────────────────────────────────┐
                  │          React Frontend         │
                  └────────────────┬────────────────┘
                                   │ HTTPS / WSS
                                   ▼
                  ┌─────────────────────────────────┐
                  │           API Gateway           │
                  └────────────────┬────────────────┘
                                   │
                                   ▼
                  ┌─────────────────────────────────┐
                  │      Spring Boot Monolith       │
                  │  (Modular Domain Architecture)  │
                  └──────┬──────────┬──────────┬────┘
                         │          │          │
         ┌───────────────┘          │          └───────────────┐
         ▼                          ▼                          ▼
┌────────────────┐          ┌───────────────┐          ┌───────────────┐
│     MySQL      │          │     Redis     │          │   RabbitMQ    │
│ (Multi-Tenant) │          │ (Cache/Sess)  │          │ (Async Tasks) │
└────────────────┘          └───────────────┘          └───────────────┘
         │                                                     │
         ▼                                                     ▼
┌────────────────┐                                     ┌───────────────┐
│  Object Store  │                                     │  Gemini AI    │
│  (S3 / MinIO)  │                                     │  (Async Ops)  │
└────────────────┘                                     └───────────────┘
```

### Infrastructure Components
* **Database (MySQL):** Multi-tenant schema design. All tenant-specific tables contain a `tenant_id` column indexed for query separation and security boundaries.
* **Cache (Redis):** Handles session distribution, API rate limiting, dynamic configuration caching, and dashboard KPI caching.
* **Queue (RabbitMQ):** Manages asynchronous jobs (sending emails, webhook retries, heavy SLA calculations, and AI model queries).
* **Storage (S3/MinIO):** Development uses MinIO; production points to AWS S3. Used for minutes of meeting files, screenshots, and custom user profile uploads.
* **Monitoring:** Prometheus scrapes metrics exposed by Spring Boot Actuator; Grafana visualizes system health, memory usage, and API response times.

---

## 🗺️ Domain Architecture (Module-by-Module Blueprint)

### DOMAIN 1 – CORE PLATFORM (Foundation & SaaS Identity)
* **Design Pattern:** The foundation of the system. Multi-tenant context is resolved during the API request filter chain (reading `tenant_id` from the JWT token).

#### Module 1: Tenant & Organization Management
* **Features:**
  * Register Tenant (Company / Organization profile)
  * Manage Branches, Departments, Teams, and Designations within the Tenant scope
* **Tables:**
  * `tenants` (id, name, subdomain, logo_url, is_active, created_at)
  * `branches` (id, tenant_id, name, address, contact_phone, created_at)
  * `departments` (id, tenant_id, branch_id, name, manager_user_id)
  * `teams` (id, tenant_id, department_id, name, team_lead_id)
  * `designations` (id, tenant_id, title, grade_level)

#### Module 2: User Management
* **Features:**
  * Create, edit, activate, or disable User profiles
  * Manage profiles, contact directories, and team assignments
* **Tables:**
  * `users` (id, tenant_id, username, email, is_active, phone, firebase_uid, created_at)
  * `user_profiles` (id, user_id, first_name, last_name, bio, avatar_url, preferred_language)

#### Module 3: Role-Based Access Control (RBAC)
* **Features:**
  * Configure custom User Roles and Permissions
  * Apply Screen Permissions (Frontend UI visibility) and API Permissions (Backend controller filters)
* **Tables:**
  * `roles` (id, tenant_id, name, description, is_system_default)
  * `permissions` (id, perm_key, description)
  * `role_permissions` (role_id, permission_id)
  * `user_roles` (user_id, role_id)

#### Module 4: Audit & Logs
* **Features:**
  * Track user login history, IP addresses, and session locations
  * Record database mutations (who changed which record, old value vs. new value)
* **Tables:**
  * `login_logs` (id, tenant_id, user_id, ip_address, user_agent, timestamp)
  * `audit_logs` (id, tenant_id, user_id, action, entity_name, entity_id, old_values_json, new_values_json, timestamp)

#### Module 5: File & Attachment Engine
* **Features:**
  * Secure file uploads, content-type checks, and size validation
  * File versioning and temporary secure URL signing
* **Tables:**
  * `attachments` (id, tenant_id, file_name, file_path, file_size, content_type, uploaded_by, created_at)
  * `attachment_links` (id, attachment_id, entity_name, entity_id)

---

### DOMAIN 2 – ITSM (IT Service Management)
* **Design Pattern:** The operations engine of the app. All tickets use an auto-incrementing serial sequence per tenant.

#### Module 1: Incident Management
* **Features:**
  * Create, assign, update, and resolve incidents
  * Merge duplicate tickets; split multi-issue incidents
  * Escalation handling (mapping tickets to Tier-1, Tier-2, or Tier-3 queues)
* **Tables:**
  * `tickets` (id, tenant_id, ticket_number, caller, title, description, status, priority, channel, assigned_team_id, assigned_user_id, response_deadline, resolution_deadline, created_at, resolved_at)
  * `ticket_watchers` (ticket_id, user_id)

#### Module 2: Service Request Management
* **Features:**
  * Service Catalog presentation
  * Request Approval triggering; Request Fulfillment workflow steps
* **Tables:**
  * `service_requests` (id, tenant_id, ticket_id, catalog_item_id, requested_for, quantity, total_cost)
  * `service_request_tasks` (id, service_request_id, task_name, assignee_id, status, order_index)

#### Module 3: Problem Management
* **Features:**
  * Group incidents into problem tickets
  * Document Root Cause Analysis (RCA) and maintain a Known Error Database (KEDB)
* **Tables:**
  * `problems` (id, tenant_id, title, description, status, rca_owner_id, workaround, resolution)
  * `problem_incidents` (problem_id, ticket_id)
  * `known_errors` (id, title, symptom, root_cause, workaround, article_id)

#### Module 4: Change Management
* **Features:**
  * Create Change Requests (Standard, Normal, Emergency)
  * CAB (Change Advisory Board) approval flows, risk assessments, and rollback plans
* **Tables:**
  * `changes` (id, tenant_id, title, change_type, risk_level, status, plan_start, plan_end, rollback_plan, implementation_steps)
  * `change_reviews` (id, change_id, reviewer_id, approval_decision, feedback, reviewed_at)

#### Module 5: Release & Deployment Management
* **Features:**
  * Group Change Requests into Releases
  * Release phase tracking, rollback triggers, and validation lists
* **Tables:**
  * `releases` (id, tenant_id, version_name, status, release_date, description)
  * `release_changes` (release_id, change_id)

---

### DOMAIN 3 – CMDB & ASSETS
* **Design Pattern:** Optimized relationship matrix. Uses adjacency schemas to model configuration items without database recursive slowdowns.

#### Module 1: Asset Tracking & Inventory
* **Features:**
  * Track hardware lifecycle (ordered, active, retired)
  * Software asset discovery, license count checks, and warranty notifications
* **Tables:**
  * `assets` (id, tenant_id, asset_tag, name, serial_number, status, purchase_date, warranty_expiry, price)
  * `licenses` (id, tenant_id, software_name, total_keys, used_keys, expiry_date, key_type)

#### Module 2: Configuration Management (CMDB)
* **Features:**
  * Map Configuration Items (CIs - databases, servers, networks)
  * Define relationships (e.g., Database Server X *depends on* Host Server Y)
* **Tables:**
  * `configuration_items` (id, tenant_id, ci_type, name, model, ip_address, owner_id)
  * `ci_relationships` (id, parent_ci_id, child_ci_id, relationship_type)

#### Module 3: Vendor & Contract Management
* **Features:**
  * Supplier directories and Service Level Agreements (SLA) with vendors
  * Contract renewals, lifecycle statuses, and document storage
* **Tables:**
  * `vendors` (id, tenant_id, company_name, contact_name, email, phone, status)
  * `contracts` (id, tenant_id, vendor_id, title, start_date, end_date, annual_value, status, file_attachment_id)

---

### DOMAIN 4 – WORKFLOW & AUTOMATION ENGINE
* **Design Pattern:** Dynamic event-driven state triggers. Built on Spring Events for synchronous automation, offloading to RabbitMQ for long-running queues.

#### Module 1: Approval Engine
* **Features:**
  * Multi-level approval chains (e.g., manager approval followed by finance approval)
  * Conditional thresholds (e.g., requests > $500 automatically require director approval)
* **Tables:**
  * `approval_templates` (id, tenant_id, name, entity_type, min_amount, steps_json)
  * `approval_history` (id, tenant_id, entity_type, entity_id, approver_id, decision, remarks, step_index, decided_at)

#### Module 2: Dynamic Form Builder
* **Features:**
  * Drag-and-drop form definition (textfields, select boxes, dates)
  * Custom field validations and visibility configurations
* **Tables:**
  * `dynamic_forms` (id, tenant_id, form_name, associated_entity_type)
  * `dynamic_fields` (id, form_id, label, field_type, validation_regex, is_required, order_index)
  * `form_submissions` (id, form_id, entity_id, field_id, field_value)

#### Module 3: Event-Driven Automation Rules
* **Features:**
  * If-This-Then-That rule engine (e.g., *"If Category is 'Network', assign to team 'Net-Ops'"*)
  * Auto-escalation rules when a ticket is unassigned for over 30 minutes
* **Tables:**
  * `automation_rules` (id, tenant_id, event_type, condition_expression, action_expression, order_weight, is_active)

---

### DOMAIN 5 – COMMUNICATION CORE
* **Design Pattern:** Abstracted omnichannel design. Extensible to add new channels (Slack, Teams) without touching the core messaging queues.

#### Module 1: Email Integrations (SMTP/IMAP)
* **Features:**
  * Support custom customer inboxes (Multi-tenant IMAP mail reader)
  * Inbound parsing logic creating tickets from body and attachments
* **Tables:**
  * `mailboxes` (id, tenant_id, email_address, smtp_host, smtp_port, smtp_user, smtp_pass, imap_host, imap_port, imap_user, imap_pass, is_active)
  * `email_logs` (id, tenant_id, direction, recipient, sender, subject, status, error_message, message_id)

#### Module 2: Notification Pipeline
* **Features:**
  * Send alerts across In-App (SSE), Email (SMTP), and SMS (Twilio)
  * Keep templates localized and custom-branded per tenant
* **Tables:**
  * `notifications` (id, tenant_id, recipient_id, title, message, is_read, channel_type, sent_at)
  * `notification_templates` (id, tenant_id, template_key, subject, body_template, channel_type)

#### Module 3: Real-Time Chat
* **Features:**
  * Inter-team chats and ticket-linked conversation boards
  * File uploads and read receipts inside chats
* **Tables:**
  * `chat_rooms` (id, tenant_id, room_type, entity_type, entity_id, created_at)
  * `messages` (id, room_id, sender_id, text, attachment_id, timestamp)

---

### DOMAIN 6 – KNOWLEDGE MANAGEMENT
* **Features:**
  * Article drafts, workflow approvals (Review, Publish, Retract)
  * Category structuring, user ratings, feedback logs, and version control
* **Tables:**
  * `knowledge_articles` (id, tenant_id, category_id, title, content, author_id, status, version)
  * `knowledge_categories` (id, tenant_id, parent_id, name, description)
  * `knowledge_feedback` (id, article_id, rater_id, score, comment, created_at)
  * `knowledge_versions` (id, article_id, content, version, updated_by, updated_at)

---

### DOMAIN 7 – COLLABORATION & MEETING ENGINE
* **Features:**
  * Schedule meetings, generate room links, and log attendees
  * Draft Minutes of Meetings (MOM) with version control and generate Action Items mapped to users
* **Tables:**
  * `meetings` (id, tenant_id, title, description, meeting_date, platform, conducted_by, status)
  * `meeting_minutes` (id, meeting_id, detailed_text, decisions_taken, next_steps, version, updated_by)
  * `meeting_actions` (id, meeting_id, description, responsible_user_id, target_date, status)

---

### DOMAIN 8 – AI OPERATIONS (Async Gemini AI Engine)
* **Design Pattern:** All AI evaluations are executed asynchronously via RabbitMQ to prevent blocking the web thread pool.

```
[Web Service: Ticket Created] 
      │
      ▼ (Publish Job)
[RabbitMQ AI Queue]
      │
      ▼ (Consume Job)
[Spring Boot AI Worker] ──► [Gemini API (HTTPS)] ──► [WebSocket Push] ──► [React UI]
```

* **Features:**
  * Automatic Ticket Classification and priority matching based on caller query
  * Sentiment analysis, similar ticket lookup, and automated timesheet activity summarization
* **Tables:**
  * `ai_requests` (id, tenant_id, model_name, request_type, prompt_text, created_at)
  * `ai_responses` (id, request_id, raw_response, response_status, execution_time_ms)

---

### DOMAIN 9 – ANALYTICS & REPORTING
* **Features:**
  * Custom Dashboards with user-configured cards
  * KPI reporting (e.g., Mean Time to Resolution, First Response Rate, Breach Counts)
* **Tables:**
  * `dashboards` (id, tenant_id, user_id, layout_config_json)
  * `reports` (id, tenant_id, name, query_config_json, created_by, is_public)
  * `kpi_definitions` (id, name, target_value, calculation_type, unit)

---

### DOMAIN 10 – INTEGRATION HUB
* **Features:**
  * API Key generation for third-party scripts (e.g., monitoring agents)
  * Webhook dispatch configuration for external ticket synchronization
* **Tables:**
  * `api_keys` (id, tenant_id, user_id, api_key_hash, description, expiry_date, is_active)
  * `webhooks` (id, tenant_id, event_name, target_url, secret_token, is_active)

---

### DOMAIN 11 – PROJECT MANAGEMENT
* **Features:**
  * Track project milestones, epics, sprint loops, and backlog items
  * Kanban boards, Gantt chart configs, and individual user work logs
* **Tables:**
  * `projects` (id, tenant_id, name, code, manager_id, status, start_date, end_date)
  * `tasks` (id, project_id, title, description, parent_task_id, assigned_id, order_weight, status)
  * `worklogs` (id, task_id, user_id, hours_spent, logged_date, description)

---

### DOMAIN 12 – PROCUREMENT & INVENTORY
* **Features:**
  * Purchase Request (PR) and Purchase Order (PO) approval flows
  * Inventory levels, stock items, and stock movement auditing
* **Tables:**
  * `purchase_requests` (id, tenant_id, requested_by, items_json, estimated_cost, status)
  * `purchase_orders` (id, tenant_id, pr_id, vendor_id, order_number, total_amount, status)
  * `inventory` (id, tenant_id, asset_tag, item_name, quantity, warehouse_location, min_stock_threshold)
  * `stock_movements` (id, inventory_id, quantity_delta, transaction_type, performed_by, description)

---

### DOMAIN 13 – SYSTEM SETTINGS & CONFIGURATION
* **Features:**
  * Auto-number sequence generator configurations (e.g., `INC-[yyyy]-[00000]`)
  * Work calendar schedules, holiday lists, custom branding, and custom color schemas
* **Tables:**
  * `settings` (id, tenant_id, setting_key, setting_value, field_type)
  * `number_sequences` (id, tenant_id, prefix, format, current_index)
  * `holiday_calendars` (id, tenant_id, holiday_date, description)
  * `business_hours` (id, tenant_id, day_of_week, start_time, end_time)

---

### DOMAIN 14 – COMPLIANCE & RISK REGISTER
* **Features:**
  * Policy registry, risk assessments, and compliance checks mapping
* **Tables:**
  * `policies` (id, tenant_id, title, policy_text, published_date, expiry_date)
  * `risks` (id, tenant_id, description, impact_score, probability_score, mitigation_plan, status)
  * `compliance_controls` (id, tenant_id, name, frequency, next_audit_date, status)

---

### DOMAIN 15 – SECURITY OPERATIONS (SecOps)
* **Features:**
  * Incident response tracking for security-related issues
  * Vulnerability registers, system impact assessments, and mitigation schedules
* **Tables:**
  * `security_incidents` (id, tenant_id, severity, threat_vector, remediation_status)
  * `vulnerabilities` (id, tenant_id, cve_id, description, severity_score, patch_status)

---

## 🏗️ Universal Shared Frameworks

* **Comment Framework:** Provides uniform comment sections for tickets, changes, problems, tasks, and projects. Mapped via polymorphic relations.
* **Notification Framework:** Centralized alert dispatcher. Automatically translates business events to mail, SMS, or screen popups.
* **Tag Framework:** Handles text labels (e.g., `#Critical`, `#HR`, `#AWS`) that users can append to tickets or assets to facilitate searching.
* **Watcher Framework:** Subscribes user IDs to specific database entities, triggering notifications when updates occur.

---

## ⏱️ Scheduler Jobs

| Interval | Task Description | Platform Component |
| :--- | :--- | :--- |
| **Every Minute** | SLA breach verification & Ticket Escalation | Spring `@Scheduled` thread |
| **Every 5 Minutes** | Process un-sent email alerts and outbound notification queues | RabbitMQ dispatch loop |
| **Every 10 Minutes** | Poll incoming emails from all active tenant IMAP servers | Multi-tenant IMAP service |
| **Every Hour** | Fetch and process non-critical background AI tasks (e.g., sentiment trending) | Background AI worker |
| **Daily** | Clean up expired API tokens, run DB backups, compile daily reports | Spring Batch / Cron Job |

---

## 📈 Revised Development Order (Phases 1-7)

```
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 1: FOUNDATION                                             │
│ - Tenant Management (Multi-Tenancy) & Core RBAC                 │
│ - Notification Framework, Audits, Settings                      │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 2: MVP OPERATIONS                                         │
│ - Incident Management & Service Catalogs                        │
│ - Email Pollers, SLA Deadlines, Knowledge Base                  │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 3: WORKFLOW AUTOMATION                                    │
│ - Multi-level Approval Engine, Dynamic Form Builder             │
│ - Analytics Engine & Dashboard KPIs                             │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 4: ASSET & WORK MANAGEMENT                                │
│ - CMDB Relationship Matrix & Asset Discovery                    │
│ - Timesheets, Time Cards, Projects & Tasks                      │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 5: INTELLECTUAL COLLABORATION                             │
│ - Async Gemini AI Service (Classification, Summary)             │
│ - Minutes of Meeting & WebSockets                               │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 6: ENTERPRISE AUDIT                                       │
│ - Compliance Policy Registries & SecOps (CVE logs)              │
│ - Procurement Workflows & Inventory Stocking                    │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PHASE 7: SAAS OPERATIONS                                        │
│ - Dynamic Subdomains, White-Labeling, and Multi-Tenant Billing  │
└─────────────────────────────────────────────────────────────────┘
```
