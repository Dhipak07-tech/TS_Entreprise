import api from './api';
import type { ApiResponse, UserDTO, PageResponse, DashboardStats } from '../types';

export const userService = {
  async getUsers(page = 0, size = 20): Promise<PageResponse<UserDTO>> {
    const response = await api.get<ApiResponse<PageResponse<UserDTO>>>(`/users?page=${page}&size=${size}`);
    return response.data.data;
  },

  async getUser(id: number): Promise<UserDTO> {
    const response = await api.get<ApiResponse<UserDTO>>(`/users/${id}`);
    return response.data.data;
  },

  async createUser(data: any): Promise<UserDTO> {
    const response = await api.post<ApiResponse<UserDTO>>('/users', data);
    return response.data.data;
  },

  async updateUser(id: number, data: any): Promise<UserDTO> {
    const response = await api.put<ApiResponse<UserDTO>>(`/users/${id}`, data);
    return response.data.data;
  },

  async toggleUserStatus(id: number): Promise<void> {
    await api.patch(`/users/${id}/toggle-status`);
  },
};

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const response = await api.get<ApiResponse<DashboardStats>>('/dashboard/stats');
    return response.data.data;
  },
};

export const ticketService = {
  async getTickets(page = 0, size = 20, status?: string, search?: string): Promise<any> {
    let url = `/tickets?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    const response = await api.get<ApiResponse<any>>(url);
    return response.data.data;
  },

  async getMyTickets(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/tickets');
    return response.data.data;
  },

  async createTicket(data: any): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/tickets', data);
    return response.data.data;
  },

  async updateStatus(id: number, status: string): Promise<any> {
    const response = await api.put<ApiResponse<any>>(`/tickets/${id}/status?status=${status}`);
    return response.data.data;
  },

  async assignTicket(id: number, teamId?: number, agentId?: number): Promise<any> {
    const query = [
      teamId ? `teamId=${teamId}` : '',
      agentId ? `agentId=${agentId}` : ''
    ].filter(Boolean).join('&');
    const response = await api.put<ApiResponse<any>>(`/tickets/${id}/assign?${query}`);
    return response.data.data;
  },

  async getTicketDetail(id: number): Promise<any> {
    const response = await api.get<ApiResponse<any>>(`/tickets/${id}`);
    return response.data.data;
  },

  async getComments(id: number): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>(`/tickets/${id}/comments`);
    return response.data.data;
  },

  async addComment(id: number, body: string, isInternal: boolean): Promise<any> {
    const response = await api.post<ApiResponse<any>>(`/tickets/${id}/comments`, { body, isInternal });
    return response.data.data;
  },

  async getActivities(id: number): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>(`/tickets/${id}/activities`);
    return response.data.data;
  }
};

export const incidentService = {
  async getIncidents(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/incidents');
    return response.data.data;
  },

  async createIncident(data: { impact: string; urgency: string; category: string; subcategory: string; ticketIds: number[] }): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/incidents', data);
    return response.data.data;
  }
};

export const problemService = {
  async getProblems(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/problems');
    return response.data.data;
  },

  async createProblem(data: { title: string; description: string; incidentIds: number[] }): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/problems', data);
    return response.data.data;
  },

  async updateInvestigation(id: number, data: { rootCause: string; workaround: string; status: string }): Promise<any> {
    const response = await api.put<ApiResponse<any>>(`/problems/${id}/investigation`, data);
    return response.data.data;
  }
};

export const changeService = {
  async getChanges(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/changes');
    return response.data.data;
  },

  async createChange(data: {
    title: string;
    description: string;
    changeType: string;
    riskLevel: string;
    rollbackPlan: string;
    testPlan: string;
    plannedStart: string;
    plannedEnd: string;
    ticketIds: number[];
  }): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/changes', data);
    return response.data.data;
  },

  async updateStatus(id: number, status: string): Promise<any> {
    const response = await api.put<ApiResponse<any>>(`/changes/${id}/status?status=${status}`);
    return response.data.data;
  }
};

export const catalogService = {
  async getCatalog(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/catalog');
    return response.data.data;
  },

  async raiseRequest(data: { itemId: number; quantity: number }): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/catalog/request', data);
    return response.data.data;
  }
};

export const kbService = {
  async getArticles(page = 0, size = 20, status?: string): Promise<PageResponse<any>> {
    const url = status ? `/knowledge/articles?page=${page}&size=${size}&status=${status}` : `/knowledge/articles?page=${page}&size=${size}`;
    const response = await api.get<ApiResponse<PageResponse<any>>>(url);
    return response.data.data;
  },

  async searchArticles(query: string, page = 0, size = 20): Promise<PageResponse<any>> {
    const response = await api.get<ApiResponse<PageResponse<any>>>(`/knowledge/articles/search?query=${query}&page=${page}&size=${size}`);
    return response.data.data;
  },

  async getCategories(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/knowledge/categories');
    return response.data.data;
  }
};

export const approvalService = {
  async getPendingApprovals(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/approvals/pending');
    return response.data.data;
  },

  async makeDecision(id: number, decision: string, remarks?: string): Promise<any> {
    const response = await api.post<ApiResponse<any>>(`/approvals/${id}/decide`, { decision, remarks });
    return response.data.data;
  }
};

export const assetService = {
  async getAssets(page = 0, size = 20): Promise<PageResponse<any>> {
    const response = await api.get<ApiResponse<PageResponse<any>>>(`/cmdb/assets?page=${page}&size=${size}`);
    return response.data.data;
  },

  async createAsset(data: any): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/cmdb/assets', data);
    return response.data.data;
  }
};

export const grcService = {
  async getSecurityIncidents(page = 0, size = 20): Promise<PageResponse<any>> {
    const response = await api.get<ApiResponse<PageResponse<any>>>(`/grc/security-incidents?page=${page}&size=${size}`);
    return response.data.data;
  },

  async createSecurityIncident(data: any): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/grc/security-incidents', data);
    return response.data.data;
  },

  async getSecurityStats(): Promise<any> {
    const response = await api.get<ApiResponse<any>>('/grc/security-incidents/stats');
    return response.data.data;
  }
};

export const billingService = {
  async getSubscription(): Promise<any> {
    const response = await api.get<ApiResponse<any>>('/saas/billing/subscription');
    return response.data.data;
  },

  async upgradePlan(tier: string): Promise<any> {
    const response = await api.post<ApiResponse<any>>(`/saas/billing/subscription/upgrade?tier=${tier}`);
    return response.data.data;
  }
};

export const teamService = {
  async getTeams(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/teams');
    return response.data.data;
  },
  async getTeamMembers(teamId: number): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>(`/teams/${teamId}/members`);
    return response.data.data;
  }
};

export const rbacService = {
  async getRoles(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/rbac/roles');
    return response.data.data;
  },
  async getPermissions(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/rbac/permissions');
    return response.data.data;
  },
  async updateRolePermissions(roleId: number, permissionKeys: string[]): Promise<any> {
    const response = await api.put<ApiResponse<any>>(`/rbac/roles/${roleId}/permissions`, { permissionKeys });
    return response.data.data;
  },
  async getMenu(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/rbac/menu');
    return response.data.data;
  },
  async getPagePermissionMatrix(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/rbac/matrix');
    return response.data.data;
  },
  async updatePagePermissionMatrix(matrix: any[]): Promise<void> {
    await api.put<ApiResponse<void>>('/rbac/matrix', matrix);
  }
};

export const auditService = {
  async getAuditLogs(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/audit/activities');
    return response.data.data;
  },
  async getLoginLogs(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/audit/logins');
    return response.data.data;
  }
};

export const systemSettingsService = {
  async getSettings(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/settings');
    return response.data.data;
  },
  async updateSettings(settings: Record<string, string>): Promise<void> {
    await api.put<ApiResponse<void>>('/settings', settings);
  }
};

export const employeeService = {
  async getEmployees(page = 0, size = 20): Promise<any> {
    const response = await api.get<ApiResponse<PageResponse<any>>>(`/employees?page=${page}&size=${size}`);
    return response.data.data;
  },
  async getUnprovisionedEmployees(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/employees/unprovisioned');
    return response.data.data;
  },
  async getEmployeeById(id: number): Promise<any> {
    const response = await api.get<ApiResponse<any>>(`/employees/${id}`);
    return response.data.data;
  },
  async createEmployee(data: any): Promise<any> {
    const response = await api.post<ApiResponse<any>>('/employees', data);
    return response.data.data;
  },
  async updateEmployee(id: number, data: any): Promise<any> {
    const response = await api.put<ApiResponse<any>>(`/employees/${id}`, data);
    return response.data.data;
  },
  async provisionUser(data: any): Promise<void> {
    await api.post<ApiResponse<void>>('/employees/provision', data);
  }
};

export const departmentService = {
  async getDepartments(): Promise<any[]> {
    const response = await api.get<ApiResponse<any[]>>('/departments');
    return response.data.data;
  }
};

