import { useEffect, useState } from 'react';
import { userService, employeeService, rbacService, departmentService } from '../../services/dataService';
import {
  Plus, Search, UserCheck, UserX, ChevronLeft, ChevronRight,
  Mail, Phone, ShieldAlert, Key, FolderOpen, Briefcase, Eye, EyeOff, X
} from 'lucide-react';
import './Users.css';

export default function UsersPage() {
  const [activeTab, setActiveTab] = useState<'employees' | 'users'>('employees');
  
  // Master Lists State
  const [employees, setEmployees] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const [unprovisioned, setUnprovisioned] = useState<any[]>([]);
  const [departments, setDepartments] = useState<any[]>([]);
  const [roles, setRoles] = useState<any[]>([]);
  
  // Loading & Pagination State
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Employee Modal State
  const [showEmployeeModal, setShowEmployeeModal] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<any | null>(null);
  const [employeeForm, setEmployeeForm] = useState({
    employeeCode: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    departmentId: '',
    jobTitle: '',
    managerId: '',
    status: 'ACTIVE'
  });

  // Provisioning Modal State
  const [showProvisionModal, setShowProvisionModal] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [provisionForm, setProvisionForm] = useState({
    employeeId: '',
    username: '',
    password: '',
    roles: [] as string[],
    departmentId: ''
  });

  // Load basic helpers
  useEffect(() => {
    loadTabContent();
    loadFilters();
  }, [activeTab, page]);

  const loadTabContent = async () => {
    setLoading(true);
    try {
      if (activeTab === 'employees') {
        const data = await employeeService.getEmployees(page, 10);
        setEmployees(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } else {
        const data = await userService.getUsers(page, 10);
        setUsers(data.content || []);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      }
    } catch (err) {
      console.error('Failed to load tab content:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadFilters = async () => {
    try {
      const [deptData, rolesData] = await Promise.all([
        departmentService.getDepartments(),
        rbacService.getRoles()
      ]);
      setDepartments(deptData || []);
      setRoles(rolesData || []);
    } catch (err) {
      console.error('Failed to load filter lookups:', err);
    }
  };

  const loadUnprovisioned = async () => {
    try {
      const data = await employeeService.getUnprovisionedEmployees();
      setUnprovisioned(data || []);
    } catch (err) {
      console.error('Failed to load unprovisioned employees:', err);
    }
  };

  const handleOpenEmployeeModal = (emp: any | null = null) => {
    if (emp) {
      setEditingEmployee(emp);
      setEmployeeForm({
        employeeCode: emp.employeeCode,
        firstName: emp.firstName,
        lastName: emp.lastName,
        email: emp.email,
        phone: emp.phone || '',
        departmentId: emp.departmentId ? emp.departmentId.toString() : '',
        jobTitle: emp.jobTitle || '',
        managerId: emp.managerId ? emp.managerId.toString() : '',
        status: emp.status
      });
    } else {
      setEditingEmployee(null);
      setEmployeeForm({
        employeeCode: `EMP-${Date.now().toString().slice(-5)}`,
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        departmentId: '',
        jobTitle: '',
        managerId: '',
        status: 'ACTIVE'
      });
    }
    setShowEmployeeModal(true);
  };

  const handleOpenProvisionModal = async () => {
    await loadUnprovisioned();
    setProvisionForm({
      employeeId: '',
      username: '',
      password: '',
      roles: ['USER'],
      departmentId: ''
    });
    setShowProvisionModal(true);
  };

  const handleSaveEmployee = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const payload = {
        ...employeeForm,
        departmentId: employeeForm.departmentId ? parseInt(employeeForm.departmentId) : null,
        managerId: employeeForm.managerId ? parseInt(employeeForm.managerId) : null
      };

      if (editingEmployee) {
        await employeeService.updateEmployee(editingEmployee.id, payload);
      } else {
        await employeeService.createEmployee(payload);
      }
      setShowEmployeeModal(false);
      loadTabContent();
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Error saving employee');
    }
  };

  const handleProvisionUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!provisionForm.employeeId) {
      alert('Please select an employee');
      return;
    }
    try {
      const payload = {
        employeeId: parseInt(provisionForm.employeeId),
        username: provisionForm.username,
        password: provisionForm.password,
        roles: provisionForm.roles,
        departmentId: provisionForm.departmentId ? parseInt(provisionForm.departmentId) : null
      };
      await employeeService.provisionUser(payload);
      setShowProvisionModal(false);
      // switch to active accounts tab to see the newly provisioned user login!
      setActiveTab('users');
      setPage(0);
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Error provisioning login credentials');
    }
  };

  const handleToggleStatus = async (id: number) => {
    try {
      await userService.toggleUserStatus(id);
      loadTabContent();
    } catch (err) {
      console.error('Failed to toggle status:', err);
    }
  };

  // UI Filtering
  const filteredEmployees = searchTerm
    ? employees.filter(
        (e) =>
          e.employeeCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
          e.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          e.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          e.email.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : employees;

  const filteredUsers = searchTerm
    ? users.filter(
        (u) =>
          u.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
          u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
          (u.firstName?.toLowerCase() || '').includes(searchTerm.toLowerCase())
      )
    : users;

  const getInitials = (firstName: string, lastName: string) => {
    return `${firstName?.[0] || ''}${lastName?.[0] || ''}`.toUpperCase();
  };

  return (
    <div className="users-page animate-fade-in">
      {/* Page Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Directory & Credential Provisioning</h1>
          <p className="page-subtitle">Manage Employee Master records and provision secure SaaS accounts.</p>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-secondary" onClick={() => handleOpenEmployeeModal(null)}>
            <Plus size={18} />
            Add Employee
          </button>
          <button className="btn btn-primary" onClick={handleOpenProvisionModal}>
            <Key size={18} />
            Provision System Login
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs-container">
        <button 
          className={`tab-btn ${activeTab === 'employees' ? 'active' : ''}`}
          onClick={() => { setActiveTab('employees'); setPage(0); }}
        >
          Employee Master (Source of Truth)
        </button>
        <button 
          className={`tab-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => { setActiveTab('users'); setPage(0); }}
        >
          SaaS User Logins
        </button>
      </div>

      {/* Toolbar */}
      <div className="users-toolbar">
        <div className="toolbar-search">
          <Search size={16} className="toolbar-search-icon" />
          <input
            type="text"
            placeholder={activeTab === 'employees' ? "Search by employee code, name, email..." : "Search users by username, email..."}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="form-input toolbar-search-input"
          />
        </div>
      </div>

      {/* Content Table */}
      <div className="table-container">
        {activeTab === 'employees' ? (
          <table className="data-table">
            <thead>
              <tr>
                <th>Employee Code</th>
                <th>Employee Name</th>
                <th>Email / Contact</th>
                <th>Job Title</th>
                <th>Department</th>
                <th>Status</th>
                <th>Account Status</th>
                <th style={{ width: 60 }}></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    <td><div className="skeleton" style={{ width: 80, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 150, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 180, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 120, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 120, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 60, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 100, height: 20 }} /></td>
                    <td></td>
                  </tr>
                ))
              ) : filteredEmployees.length === 0 ? (
                <tr>
                  <td colSpan={8} className="empty-state">
                    No employees found in the Master directory.
                  </td>
                </tr>
              ) : (
                filteredEmployees.map((emp) => (
                  <tr key={emp.id}>
                    <td><strong>{emp.employeeCode}</strong></td>
                    <td>
                      <div className="user-cell">
                        <div className="user-cell-avatar">
                          {getInitials(emp.firstName, emp.lastName)}
                        </div>
                        <div className="user-cell-name">
                          {emp.firstName} {emp.lastName}
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="user-contact">
                        <Mail size={12} /> {emp.email}
                      </div>
                      {emp.phone && (
                        <div className="user-contact" style={{ marginTop: 2 }}>
                          <Phone size={12} /> {emp.phone}
                        </div>
                      )}
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <Briefcase size={14} className="text-muted" />
                        {emp.jobTitle || 'N/A'}
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <FolderOpen size={14} className="text-muted" />
                        {emp.departmentName || 'N/A'}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${emp.status === 'ACTIVE' ? 'badge-success' : 'badge-danger'}`}>
                        {emp.status}
                      </span>
                    </td>
                    <td>
                      {emp.provisioned ? (
                        <span className="badge badge-success flex items-center gap-1" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <ShieldAlert size={12} /> Login Created
                        </span>
                      ) : (
                        <span className="badge badge-warning">No Login Profile</span>
                      )}
                    </td>
                    <td>
                      <button className="btn btn-ghost btn-xs" onClick={() => handleOpenEmployeeModal(emp)}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Security Roles</th>
                <th>Status</th>
                <th>First Login Reset</th>
                <th style={{ width: 100 }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    <td><div className="skeleton" style={{ width: 150, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 180, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 120, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 70, height: 20 }} /></td>
                    <td><div className="skeleton" style={{ width: 60, height: 20 }} /></td>
                    <td></td>
                  </tr>
                ))
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="empty-state">
                    No system user logins found.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <div className="user-cell">
                        <div className="user-cell-avatar" style={{ background: 'var(--primary-color)' }}>
                          {getInitials(user.firstName || '', user.lastName || '') || 'U'}
                        </div>
                        <div>
                          <div className="user-cell-name">@{user.username}</div>
                          <div className="user-cell-username">
                            {user.firstName} {user.lastName}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td>{user.email}</td>
                    <td>
                      <div className="role-badges">
                        {user.roles?.map((role: string) => (
                          <span key={role} className="badge badge-primary">{role}</span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${user.isActive ? 'badge-success' : 'badge-danger'}`}>
                        {user.isActive ? 'Active' : 'Locked'}
                      </span>
                    </td>
                    <td>
                      {user.passwordResetRequired ? (
                        <span className="badge badge-danger">Required</span>
                      ) : (
                        <span className="badge badge-success">Completed</span>
                      )}
                    </td>
                    <td>
                      <button
                        className="btn btn-ghost btn-icon"
                        onClick={() => handleToggleStatus(user.id)}
                        title={user.isActive ? 'Lock Login' : 'Unlock Login'}
                      >
                        {user.isActive ? <UserX size={16} /> : <UserCheck size={16} />}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={page === 0}
          >
            <ChevronLeft size={16} />
            Previous
          </button>
          <span className="pagination-info">
            Page {page + 1} of {totalPages}
          </span>
          <button
            className="btn btn-ghost btn-sm"
            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
          >
            Next
            <ChevronRight size={16} />
          </button>
        </div>
      )}

      {/* 1. Employee Form Modal */}
      {showEmployeeModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{editingEmployee ? 'Edit Employee Master Record' : 'New Employee Master Entry'}</h3>
              <button className="modal-close" onClick={() => setShowEmployeeModal(false)}>
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleSaveEmployee}>
              <div className="modal-body">
                <div className="form-row">
                  <div className="form-group">
                    <label>Employee Code</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      required 
                      value={employeeForm.employeeCode}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, employeeCode: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Job Title</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      placeholder="e.g. System Engineer"
                      value={employeeForm.jobTitle}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, jobTitle: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>First Name</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      required 
                      value={employeeForm.firstName}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, firstName: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Last Name</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      required 
                      value={employeeForm.lastName}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, lastName: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Email Address</label>
                    <input 
                      type="email" 
                      className="form-input" 
                      required 
                      value={employeeForm.email}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, email: e.target.value })}
                    />
                  </div>
                  <div className="form-group">
                    <label>Phone Number</label>
                    <input 
                      type="text" 
                      className="form-input" 
                      value={employeeForm.phone}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, phone: e.target.value })}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Department</label>
                    <select 
                      className="form-input"
                      value={employeeForm.departmentId}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, departmentId: e.target.value })}
                    >
                      <option value="">Select Department</option>
                      {departments.map((dept) => (
                        <option key={dept.id} value={dept.id}>{dept.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Status</label>
                    <select 
                      className="form-input"
                      value={employeeForm.status}
                      onChange={(e) => setEmployeeForm({ ...employeeForm, status: e.target.value })}
                    >
                      <option value="ACTIVE">ACTIVE</option>
                      <option value="INACTIVE">INACTIVE</option>
                      <option value="LEAVE">ON LEAVE</option>
                      <option value="TERMINATED">TERMINATED</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>Reporting Manager</label>
                  <select 
                    className="form-input"
                    value={employeeForm.managerId}
                    onChange={(e) => setEmployeeForm({ ...employeeForm, managerId: e.target.value })}
                  >
                    <option value="">Select Manager</option>
                    {employees
                      .filter(e => !editingEmployee || e.id !== editingEmployee.id)
                      .map((e) => (
                        <option key={e.id} value={e.id}>{e.firstName} {e.lastName}</option>
                      ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={() => setShowEmployeeModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Employee</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 2. User Provisioning Modal */}
      {showProvisionModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Provision User Credentials</h3>
              <button className="modal-close" onClick={() => setShowProvisionModal(false)}>
                <X size={18} />
              </button>
            </div>
            <form onSubmit={handleProvisionUser}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Select Unprovisioned Employee</label>
                  <select 
                    className="form-input"
                    required
                    value={provisionForm.employeeId}
                    onChange={(e) => {
                      const selectedEmp = unprovisioned.find(emp => emp.id.toString() === e.target.value);
                      setProvisionForm({ 
                        ...provisionForm, 
                        employeeId: e.target.value,
                        departmentId: selectedEmp?.departmentId ? selectedEmp.departmentId.toString() : ''
                      });
                    }}
                  >
                    <option value="">Choose Employee...</option>
                    {unprovisioned.map((emp) => (
                      <option key={emp.id} value={emp.id}>
                        {emp.firstName} {emp.lastName} ({emp.employeeCode} - {emp.email})
                      </option>
                    ))}
                  </select>
                </div>

                {provisionForm.employeeId && (
                  <div className="alert alert-info" style={{ display: 'flex', flexDirection: 'column', gap: 4, background: 'var(--bg-hover)', padding: 12, borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                    <div style={{ fontSize: 13 }}><strong>Selected Employee Details:</strong></div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                      Code: {unprovisioned.find(e => e.id.toString() === provisionForm.employeeId)?.employeeCode}
                    </div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>
                      Email: {unprovisioned.find(e => e.id.toString() === provisionForm.employeeId)?.email}
                    </div>
                  </div>
                )}

                <div className="form-group">
                  <label>Username</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    required 
                    placeholder="e.g. jsmith"
                    value={provisionForm.username}
                    onChange={(e) => setProvisionForm({ ...provisionForm, username: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Temporary Password</label>
                  <div style={{ position: 'relative' }}>
                    <input 
                      type={showPassword ? 'text' : 'password'} 
                      className="form-input" 
                      required 
                      placeholder="Minimum 8 characters"
                      style={{ paddingRight: 40 }}
                      value={provisionForm.password}
                      onChange={(e) => setProvisionForm({ ...provisionForm, password: e.target.value })}
                    />
                    <button 
                      type="button"
                      style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                      onClick={() => setShowPassword(!showPassword)}
                    >
                      {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                  </div>
                  <small style={{ fontSize: 11, color: 'var(--text-muted)' }}>
                    User will be forced to change this password upon first successful login.
                  </small>
                </div>

                <div className="form-group">
                  <label>Initial Role Assignment</label>
                  <select 
                    className="form-input"
                    value={provisionForm.roles[0] || 'USER'}
                    onChange={(e) => setProvisionForm({ ...provisionForm, roles: [e.target.value] })}
                  >
                    {roles.map((r) => (
                      <option key={r.id} value={r.name}>{r.name} - {r.description}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Login Department Mapping</label>
                  <select 
                    className="form-input"
                    value={provisionForm.departmentId}
                    onChange={(e) => setProvisionForm({ ...provisionForm, departmentId: e.target.value })}
                  >
                    <option value="">Inherit Department from Employee</option>
                    {departments.map((dept) => (
                      <option key={dept.id} value={dept.id}>{dept.name}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={() => setShowProvisionModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Provision Login Profile</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
