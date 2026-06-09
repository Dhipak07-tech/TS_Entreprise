import React, { useEffect, useState } from 'react';
import { auditService } from '../../services/dataService';
import { ScrollText, ShieldAlert, Key, Search, RefreshCw, Eye, EyeOff } from 'lucide-react';

interface AuditLog {
  id: number;
  username: string;
  userEmail: string | null;
  action: string;
  entityName: string;
  entityId: number;
  oldValues: string | null;
  newValues: string | null;
  timestamp: string;
}

interface LoginLog {
  id: number;
  username: string;
  userEmail: string;
  ipAddress: string;
  userAgent: string;
  mfaVerified: boolean;
  timestamp: string;
}

export default function AuditPage() {
  const [activeTab, setActiveTab] = useState<'activities' | 'logins'>('activities');
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loginLogs, setLoginLogs] = useState<LoginLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Search / Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedRow, setExpandedRow] = useState<number | null>(null);

  useEffect(() => {
    loadLogs();
  }, [activeTab]);

  const loadLogs = async () => {
    setLoading(true);
    setError(null);
    try {
      if (activeTab === 'activities') {
        const data = await auditService.getAuditLogs();
        setAuditLogs(data || []);
      } else {
        const data = await auditService.getLoginLogs();
        setLoginLogs(data || []);
      }
    } catch (err) {
      console.error('Failed to load audit logs:', err);
      setError('Failed to retrieve audit log data. Please verify your administrative permissions.');
    } finally {
      setLoading(false);
    }
  };

  const getBrowserName = (userAgent: string) => {
    if (!userAgent) return 'Unknown';
    if (userAgent.includes('Firefox')) return 'Mozilla Firefox';
    if (userAgent.includes('Chrome')) return 'Google Chrome';
    if (userAgent.includes('Safari') && !userAgent.includes('Chrome')) return 'Apple Safari';
    if (userAgent.includes('Edge')) return 'Microsoft Edge';
    return 'Web Browser / API Client';
  };

  const filteredAudits = auditLogs.filter(log => {
    const q = searchQuery.toLowerCase();
    return (
      log.username.toLowerCase().includes(q) ||
      log.action.toLowerCase().includes(q) ||
      log.entityName.toLowerCase().includes(q) ||
      (log.oldValues && log.oldValues.toLowerCase().includes(q)) ||
      (log.newValues && log.newValues.toLowerCase().includes(q))
    );
  });

  const filteredLogins = loginLogs.filter(log => {
    const q = searchQuery.toLowerCase();
    return (
      log.username.toLowerCase().includes(q) ||
      log.ipAddress.toLowerCase().includes(q) ||
      log.userAgent.toLowerCase().includes(q)
    );
  });

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '1400px', margin: '0 auto', padding: '8px 16px' }}>
      
      {/* Header */}
      <div className="page-header" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <ScrollText size={28} color="var(--primary-400)" />
            System Audit & Compliance Logs
          </h1>
          <p className="page-subtitle">Track modifications, configurations, and administrative access histories across the tenant.</p>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={loadLogs} disabled={loading} style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
          <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
          Reload logs
        </button>
      </div>

      {/* Tabs */}
      <div className="tabs-container" style={{ display: 'flex', gap: '12px', borderBottom: '1px solid var(--border-color)', paddingBottom: '10px' }}>
        <button
          className={`tab-btn ${activeTab === 'activities' ? 'active' : ''}`}
          onClick={() => { setActiveTab('activities'); setSearchQuery(''); }}
          style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', padding: '10px 18px', border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 600 }}
        >
          <ShieldAlert size={16} />
          System Activities
        </button>
        <button
          className={`tab-btn ${activeTab === 'logins' ? 'active' : ''}`}
          onClick={() => { setActiveTab('logins'); setSearchQuery(''); }}
          style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', padding: '10px 18px', border: 'none', background: 'transparent', cursor: 'pointer', fontWeight: 600 }}
        >
          <Key size={16} />
          Authentication History
        </button>
      </div>

      {/* Filter Bar */}
      <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
        <div className="toolbar-search" style={{ position: 'relative', flex: 1, display: 'flex', alignItems: 'center' }}>
          <Search size={16} style={{ position: 'absolute', left: '12px', color: 'var(--text-muted)' }} />
          <input
            type="text"
            placeholder={activeTab === 'activities' ? "Search actions, entities, actors, values..." : "Search users, IP address, user-agent..."}
            className="form-input"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            style={{ width: '100%', paddingLeft: '38px', height: '40px' }}
          />
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <ShieldAlert size={18} />
          {error}
        </div>
      )}

      {/* Main Table Card */}
      <div className="card" style={{ padding: '0px', overflow: 'hidden', border: '1px solid var(--border-color)' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
            <div className="spinner" style={{ marginBottom: '12px' }}>Loading logs...</div>
          </div>
        ) : activeTab === 'activities' ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-color)', height: '44px' }}>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Timestamp</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Actor</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Action</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Entity Type</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Entity ID</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', textAlign: 'right' }}>Payload</th>
                </tr>
              </thead>
              <tbody>
                {filteredAudits.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No audit activities matching query.
                    </td>
                  </tr>
                ) : (
                  filteredAudits.map((log) => (
                    <React.Fragment key={log.id}>
                      <tr style={{ borderBottom: '1px solid var(--border-color)', height: '48px' }}>
                        <td style={{ padding: '12px 16px', fontSize: '13px' }}>
                          {new Date(log.timestamp).toLocaleString()}
                        </td>
                        <td style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600 }}>
                          {log.username}
                          {log.userEmail && <div style={{ fontSize: '11px', fontWeight: 400, color: 'var(--text-muted)' }}>{log.userEmail}</div>}
                        </td>
                        <td style={{ padding: '12px 16px', fontSize: '13px' }}>
                          <span className="badge badge-light" style={{ fontFamily: 'monospace' }}>{log.action}</span>
                        </td>
                        <td style={{ padding: '12px 16px', fontSize: '13px' }}>{log.entityName}</td>
                        <td style={{ padding: '12px 16px', fontSize: '13px', fontFamily: 'monospace' }}>#{log.entityId}</td>
                        <td style={{ padding: '12px 16px', fontSize: '13px', textAlign: 'right' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => setExpandedRow(expandedRow === log.id ? null : log.id)}
                            style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', padding: '4px 10px' }}
                          >
                            {expandedRow === log.id ? <EyeOff size={13} /> : <Eye size={13} />}
                            {expandedRow === log.id ? 'Hide Changes' : 'View Changes'}
                          </button>
                        </td>
                      </tr>
                      {expandedRow === log.id && (
                        <tr style={{ background: 'rgba(0,0,0,0.01)', borderBottom: '1px solid var(--border-color)' }}>
                          <td colSpan={6} style={{ padding: '16px 24px' }}>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                              <div>
                                <strong style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>OLD VALUES</strong>
                                <pre style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: '6px', padding: '12px', fontSize: '12px', overflowX: 'auto', margin: 0, whiteSpace: 'pre-wrap' }}>
                                  {log.oldValues || 'None'}
                                </pre>
                              </div>
                              <div>
                                <strong style={{ fontSize: '12px', color: 'var(--text-muted)', display: 'block', marginBottom: '6px' }}>NEW VALUES</strong>
                                <pre style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-color)', borderRadius: '6px', padding: '12px', fontSize: '12px', overflowX: 'auto', margin: 0, whiteSpace: 'pre-wrap' }}>
                                  {log.newValues || 'None'}
                                </pre>
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))
                )}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ background: 'var(--bg-surface)', borderBottom: '1px solid var(--border-color)', height: '44px' }}>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Timestamp</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>User</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>IP Address</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Browser / client</th>
                  <th style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)', textAlign: 'right' }}>Security Status</th>
                </tr>
              </thead>
              <tbody>
                {filteredLogins.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No authentication sessions matching query.
                    </td>
                  </tr>
                ) : (
                  filteredLogins.map((log) => (
                    <tr key={log.id} style={{ borderBottom: '1px solid var(--border-color)', height: '48px' }}>
                      <td style={{ padding: '12px 16px', fontSize: '13px' }}>
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td style={{ padding: '12px 16px', fontSize: '13px', fontWeight: 600 }}>
                        {log.username}
                        <div style={{ fontSize: '11px', fontWeight: 400, color: 'var(--text-muted)' }}>{log.userEmail}</div>
                      </td>
                      <td style={{ padding: '12px 16px', fontSize: '13px', fontFamily: 'monospace' }}>
                        {log.ipAddress}
                      </td>
                      <td style={{ padding: '12px 16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                        {getBrowserName(log.userAgent)}
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', maxWidth: '300px' }} title={log.userAgent}>
                          {log.userAgent}
                        </div>
                      </td>
                      <td style={{ padding: '12px 16px', fontSize: '13px', textAlign: 'right' }}>
                        {log.mfaVerified ? (
                          <span className="badge badge-success" style={{ padding: '4px 10px', fontSize: '11px' }}>MFA Secures</span>
                        ) : (
                          <span className="badge badge-warning" style={{ padding: '4px 10px', fontSize: '11px' }}>Password Only</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
}
