import { useEffect, useState } from 'react';
import { approvalService } from '../../services/dataService';
import { CheckCircle2, XCircle, Clock, FileText, CornerDownRight, MessageSquare } from 'lucide-react';
import './Approvals.css';

export default function ApprovalsPage() {
  const [approvals, setApprovals] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [remarks, setRemarks] = useState<{ [key: number]: string }>({});

  useEffect(() => {
    loadApprovals();
  }, []);

  const loadApprovals = async () => {
    setLoading(true);
    try {
      const data = await approvalService.getPendingApprovals();
      setApprovals(data);
    } catch (err) {
      console.error('Failed to load approvals:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDecision = async (id: number, decision: string) => {
    const comment = remarks[id] || `${decision === 'APPROVED' ? 'Approved' : 'Rejected'} from system portal`;
    try {
      await approvalService.makeDecision(id, decision, comment);
      // clear remark
      setRemarks(prev => {
        const copy = { ...prev };
        delete copy[id];
        return copy;
      });
      loadApprovals();
    } catch (err) {
      console.error('Decision failed:', err);
    }
  };

  const handleRemarkChange = (id: number, val: string) => {
    setRemarks(prev => ({ ...prev, [id]: val }));
  };

  return (
    <div className="approvals-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Approvals</h1>
          <p className="page-subtitle">Review, comment on, and authorize Change Requests and Purchasing Catalog orders.</p>
        </div>
      </div>

      <div className="approvals-list">
        {loading ? (
          <div className="card skeleton" style={{ height: 120 }}></div>
        ) : approvals.length === 0 ? (
          <div className="card empty-state">
            <CheckCircle2 size={48} className="empty-icon text-success" />
            <h3>All Caught Up!</h3>
            <p>You have no pending approvals requiring your authorization at this time.</p>
          </div>
        ) : (
          approvals.map(approval => (
            <div key={approval.id} className="approval-card card hover-lift">
              <div className="approval-main-content">
                <div className="approval-header-row">
                  <div className="approval-icon-box">
                    <FileText size={20} className="text-primary" />
                  </div>
                  <div className="approval-meta-info">
                    <h3 className="approval-title">
                      {approval.entityType} Request #{approval.entityId}
                    </h3>
                    <div className="approval-subtext">
                      <span className="badge badge-light">Step {approval.stepIndex}</span>
                      <span>• Policy: {approval.policy?.name || 'Standard Approval Policy'}</span>
                    </div>
                  </div>
                </div>

                {/* Input for comments/remarks */}
                <div className="remarks-box">
                  <MessageSquare size={14} className="remarks-icon" />
                  <input
                    type="text"
                    placeholder="Provide decision remarks (optional)..."
                    value={remarks[approval.id] || ''}
                    onChange={(e) => handleRemarkChange(approval.id, e.target.value)}
                    className="remarks-input"
                  />
                </div>
              </div>

              <div className="approval-actions">
                <button 
                  className="btn btn-danger btn-sm"
                  onClick={() => handleDecision(approval.id, 'REJECTED')}
                >
                  <XCircle size={16} /> Reject
                </button>
                <button 
                  className="btn btn-primary btn-sm"
                  onClick={() => handleDecision(approval.id, 'APPROVED')}
                >
                  <CheckCircle2 size={16} /> Approve
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
