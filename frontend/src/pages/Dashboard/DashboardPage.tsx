import { useEffect, useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import { dashboardService } from '../../services/dataService';
import { useNavigate } from 'react-router-dom';
import {
  Ticket, Calendar, BookOpen, Settings
} from 'lucide-react';
import './Dashboard.css';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await dashboardService.getStats();
      setStats(data);
    } catch (err) {
      console.error('Failed to load stats:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="personal-dashboard-container animate-fade-in">
      {/* Header Row */}
      <div className="dashboard-header-row">
        <div>
          <h1 className="dashboard-title">Personal Dashboard</h1>
          <p className="dashboard-subtitle">Real-time performance metrics and active tasks</p>
        </div>
        <div className="operator-badge">
          Operator: <span className="operator-email">{user?.email || 'admin@connectit.com'}</span>
        </div>
      </div>

      {/* Quick Shortcuts Section */}
      <div className="quick-shortcuts-section">
        <h3 className="shortcuts-title">QUICK SHORTCUTS</h3>
        <div className="shortcuts-grid">
          <div className="shortcut-card" onClick={() => navigate('/app/tickets')}>
            <div className="shortcut-icon-wrapper text-blue">
              <Ticket size={18} />
            </div>
            <div className="shortcut-details">
              <h4>View Tickets</h4>
              <p>Manage your assigned tickets</p>
            </div>
          </div>

          <div className="shortcut-card" onClick={() => navigate('/app/calendar')}>
            <div className="shortcut-icon-wrapper text-blue">
              <Calendar size={18} />
            </div>
            <div className="shortcut-details">
              <h4>View Calendar</h4>
              <p>See scheduled tasks & outages</p>
            </div>
          </div>

          <div className="shortcut-card" onClick={() => navigate('/app/knowledge')}>
            <div className="shortcut-icon-wrapper text-yellow">
              <BookOpen size={18} />
            </div>
            <div className="shortcut-details">
              <h4>Knowledge Base</h4>
              <p>Browse articles & guides</p>
            </div>
          </div>

          <div className="shortcut-card" onClick={() => navigate('/app/settings')}>
            <div className="shortcut-icon-wrapper text-blue">
              <Settings size={18} />
            </div>
            <div className="shortcut-details">
              <h4>System Settings</h4>
              <p>Manage your preferences</p>
            </div>
          </div>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="metrics-section">
        {loading ? (
          <div className="metrics-loading">Loading live metrics...</div>
        ) : (
          <>
            <div className="metrics-grid">
              {/* Row 1 */}
              <div className="metric-card border-accent-blue">
                <span className="metric-label">TOTAL INCIDENTS ASSIGNED</span>
                <span className="metric-value">{stats?.totalIncidentsAssigned ?? 0}</span>
              </div>
              <div className="metric-card border-accent-blue">
                <span className="metric-label">TOTAL INCIDENTS CREATED</span>
                <span className="metric-value">{stats?.totalIncidentsCreated ?? 0}</span>
              </div>
              <div className="metric-card border-accent-blue">
                <span className="metric-label">OPEN INCIDENTS</span>
                <span className="metric-value">{stats?.openIncidents ?? 0}</span>
              </div>
              <div className="metric-card border-accent-orange">
                <span className="metric-label">IN PROGRESS INCIDENTS</span>
                <span className="metric-value">{stats?.inProgressIncidents ?? 0}</span>
              </div>

              {/* Row 2 */}
              <div className="metric-card border-accent-blue">
                <span className="metric-label">RESOLVED INCIDENTS</span>
                <span className="metric-value">{stats?.resolvedIncidents ?? 0}</span>
              </div>
              <div className="metric-card border-accent-green">
                <span className="metric-label">CLOSED INCIDENTS</span>
                <span className="metric-value">{stats?.closedIncidents ?? 0}</span>
              </div>
              <div className="metric-card border-accent-blue">
                <span className="metric-label">PENDING INCIDENTS</span>
                <span className="metric-value">{stats?.pendingIncidents ?? 0}</span>
              </div>
              <div className="metric-card border-accent-red">
                <span className="metric-label">OVERDUE INCIDENTS</span>
                <span className="metric-value">{stats?.overdueIncidents ?? 0}</span>
              </div>
            </div>

            {/* Row 3 */}
            <div className="metrics-single-row">
              <div className="metric-card border-accent-pink">
                <span className="metric-label">TOTAL SLA BREACHES</span>
                <span className="metric-value">{stats?.totalSlaBreaches ?? 0}</span>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
