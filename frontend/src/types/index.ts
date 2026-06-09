export interface AuthUser {
  userId: number;
  tenantId: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  tenantName: string | null;
  roles: string[];
  permissions: string[];
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  organizationName: string;
  subdomain?: string;
  industry?: string;
  companySize?: string;
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  tenantId: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  tenantName: string | null;
  roles: string[];
  permissions: string[];
  passwordResetRequired?: boolean;
}

export interface ResetPasswordRequest {
  usernameOrEmail: string;
  oldPassword: string;
  newPassword: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface UserDTO {
  id: number;
  tenantId: number;
  username: string;
  email: string;
  isActive: boolean;
  phone: string | null;
  firstName: string | null;
  lastName: string | null;
  avatarUrl: string | null;
  bio: string | null;
  designationId: number | null;
  departmentId: number | null;
  teamId: number | null;
  branchId: number | null;
  roles: string[];
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface DashboardStats {
  totalUsers: number;
  totalBranches: number;
  totalDepartments: number;
  totalTeams: number;
  tenantName: string;
}

// ITSM Types
export interface TicketDTO {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: 'NEW' | 'OPEN' | 'IN_PROGRESS' | 'ON_HOLD' | 'RESOLVED' | 'CLOSED' | 'CANCELLED';
  priority: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  source: 'WEB' | 'EMAIL' | 'PHONE' | 'CHAT' | 'API';
  requesterId: number;
  requesterName: string;
  assignedTeamId: number | null;
  assignedTeamName: string | null;
  assignedUserId: number | null;
  assignedUserName: string | null;
  createdAt: string;
  updatedAt: string;
  resolutionNotes?: string | null;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  priority?: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  impact?: 'HIGH' | 'MEDIUM' | 'LOW';
  urgency?: 'HIGH' | 'MEDIUM' | 'LOW';
  category?: string;
  subcategory?: string;
  channel?: 'WEB' | 'EMAIL' | 'PHONE' | 'CHAT' | 'API';
  ticketType?: 'INCIDENT' | 'SERVICE_REQUEST';
  assignedTeamId?: number;
  assignedUserId?: number;
}

export interface KnowledgeArticleDTO {
  id: number;
  tenantId: number;
  categoryId: number | null;
  title: string;
  content: string;
  authorId: number;
  status: 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'RETRACTED' | 'ARCHIVED';
  version: number;
  viewCount: number;
  isPinned: boolean;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateArticleRequest {
  categoryId?: number;
  title: string;
  content: string;
  isPinned?: boolean;
  initialStatus?: 'DRAFT' | 'IN_REVIEW' | 'PUBLISHED' | 'RETRACTED' | 'ARCHIVED';
}

export interface ApprovalHistoryDTO {
  id: number;
  tenantId: number;
  entityType: string;
  entityId: number;
  approverId: number;
  decision: 'PENDING' | 'APPROVED' | 'REJECTED';
  remarks: string | null;
  stepIndex: number;
  decidedAt: string | null;
  createdAt: string;
}

// CMDB Types
export interface AssetDTO {
  id: number;
  tenantId: number;
  assetTag: string;
  name: string;
  serialNumber: string | null;
  category: string | null;
  status: 'IN_USE' | 'IN_STOCK' | 'REPAIR' | 'RETIRED' | 'LOST';
  assignedUserId: number | null;
  vendorId: number | null;
  purchaseDate: string | null;
  warrantyExpiry: string | null;
  price: number | null;
  createdAt: string;
  updatedAt: string | null;
}

// GRC Types
export interface SecurityIncidentDTO {
  id: number;
  tenantId: number;
  incidentNumber: string;
  title: string;
  description: string | null;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'IDENTIFIED' | 'CONTAINED' | 'ERADICATED' | 'RECOVERED' | 'CLOSED';
  assignedUserId: number | null;
  identifiedAt: string;
  containedAt: string | null;
  rootCause: string | null;
  createdAt: string;
  updatedAt: string | null;
}

// SaaS Billing Types
export interface SubscriptionDTO {
  id: number;
  tenantId: number;
  planTier: 'FREE' | 'STARTER' | 'PROFESSIONAL' | 'ENTERPRISE';
  status: 'TRIALING' | 'ACTIVE' | 'PAST_DUE' | 'CANCELED' | 'UNPAID';
  billingCycle: 'MONTHLY' | 'ANNUALLY';
  currentPeriodStart: string | null;
  currentPeriodEnd: string | null;
  cancelAtPeriodEnd: boolean;
}
