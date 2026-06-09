import React, { useEffect, useState } from 'react';
import { changeService, ticketService } from '../../services/dataService';
import { Plus, GitPullRequest, Calendar, ShieldAlert, FileText, CheckCircle2 } from 'lucide-react';
import './ChangeManagementPage.css';

export default function ChangeManagementPage() {
  const [changes, setChanges] = useState<any[]>([]);
  const [tickets, setTickets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState({
    title: '',
    description: '',
    changeType: 'STANDARD',
    riskLevel: 'LOW',
    rollbackPlan: '',
    testPlan: '',
    plannedStart: '',
    plannedEnd: '',
    ticketIds: [] as number[]
  });

  useEffect(() => {
    loadChanges();
    loadTickets();
  }, []);

  const loadChanges = async () => {
    setLoading(true);
    try {
      const data = await changeService.getChanges();
      setChanges(data);
    } catch (err) {
      console.error('Failed to load changes:', err);
    } finally {
      setLoading(false);
    }
  };

  const loadTickets = async () => {
    try {
      const ticketData = await ticketService.getMyTickets();
      setTickets(ticketData);
    } catch (err) {
      console.error('Failed to load tickets:', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      // Map local dates to ISO format
      const startISO = new Date(form.plannedStart).toISOString();
      const endISO = new Date(form.plannedEnd).toISOString();

      await changeService.createChange({
        ...form,
        plannedStart: startISO,
        plannedEnd: endISO
      });

      setShowModal(false);
      setForm({
        title: '',
        description: '',
        changeType: 'STANDARD',
        riskLevel: 'LOW',
        rollbackPlan: '',
        testPlan: '',
        plannedStart: '',
        plannedEnd: '',
        ticketIds: []
      });
      loadChanges();
    } catch (err) {
      console.error('Failed to submit change request:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleTicketToggle = (ticketId: number) => {
    setForm(prev => {
      const exists = prev.ticketIds.includes(ticketId);
      if (exists) {
        return { ...prev, ticketIds: prev.ticketIds.filter(id => id !== ticketId) };
      } else {
        return { ...prev, ticketIds: [...prev.ticketIds, ticketId] };
      }
    });
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CAB_APPROVAL':
        return <span className="badge badge-warning">CAB Approval</span>;
      case 'SCHEDULED':
        return <span className="badge badge-primary">Scheduled</span>;
      case 'IMPLEMENTING':
        return <span className="badge badge-info">Implementing</span>;
      case 'REVIEW':
        return <span className="badge badge-secondary">Review</span>;
      case 'CLOSED':
        return <span className="badge badge-success">Closed</span>;
      default:
        return <span className="badge badge-light">{status}</span>;
    }
  };

  const getRiskBadge = (risk: string) => {
    switch (risk) {
      case 'HIGH':
        return <span className="badge badge-danger">High Risk</span>;
      case 'MEDIUM':
        return <span className="badge badge-warning">Medium Risk</span>;
      default:
        return <span className="badge badge-success">Low Risk</span>;
    }
  };

  return (
    <div className="change-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Change Control</h1>
          <p className="page-subtitle">Standard, Normal, and Emergency deployment governance with audit-ready rollbacks.</p>
        </div>
        <button className="btn btn-primary btn-icon" onClick={() => setShowModal(true)}>
          <Plus size={16} /> Request Change
        </button>
      </div>

      {loading ? (
        <div className="change-list">
          {[1, 2, 3].map(n => (
            <div key={n} className="card skeleton" style={{ height: 100, marginBottom: 16 }}></div>
          ))}
        </div>
      ) : changes.length === 0 ? (
        <div className="card empty-state">
          <GitPullRequest size={48} className="empty-icon text-muted" />
          <h3>No Change Requests</h3>
          <p>No deployments or configuration changes have been recorded yet.</p>
        </div>
      ) : (
        <div className="change-list">
          {changes.map(change => (
            <div key={change.id} className="change-card card hover-lift">
              <div className="change-main">
                <div className="change-type-icon">
                  <GitPullRequest size={20} />
                </div>
                <div className="change-details">
                  <div className="change-title-row">
                    <h3>{change.title}</h3>
                    <div className="badge-row">
                      <span className={`badge ${change.changeType === 'EMERGENCY' ? 'badge-danger' : change.changeType === 'NORMAL' ? 'badge-primary' : 'badge-light'}`}>
                        {change.changeType}
                      </span>
                      {getStatusBadge(change.status)}
                      {getRiskBadge(change.riskLevel)}
                    </div>
                  </div>
                  <p className="change-desc">{change.description}</p>
                  
                  <div className="change-plans">
                    <div className="plan-item">
                      <strong>Rollback Plan:</strong> {change.rollbackPlan}
                    </div>
                    <div className="plan-item">
                      <strong>Test Plan:</strong> {change.testPlan}
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="change-schedule">
                <div className="schedule-item">
                  <Calendar size={14} />
                  <span>Start: {new Date(change.plannedStart).toLocaleString()}</span>
                </div>
                <div className="schedule-item">
                  <Calendar size={14} />
                  <span>End: {new Date(change.plannedEnd).toLocaleString()}</span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-backdrop" onClick={() => !submitting && setShowModal(false)}>
          <div className="modal-content glassmorphism-change-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>New Change Request (RFC)</h2>
              <button className="close-btn-x" onClick={() => setShowModal(false)} disabled={submitting}>&times;</button>
            </div>

            <form onSubmit={handleSubmit} className="modal-body">
              <div className="form-grid-2">
                <div className="form-group">
                  <label htmlFor="title">Change Title</label>
                  <input 
                    type="text" 
                    id="title"
                    value={form.title}
                    onChange={e => setForm({ ...form, title: e.target.value })}
                    required
                    placeholder="e.g. Upgrade production DB cluster"
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="changeType">Change Type</label>
                  <select
                    id="changeType"
                    value={form.changeType}
                    onChange={e => setForm({ ...form, changeType: e.target.value })}
                  >
                    <option value="STANDARD">Standard (Pre-Approved)</option>
                    <option value="NORMAL">Normal (CAB Review Needed)</option>
                    <option value="EMERGENCY">Emergency (Immediate Fix)</option>
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="description">Detailed Description</label>
                <textarea 
                  id="description"
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                  required
                  placeholder="Describe the technical implementation, scope, and impact."
                  rows={3}
                />
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label htmlFor="riskLevel">Risk Assessment</label>
                  <select
                    id="riskLevel"
                    value={form.riskLevel}
                    onChange={e => setForm({ ...form, riskLevel: e.target.value })}
                  >
                    <option value="LOW">Low Risk</option>
                    <option value="MEDIUM">Medium Risk</option>
                    <option value="HIGH">High Risk</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Link Active Tickets</label>
                  <div className="linked-tickets-selector">
                    {tickets.map(t => (
                      <label key={t.id} className="ticket-checkbox-item">
                        <input 
                          type="checkbox"
                          checked={form.ticketIds.includes(t.id)}
                          onChange={() => handleTicketToggle(t.id)}
                        />
                        <span>{t.ticketNumber} - {t.title}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label htmlFor="plannedStart">Planned Start</label>
                  <input 
                    type="datetime-local" 
                    id="plannedStart"
                    value={form.plannedStart}
                    onChange={e => setForm({ ...form, plannedStart: e.target.value })}
                    required
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="plannedEnd">Planned End</label>
                  <input 
                    type="datetime-local" 
                    id="plannedEnd"
                    value={form.plannedEnd}
                    onChange={e => setForm({ ...form, plannedEnd: e.target.value })}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="rollbackPlan">Rollback Plan (Disaster Recovery)</label>
                <textarea 
                  id="rollbackPlan"
                  value={form.rollbackPlan}
                  onChange={e => setForm({ ...form, rollbackPlan: e.target.value })}
                  required
                  placeholder="Step-by-step instructions to revert changes if validation fails."
                  rows={2}
                />
              </div>

              <div className="form-group">
                <label htmlFor="testPlan">Validation / Test Plan</label>
                <textarea 
                  id="testPlan"
                  value={form.testPlan}
                  onChange={e => setForm({ ...form, testPlan: e.target.value })}
                  required
                  placeholder="Post-deployment checks to verify service integrity."
                  rows={2}
                />
              </div>

              <div className="modal-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => setShowModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn btn-primary"
                  disabled={submitting}
                >
                  {submitting ? 'Submitting...' : 'Submit Request'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
