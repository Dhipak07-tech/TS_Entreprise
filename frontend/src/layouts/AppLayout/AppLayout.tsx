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

interface MenuConfigDTO {
  id: number;
  name: string;
  path: string;
  icon?: string;
  parentId?: number;
  page?: { pageId: number; name: string };
  sortOrder: number;
  children: MenuConfigDTO[];
}

const iconMap: Record<string, React.ComponentType<any>> = {
  LayoutDashboard,
  Ticket,
  MessageSquare,
  BookOpen,
  GitBranch: GitPullRequest,
  GitPullRequest,
  ShoppingBag,
  CheckCircle2,
  Laptop,
  ShieldAlert,
  CreditCard,
  Users,
  Shield,
  ScrollText,
  Settings,
  AlertTriangle: ShieldAlert,
  FileSearch2: ScrollText,
  HardDrive: Laptop
};

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [notifications, setNotifications] = useState<NotificationDTO[]>([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const [menuItems, setMenuItems] = useState<MenuConfigDTO[]>([]);

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

    // Fetch dynamic menu configurations
    const fetchMenuConfigs = async () => {
      try {
        const response = await api.get('/rbac/menu-configurations');
        if (response.data.success) {
          // Sort items by sortOrder
          const sorted = response.data.data.sort((a: any, b: any) => a.sortOrder - b.sortOrder);
          // Recursively sort children
          sorted.forEach((item: any) => {
            if (item.children) {
              item.children.sort((a: any, b: any) => a.sortOrder - b.sortOrder);
            }
          });
          setMenuItems(sorted);
        }
      } catch (err) {
        console.error('Failed to fetch dynamic menu configurations:', err);
        // Fallback to local hardcoded configurations if API fails
        const fallbackMenus: MenuConfigDTO[] = [
          { id: 1, name: 'Dashboard', path: '/app/dashboard', icon: 'LayoutDashboard', sortOrder: 1, children: [] },
          { id: 2, name: 'Tickets', path: '/app/tickets', icon: 'Ticket', sortOrder: 2, children: [] },
          { id: 3, name: 'Knowledge Base', path: '/app/knowledge', icon: 'BookOpen', sortOrder: 7, children: [] },
          { id: 4, name: 'Users', path: '/app/users', icon: 'Users', sortOrder: 8, children: [] },
          { id: 5, name: 'Settings', path: '/app/settings', icon: 'Settings', sortOrder: 9, children: [] },
        ];
        setMenuItems(fallbackMenus);
      }
    };
    fetchMenuConfigs();

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
            {menuItems.map((item) => {
              const IconComponent = iconMap[item.icon || ''] || Settings;
              return (
                <div key={item.id} className="nav-item-group" style={{ marginBottom: '0.5rem' }}>
                  <NavLink
                    to={item.path}
                    className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    onClick={() => setMobileOpen(false)}
                    title={item.name}
                  >
                    <IconComponent size={20} />
                    {!collapsed && <span>{item.name}</span>}
                  </NavLink>
                  {/* Nested/Child configurations */}
                  {!collapsed && item.children && item.children.length > 0 && (
                    <div className="nav-item-children" style={{ paddingLeft: '1.25rem', marginTop: '0.25rem', display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                      {item.children.map((child) => {
                        const ChildIcon = iconMap[child.icon || ''] || Settings;
                        return (
                          <NavLink
                            key={child.id}
                            to={child.path}
                            className={({ isActive }) => `nav-item child-nav-item ${isActive ? 'active' : ''}`}
                            onClick={() => setMobileOpen(false)}
                            title={child.name}
                            style={{ fontSize: '0.825rem', padding: '0.35rem 0.75rem', opacity: 0.85 }}
                          >
                            <ChildIcon size={15} />
                            <span>{child.name}</span>
                          </NavLink>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
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
