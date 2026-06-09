-- =========================================================================
-- V7: Multi-Tenant Data Isolation and Page-Level Access Control
-- =========================================================================

-- 1. COMPANIES Table
CREATE TABLE COMPANIES (
    ID BIGINT AUTO_INCREMENT,
    NAME NVARCHAR(150) NOT NULL,
    SUBDOMAIN NVARCHAR(100) UNIQUE,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    CREATED_AT DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_COMPANIES PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed Default Company
INSERT INTO COMPANIES (NAME, SUBDOMAIN, IS_ACTIVE) VALUES ('Enterprise Corp', 'enterprise', true);

-- Add COMPANY_ID column to existing tables
ALTER TABLE USERS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE DEPARTMENTS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE TEAMS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE ROLES ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE TICKETS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE NOTIFICATIONS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE EMAIL_INBOXES ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE SLA_POLICIES ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE AUDIT_LOGS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE LOGIN_LOGS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE TICKET_COMMENTS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE TICKET_ACTIVITIES ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;
ALTER TABLE SETTINGS ADD COLUMN COMPANY_ID BIGINT DEFAULT 1;

-- Enforce constraints & foreign keys
ALTER TABLE USERS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE USERS ADD CONSTRAINT FK_USERS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE DEPARTMENTS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE DEPARTMENTS ADD CONSTRAINT FK_DEPARTMENTS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE TEAMS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE TEAMS ADD CONSTRAINT FK_TEAMS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE ROLES MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE ROLES ADD CONSTRAINT FK_ROLES_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE TICKETS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE TICKETS ADD CONSTRAINT FK_TICKETS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE NOTIFICATIONS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE NOTIFICATIONS ADD CONSTRAINT FK_NOTIFICATIONS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE EMAIL_INBOXES MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE EMAIL_INBOXES ADD CONSTRAINT FK_EMAIL_INBOXES_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE SLA_POLICIES MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE SLA_POLICIES ADD CONSTRAINT FK_SLA_POLICIES_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE AUDIT_LOGS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE AUDIT_LOGS ADD CONSTRAINT FK_AUDIT_LOGS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE LOGIN_LOGS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE LOGIN_LOGS ADD CONSTRAINT FK_LOGIN_LOGS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE TICKET_COMMENTS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE TICKET_COMMENTS ADD CONSTRAINT FK_TICKET_COMMENTS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE TICKET_ACTIVITIES MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE TICKET_ACTIVITIES ADD CONSTRAINT FK_TICKET_ACTIVITIES_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;

ALTER TABLE SETTINGS MODIFY COLUMN COMPANY_ID BIGINT NOT NULL;
ALTER TABLE SETTINGS ADD CONSTRAINT FK_SETTINGS_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE;


-- 2. PAGES Table (Unique identifiers for dynamic authorization)
CREATE TABLE PAGES (
    PAGE_ID INT NOT NULL,
    NAME NVARCHAR(100) NOT NULL,
    PATH NVARCHAR(255) NOT NULL,
    DESCRIPTION NVARCHAR(255),
    CONSTRAINT PK_PAGES PRIMARY KEY (PAGE_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default Pages
INSERT INTO PAGES (PAGE_ID, NAME, PATH, DESCRIPTION) VALUES
(1001, 'Dashboard', '/app/dashboard', 'System metrics and operational summary'),
(1002, 'Ticket Management', '/app/tickets', 'Helpdesk operations and lifecycle tracking'),
(1003, 'Create Ticket', 'ACTION_CREATE_TICKET', 'Ability to raise support tickets'),
(1004, 'Incident Management', 'TAB_INCIDENTS', 'Incident control and outages logging'),
(1005, 'Problem Management', 'TAB_PROBLEMS', 'RCA investigation and KEDB mapping'),
(1006, 'Change Management', '/app/changes', 'CAB approvals and implementation steps'),
(1007, 'Asset Management', '/app/assets', 'Hardware and configuration items inventory'),
(1008, 'Knowledge Base', '/app/knowledge', 'Troubleshooting guides and article catalog'),
(1009, 'User Management', '/app/users', 'Create and modify employee accounts'),
(1010, 'Role Management', '/app/roles', 'Granular RBAC permission matrix setup');


-- 3. ROLE_PAGE_PERMISSIONS Table (Granular flags per role-page combination)
CREATE TABLE ROLE_PAGE_PERMISSIONS (
    ROLE_ID BIGINT NOT NULL,
    PAGE_ID INT NOT NULL,
    CAN_VIEW BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_CREATE BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_UPDATE BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_DELETE BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_APPROVE BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_REJECT BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_ASSIGN BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_IMPORT BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_EXPORT BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_PRINT BOOLEAN NOT NULL DEFAULT FALSE,
    CAN_REPORT_ACCESS BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT PK_ROLE_PAGE_PERMISSIONS PRIMARY KEY (ROLE_ID, PAGE_ID),
    CONSTRAINT FK_RPP_ROLES FOREIGN KEY (ROLE_ID) REFERENCES ROLES(ID) ON DELETE CASCADE,
    CONSTRAINT FK_RPP_PAGES FOREIGN KEY (PAGE_ID) REFERENCES PAGES(PAGE_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed Default Page Permissions for existing Roles
-- Role 1: USER
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE) VALUES
(1, 1001, true, false), -- Dashboard (View)
(1, 1002, true, false), -- Ticket list (View own)
(1, 1003, true, true),  -- Create ticket (Yes)
(1, 1008, true, false); -- KB (View)

-- Role 2: SUPPORT_AGENT
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE, CAN_UPDATE, CAN_ASSIGN) VALUES
(2, 1001, true, false, false, false),
(2, 1002, true, true, true, true),
(2, 1003, true, true, false, false),
(2, 1008, true, true, true, false);

-- Role 3: TEAM_LEAD
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE, CAN_UPDATE, CAN_DELETE, CAN_APPROVE, CAN_REJECT, CAN_ASSIGN) VALUES
(3, 1001, true, false, false, false, false, false, false),
(3, 1002, true, true, true, true, true, true, true),
(3, 1003, true, true, false, false, false, false, false),
(3, 1004, true, true, true, false, false, false, false),
(3, 1005, true, true, true, false, false, false, false),
(3, 1006, true, true, true, false, true, true, false),
(3, 1008, true, true, true, true, false, false, false);

-- Role 4: ADMINISTRATOR
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE, CAN_UPDATE, CAN_DELETE, CAN_APPROVE, CAN_REJECT, CAN_ASSIGN, CAN_EXPORT, CAN_REPORT_ACCESS) VALUES
(4, 1001, true, false, false, false, false, false, false, true, true),
(4, 1002, true, true, true, true, true, true, true, true, true),
(4, 1003, true, true, false, false, false, false, false, false, false),
(4, 1004, true, true, true, true, true, true, true, true, true),
(4, 1005, true, true, true, true, true, true, true, true, true),
(4, 1006, true, true, true, true, true, true, true, true, true),
(4, 1007, true, true, true, true, true, true, true, true, true),
(4, 1008, true, true, true, true, true, true, true, true, true),
(4, 1009, true, true, true, true, false, false, false, true, true);

-- Role 5: SUPER_ADMIN (Full company-level access)
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE, CAN_UPDATE, CAN_DELETE, CAN_APPROVE, CAN_REJECT, CAN_ASSIGN, CAN_IMPORT, CAN_EXPORT, CAN_PRINT, CAN_REPORT_ACCESS) VALUES
(5, 1001, true, true, true, true, true, true, true, true, true, true, true),
(5, 1002, true, true, true, true, true, true, true, true, true, true, true),
(5, 1003, true, true, true, true, true, true, true, true, true, true, true),
(5, 1004, true, true, true, true, true, true, true, true, true, true, true),
(5, 1005, true, true, true, true, true, true, true, true, true, true, true),
(5, 1006, true, true, true, true, true, true, true, true, true, true, true),
(5, 1007, true, true, true, true, true, true, true, true, true, true, true),
(5, 1008, true, true, true, true, true, true, true, true, true, true, true),
(5, 1009, true, true, true, true, true, true, true, true, true, true, true),
(5, 1010, true, true, true, true, true, true, true, true, true, true, true);

-- Role 6: ULTRA_SUPER_ADMIN (System wide access)
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, CAN_VIEW, CAN_CREATE, CAN_UPDATE, CAN_DELETE, CAN_APPROVE, CAN_REJECT, CAN_ASSIGN, CAN_IMPORT, CAN_EXPORT, CAN_PRINT, CAN_REPORT_ACCESS) VALUES
(6, 1001, true, true, true, true, true, true, true, true, true, true, true),
(6, 1002, true, true, true, true, true, true, true, true, true, true, true),
(6, 1003, true, true, true, true, true, true, true, true, true, true, true),
(6, 1004, true, true, true, true, true, true, true, true, true, true, true),
(6, 1005, true, true, true, true, true, true, true, true, true, true, true),
(6, 1006, true, true, true, true, true, true, true, true, true, true, true),
(6, 1007, true, true, true, true, true, true, true, true, true, true, true),
(6, 1008, true, true, true, true, true, true, true, true, true, true, true),
(6, 1009, true, true, true, true, true, true, true, true, true, true, true),
(6, 1010, true, true, true, true, true, true, true, true, true, true, true);
