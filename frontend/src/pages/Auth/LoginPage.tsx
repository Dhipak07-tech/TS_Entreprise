import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import { 
  Shield, 
  Zap, 
  TrendingUp, 
  User, 
  Headphones, 
  Users, 
  Settings, 
  UserCheck, 
  Lock, 
  Eye, 
  EyeOff, 
  ArrowRight,
  Sun,
  Moon,
  MessageSquare,
  Sparkles
} from 'lucide-react';
import './Auth.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const { setAuth } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [theme, setTheme] = useState<'dark' | 'light'>('dark');
  const [showMfa, setShowMfa] = useState(false);
  const [mfaCode, setMfaCode] = useState('');
  const [botOpen, setBotOpen] = useState(true);
  
  const [form, setForm] = useState({
    username: '',
    password: '',
  });

  const [showResetModal, setShowResetModal] = useState(false);
  const [resetForm, setResetForm] = useState({
    usernameOrEmail: '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [resetError, setResetError] = useState('');
  const [resetSuccess, setResetSuccess] = useState(false);

  useEffect(() => {
    const autoLogin = async () => {
      try {
        setLoading(true);
        const response = await authService.login({
          usernameOrEmail: 'admin',
          password: 'Password@123'
        });

        if (response.passwordResetRequired) {
          setResetForm({
            usernameOrEmail: 'admin',
            oldPassword: 'Password@123',
            newPassword: '',
            confirmPassword: ''
          });
          setShowResetModal(true);
          setLoading(false);
          return;
        }

        const backendPermissions = response.permissions || [];
        const extractedRoles = backendPermissions
          .filter((perm: string) => perm.startsWith('ROLE_'))
          .map((perm: string) => perm.substring(5));
        const roles = extractedRoles.length > 0 ? extractedRoles : ['USER'];
        const permissions = backendPermissions.filter((perm: string) => !perm.startsWith('ROLE_'));

        setAuth(
          {
            userId: (response as any).id || response.userId || 1,
            tenantId: 1, // default tenant
            username: response.username,
            email: response.email,
            firstName: response.username.toUpperCase(),
            lastName: 'USER',
            tenantName: 'ConnectIT Enterprise',
            roles: roles,
            permissions: permissions,
          },
          response.token
        );
        navigate('/app/dashboard');
      } catch (err: any) {
        console.error('Auto login failed:', err);
      } finally {
        setLoading(false);
      }
    };

    autoLogin();
  }, [navigate, setAuth]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Simulate MFA check for demo or invoke actual MFA if enabled
    if (form.username === 'admin' && !showMfa) {
      setTimeout(() => {
        setShowMfa(true);
        setLoading(false);
      }, 800);
      return;
    }

    try {
      const response = await authService.login({
        usernameOrEmail: form.username,
        password: form.password
      });

      if (response.passwordResetRequired) {
        setResetForm({
          usernameOrEmail: form.username,
          oldPassword: form.password,
          newPassword: '',
          confirmPassword: ''
        });
        setShowResetModal(true);
        setLoading(false);
        return;
      }

      const backendPermissions = response.permissions || [];
      const extractedRoles = backendPermissions
        .filter((perm: string) => perm.startsWith('ROLE_'))
        .map((perm: string) => perm.substring(5));
      const roles = extractedRoles.length > 0 ? extractedRoles : ['USER'];
      const permissions = backendPermissions.filter((perm: string) => !perm.startsWith('ROLE_'));

      setAuth(
        {
          userId: (response as any).id || response.userId || 1,
          tenantId: 1, // default tenant
          username: response.username,
          email: response.email,
          firstName: response.username.toUpperCase(),
          lastName: 'USER',
          tenantName: 'ConnectIT Enterprise',
          roles: roles,
          permissions: permissions,
        },
        response.token
      );
      navigate('/app/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setResetError('');
    
    if (resetForm.newPassword !== resetForm.confirmPassword) {
      setResetError('Passwords do not match.');
      setLoading(false);
      return;
    }
    if (resetForm.newPassword.length < 8) {
      setResetError('Password must be at least 8 characters long.');
      setLoading(false);
      return;
    }

    try {
      await authService.resetPassword({
        usernameOrEmail: resetForm.usernameOrEmail,
        oldPassword: resetForm.oldPassword,
        newPassword: resetForm.newPassword
      });
      setResetSuccess(true);
      setTimeout(() => {
        setShowResetModal(false);
        setResetSuccess(false);
        // Prompt user to log in with new password
        setForm({
          username: resetForm.usernameOrEmail,
          password: resetForm.newPassword
        });
      }, 1500);
    } catch (err: any) {
      setResetError(err.response?.data?.message || 'Failed to update password.');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAccess = (roleKey: string) => {
    // Fill credentials dynamically for demo ease
    let username = '';
    let password = 'Password@123';
    switch(roleKey) {
      case 'user': username = 'employee_user'; break;
      case 'agent': username = 'support_agent'; break;
      case 'lead': username = 'team_lead'; break;
      case 'admin': username = 'admin'; break;
      case 'super_admin': username = 'super_admin'; break;
      case 'ultra_admin': username = 'ultra_admin'; break;
    }
    setForm({ username, password });
    setShowMfa(false);
  };

  const toggleTheme = () => {
    const newTheme = theme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  return (
    <div className={`auth-page ${theme}`}>
      {/* Background orbs */}
      <div className="auth-background">
        <div className="bg-orb bg-orb-1" />
        <div className="bg-orb bg-orb-2" />
        <div className="bg-orb bg-orb-3" />
        <div className="bg-grid" />
      </div>

      {/* Theme Toggle Button */}
      <button className="theme-toggle-btn glass" onClick={toggleTheme} aria-label="Toggle Theme">
        {theme === 'dark' ? <Sun size={20} /> : <Moon size={20} />}
      </button>

      {/* Main Container Split Panel */}
      <div className="auth-split-layout glass animate-fade-in">
        
        {/* LEFT PANEL: Branding, highlights, Form */}
        <div className="left-panel">
          <div className="branding-section">
            <div className="brand-logo">
              <span>C</span>
            </div>
            <span className="brand-name">Connect IT</span>
          </div>

          <div className="welcome-header">
            <h1 className="welcome-title">Welcome back!</h1>
            <p className="welcome-subtitle">Sign in to your employee portal</p>
          </div>

          {/* Highlights */}
          <div className="highlights-row">
            <div className="highlight-item glass">
              <div className="highlight-icon"><Shield size={16} /></div>
              <div className="highlight-text">
                <h4>Enterprise Security</h4>
                <p>SSO & 2FA Protected</p>
              </div>
            </div>
            <div className="highlight-item glass">
              <div className="highlight-icon"><Sparkles size={16} /></div>
              <div className="highlight-text">
                <h4>Smart Ticketing</h4>
                <p>AI-Powered Support</p>
              </div>
            </div>
            <div className="highlight-item glass">
              <div className="highlight-icon"><TrendingUp size={16} /></div>
              <div className="highlight-text">
                <h4>Real-time Insights</h4>
                <p>Track & Resolve Faster</p>
              </div>
            </div>
          </div>

          {error && <div className="auth-error">{error}</div>}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="auth-form-redesign">
            {!showMfa ? (
              <>
                <div className="form-group-redesign">
                  <label className="form-label-redesign">Email address</label>
                  <input
                    type="text"
                    className="form-input-redesign glass"
                    placeholder="name@company.com"
                    value={form.username}
                    onChange={(e) => setForm({ ...form, username: e.target.value })}
                    required
                    autoFocus
                  />
                </div>

                <div className="form-group-redesign">
                  <div className="label-row">
                    <label className="form-label-redesign">Password</label>
                    <a href="#forgot" className="forgot-link">Forgot password?</a>
                  </div>
                  <div className="password-input-wrapper">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      className="form-input-redesign glass"
                      placeholder="••••••••"
                      value={form.password}
                      onChange={(e) => setForm({ ...form, password: e.target.value })}
                      required
                    />
                    <button
                      type="button"
                      className="password-toggle-btn"
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                </div>

                <div className="remember-sso-row">
                  <label className="checkbox-container">
                    <input type="checkbox" />
                    <span className="checkmark"></span>
                    Remember me
                  </label>
                  <a href="#sso" className="sso-link">Use Single Sign-On</a>
                </div>
              </>
            ) : (
              <div className="mfa-section animate-slide-up">
                <div className="mfa-header">
                  <Lock size={28} className="mfa-icon" />
                  <h4>Two-Factor Authentication</h4>
                  <p>Enter the 6-digit authentication code from your authenticator app.</p>
                </div>
                <div className="form-group-redesign">
                  <input
                    type="text"
                    className="form-input-redesign glass mfa-input"
                    placeholder="000 000"
                    maxLength={6}
                    value={mfaCode}
                    onChange={(e) => setMfaCode(e.target.value)}
                    required
                    autoFocus
                  />
                </div>
                <button type="button" className="back-to-login" onClick={() => setShowMfa(false)}>
                  Back to credentials
                </button>
              </div>
            )}

            <button
              type="submit"
              className="submit-btn-premium"
              disabled={loading}
            >
              {loading ? (
                <span className="btn-loader" />
              ) : (
                <>
                  <span>{showMfa ? 'Verify & Sign In' : 'Sign In'}</span>
                  <ArrowRight size={18} />
                </>
              )}
            </button>
          </form>

          {/* SSO Options */}
          <div className="sso-divider">
            <span>or continue with</span>
          </div>

          <div className="sso-buttons">
            <button className="sso-provider-btn glass">
              <img src="https://auth.globus.org/static/images/microsoft_logo.png" alt="Microsoft" />
              <span>Microsoft</span>
            </button>
            <button className="sso-provider-btn glass">
              <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" />
              <span>Google</span>
            </button>
            <button className="sso-provider-btn glass">
              <img src="https://upload.wikimedia.org/wikipedia/commons/7/7e/Okta_logo.svg" alt="Okta" />
              <span>Okta</span>
            </button>
          </div>

          <div className="left-panel-footer">
            <span>New to Connect IT?</span> <a href="#request">Request Access</a>
          </div>
        </div>

        {/* RIGHT PANEL: Quick Access Cards & Metrics */}
        <div className="right-panel">
          <div className="quick-access-section">
            <div className="quick-access-title-row">
              <span className="rocket-emoji">🚀</span>
              <h3>Quick Access</h3>
            </div>
            <p className="quick-access-subtitle">Select a role to sign in instantly</p>

            <div className="role-cards-container">
              {/* User */}
              <div className="role-card glass" onClick={() => handleQuickAccess('user')}>
                <div className="role-card-icon role-user"><User size={18} /></div>
                <div className="role-card-info">
                  <h4>User</h4>
                  <p>End user — raise & track tickets</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>

              {/* Support Agent */}
              <div className="role-card glass" onClick={() => handleQuickAccess('agent')}>
                <div className="role-card-icon role-agent"><Headphones size={18} /></div>
                <div className="role-card-info">
                  <h4>Support Agent</h4>
                  <p>Support agent — manage incidents</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>

              {/* Team Lead */}
              <div className="role-card glass" onClick={() => handleQuickAccess('lead')}>
                <div className="role-card-icon role-lead"><Users size={18} /></div>
                <div className="role-card-info">
                  <h4>Team Lead</h4>
                  <p>Team Lead — manage team ticket assignment</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>

              {/* Administrator */}
              <div className="role-card glass" onClick={() => handleQuickAccess('admin')}>
                <div className="role-card-icon role-admin"><Settings size={18} /></div>
                <div className="role-card-info">
                  <h4>Administrator</h4>
                  <p>Manage users, SLA & approvals</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>

              {/* Super Admin */}
              <div className="role-card glass" onClick={() => handleQuickAccess('super_admin')}>
                <div className="role-card-icon role-super"><UserCheck size={18} /></div>
                <div className="role-card-info">
                  <h4>Super Admin</h4>
                  <p>Manage dropdowns & system config</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>

              {/* Ultra Super Admin */}
              <div className="role-card glass" onClick={() => handleQuickAccess('ultra_admin')}>
                <div className="role-card-icon role-ultra"><Sparkles size={18} /></div>
                <div className="role-card-info">
                  <h4>Ultra Super Admin</h4>
                  <p>Full control — grant/remove all access</p>
                </div>
                <div className="card-arrow"><ArrowRight size={14} /></div>
              </div>
            </div>
          </div>

          {/* Operational Metrics */}
          <div className="metrics-dashboard glass">
            <div className="metric-box">
              <span className="metric-value">1,248</span>
              <span className="metric-label">Open Tickets</span>
              <span className="metric-trend positive">+12% vs yesterday</span>
            </div>
            <div className="metric-box">
              <span className="metric-value">324</span>
              <span className="metric-label">Resolved Today</span>
              <span className="metric-trend positive">+8% vs yesterday</span>
            </div>
            <div className="metric-box">
              <span className="metric-value">98.7%</span>
              <span className="metric-label">SLA Compliance</span>
              <span className="metric-trend status-excellent">Excellent</span>
            </div>
          </div>

          <div className="connection-status-badge glass">
            <div className="status-badge-icon">
              <Lock size={14} />
            </div>
            <div className="status-badge-text">
              <h4>Your connection is secure</h4>
              <p>Protected by enterprise-grade encryption</p>
            </div>
            <span className="ssl-badge">SSL Secured ✓</span>
          </div>
        </div>
      </div>

      {/* Floating chatbot assistant */}
      {botOpen && (
        <div className="assistant-floating-bot glass animate-fade-in">
          <button className="bot-close-btn" onClick={() => setBotOpen(false)}>×</button>
          <div className="bot-header">
            <div className="bot-avatar">🤖</div>
            <div className="bot-title">
              <h4>TECHNOSPRINT PET</h4>
              <span className="online-indicator"></span>
            </div>
          </div>
          <div className="bot-body">
            <p>Technosprint Pet is at your service! ⚡</p>
          </div>
        </div>
      )}
      {!botOpen && (
        <button className="bot-launcher glass" onClick={() => setBotOpen(true)}>
          <MessageSquare size={24} />
        </button>
      )}
      {/* Forced Password Reset Modal */}
      {showResetModal && (
        <div className="reset-modal-overlay">
          <div className="modal-container glass animate-scale-in">
            <div className="modal-header">
              <Lock className="modal-icon text-amber-500" size={24} style={{ color: '#f59e0b', marginBottom: '8px' }} />
              <h3>Password Reset Required</h3>
              <p>For security, you must change your temporary password before proceeding.</p>
            </div>
            
            {resetError && <div className="auth-error" style={{ marginBottom: '16px' }}>{resetError}</div>}
            {resetSuccess && <div className="auth-success">Password updated successfully!</div>}
            
            <form onSubmit={handleResetSubmit} className="modal-form">
              <div className="form-group-redesign">
                <label className="form-label-redesign">New Password</label>
                <input
                  type="password"
                  className="form-input-redesign glass"
                  placeholder="Minimum 8 characters"
                  value={resetForm.newPassword}
                  onChange={(e) => setResetForm({ ...resetForm, newPassword: e.target.value })}
                  required
                  autoFocus
                />
              </div>
              <div className="form-group-redesign">
                <label className="form-label-redesign">Confirm New Password</label>
                <input
                  type="password"
                  className="form-input-redesign glass"
                  placeholder="Re-enter password"
                  value={resetForm.confirmPassword}
                  onChange={(e) => setResetForm({ ...resetForm, confirmPassword: e.target.value })}
                  required
                />
              </div>
              <div className="modal-actions">
                <button
                  type="button"
                  className="btn-secondary glass"
                  onClick={() => setShowResetModal(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="submit-btn-premium"
                  disabled={loading}
                >
                  {loading ? <span className="btn-loader" /> : 'Update Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
