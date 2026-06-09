import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ticketService } from '../../services/dataService';
import { useAuthStore } from '../../store/authStore';
import {
  ArrowLeft, Clock, User, Users, CheckCircle2, AlertCircle,
  MessageSquare, History, Send, Lock, Unlock, AlertTriangle, ShieldAlert
} from 'lucide-react';
import './TicketDetail.css';

interface Comment {
  id: number;
  ticketId: number;
  authorName: string;
  authorEmail: string;
  body: string;
  isInternal: boolean;
  createdAt: string;
}

interface Activity {
  id: number;
  ticketId: number;
  actorName: string;
  activityType: string;
  oldValue: string;
  newValue: string;
  description: string;
  occurredAt: string;
}

interface TicketDetail {
  id: number;
  ticketNumber: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  source: string;
  requesterName: string;
  requesterEmail: string;
  assignedTeamName: string | null;
  assignedUserName: string | null;
  createdAt: string;
  updatedAt: string | null;
  responseDeadline: string | null;
  resolutionDeadline: string | null;
  isResponseBreached: boolean;
  isResolutionBreached: boolean;
  minutesUntilResolutionDeadline: number | null;
}

export default function TicketDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const isAgentOrAdmin = user?.roles.some(r =>
    ['ADMINISTRATOR', 'SUPPORT_AGENT', 'TEAM_LEAD', 'SUPER_ADMIN'].includes(r)
  ) || false;

  const [ticket, setTicket] = useState<TicketDetail | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [commentFilter, setCommentFilter] = useState<'all' | 'public' | 'internal'>('all');
  const [newComment, setNewComment] = useState('');
  const [isInternal, setIsInternal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [commenting, setCommenting] = useState(false);
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadAllData();
    }
  }, [id]);

  const loadAllData = async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const ticketId = Number(id);
      const detail = await ticketService.getTicketDetail(ticketId);
      setTicket(detail);

      const commentData = await ticketService.getComments(ticketId);
      setComments(commentData || []);

      const activityData = await ticketService.getActivities(ticketId);
      setActivities(activityData || []);
    } catch (err) {
      console.error('Failed to load ticket detail:', err);
      setError('Could not retrieve ticket details. Please make sure the ticket exists.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !newComment.trim()) return;

    setCommenting(true);
    try {
      const ticketId = Number(id);
      const added = await ticketService.addComment(ticketId, newComment.trim(), isInternal);
      setComments(prev => [...prev, added]);
      setNewComment('');
      setIsInternal(false);

      // Reload activities
      const activityData = await ticketService.getActivities(ticketId);
      setActivities(activityData || []);
    } catch (err) {
      console.error('Failed to post comment:', err);
    } finally {
      setCommenting(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    if (!id || !ticket) return;
    setUpdatingStatus(true);
    try {
      const ticketId = Number(id);
      await ticketService.updateStatus(ticketId, newStatus);
      
      // Reload ticket detail and activities
      const detail = await ticketService.getTicketDetail(ticketId);
      setTicket(detail);
      const activityData = await ticketService.getActivities(ticketId);
      setActivities(activityData || []);
    } catch (err) {
      console.error('Failed to update ticket status:', err);
    } finally {
      setUpdatingStatus(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <div className="spinner">Loading Ticket details...</div>
      </div>
    );
  }

  if (error || !ticket) {
    return (
      <div className="ticket-detail-container">
        <button onClick={() => navigate('/app/tickets')} className="btn btn-secondary" style={{ width: 'fit-content' }}>
          <ArrowLeft size={16} /> Back to Tickets
        </button>
        <div className="detail-card" style={{ marginTop: '20px', alignItems: 'center', padding: '40px' }}>
          <AlertCircle size={48} color="var(--danger-500)" style={{ marginBottom: '16px' }} />
          <h3>Error Loading Ticket</h3>
          <p style={{ color: 'var(--text-secondary)' }}>{error || 'Ticket not found'}</p>
        </div>
      </div>
    );
  }

  // Filter comments
  const filteredComments = comments.filter(c => {
    if (commentFilter === 'public') return !c.isInternal;
    if (commentFilter === 'internal') return c.isInternal;
    return true;
  });

  const getSlaTimeLabel = (mins: number) => {
    if (mins < 0) return `Breached by ${Math.abs(mins)}m`;
    const hours = Math.floor(mins / 60);
    const remainingMins = mins % 60;
    if (hours > 0) {
      return `${hours}h ${remainingMins}m remaining`;
    }
    return `${remainingMins}m remaining`;
  };

  return (
    <div className="ticket-detail-container">
      {/* Top Header */}
      <div className="ticket-detail-header">
        <div className="ticket-header-title-area">
          <div className="ticket-header-meta">
            <button onClick={() => navigate('/app/tickets')} className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              <ArrowLeft size={14} /> Back
            </button>
            <span className="ticket-number">{ticket.ticketNumber}</span>
            <span className={`badge-detail priority-${ticket.priority.toLowerCase()}`}>
              {ticket.priority} Priority
            </span>
            <span className={`badge-detail status-${ticket.status.toLowerCase()}`}>
              {ticket.status.replace('_', ' ')}
            </span>
          </div>
          <h2 className="ticket-detail-title">{ticket.title}</h2>
        </div>

        {/* Action Controls for Support/Admins */}
        {isAgentOrAdmin && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <label style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>Status:</label>
            <select
              value={ticket.status}
              onChange={(e) => handleStatusChange(e.target.value)}
              disabled={updatingStatus}
              className="form-select"
              style={{ minWidth: '160px', padding: '6px 12px' }}
            >
              <option value="NEW">New</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="ON_HOLD">On Hold</option>
              <option value="RESOLVED">Resolved</option>
              <option value="CLOSED">Closed</option>
            </select>
          </div>
        )}
      </div>

      {/* Main Grid */}
      <div className="ticket-detail-body">
        
        {/* Left Column: Description & Comments */}
        <div className="ticket-main-section">
          
          {/* Description */}
          <div className="detail-card">
            <h3 className="detail-card-title">Description</h3>
            <div className="ticket-desc-text">{ticket.description}</div>
          </div>

          {/* Conversation (Comments) */}
          <div className="detail-card">
            <div className="comment-tabs">
              <button
                className={`comment-tab-btn ${commentFilter === 'all' ? 'active-public' : ''}`}
                onClick={() => setCommentFilter('all')}
              >
                All Comments ({comments.length})
              </button>
              <button
                className={`comment-tab-btn ${commentFilter === 'public' ? 'active-public' : ''}`}
                onClick={() => setCommentFilter('public')}
              >
                Public ({comments.filter(c => !c.isInternal).length})
              </button>
              {isAgentOrAdmin && (
                <button
                  className={`comment-tab-btn ${commentFilter === 'internal' ? 'active-internal' : ''}`}
                  onClick={() => setCommentFilter('internal')}
                >
                  <Lock size={12} /> Internal Notes ({comments.filter(c => c.isInternal).length})
                </button>
              )}
            </div>

            <div className="comments-list">
              {filteredComments.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '24px 0', color: 'var(--text-muted)' }}>
                  <MessageSquare size={32} style={{ opacity: 0.3, marginBottom: '8px' }} />
                  <p>No comments found in this filter.</p>
                </div>
              ) : (
                filteredComments.map(comment => (
                  <div key={comment.id} className={`comment-card ${comment.isInternal ? 'internal' : ''}`}>
                    <div className="comment-header">
                      <div className="comment-author">
                        <User size={14} />
                        <span>{comment.authorName}</span>
                        <span className="comment-author-email">({comment.authorEmail})</span>
                        {comment.isInternal && (
                          <span className="badge-detail status-on-hold" style={{ fontSize: '10px', padding: '1px 6px', display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                            <Lock size={10} /> Internal Note
                          </span>
                        )}
                      </div>
                      <span className="comment-date">
                        {new Date(comment.createdAt).toLocaleString()}
                      </span>
                    </div>
                    <div className="comment-body">{comment.body}</div>
                  </div>
                ))
              )}
            </div>

            {/* Comment Form */}
            <form onSubmit={handleAddComment} className="comment-form">
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder={isInternal ? "Write an internal note..." : "Reply to requester..."}
                required
                className="comment-input"
              />
              <div className="comment-form-actions">
                {isAgentOrAdmin ? (
                  <label className="internal-note-toggle">
                    <input
                      type="checkbox"
                      checked={isInternal}
                      onChange={(e) => setIsInternal(e.target.checked)}
                    />
                    {isInternal ? <Lock size={14} color="var(--warning-500)" /> : <Unlock size={14} />}
                    <span>Internal Note (Agents only)</span>
                  </label>
                ) : <div />}
                
                <button type="submit" disabled={commenting || !newComment.trim()} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                  <Send size={14} />
                  {commenting ? 'Sending...' : 'Send'}
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Right Column: Properties & SLA & Activities */}
        <div className="ticket-sidebar-section">
          
          {/* Ticket Properties */}
          <div className="detail-card">
            <h3 className="detail-card-title"><Users size={16} /> Properties</h3>
            <div className="property-list">
              <div className="property-item">
                <span className="property-label">Requester</span>
                <span className="property-value">{ticket.requesterName}</span>
              </div>
              <div className="property-item">
                <span className="property-label">Email</span>
                <span className="property-value" style={{ wordBreak: 'break-all' }}>{ticket.requesterEmail}</span>
              </div>
              <div className="property-item">
                <span className="property-label">Assigned Team</span>
                <span className="property-value">{ticket.assignedTeamName || 'Unassigned'}</span>
              </div>
              <div className="property-item">
                <span className="property-label">Assigned Agent</span>
                <span className="property-value">{ticket.assignedUserName || 'Unassigned'}</span>
              </div>
              <div className="property-item">
                <span className="property-label">Source</span>
                <span className="property-value">{ticket.source}</span>
              </div>
              <div className="property-item">
                <span className="property-label">Created At</span>
                <span className="property-value">
                  {new Date(ticket.createdAt).toLocaleString()}
                </span>
              </div>
            </div>
          </div>

          {/* SLA Tracking Metrics */}
          <div className="detail-card">
            <h3 className="detail-card-title"><Clock size={16} /> SLA Tracking</h3>
            <div className="sla-metrics">
              {/* Response SLA */}
              {ticket.responseDeadline ? (
                <div className={`sla-metric-box ${ticket.isResponseBreached ? 'breached' : ''}`}>
                  <div className="sla-metric-info">
                    <span className="sla-metric-name">First Response SLA</span>
                    <span className="sla-metric-deadline">
                      Deadline: {new Date(ticket.responseDeadline).toLocaleString()}
                    </span>
                  </div>
                  <div className="sla-metric-status">
                    {ticket.isResponseBreached ? (
                      <span className="sla-status-badge breached">
                        <ShieldAlert size={12} /> Breached
                      </span>
                    ) : (
                      <span className="sla-status-badge ok">
                        <CheckCircle2 size={12} /> Active
                      </span>
                    )}
                  </div>
                </div>
              ) : (
                <div style={{ fontStyle: 'italic', fontSize: '13px', color: 'var(--text-muted)' }}>
                  No Response SLA configured.
                </div>
              )}

              {/* Resolution SLA */}
              {ticket.resolutionDeadline ? (
                <div className={`sla-metric-box ${
                  ticket.isResolutionBreached ? 'breached' : 
                  ticket.minutesUntilResolutionDeadline !== null && ticket.minutesUntilResolutionDeadline < 60 ? 'warning' : ''
                }`}>
                  <div className="sla-metric-info">
                    <span className="sla-metric-name">Resolution SLA</span>
                    <span className="sla-metric-deadline">
                      Deadline: {new Date(ticket.resolutionDeadline).toLocaleString()}
                    </span>
                  </div>
                  <div className="sla-metric-status">
                    {ticket.isResolutionBreached ? (
                      <span className="sla-status-badge breached">
                        <ShieldAlert size={12} /> Breached
                      </span>
                    ) : (
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
                        <span className="sla-status-badge ok">
                          <CheckCircle2 size={12} /> Active
                        </span>
                        {ticket.minutesUntilResolutionDeadline !== null && (
                          <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 600 }}>
                            {getSlaTimeLabel(ticket.minutesUntilResolutionDeadline)}
                          </span>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              ) : (
                <div style={{ fontStyle: 'italic', fontSize: '13px', color: 'var(--text-muted)' }}>
                  No Resolution SLA configured.
                </div>
              )}
            </div>
          </div>

          {/* Audit Logs / Activity History */}
          <div className="detail-card">
            <h3 className="detail-card-title"><History size={16} /> Audit History</h3>
            <div className="activity-feed">
              {activities.length === 0 ? (
                <div style={{ fontStyle: 'italic', fontSize: '13px', color: 'var(--text-muted)', textAlign: 'center', padding: '16px' }}>
                  No activity recorded yet.
                </div>
              ) : (
                activities.map(act => (
                  <div key={act.id} className="activity-item">
                    <div className="activity-icon-container">
                      <div className="activity-icon">
                        {act.activityType === 'STATUS_CHANGE' && <AlertTriangle size={12} />}
                        {act.activityType === 'ASSIGNED' && <Users size={12} />}
                        {act.activityType === 'COMMENT_ADDED' && <MessageSquare size={12} />}
                      </div>
                      <div className="activity-line" />
                    </div>
                    <div className="activity-content">
                      <span className="activity-desc">{act.description}</span>
                      <div className="activity-meta">
                        <span>By {act.actorName}</span>
                        <span>•</span>
                        <span>{new Date(act.occurredAt).toLocaleTimeString()}</span>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

        </div>

      </div>
    </div>
  );
}
