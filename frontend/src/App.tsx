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

import { PageGuard } from './components/PageGuard';

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
          <Route path="dashboard" element={<PageGuard pageId={1001}><DashboardPage /></PageGuard>} />
          <Route path="tickets" element={<PageGuard pageId={1002}><TicketsPage /></PageGuard>} />
          <Route path="tickets/:id" element={<PageGuard pageId={1002}><TicketDetailPage /></PageGuard>} />
          <Route path="messages" element={<MessagesPage />} />
          <Route path="knowledge" element={<PageGuard pageId={1008}><KnowledgeBasePage /></PageGuard>} />
          <Route path="changes" element={<PageGuard pageId={1006}><ChangeManagementPage /></PageGuard>} />
          <Route path="catalog" element={<PageGuard pageId={1003}><ServiceCatalogPage /></PageGuard>} />
          <Route path="approvals" element={<PageGuard pageId={1002}><ApprovalsPage /></PageGuard>} />
          <Route path="assets" element={<PageGuard pageId={1007}><AssetsPage /></PageGuard>} />
          <Route path="security" element={<PageGuard pageId={1004}><SecurityPage /></PageGuard>} />
          <Route path="billing" element={<BillingPage />} />
          <Route path="users" element={<PageGuard pageId={1009}><UsersPage /></PageGuard>} />
          <Route path="roles" element={<PageGuard pageId={1010}><RolesPage /></PageGuard>} />
          <Route path="audit" element={<PageGuard pageId={1010}><AuditPage /></PageGuard>} />
          <Route path="settings" element={<PageGuard pageId={1010}><SettingsPage /></PageGuard>} />
          <Route index element={<Navigate to="dashboard" replace />} />
        </Route>

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
