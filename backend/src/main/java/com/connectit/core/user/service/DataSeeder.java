package com.connectit.core.user.service;

import com.connectit.core.department.entity.Department;
import com.connectit.core.department.repository.DepartmentRepository;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.rbac.repository.RoleRepository;
import com.connectit.core.team.entity.Team;
import com.connectit.core.team.repository.TeamRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.repository.UserProfileRepository;
import com.connectit.core.user.repository.UserRepository;
import com.connectit.core.request.entity.ServiceCatalogItem;
import com.connectit.core.request.repository.ServiceCatalogItemRepository;
import com.connectit.core.approval.entity.ApprovalPolicy;
import com.connectit.core.approval.entity.ApprovalStep;
import com.connectit.core.approval.repository.ApprovalPolicyRepository;
import com.connectit.core.vendor.entity.Vendor;
import com.connectit.core.vendor.repository.VendorRepository;
import com.connectit.core.asset.entity.Asset;
import com.connectit.core.asset.repository.AssetRepository;
import com.connectit.core.cmdb.entity.ConfigurationItem;
import com.connectit.core.cmdb.repository.ConfigurationItemRepository;
import com.connectit.core.cmdb.entity.CiRelationship;
import com.connectit.core.cmdb.repository.CiRelationshipRepository;
import com.connectit.core.knowledge.entity.KbCategory;
import com.connectit.core.knowledge.repository.KbCategoryRepository;
import com.connectit.core.knowledge.entity.KbArticle;
import com.connectit.core.knowledge.repository.KbArticleRepository;
import com.connectit.core.grc.entity.SecurityIncident;
import com.connectit.core.grc.repository.SecurityIncidentRepository;
import com.connectit.core.saas.entity.Subscription;
import com.connectit.core.saas.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ConfigurationItemRepository configurationItemRepository;

    @Autowired
    private CiRelationshipRepository ciRelationshipRepository;

    @Autowired
    private KbCategoryRepository kbCategoryRepository;

    @Autowired
    private KbArticleRepository kbArticleRepository;

    @Autowired
    private SecurityIncidentRepository securityIncidentRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ServiceCatalogItemRepository serviceCatalogItemRepository;

    @Autowired
    private ApprovalPolicyRepository approvalPolicyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            seedPhase3DataIfEmpty();
            seedPhase4DataIfEmpty();
            seedPhase5DataIfEmpty();
            return;
        }

        // 1. Create Default Departments
        Department itDept = Department.builder()
                .name("Information Technology")
                .code("DEPT_IT")
                .build();
        departmentRepository.save(itDept);

        Department hrDept = Department.builder()
                .name("Human Resources")
                .code("DEPT_HR")
                .build();
        departmentRepository.save(hrDept);

        // 2. Helper method to create and save a user
        createUser("employee_user", "employee@connectit.com", "USER", "Employee", "User", itDept);
        createUser("support_agent", "agent@connectit.com", "SUPPORT_AGENT", "Support", "Agent", itDept);
        createUser("team_lead", "lead@connectit.com", "TEAM_LEAD", "Team", "Lead", itDept);
        createUser("admin", "admin@connectit.com", "ADMINISTRATOR", "System", "Admin", itDept);
        createUser("super_admin", "superadmin@connectit.com", "SUPER_ADMIN", "Super", "Admin", itDept);
        createUser("ultra_admin", "ultraadmin@connectit.com", "ULTRA_SUPER_ADMIN", "Ultra", "Admin", itDept);

        // 3. Create Default Teams
        User leadUser = userRepository.findByUsername("team_lead").orElse(null);
        Team supportTeam = Team.builder()
                .name("IT Support Tier 1")
                .department(itDept)
                .teamLead(leadUser)
                .build();
        teamRepository.save(supportTeam);

        // Update department managers
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        itDept.setManager(adminUser);
        departmentRepository.save(itDept);

        seedPhase3DataIfEmpty();
        seedPhase4DataIfEmpty();
        seedPhase5DataIfEmpty();
    }

    private void seedPhase3DataIfEmpty() {
        if (serviceCatalogItemRepository.count() == 0) {
            serviceCatalogItemRepository.save(ServiceCatalogItem.builder()
                    .name("Developer Laptop (MacBook Pro)")
                    .description("Apple M3 Pro, 18GB Unified Memory, 512GB SSD, 14-inch Liquid Retina XDR display.")
                    .category("Hardware")
                    .cost(BigDecimal.valueOf(1999.00))
                    .isActive(true)
                    .build());

            serviceCatalogItemRepository.save(ServiceCatalogItem.builder()
                    .name("UltraWide 34\" Monitor")
                    .description("34-inch curved WQHD monitor, 21:9 aspect ratio, HDR10, USB-C connectivity.")
                    .category("Hardware")
                    .cost(BigDecimal.valueOf(499.00))
                    .isActive(true)
                    .build());

            serviceCatalogItemRepository.save(ServiceCatalogItem.builder()
                    .name("Ergonomic Office Chair")
                    .description("Premium mesh task chair with lumbar support, 3D armrests, and seat depth adjustment.")
                    .category("Office Supply")
                    .cost(BigDecimal.valueOf(350.00))
                    .isActive(true)
                    .build());

            serviceCatalogItemRepository.save(ServiceCatalogItem.builder()
                    .name("AWS Sandbox Environment")
                    .description("Provisioning a pre-configured AWS sandbox account capped at $100 monthly spend limit.")
                    .category("Cloud Infrastructure")
                    .cost(BigDecimal.valueOf(100.00))
                    .isActive(true)
                    .build());
        }

        if (approvalPolicyRepository.count() == 0) {
            Role leadRole = roleRepository.findByName("TEAM_LEAD").orElse(null);
            Role adminRole = roleRepository.findByName("ADMINISTRATOR").orElse(null);

            if (leadRole != null && adminRole != null) {
                // Change approval policy (Requires team lead -> Administrator approval)
                ApprovalPolicy changePolicy = ApprovalPolicy.builder()
                        .name("CAB Review Policy")
                        .entityType("CHANGE")
                        .minAmount(BigDecimal.ZERO)
                        .steps(new ArrayList<>())
                        .build();

                changePolicy.getSteps().add(ApprovalStep.builder()
                        .policy(changePolicy)
                        .approverRole(leadRole)
                        .stepOrder(1)
                        .build());

                changePolicy.getSteps().add(ApprovalStep.builder()
                        .policy(changePolicy)
                        .approverRole(adminRole)
                        .stepOrder(2)
                        .build());

                approvalPolicyRepository.save(changePolicy);

                // Service Request policy (Requires team lead approval)
                ApprovalPolicy requestPolicy = ApprovalPolicy.builder()
                        .name("Request Purchasing Policy")
                        .entityType("REQUEST")
                        .minAmount(BigDecimal.valueOf(200.00))
                        .steps(new ArrayList<>())
                        .build();

                requestPolicy.getSteps().add(ApprovalStep.builder()
                        .policy(requestPolicy)
                        .approverRole(leadRole)
                        .stepOrder(1)
                        .build());

                approvalPolicyRepository.save(requestPolicy);
            }
        }
    }

    private void createUser(String username, String email, String roleName, String first, String last, Department dept) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode("Password@123"))
                .isActive(true)
                .mfaEnabled(false)
                .department(dept)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .firstName(first)
                .lastName(last)
                .phone("+1 555-0199")
                .preferredLanguage("en")
                .build();

        userProfileRepository.save(profile);
    }

    private void seedPhase4DataIfEmpty() {
        if (vendorRepository.count() == 0) {
            Vendor apple = Vendor.builder()
                    .companyName("Apple Inc.")
                    .contactName("Enterprise Sales")
                    .email("enterprise@apple.com")
                    .phone("+1 800-854-3680")
                    .status("ACTIVE")
                    .build();
            vendorRepository.save(apple);

            Vendor dell = Vendor.builder()
                    .companyName("Dell Technologies")
                    .contactName("Corporate Support")
                    .email("support@dell.com")
                    .phone("+1 800-456-3355")
                    .status("ACTIVE")
                    .build();
            vendorRepository.save(dell);

            Vendor cisco = Vendor.builder()
                    .companyName("Cisco Systems")
                    .contactName("Cisco Enterprise")
                    .email("sales@cisco.com")
                    .phone("+1 800-553-6387")
                    .status("ACTIVE")
                    .build();
            vendorRepository.save(cisco);

            if (assetRepository.count() == 0) {
                Asset laptop1 = Asset.builder()
                        .assetTag("AST-LP-001")
                        .name("MacBook Pro 16\"")
                        .serialNumber("C02F1234Q6YY")
                        .model("Apple Silicon M3 Max")
                        .assetType("HARDWARE")
                        .status("IN_USE")
                        .vendor(apple)
                        .purchaseDate(LocalDate.now().minusMonths(3))
                        .warrantyExpiry(LocalDate.now().plusMonths(33))
                        .cost(BigDecimal.valueOf(3499.00))
                        .build();
                assetRepository.save(laptop1);

                Asset laptop2 = Asset.builder()
                        .assetTag("AST-LP-002")
                        .name("Dell XPS 15")
                        .serialNumber("DELL9876543")
                        .model("Dell XPS 15 9530")
                        .assetType("HARDWARE")
                        .status("IN_STOCK")
                        .vendor(dell)
                        .purchaseDate(LocalDate.now().minusMonths(6))
                        .warrantyExpiry(LocalDate.now().plusMonths(18))
                        .cost(BigDecimal.valueOf(1899.00))
                        .build();
                assetRepository.save(laptop2);

                Asset router = Asset.builder()
                        .assetTag("AST-NET-001")
                        .name("Cisco Catalyst 9300")
                        .serialNumber("CSCO0001122")
                        .model("Catalyst 9300 48-port")
                        .assetType("HARDWARE")
                        .status("IN_USE")
                        .vendor(cisco)
                        .purchaseDate(LocalDate.now().minusYears(1))
                        .warrantyExpiry(LocalDate.now().plusYears(2))
                        .cost(BigDecimal.valueOf(5500.00))
                        .build();
                assetRepository.save(router);

                if (configurationItemRepository.count() == 0) {
                    User adminUser = userRepository.findByUsername("admin").orElse(null);

                    ConfigurationItem serverCi = ConfigurationItem.builder()
                            .name("Production Web Server")
                            .ciType("SERVER")
                            .ipAddress("192.168.1.100")
                            .environment("PROD")
                            .owner(adminUser)
                            .asset(laptop1)
                            .build();
                    configurationItemRepository.save(serverCi);

                    ConfigurationItem dbCi = ConfigurationItem.builder()
                            .name("Production Database Cluster")
                            .ciType("DATABASE")
                            .ipAddress("192.168.1.101")
                            .environment("PROD")
                            .owner(adminUser)
                            .build();
                    configurationItemRepository.save(dbCi);

                    ConfigurationItem routerCi = ConfigurationItem.builder()
                            .name("Core Router Office")
                            .ciType("ROUTER")
                            .ipAddress("192.168.1.1")
                            .environment("PROD")
                            .owner(adminUser)
                            .asset(router)
                            .build();
                    configurationItemRepository.save(routerCi);

                    if (ciRelationshipRepository.count() == 0) {
                        ciRelationshipRepository.save(CiRelationship.builder()
                                .parentCi(dbCi)
                                .childCi(serverCi)
                                .relationshipType("DEPENDS_ON")
                                .build());

                        ciRelationshipRepository.save(CiRelationship.builder()
                                .parentCi(serverCi)
                                .childCi(routerCi)
                                .relationshipType("RUNS_ON")
                                .build());
                    }
                }
            }
        }
    }

    private void seedPhase5DataIfEmpty() {
        // Seed Subscriptions (Billing)
        if (subscriptionRepository.count() == 0) {
            subscriptionRepository.save(Subscription.builder()
                    .planTier("PROFESSIONAL")
                    .status("ACTIVE")
                    .billingCycle("MONTHLY")
                    .currentPeriodStart(LocalDateTime.now().minusDays(10))
                    .currentPeriodEnd(LocalDateTime.now().plusDays(20))
                    .build());
        }

        // Seed GRC Security Incidents
        if (securityIncidentRepository.count() == 0) {
            securityIncidentRepository.save(SecurityIncident.builder()
                    .incidentNumber("SEC-INC-94830")
                    .title("Phishing Campaign Targeting HR")
                    .description("Multiple HR employees reported suspicious emails requesting credential verification.")
                    .severity("HIGH")
                    .status("INVESTIGATING")
                    .identifiedAt(LocalDateTime.now().minusDays(2))
                    .build());

            securityIncidentRepository.save(SecurityIncident.builder()
                    .incidentNumber("SEC-INC-10294")
                    .title("Brute Force Attack Blocked")
                    .description("Automatic perimeter firewall detected and blacklisted IP range performing ssh attempts.")
                    .severity("MEDIUM")
                    .status("RESOLVED")
                    .identifiedAt(LocalDateTime.now().minusDays(5))
                    .build());
        }

        // Seed Knowledge Base
        if (kbCategoryRepository.count() == 0) {
            KbCategory gen = KbCategory.builder()
                    .name("Getting Started")
                    .description("General orientation for platform users.")
                    .build();
            kbCategoryRepository.save(gen);

            KbCategory hardware = KbCategory.builder()
                    .name("Hardware Support")
                    .description("Laptop, peripheral, and local setup assistance.")
                    .build();
            kbCategoryRepository.save(hardware);

            KbCategory software = KbCategory.builder()
                    .name("Software & Apps")
                    .description("Corporate licensed installations and access requests.")
                    .build();
            kbCategoryRepository.save(software);

            KbCategory network = KbCategory.builder()
                    .name("Network & WiFi")
                    .description("Enterprise network connection credentials and troubleshooting.")
                    .build();
            kbCategoryRepository.save(network);

            if (kbArticleRepository.count() == 0) {
                User author = userRepository.findByUsername("admin").orElse(null);

                kbArticleRepository.save(KbArticle.builder()
                        .category(gen)
                        .title("How to Connect to Enterprise Wi-Fi")
                        .content("To connect to the ConnectIT enterprise Wi-Fi: 1. Select the CONNECTIT_SECURE SSID. 2. Log in using your corporate username and password. 3. Accept the security certificate if prompted.")
                        .status("PUBLISHED")
                        .author(author)
                        .viewCount(152)
                        .isPinned(true)
                        .publishedAt(LocalDateTime.now().minusDays(15))
                        .build());

                kbArticleRepository.save(KbArticle.builder()
                        .category(hardware)
                        .title("Troubleshooting Laptop Display Issues")
                        .content("If your external monitor is not receiving signal: 1. Verify all HDMI/USB-C cables are connected securely. 2. Power cycle the docking station by disconnecting power for 10 seconds. 3. Update the display drivers via self-service portal.")
                        .status("PUBLISHED")
                        .author(author)
                        .viewCount(84)
                        .isPinned(false)
                        .publishedAt(LocalDateTime.now().minusDays(10))
                        .build());
            }
        }
    }
}
