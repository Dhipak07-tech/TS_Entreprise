-- =========================================================================
-- V9: Enterprise Dynamic Permission Management System Schema
-- =========================================================================

-- 1. Create MODULES Table
CREATE TABLE MODULES (
    ID BIGINT AUTO_INCREMENT,
    NAME NVARCHAR(100) NOT NULL,
    CODE NVARCHAR(50) NOT NULL UNIQUE,
    ICON NVARCHAR(50),
    SORT_ORDER INT NOT NULL DEFAULT 0,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_MODULES PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default Modules
INSERT INTO MODULES (NAME, CODE, ICON, SORT_ORDER, IS_ACTIVE) VALUES
('Dashboard', 'DASHBOARD', 'LayoutDashboard', 1, true),
('Ticketing', 'TICKETING', 'Ticket', 2, true),
('Incident Management', 'INCIDENTS', 'AlertTriangle', 3, true),
('Problem Management', 'PROBLEMS', 'FileSearch2', 4, true),
('Asset Management', 'ASSETS', 'HardDrive', 5, true),
('Changes', 'CHANGES', 'GitBranch', 6, true),
('Knowledge Base', 'KNOWLEDGE', 'BookOpen', 7, true),
('User Management', 'USERS', 'Users', 8, true),
('Settings', 'SETTINGS', 'Settings', 9, true);

-- 2. Alter PAGES to link to MODULES
ALTER TABLE PAGES ADD COLUMN MODULE_ID BIGINT DEFAULT NULL;
ALTER TABLE PAGES ADD CONSTRAINT FK_PAGES_MODULES FOREIGN KEY (MODULE_ID) REFERENCES MODULES(ID) ON DELETE SET NULL;

-- Update existing seeded Pages with corresponding Module ID
UPDATE PAGES SET MODULE_ID = 1 WHERE PAGE_ID = 1001; -- Dashboard
UPDATE PAGES SET MODULE_ID = 2 WHERE PAGE_ID = 1002; -- Ticket Management
UPDATE PAGES SET MODULE_ID = 2 WHERE PAGE_ID = 1003; -- Create Ticket
UPDATE PAGES SET MODULE_ID = 3 WHERE PAGE_ID = 1004; -- Incident Management
UPDATE PAGES SET MODULE_ID = 4 WHERE PAGE_ID = 1005; -- Problem Management
UPDATE PAGES SET MODULE_ID = 6 WHERE PAGE_ID = 1006; -- Change Management
UPDATE PAGES SET MODULE_ID = 5 WHERE PAGE_ID = 1007; -- Asset Management
UPDATE PAGES SET MODULE_ID = 7 WHERE PAGE_ID = 1008; -- Knowledge Base
UPDATE PAGES SET MODULE_ID = 8 WHERE PAGE_ID = 1009; -- User Management
UPDATE PAGES SET MODULE_ID = 9 WHERE PAGE_ID = 1010; -- Role Management

-- 3. Create ACTIONS Table
CREATE TABLE ACTIONS (
    ID BIGINT AUTO_INCREMENT,
    NAME NVARCHAR(100) NOT NULL,
    CODE NVARCHAR(50) NOT NULL UNIQUE,
    DESCRIPTION NVARCHAR(255),
    CONSTRAINT PK_ACTIONS PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default Actions
INSERT INTO ACTIONS (NAME, CODE, DESCRIPTION) VALUES
('VIEW', 'VIEW', 'Ability to view pages/resources'),
('CREATE', 'CREATE', 'Ability to create new resources'),
('UPDATE', 'UPDATE', 'Ability to update existing resources'),
('DELETE', 'DELETE', 'Ability to delete resources'),
('APPROVE', 'APPROVE', 'Ability to approve workflows'),
('REJECT', 'REJECT', 'Ability to reject workflows'),
('ASSIGN', 'ASSIGN', 'Ability to assign ownership'),
('EXPORT', 'EXPORT', 'Ability to export data'),
('IMPORT', 'IMPORT', 'Ability to import data'),
('PRINT', 'PRINT', 'Ability to print reports');

-- 4. Re-create ROLE_PAGE_PERMISSIONS as dynamic junction Role -> Page -> Action
DROP TABLE IF EXISTS ROLE_PAGE_PERMISSIONS;

CREATE TABLE ROLE_PAGE_PERMISSIONS (
    ROLE_ID BIGINT NOT NULL,
    PAGE_ID INT NOT NULL,
    ACTION_ID BIGINT NOT NULL,
    IS_ALLOWED BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_ROLE_PAGE_PERMISSIONS PRIMARY KEY (ROLE_ID, PAGE_ID, ACTION_ID),
    CONSTRAINT FK_RPP_ROLES FOREIGN KEY (ROLE_ID) REFERENCES ROLES(ID) ON DELETE CASCADE,
    CONSTRAINT FK_RPP_PAGES FOREIGN KEY (PAGE_ID) REFERENCES PAGES(PAGE_ID) ON DELETE CASCADE,
    CONSTRAINT FK_RPP_ACTIONS FOREIGN KEY (ACTION_ID) REFERENCES ACTIONS(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Create USER_PAGE_PERMISSIONS for individual user permission overrides
CREATE TABLE USER_PAGE_PERMISSIONS (
    USER_ID BIGINT NOT NULL,
    PAGE_ID INT NOT NULL,
    ACTION_ID BIGINT NOT NULL,
    IS_ALLOWED BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT PK_USER_PAGE_PERMISSIONS PRIMARY KEY (USER_ID, PAGE_ID, ACTION_ID),
    CONSTRAINT FK_UPP_USERS FOREIGN KEY (USER_ID) REFERENCES USERS(ID) ON DELETE CASCADE,
    CONSTRAINT FK_UPP_PAGES FOREIGN KEY (PAGE_ID) REFERENCES PAGES(PAGE_ID) ON DELETE CASCADE,
    CONSTRAINT FK_UPP_ACTIONS FOREIGN KEY (ACTION_ID) REFERENCES ACTIONS(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Create MENU_CONFIGURATIONS Table for Dynamic Sidebar
CREATE TABLE MENU_CONFIGURATIONS (
    ID BIGINT AUTO_INCREMENT,
    NAME NVARCHAR(100) NOT NULL,
    PATH NVARCHAR(255) NOT NULL,
    ICON NVARCHAR(50),
    PARENT_ID BIGINT DEFAULT NULL,
    PAGE_ID INT DEFAULT NULL,
    SORT_ORDER INT NOT NULL DEFAULT 0,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
    COMPANY_ID BIGINT DEFAULT 1,
    CONSTRAINT PK_MENU_CONFIGURATIONS PRIMARY KEY (ID),
    CONSTRAINT FK_MENU_PARENT FOREIGN KEY (PARENT_ID) REFERENCES MENU_CONFIGURATIONS(ID) ON DELETE SET NULL,
    CONSTRAINT FK_MENU_PAGES FOREIGN KEY (PAGE_ID) REFERENCES PAGES(PAGE_ID) ON DELETE SET NULL,
    CONSTRAINT FK_MENU_COMPANIES FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default Menu Configurations for Dynamic Sidebar
INSERT INTO MENU_CONFIGURATIONS (NAME, PATH, ICON, PARENT_ID, PAGE_ID, SORT_ORDER, IS_ACTIVE, COMPANY_ID) VALUES
('Dashboard', '/app/dashboard', 'LayoutDashboard', NULL, 1001, 1, true, 1),
('Ticketing', '/app/tickets', 'Ticket', NULL, 1002, 2, true, 1),
('Incident Management', '/app/incidents', 'AlertTriangle', NULL, 1004, 3, true, 1),
('Problem Management', '/app/problems', 'FileSearch2', NULL, 1005, 4, true, 1),
('Asset Management', '/app/assets', 'HardDrive', NULL, 1007, 5, true, 1),
('Changes', '/app/changes', 'GitBranch', NULL, 1006, 6, true, 1),
('Knowledge Base', '/app/knowledge', 'BookOpen', NULL, 1008, 7, true, 1),
('User Management', '/app/users', 'Users', NULL, 1009, 8, true, 1),
('Settings', '/app/settings', 'Settings', NULL, 1010, 9, true, 1);


-- 7. Seed initial Role-Page-Action Permissions
-- Action IDs: VIEW (1), CREATE (2), UPDATE (3), DELETE (4), APPROVE (5), REJECT (6), ASSIGN (7), EXPORT (8), IMPORT (9), PRINT (10)

-- Role 6 (ULTRA_SUPER_ADMIN) & Role 5 (SUPER_ADMIN) - Allow all actions on all pages
-- Let's populate this using helper Cartesian product or insert explicitly.
-- Explicit inserts for Role 1 (USER):
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED) VALUES
(1, 1001, 1, true), -- Dashboard View
(1, 1002, 1, true), -- Ticket List View
(1, 1003, 1, true), -- Create Ticket View
(1, 1003, 2, true), -- Create Ticket Create
(1, 1008, 1, true); -- Knowledge Base View

-- Role 2 (SUPPORT_AGENT):
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED) VALUES
(2, 1001, 1, true), -- Dashboard View
(2, 1002, 1, true), -- Ticket List View
(2, 1002, 2, true), -- Ticket Create
(2, 1002, 3, true), -- Ticket Update
(2, 1002, 7, true), -- Ticket Assign
(2, 1003, 1, true),
(2, 1003, 2, true),
(2, 1008, 1, true), -- KB View
(2, 1008, 2, true), -- KB Create
(2, 1008, 3, true); -- KB Update

-- Role 3 (TEAM_LEAD):
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED) VALUES
(3, 1001, 1, true),
(3, 1002, 1, true), (3, 1002, 2, true), (3, 1002, 3, true), (3, 1002, 4, true), (3, 1002, 5, true), (3, 1002, 6, true), (3, 1002, 7, true),
(3, 1003, 1, true), (3, 1003, 2, true),
(3, 1004, 1, true), (3, 1004, 2, true), (3, 1004, 3, true),
(3, 1005, 1, true), (3, 1005, 2, true), (3, 1005, 3, true),
(3, 1006, 1, true), (3, 1006, 2, true), (3, 1006, 3, true), (3, 1006, 5, true), (3, 1006, 6, true),
(3, 1008, 1, true), (3, 1008, 2, true), (3, 1008, 3, true), (3, 1008, 4, true);

-- Role 4 (ADMINISTRATOR):
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED) VALUES
(4, 1001, 1, true), (4, 1001, 8, true),
(4, 1002, 1, true), (4, 1002, 2, true), (4, 1002, 3, true), (4, 1002, 4, true), (4, 1002, 5, true), (4, 1002, 6, true), (4, 1002, 7, true), (4, 1002, 8, true),
(4, 1003, 1, true), (4, 1003, 2, true),
(4, 1004, 1, true), (4, 1004, 2, true), (4, 1004, 3, true), (4, 1004, 4, true), (4, 1004, 5, true), (4, 1004, 6, true), (4, 1004, 7, true), (4, 1004, 8, true),
(4, 1005, 1, true), (4, 1005, 2, true), (4, 1005, 3, true), (4, 1005, 4, true), (4, 1005, 5, true), (4, 1005, 6, true), (4, 1005, 7, true), (4, 1005, 8, true),
(4, 1006, 1, true), (4, 1006, 2, true), (4, 1006, 3, true), (4, 1006, 4, true), (4, 1006, 5, true), (4, 1006, 6, true), (4, 1006, 7, true), (4, 1006, 8, true),
(4, 1007, 1, true), (4, 1007, 2, true), (4, 1007, 3, true), (4, 1007, 4, true), (4, 1007, 5, true), (4, 1007, 6, true), (4, 1007, 7, true), (4, 1007, 8, true),
(4, 1008, 1, true), (4, 1008, 2, true), (4, 1008, 3, true), (4, 1008, 4, true), (4, 1008, 5, true), (4, 1008, 6, true), (4, 1008, 7, true), (4, 1008, 8, true),
(4, 1009, 1, true), (4, 1009, 2, true), (4, 1009, 3, true), (4, 1009, 4, true), (4, 1009, 8, true);

-- Role 5 (SUPER_ADMIN) & Role 6 (ULTRA_SUPER_ADMIN): Add all combinations of pages (1001-1010) and actions (1-10)
-- Using a CROSS JOIN pattern helper or explicit loops in a stored procedure, or simple inserts. Let's do a direct Cartesian insert from existing data:
INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED)
SELECT 5, p.PAGE_ID, a.ID, true
FROM PAGES p CROSS JOIN ACTIONS a;

INSERT INTO ROLE_PAGE_PERMISSIONS (ROLE_ID, PAGE_ID, ACTION_ID, IS_ALLOWED)
SELECT 6, p.PAGE_ID, a.ID, true
FROM PAGES p CROSS JOIN ACTIONS a;
