import api from './api';
import type { LoginRequest, RegisterRequest, AuthResponse, ApiResponse, ResetPasswordRequest } from '../types';

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', data);
    return response.data.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return response.data.data;
  },

  async resetPassword(data: ResetPasswordRequest): Promise<void> {
    await api.post<ApiResponse<void>>('/auth/reset-password', data);
  },
};
