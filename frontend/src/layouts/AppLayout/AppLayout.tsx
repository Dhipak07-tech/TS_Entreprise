import { useState, useEffect } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import api from '../../services/api';
import { rbacService } from '../../services/dataService';
import {
  LayoutDashboard, Users, Shield, ScrollText, Settings,
  ChevronLeft, ChevronRight, LogOut, Bell, Search,
  Building2, Menu, Zap, Ticket, BookOpen, CheckCircle2, Laptop, MessageSquare, ShieldAlert, CreditCard,
  GitPullRequest, ShoppingBag, Sun, Moon
} from 'lucide-react';
import './AppLayout.css';

interface NotificationDTO {
  id: number;
  title: string;
  message: string;
  isRead: boolean;
  type: string;
  sentAt: string;
}

const navItems = [
  { label: 'Dashboard', icon: LayoutDashboard, path: '/app/dashboard', pageId: 1001 },
  { label: 'Tickets', icon: Ticket, path: '/app/tickets', pageId: 1002 },
  { label: 'Messages', icon: MessageSquare, path: '/app/messages', pageId: null },
  { label: 'Knowledge Base', icon: BookOpen, path: '/app/knowledge', pageId: 1008 },
  { label: 'Change Control', icon: GitPullRequest, path: '/app/changes', pageId: 1006 },
  { label: 'Service Catalog', icon: ShoppingBag, path: '/app/catalog', pageId: 1003 },
  { label: 'Approvals', icon: CheckCircle2, path: '/app/approvals', pageId: 1002 },
  { label: 'Assets & CMDB', icon: Laptop, path: '/app/assets', pageId: 1007 },
  { label: 'SecOps & GRC', icon: ShieldAlert, path: '/app/security', pageId: 1004 },
  { label: 'Billing & Plan', icon: CreditCard, path: '/app/billing', pageId: null },
  { label: 'Users', icon: Users, path: '/app/users', pageId: 1009 },
  { label: 'Roles & Access', icon: Shield, path: '/app/roles', pageId: 1010 },
  { label: 'Audit Trail', icon: ScrollText, path: '/app/audit', pageId: 1010 },
  { label: 'Settings', icon: Settings, path: '/app/settings', pageId: 1010 },
];

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [permittedPageIds, setPermittedPageIds] = useState<Set<number> | null>(null);

  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    return (localStorage.getItem('ticklora_theme') as 'light' | 'dark') || 'light';
  });

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark-theme');
    } else {
      document.documentElement.classList.remove('dark-theme');
    }
    localStorage.setItem('ticklora_theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  useEffect(() => {
    // Load notifications initially
    const loadNotifications = async () => {
      try {
        const response = await api.get('/notifications');
        if (response.data.success) {
          setNotifications(response.data.data);
        }
      } catch (err) {
        console.error('Failed to load notifications:', err);
      }
    };
    loadNotifications();

    // Fetch dynamic menu permissions
    const fetchMenu = async () => {
      try {
        const pages = await rbacService.getMenu();
        const ids = new Set<number>(pages.map((p: any) => p.pageId));
        setPermittedPageIds(ids);
        (window as any).permittedPageIds = ids;
      } catch (err) {
        console.error('Failed to fetch menu:', err);
        // Fallback
        const fallbackIds = new Set<number>([1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010]);
        setPermittedPageIds(fallbackIds);
        (window as any).permittedPageIds = fallbackIds;
      }
    };
    fetchMenu();

    // Subscribe to SSE
    const token = localStorage.getItem('ticklora_token');
    if (token) {
      const eventSource = new EventSource(`/api/notifications/subscribe?token=${token}`);

      eventSource.addEventListener('NOTIFICATION', (event: any) => {
        try {
          const newNotif = JSON.parse(event.data);
          setNotifications(prev => [newNotif, ...prev]);
        } catch (err) {
          console.error('Failed to parse SSE notification:', err);
        }
      });

      eventSource.onerror = (err) => {
        console.error('EventSource error:', err);
        eventSource.close();
      };

      return () => {
        eventSource.close();
      };
    }
  }, []);

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.put(`/notifications/${id}/read`);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    } catch (err) {
      console.error('Failed to mark notification as read:', err);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await api.put('/notifications/read-all');
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
    } catch (err) {
      console.error('Failed to mark all notifications as read:', err);
    }
  };

  const unreadCount = notifications.filter(n => !n.isRead).length;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const initials = user
    ? `${(user.firstName?.[0] || user.username[0]).toUpperCase()}${(user.lastName?.[0] || '').toUpperCase()}`
    : '?';

  return (
    <div className="app-layout">
      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="sidebar-overlay" onClick={() => setMobileOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`sidebar ${collapsed ? 'collapsed' : ''} ${mobileOpen ? 'mobile-open' : ''}`}>
        <div className="sidebar-header">
          <div className="sidebar-brand">
            <div className="brand-icon">
              <Zap size={22} />
            </div>
            {!collapsed && <span className="brand-text">Ticklora</span>}
          </div>
          <button
            className="collapse-btn"
            onClick={() => setCollapsed(!collapsed)}
            title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section">
            {!collapsed && <div className="nav-section-title">Main Menu</div>}
            {navItems
              .filter(item => item.pageId === null || (permittedPageIds && permittedPageIds.has(item.pageId)))
              .map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                  onClick={() => setMobileOpen(false)}
                  title={item.label}
                >
                  <item.icon size={20} />
                  {!collapsed && <span>{item.label}</span>}
                </NavLink>
              ))}
          </div>
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-action-item" onClick={toggleTheme} title={theme === 'light' ? 'Switch to Dark Cyber Mode' : 'Switch to Light Office Mode'}>
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            {!collapsed && <span>{theme === 'light' ? 'Dark Cyber Mode' : 'Light Office Mode'}</span>}
          </div>
          <div className="sidebar-action-item logout" onClick={handleLogout} title="System Logout">
            <LogOut size={18} />
            {!collapsed && <span>System Logout</span>}
          </div>
          <div className="user-card" title={user?.email || ''}>
            <div className="user-avatar">{initials}</div>
            {!collapsed && (
              <div className="user-info">
                <div className="user-name">{user?.firstName || user?.username}</div>
                <div className="user-role">{user?.roles?.[0] || 'User'}</div>
              </div>
            )}
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className={`main-wrapper ${collapsed ? 'expanded' : ''}`}>
        {/* Top Bar */}
        <header className="topbar">
          <div className="topbar-left">
            <button className="mobile-menu-btn" onClick={() => setMobileOpen(true)}>
              <Menu size={20} />
            </button>
            <div className="search-container">
              <Search size={16} className="search-icon" />
              <input
                type="text"
                placeholder="Search anything..."
                className="search-input"
              />
            </div>
          </div>
          <div className="topbar-right">
            <div className="tenant-badge">
              <Building2 size={14} />
              <span>{user?.tenantName || 'Organization'}</span>
            </div>
            <button className="topbar-icon-btn" title="Notifications" onClick={() => setShowNotifications(!showNotifications)}>
              <Bell size={18} />
              {unreadCount > 0 && <span className="notification-dot" />}
            </button>
            {showNotifications && (
              <div className="notifications-dropdown">
                <div className="notifications-header">
                  <h4>Notifications ({unreadCount})</h4>
                  {unreadCount > 0 && (
                    <button className="mark-all-read-btn" onClick={handleMarkAllAsRead}>
                      Mark all as read
                    </button>
                  )}
                </div>
                <div className="notifications-list">
                  {notifications.length === 0 ? (
                    <div className="notifications-empty">
                      <Bell size={28} />
                      <span>No notifications yet</span>
                    </div>
                  ) : (
                    notifications.map(n => (
                      <div
                        key={n.id}
                        className={`notification-item ${!n.isRead ? 'unread' : ''}`}
                        onClick={() => handleMarkAsRead(n.id)}
                      >
                        <span className="notification-title">{n.title}</span>
                        <span className="notification-message">{n.message}</span>
                        <span className="notification-time">{new Date(n.sentAt).toLocaleTimeString()}</span>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
            <button className="topbar-icon-btn logout-btn" onClick={handleLogout} title="Logout">
              <LogOut size={18} />
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="page-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
