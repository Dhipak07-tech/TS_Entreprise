import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ticketService, incidentService, problemService, teamService, userService } from '../../services/dataService';
import type { TicketDTO } from '../../types';
import {
  Ticket, Search, Plus, Clock, AlertCircle, CheckCircle2, MoreVertical,
  ShieldAlert, Sparkles, AlertTriangle, Layers, BookOpen, UserCheck, Check
} from 'lucide-react';
import './Tickets.css';

export default function TicketsPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'tickets' | 'incidents' | 'problems'>('tickets');
  
  // Ticket States
  const [tickets, setTickets] = useState<TicketDTO[]>([]);
  const [loadingTickets, setLoadingTickets] = useState(true);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchDebounce, setSearchDebounce] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [ticketForm, setTicketForm] = useState({ title: '', description: '', priority: 'MEDIUM' });

  // Incident States
  const [incidents, setIncidents] = useState<any[]>([]);
  const [loadingIncidents, setLoadingIncidents] = useState(false);
  const [showIncidentModal, setShowIncidentModal] = useState(false);
  const [selectedTicketForIncident, setSelectedTicketForIncident] = useState<number | null>(null);
  const [incidentForm, setIncidentForm] = useState({
    impact: 'MEDIUM',
    urgency: 'MEDIUM',
    category: 'Software',
    subcategory: 'Application Bug',
    majorIncident: false
  });

  // Problem States
  const [problems, setProblems] = useState<any[]>([]);
  const [loadingProblems, setLoadingProblems] = useState(false);
  const [showProblemModal, setShowProblemModal] = useState(false);
  const [selectedIncidentsForProblem, setSelectedIncidentsForProblem] = useState<number[]>([]);
  const [problemForm, setProblemForm] = useState({
    title: '',
    description: ''
  });

  // Problem Investigation States
  const [showInvestigationModal, setShowInvestigationModal] = useState(false);
  const [selectedProblemForInvestigate, setSelectedProblemForInvestigate] = useState<any | null>(null);
  const [investigationForm, setInvestigationForm] = useState({
    rootCause: '',
    workaround: '',
    status: 'INVESTIGATING'
  });

  // Assign States
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedTicketForAssign, setSelectedTicketForAssign] = useState<number | null>(null);
  const [assignForm, setAssignForm] = useState({
    teamId: '',
    agentId: ''
  });
  const [teams, setTeams] = useState<any[]>([]);
  const [teamMembers, setTeamMembers] = useState<any[]>([]);

  useEffect(() => {
    loadTeams();
  }, []);

  const loadTeams = async () => {
    try {
      const teamData = await teamService.getTeams();
      setTeams(teamData || []);
    } catch (err) {
      console.error('Failed to load teams:', err);
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      setSearchDebounce(searchQuery);
    }, 400);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    if (activeTab === 'tickets') {
      loadTickets();
    } else if (activeTab === 'incidents') {
      loadIncidents();
    } else if (activeTab === 'problems') {
      loadProblems();
    }
  }, [activeTab, page, statusFilter, searchDebounce]);

  const loadTickets = async () => {
    setLoadingTickets(true);
    try {
      const data = await ticketService.getTickets(page, 20, statusFilter, searchDebounce);
      setTickets(Array.isArray(data) ? data : data.content || []);
    } catch (err) {
      console.error('Failed to load tickets:', err);
    } finally {
      setLoadingTickets(false);
    }
  };

  const loadIncidents = async () => {
    setLoadingIncidents(true);
    try {
      const data = await incidentService.getIncidents();
      setIncidents(data);
    } catch (err) {
      console.error('Failed to load incidents:', err);
    } finally {
      setLoadingIncidents(false);
    }
  };

  const loadProblems = async () => {
    setLoadingProblems(true);
    try {
      const data = await problemService.getProblems();
      setProblems(data);
    } catch (err) {
      console.error('Failed to load problems:', err);
    } finally {
      setLoadingProblems(false);
    }
  };

  const handleCreateTicket = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await ticketService.createTicket({
        title: ticketForm.title,
        description: ticketForm.description,
        priority: ticketForm.priority,
        source: 'WEB'
      });
      setShowCreateModal(false);
      setTicketForm({ title: '', description: '', priority: 'MEDIUM' });
      loadTickets();
    } catch (err) {
      console.error('Failed to create ticket:', err);
    }
  };

  const handleOpenPromote = (ticketId: number) => {
    setSelectedTicketForIncident(ticketId);
    setIncidentForm({
      impact: 'MEDIUM',
      urgency: 'MEDIUM',
      category: 'Software',
      subcategory: 'Application Bug',
      majorIncident: false
    });
    setShowIncidentModal(true);
  };

  const handleCreateIncident = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTicketForIncident) return;
    try {
      await incidentService.createIncident({
        ...incidentForm,
        ticketIds: [selectedTicketForIncident]
      });
      setShowIncidentModal(false);
      setSelectedTicketForIncident(null);
      setActiveTab('incidents');
    } catch (err) {
      console.error('Failed to promote ticket to incident:', err);
    }
  };

  const handleOpenAssign = (ticketId: number) => {
    setSelectedTicketForAssign(ticketId);
    setAssignForm({ teamId: '', agentId: '' });
    setTeamMembers([]);
    setShowAssignModal(true);
  };

  const handleTeamChange = async (teamId: string) => {
    setAssignForm({ teamId, agentId: '' });
    if (teamId) {
      try {
        const members = await teamService.getTeamMembers(Number(teamId));
        setTeamMembers(members || []);
      } catch (err) {
        console.error('Failed to load team members:', err);
      }
    } else {
      setTeamMembers([]);
    }
  };

  const handleAssignTicket = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedTicketForAssign) return;
    try {
      await ticketService.assignTicket(
        selectedTicketForAssign,
        assignForm.teamId ? Number(assignForm.teamId) : undefined,
        assignForm.agentId ? Number(assignForm.agentId) : undefined
      );
      setShowAssignModal(false);
      setSelectedTicketForAssign(null);
      loadTickets();
    } catch (err) {
      console.error('Failed to assign ticket:', err);
    }
  };

  const handleIncidentToggle = (id: number) => {
    setSelectedIncidentsForProblem(prev => {
      if (prev.includes(id)) {
        return prev.filter(item => item !== id);
      } else {
        return [...prev, id];
      }
    });
  };

  const handleCreateProblem = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await problemService.createProblem({
        title: problemForm.title,
        description: problemForm.description,
        incidentIds: selectedIncidentsForProblem
      });
      setShowProblemModal(false);
      setProblemForm({ title: '', description: '' });
      setSelectedIncidentsForProblem([]);
      setActiveTab('problems');
    } catch (err) {
      console.error('Failed to create problem investigation:', err);
    }
  };

  const handleOpenInvestigate = (problem: any) => {
    setSelectedProblemForInvestigate(problem);
    setInvestigationForm({
      rootCause: problem.rootCause || '',
      workaround: problem.workaround || '',
      status: problem.status
    });
    setShowInvestigationModal(true);
  };

  const handleSaveInvestigation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProblemForInvestigate) return;
    try {
      await problemService.updateInvestigation(
        selectedProblemForInvestigate.id,
        investigationForm
      );
      setShowInvestigationModal(false);
      setSelectedProblemForInvestigate(null);
      loadProblems();
    } catch (err) {
      console.error('Failed to save investigation details:', err);
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL': return 'var(--danger-500, #ef4444)';
      case 'HIGH': return 'var(--warning-500, #f59e0b)';
      case 'MEDIUM': return 'var(--primary-400, #60a5fa)';
      default: return 'var(--text-muted, #94a3b8)';
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'NEW': return <span className="badge badge-primary">New</span>;
      case 'OPEN': return <span className="badge badge-warning">Open</span>;
      case 'IN_PROGRESS': return <span className="badge badge-info">In Progress</span>;
      case 'RESOLVED': return <span className="badge badge-success">Resolved</span>;
      case 'CLOSED': return <span className="badge badge-light">Closed</span>;
      default: return <span className="badge">{status}</span>;
    }
  };

  return (
    <div className="tickets-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">ITSM Operations</h1>
          <p className="page-subtitle">Manage support tickets, promote critical outages to incidents, and run root-cause investigations.</p>
        </div>
        {activeTab === 'tickets' && (
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <Plus size={18} /> New Ticket
          </button>
        )}
        {activeTab === 'problems' && (
          <button className="btn btn-primary" onClick={() => {
            loadIncidents();
            setShowProblemModal(true);
          }}>
            <Plus size={18} /> New Problem
          </button>
        )}
      </div>

      {/* Tab Navigation */}
      <div className="tabs-container">
        <button 
          className={`tab-btn ${activeTab === 'tickets' ? 'active' : ''}`}
          onClick={() => setActiveTab('tickets')}
        >
          <Ticket size={16} /> Tickets
        </button>
        <button 
          className={`tab-btn ${activeTab === 'incidents' ? 'active' : ''}`}
          onClick={() => setActiveTab('incidents')}
        >
          <ShieldAlert size={16} /> Incidents
        </button>
        <button 
          className={`tab-btn ${activeTab === 'problems' ? 'active' : ''}`}
          onClick={() => setActiveTab('problems')}
        >
          <AlertTriangle size={16} /> Problems
        </button>
      </div>

      {activeTab === 'tickets' && (
        <>
          <div className="tickets-toolbar">
            <div className="toolbar-search">
              <Search size={16} className="toolbar-search-icon" />
              <input
                type="text"
                placeholder="Search tickets..."
                className="form-input toolbar-search-input"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <select 
              className="form-select" 
              style={{ width: 180 }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="NEW">New</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>

          <div className="ticket-list">
            {loadingTickets ? (
              <div className="card skeleton" style={{ height: 100 }}></div>
            ) : tickets.length === 0 ? (
              <div className="card empty-state">
                <Ticket size={48} className="empty-icon" />
                <h3>No tickets found</h3>
                <p>There are no tickets matching your current filters.</p>
              </div>
            ) : (
              tickets.map(ticket => (
                <div key={ticket.id} className="ticket-card card hover-lift">
                  <div className="ticket-card-left">
                    <div className="ticket-priority-indicator" style={{ backgroundColor: getPriorityColor(ticket.priority) }} />
                    <div className="ticket-content">
                      <div className="ticket-header-row">
                        <span className="ticket-number" style={{ cursor: 'pointer', textDecoration: 'underline' }} onClick={() => navigate(`/app/tickets/${ticket.id}`)}>
                          {ticket.ticketNumber}
                        </span>
                        {getStatusBadge(ticket.status)}
                        <span className="ticket-type">{ticket.source}</span>
                      </div>
                      <h3 className="ticket-title" style={{ cursor: 'pointer' }} onClick={() => navigate(`/app/tickets/${ticket.id}`)}>
                        {ticket.title}
                      </h3>
                      <p className="ticket-card-desc">{ticket.description}</p>
                      <div className="ticket-meta">
                        <span><Clock size={14}/> Created {new Date(ticket.createdAt).toLocaleDateString()}</span>
                        <span>• Priority: {ticket.priority}</span>
                        {ticket.assignedUserName && <span>• Assigned: {ticket.assignedUserName}</span>}
                      </div>
                    </div>
                  </div>
                  <div className="ticket-card-right actions-row">
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => handleOpenAssign(ticket.id)}
                      title="Assign Ticket"
                    >
                      <UserCheck size={14} /> Assign
                    </button>
                    <button 
                      className="btn btn-warning btn-sm"
                      onClick={() => handleOpenPromote(ticket.id)}
                      title="Promote to Incident"
                    >
                      <Sparkles size={14} /> Promote
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </>
      )}

      {activeTab === 'incidents' && (
        <div className="incident-list">
          {loadingIncidents ? (
            <div className="card skeleton" style={{ height: 120 }}></div>
          ) : incidents.length === 0 ? (
            <div className="card empty-state">
              <ShieldAlert size={48} className="empty-icon text-success" />
              <h3>All Quiet</h3>
              <p>No active incidents or service outages reported.</p>
            </div>
          ) : (
            incidents.map(incident => (
              <div key={incident.id} className="incident-card card">
                <div className="incident-details">
                  <div className="incident-title-row">
                    <h3>Incident #{incident.id}</h3>
                    <div className="badge-row">
                      <span className="badge badge-danger">Impact: {incident.impact}</span>
                      <span className="badge badge-warning">Urgency: {incident.urgency}</span>
                      {incident.majorIncident && <span className="badge badge-danger animate-pulse">MAJOR OUTAGE</span>}
                    </div>
                  </div>
                  <div className="incident-info-row">
                    <strong>Category:</strong> {incident.category} {incident.subcategory && `> ${incident.subcategory}`}
                  </div>
                  
                  {incident.tickets && incident.tickets.length > 0 && (
                    <div className="linked-tickets-list">
                      <strong>Linked Tickets:</strong>
                      {incident.tickets.map((t: any) => (
                        <span key={t.id} className="badge badge-light">{t.ticketNumber}</span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {activeTab === 'problems' && (
        <div className="problem-list">
          {loadingProblems ? (
            <div className="card skeleton" style={{ height: 120 }}></div>
          ) : problems.length === 0 ? (
            <div className="card empty-state">
              <AlertTriangle size={48} className="empty-icon text-muted" />
              <h3>No Problems Logged</h3>
              <p>Create a problem request to investigate recurrent tickets or structural infrastructure bugs.</p>
            </div>
          ) : (
            problems.map(problem => (
              <div key={problem.id} className="problem-card card hover-lift">
                <div className="problem-main">
                  <div className="problem-details">
                    <div className="problem-title-row">
                      <h3>Problem ID: PRB-{problem.id} — {problem.title}</h3>
                      <span className={`badge ${problem.status === 'CLOSED' ? 'badge-success' : 'badge-warning'}`}>
                        {problem.status}
                      </span>
                    </div>
                    <p className="problem-desc">{problem.description}</p>
                    
                    <div className="problem-investigation-details">
                      <div><strong>Root Cause:</strong> {problem.rootCause || 'Under Investigation...'}</div>
                      <div><strong>Workaround:</strong> {problem.workaround || 'None Documented'}</div>
                    </div>
                  </div>
                  <div className="problem-actions">
                    <button className="btn btn-secondary btn-sm" onClick={() => handleOpenInvestigate(problem)}>
                      Investigate / Resolve
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Raise Ticket Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal-content card">
            <h3 className="modal-title">Raise New Ticket</h3>
            <form onSubmit={handleCreateTicket}>
              <div className="form-group">
                <label>Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Printer offline on 3rd floor"
                  className="form-input"
                  value={ticketForm.title}
                  onChange={(e) => setTicketForm({ ...ticketForm, title: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  required
                  rows={4}
                  placeholder="Provide details about the issue..."
                  className="form-input"
                  value={ticketForm.description}
                  onChange={(e) => setTicketForm({ ...ticketForm, description: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Priority</label>
                <select
                  className="form-select"
                  value={ticketForm.priority}
                  onChange={(e) => setTicketForm({ ...ticketForm, priority: e.target.value })}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-ghost" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Submit Ticket
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Promote to Incident Modal */}
      {showIncidentModal && (
        <div className="modal-overlay">
          <div className="modal-content card glassmorphism-change-modal">
            <h3 className="modal-title" style={{ color: '#ffffff' }}>Promote Ticket to Incident</h3>
            <form onSubmit={handleCreateIncident}>
              <div className="form-grid-2">
                <div className="form-group">
                  <label>Impact</label>
                  <select
                    className="form-select"
                    value={incidentForm.impact}
                    onChange={(e) => setIncidentForm({ ...incidentForm, impact: e.target.value })}
                  >
                    <option value="LOW">Low (Single User)</option>
                    <option value="MEDIUM">Medium (Department)</option>
                    <option value="HIGH">High (Entire Business)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Urgency</label>
                  <select
                    className="form-select"
                    value={incidentForm.urgency}
                    onChange={(e) => setIncidentForm({ ...incidentForm, urgency: e.target.value })}
                  >
                    <option value="LOW">Low (Workaround available)</option>
                    <option value="MEDIUM">Medium (No Workaround, low impact)</option>
                    <option value="HIGH">High (Immediate resolution required)</option>
                  </select>
                </div>
              </div>

              <div className="form-grid-2">
                <div className="form-group">
                  <label>Category</label>
                  <input
                    type="text"
                    required
                    className="form-input"
                    value={incidentForm.category}
                    onChange={(e) => setIncidentForm({ ...incidentForm, category: e.target.value })}
                  />
                </div>

                <div className="form-group">
                  <label>Subcategory</label>
                  <input
                    type="text"
                    className="form-input"
                    value={incidentForm.subcategory}
                    onChange={(e) => setIncidentForm({ ...incidentForm, subcategory: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="ticket-checkbox-item">
                  <input
                    type="checkbox"
                    checked={incidentForm.majorIncident}
                    onChange={(e) => setIncidentForm({ ...incidentForm, majorIncident: e.target.checked })}
                  />
                  <span>Mark as Major Outage / Incident</span>
                </label>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowIncidentModal(false);
                  setSelectedTicketForIncident(null);
                }}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Promote Now
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Assign Ticket Modal */}
      {showAssignModal && (
        <div className="modal-overlay">
          <div className="modal-content card glassmorphism-change-modal">
            <h3 className="modal-title" style={{ color: '#ffffff' }}>Assign Ticket</h3>
            <form onSubmit={handleAssignTicket}>
              <div className="form-group">
                <label>Team</label>
                <select
                  className="form-select"
                  value={assignForm.teamId}
                  onChange={(e) => handleTeamChange(e.target.value)}
                >
                  <option value="">Select a Team...</option>
                  {teams.map(t => (
                    <option key={t.id} value={t.id}>{t.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Agent / User</label>
                <select
                  className="form-select"
                  value={assignForm.agentId}
                  onChange={(e) => setAssignForm({ ...assignForm, agentId: e.target.value })}
                  disabled={!assignForm.teamId}
                >
                  <option value="">Select an Agent...</option>
                  {teamMembers.map(a => (
                    <option key={a.id} value={a.id}>
                      {a.firstName || a.lastName ? `${a.firstName || ''} ${a.lastName || ''}`.trim() : a.username} ({a.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowAssignModal(false);
                  setSelectedTicketForAssign(null);
                }}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Assign
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Raise Problem Modal */}
      {showProblemModal && (
        <div className="modal-overlay">
          <div className="modal-content card glassmorphism-change-modal">
            <h3 className="modal-title" style={{ color: '#ffffff' }}>New Problem Investigation</h3>
            <form onSubmit={handleCreateProblem}>
              <div className="form-group">
                <label>Problem Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Recurrent DB Timeout"
                  className="form-input"
                  value={problemForm.title}
                  onChange={(e) => setProblemForm({ ...problemForm, title: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Scope Description</label>
                <textarea
                  required
                  rows={3}
                  placeholder="Describe the overall symptoms and scope of this issue..."
                  className="form-input"
                  value={problemForm.description}
                  onChange={(e) => setProblemForm({ ...problemForm, description: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Link Active Incidents</label>
                <div className="linked-tickets-selector" style={{ maxHeight: 150 }}>
                  {incidents.map(inc => (
                    <label key={inc.id} className="ticket-checkbox-item">
                      <input
                        type="checkbox"
                        checked={selectedIncidentsForProblem.includes(inc.id)}
                        onChange={() => handleIncidentToggle(inc.id)}
                      />
                      <span>Incident #{inc.id} — Category: {inc.category} (Impact: {inc.impact})</span>
                    </label>
                  ))}
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowProblemModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Create Problem
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Investigate Problem Modal */}
      {showInvestigationModal && (
        <div className="modal-overlay">
          <div className="modal-content card glassmorphism-change-modal">
            <h3 className="modal-title" style={{ color: '#ffffff' }}>Update Investigation - {selectedProblemForInvestigate?.title}</h3>
            <form onSubmit={handleSaveInvestigation}>
              <div className="form-group">
                <label>Root Cause</label>
                <textarea
                  rows={3}
                  placeholder="Describe the underlying system configuration or hardware issue."
                  className="form-input"
                  value={investigationForm.rootCause}
                  onChange={(e) => setInvestigationForm({ ...investigationForm, rootCause: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Workaround (Temp Fix)</label>
                <textarea
                  rows={2}
                  placeholder="Describe standard steps support agents can offer users until fixed."
                  className="form-input"
                  value={investigationForm.workaround}
                  onChange={(e) => setInvestigationForm({ ...investigationForm, workaround: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label>Investigation Status</label>
                <select
                  className="form-select"
                  value={investigationForm.status}
                  onChange={(e) => setInvestigationForm({ ...investigationForm, status: e.target.value })}
                >
                  <option value="INVESTIGATING">Investigating</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="CLOSED">Closed (Known Error Documented)</option>
                </select>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowInvestigationModal(false);
                  setSelectedProblemForInvestigate(null);
                }}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Save Details
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
