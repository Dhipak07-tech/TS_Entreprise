import { useEffect, useState } from 'react';
import { grcService } from '../../services/dataService';
import { ShieldAlert, AlertTriangle, CheckCircle, Search, Plus } from 'lucide-react';
import './Security.css';

export default function SecurityPage() {
  const [incidents, setIncidents] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [grcStats, setGrcStats] = useState({ openIncidents: 0, vulnerabilities: 0, complianceScore: 0 });

  useEffect(() => {
    loadIncidents();
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const data = await grcService.getSecurityStats();
      setGrcStats(data);
    } catch (err) {
      console.error('Failed to load GRC stats:', err);
    }
  };

  const loadIncidents = async () => {
    setLoading(true);
    try {
      const data = await grcService.getSecurityIncidents();
      setIncidents(data.content);
    } catch (err) {
      console.error('Failed to load incidents:', err);
    } finally {
      setLoading(false);
    }
  };

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return <span className="badge badge-danger"><AlertTriangle size={12}/> Critical</span>;
      case 'HIGH': return <span className="badge badge-warning">High</span>;
      case 'MEDIUM': return <span className="badge badge-primary">Medium</span>;
      case 'LOW': return <span className="badge badge-success">Low</span>;
      default: return <span className="badge">{severity}</span>;
    }
  };

  return (
    <div className="security-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">SecOps & Compliance</h1>
          <p className="page-subtitle">Monitor security incidents, vulnerabilities, and compliance policies.</p>
        </div>
        <button className="btn btn-danger">
          <ShieldAlert size={18} />
          Report Incident
        </button>
      </div>

      <div className="secops-dashboard">
        {/* KPI Cards */}
        <div className="grid grid-cols-3">
          <div className="card stat-card">
            <div className="stat-icon bg-danger-light text-danger">
              <ShieldAlert size={24} />
            </div>
            <div className="stat-info">
              <h3>Open Incidents</h3>
              <div className="stat-value text-danger">{grcStats.openIncidents}</div>
            </div>
          </div>
          <div className="card stat-card">
            <div className="stat-icon bg-warning-light text-warning">
              <AlertTriangle size={24} />
            </div>
            <div className="stat-info">
              <h3>Vulnerabilities</h3>
              <div className="stat-value text-warning">{grcStats.vulnerabilities}</div>
            </div>
          </div>
          <div className="card stat-card">
            <div className="stat-icon bg-success-light text-success">
              <CheckCircle size={24} />
            </div>
            <div className="stat-info">
              <h3>Policy Compliance</h3>
              <div className="stat-value text-success">{grcStats.complianceScore}%</div>
            </div>
          </div>
        </div>

        {/* Incidents Table */}
        <div className="card">
          <div className="card-header border-bottom flex justify-between p-20">
            <h3 className="text-lg font-semibold">Recent Security Incidents</h3>
            <div className="toolbar-search">
              <Search size={16} className="toolbar-search-icon" />
              <input type="text" placeholder="Search incidents..." className="form-input toolbar-search-input" />
            </div>
          </div>
          
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Severity</th>
                  <th>Status</th>
                  <th>Identified At</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={5} className="text-center p-40">Loading...</td>
                  </tr>
                ) : incidents.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="text-center p-40 text-muted">
                      <CheckCircle size={40} className="mb-12 opacity-50" />
                      <p>No security incidents recorded. System is secure.</p>
                    </td>
                  </tr>
                ) : (
                  incidents.map((inc) => (
                    <tr key={inc.id}>
                      <td><span className="font-medium">{inc.incidentNumber}</span></td>
                      <td>{inc.title}</td>
                      <td>{getSeverityBadge(inc.severity)}</td>
                      <td><span className="badge">{inc.status}</span></td>
                      <td>{new Date(inc.identifiedAt).toLocaleDateString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
