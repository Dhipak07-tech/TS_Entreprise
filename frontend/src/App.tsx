import { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import AppLayout from './layouts/AppLayout/AppLayout';
import LoginPage from './pages/Auth/LoginPage';
import RegisterPage from './pages/Auth/RegisterPage';
import DashboardPage from './pages/Dashboard/DashboardPage';
import UsersPage from './pages/Users/UsersPage';
import RolesPage from './pages/Roles/RolesPage';
import AuditPage from './pages/Audit/AuditPage';
import SettingsPage from './pages/Settings/SettingsPage';
import TicketsPage from './pages/Tickets/TicketsPage';
import TicketDetailPage from './pages/Tickets/TicketDetailPage';
import KnowledgeBasePage from './pages/KnowledgeBase/KnowledgeBasePage';
import ApprovalsPage from './pages/Approvals/ApprovalsPage';
import ServiceCatalogPage from './pages/Catalog/ServiceCatalogPage';
import ChangeManagementPage from './pages/Changes/ChangeManagementPage';
import AssetsPage from './pages/Assets/AssetsPage';
import MessagesPage from './pages/Messages/MessagesPage';
import SecurityPage from './pages/Security/SecurityPage';
import BillingPage from './pages/Billing/BillingPage';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function PublicRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <Navigate to="/app/dashboard" replace /> : <>{children}</>;
}

export default function App() {
  const { initialize } = useAuthStore();

  useEffect(() => {
    initialize();
  }, [initialize]);

  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<PublicRoute><LoginPage /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><RegisterPage /></PublicRoute>} />

        {/* Protected Routes */}
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="tickets" element={<TicketsPage />} />
          <Route path="tickets/:id" element={<TicketDetailPage />} />
          <Route path="messages" element={<MessagesPage />} />
          <Route path="knowledge" element={<KnowledgeBasePage />} />
          <Route path="changes" element={<ChangeManagementPage />} />
          <Route path="catalog" element={<ServiceCatalogPage />} />
          <Route path="approvals" element={<ApprovalsPage />} />
          <Route path="assets" element={<AssetsPage />} />
          <Route path="security" element={<SecurityPage />} />
          <Route path="billing" element={<BillingPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="roles" element={<RolesPage />} />
          <Route path="audit" element={<AuditPage />} />
          <Route path="settings" element={<SettingsPage />} />
          <Route index element={<Navigate to="dashboard" replace />} />
        </Route>

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
