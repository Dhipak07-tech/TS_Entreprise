import React, { useEffect, useState } from 'react';
import { rbacService } from '../../services/dataService';
import { Shield, Save, CheckCircle2, Lock, AlertCircle } from 'lucide-react';
import './Roles.css';

interface Role {
  id: number;
  name: string;
  description: string;
  isSystemDefault: boolean;
  permissions: string[];
}

interface Permission {
  id: number;
  permKey: string;
  description: string;
}

interface RolePagePermissionDTO {
  roleId: number;
  pageId: number;
  pageName: string;
  canView: boolean;
  canCreate: boolean;
  canUpdate: boolean;
  canDelete: boolean;
  canApprove: boolean;
  canReject: boolean;
  canAssign: boolean;
  canImport: boolean;
  canExport: boolean;
  canPrint: boolean;
  canReportAccess: boolean;
}

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [matrixPermissions, setMatrixPermissions] = useState<RolePagePermissionDTO[]>([]);
  
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [selectedRolePerms, setSelectedRolePerms] = useState<string[]>([]);
  const [activeTab, setActiveTab] = useState<'pages' | 'authorities'>('pages');
  
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  useEffect(() => {
    loadRbacData();
  }, []);

  const loadRbacData = async () => {
    setLoading(true);
    setError(null);
    try {
      const allRoles = await rbacService.getRoles();
      setRoles(allRoles || []);
      
      const allPerms = await rbacService.getPermissions();
      setPermissions(allPerms || []);

      const matrix = await rbacService.getPagePermissionMatrix();
      setMatrixPermissions(matrix || []);

      if (allRoles && allRoles.length > 0) {
        setSelectedRoleId(allRoles[0].id);
        setSelectedRolePerms(allRoles[0].permissions || []);
      }
    } catch (err) {
      console.error('Failed to load RBAC data:', err);
      setError('Failed to retrieve roles or permissions configuration.');
    } finally {
      setLoading(false);
    }
  };

  const handleRoleSelect = (role: Role) => {
    setSelectedRoleId(role.id);
    setSelectedRolePerms(role.permissions || []);
    setSuccessMsg(null);
  };

  const handlePermissionToggle = (permKey: string) => {
    setSelectedRolePerms(prev => {
      if (prev.includes(permKey)) {
        return prev.filter(k => k !== permKey);
      } else {
        return [...prev, permKey];
      }
    });
    setSuccessMsg(null);
  };

  const handleMatrixToggle = (pageId: number, field: keyof RolePagePermissionDTO) => {
    if (selectedRoleId === null || isSuperAdmin) return;
    setMatrixPermissions(prev => prev.map(item => {
      if (item.roleId === selectedRoleId && item.pageId === pageId) {
        return { ...item, [field]: !item[field] };
      }
      return item;
    }));
    setSuccessMsg(null);
  };

  const handleSavePermissions = async () => {
    if (selectedRoleId === null) return;
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      const updated = await rbacService.updateRolePermissions(selectedRoleId, selectedRolePerms);
      setRoles(prev => prev.map(r => r.id === selectedRoleId ? { ...r, permissions: updated.permissions } : r));
      setSuccessMsg('System authorities updated successfully.');
    } catch (err: any) {
      console.error('Failed to save permissions:', err);
      setError(err?.response?.data?.message || 'Failed to update permissions.');
    } finally {
      setSaving(false);
    }
  };

  const handleSaveMatrix = async () => {
    if (selectedRoleId === null) return;
    setSaving(true);
    setError(null);
    setSuccessMsg(null);
    try {
      await rbacService.updatePagePermissionMatrix(matrixPermissions);
      setSuccessMsg('Page permission matrix updated successfully.');
    } catch (err: any) {
      console.error('Failed to save page permission matrix:', err);
      setError(err?.response?.data?.message || 'Failed to update page permission matrix.');
    } finally {
      setSaving(false);
    }
  };

  const selectedRole = roles.find(r => r.id === selectedRoleId);
  const isSuperAdmin = selectedRole?.name === 'SUPER_ADMIN' || selectedRole?.name === 'ULTRA_SUPER_ADMIN';

  const selectedRoleMatrix = matrixPermissions.filter(item => item.roleId === selectedRoleId);
  selectedRoleMatrix.sort((a, b) => a.pageId - b.pageId);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <div className="spinner">Loading Roles configuration...</div>
      </div>
    );
  }

  return (
    <div className="roles-page-container">
      {/* Title */}
      <div className="page-header" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '16px' }}>
        <div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Shield size={28} color="var(--primary-400)" />
            Roles & Access Control
          </h1>
          <p className="page-subtitle">Configure security roles, manage granular permission matrices, and enforce access boundaries.</p>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {successMsg && (
        <div className="alert alert-success" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <CheckCircle2 size={18} />
          {successMsg}
        </div>
      )}

      {/* Grid Layout */}
      <div className="roles-layout">
        
        {/* Left Side: Roles List */}
        <div className="role-list-card">
          <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '8px' }}>Roles</h3>
          {roles.map(role => (
            <div
              key={role.id}
              className={`role-item ${selectedRoleId === role.id ? 'selected' : ''}`}
              onClick={() => handleRoleSelect(role)}
            >
              <div className="role-item-name">
                {role.name.replace('_', ' ')}
                {role.isSystemDefault && <span className="system-tag">System</span>}
              </div>
              <div className="role-item-desc">{role.description}</div>
            </div>
          ))}
        </div>

        {/* Right Side: Permissions Matrix */}
        {selectedRole && (
          <div className="detail-card">
            
            {/* Tabs Header */}
            <div className="matrix-header">
              <div>
                <h3 className="matrix-role-title">
                  Access Management: {selectedRole.name.replace('_', ' ')}
                </h3>
                <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: 'var(--text-secondary)' }}>
                  {isSuperAdmin 
                    ? 'Super Administrators hold all permissions implicitly. Modifications are disabled.' 
                    : `Configure access levels and specific actions for ${selectedRole.name.replace('_', ' ')}.`}
                </p>
              </div>

              {!isSuperAdmin && (
                <button
                  onClick={activeTab === 'pages' ? handleSaveMatrix : handleSavePermissions}
                  disabled={saving}
                  className="btn btn-primary"
                  style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
                >
                  <Save size={16} />
                  {saving ? 'Saving...' : 'Save Changes'}
                </button>
              )}
            </div>

            {/* Tab Buttons */}
            <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', borderBottom: '1px solid var(--border-color)', paddingBottom: '8px' }}>
              <button 
                className={`btn ${activeTab === 'pages' ? 'btn-primary' : 'btn-outline'}`}
                style={{ padding: '6px 12px', fontSize: '12px' }}
                onClick={() => setActiveTab('pages')}
              >
                Page Permissions Matrix
              </button>
              <button 
                className={`btn ${activeTab === 'authorities' ? 'btn-primary' : 'btn-outline'}`}
                style={{ padding: '6px 12px', fontSize: '12px' }}
                onClick={() => setActiveTab('authorities')}
              >
                System Authorities (API Keys)
              </button>
            </div>

            {activeTab === 'authorities' ? (
              /* TAB 1: System Authorities */
              <div className="matrix-permissions-grid">
                {permissions.map(perm => {
                  const isChecked = selectedRolePerms.includes(perm.permKey) || isSuperAdmin;
                  return (
                    <div key={perm.id} className="permission-row">
                      <div className="permission-info">
                        <span className="permission-key">{perm.permKey}</span>
                        <span className="permission-desc">{perm.description}</span>
                      </div>

                      <div>
                        <label className="switch">
                          <input
                            type="checkbox"
                            checked={isChecked}
                            disabled={isSuperAdmin || saving}
                            onChange={() => handlePermissionToggle(perm.permKey)}
                          />
                          <span className="slider" />
                        </label>
                      </div>
                    </div>
                  );
                })}
              </div>
            ) : (
              /* TAB 2: Page Permissions Matrix */
              <div style={{ overflowX: 'auto' }}>
                <table className="permission-matrix-table" style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', marginTop: '8px' }}>
                  <thead>
                    <tr style={{ background: 'var(--bg-hover)', borderBottom: '1px solid var(--border-color)' }}>
                      <th style={{ textAlign: 'left', padding: '10px 12px', color: 'var(--text-secondary)' }}>Page / Module</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>View</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Create</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Update</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Delete</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Assign</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Approve</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Reject</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Import</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Export</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Print</th>
                      <th style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>Report</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedRoleMatrix.map(row => (
                      <tr key={row.pageId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                        <td style={{ textAlign: 'left', padding: '10px 12px', fontWeight: 600 }}>
                          {row.pageName}
                          <span style={{ fontSize: '11px', color: 'var(--text-secondary)', display: 'block', fontWeight: 400 }}>
                            ID: {row.pageId}
                          </span>
                        </td>
                        {(['canView', 'canCreate', 'canUpdate', 'canDelete', 'canAssign', 'canApprove', 'canReject', 'canImport', 'canExport', 'canPrint', 'canReportAccess'] as Array<keyof RolePagePermissionDTO>).map(field => (
                          <td key={field} style={{ padding: '10px 8px', textAlign: 'center' }}>
                            <input
                              type="checkbox"
                              checked={(row[field] as boolean) || false}
                              disabled={isSuperAdmin || saving}
                              onChange={() => handleMatrixToggle(row.pageId, field)}
                              style={{ cursor: 'pointer', accentColor: 'var(--primary-500)', width: '16px', height: '16px' }}
                            />
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

      </div>
    </div>
  );
}
