import React, { useEffect, useState } from 'react';
import { systemSettingsService } from '../../services/dataService';
import { Settings, Clock, Calendar, CheckCircle2, AlertCircle, Save } from 'lucide-react';

interface SettingItem {
  id: number;
  key: string;
  value: string;
  description: string;
}

export default function SettingsPage() {
  const [settingsList, setSettingsList] = useState<SettingItem[]>([]);
  const [settingsForm, setSettingsForm] = useState<Record<string, string>>({});
  const [activeTab, setActiveTab] = useState<'general' | 'sla'>('general');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await systemSettingsService.getSettings();
      setSettingsList(list || []);

      const formObj: Record<string, string> = {};
      list.forEach((item: SettingItem) => {
        formObj[item.key] = item.value;
      });
      setSettingsForm(formObj);
    } catch (err) {
      console.error('Failed to load settings:', err);
      setError('Failed to fetch settings from server. Check your administrative permission levels.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (key: string, value: string) => {
    setSettingsForm(prev => ({
      ...prev,
      [key]: value
    }));
    setSuccess(null);
  };

  const handleSaveSettings = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await systemSettingsService.updateSettings(settingsForm);
      setSuccess('System configuration parameters saved successfully.');
    } catch (err) {
      console.error('Failed to save settings:', err);
      setError('Failed to save changes. Make sure values follow required formats.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <div className="spinner">Loading system settings...</div>
      </div>
    );
  }

  return (
    <div className="container" style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '1000px', margin: '0 auto', padding: '8px 16px' }}>
      
      {/* Title */}
      <div className="page-header" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '16px' }}>
        <div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Settings size={28} color="var(--primary-400)" />
            Global Settings & SLAs
          </h1>
          <p className="page-subtitle">Configure business operational rules, SLA response deadlines, and calendar configurations.</p>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <AlertCircle size={18} />
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <CheckCircle2 size={18} />
          {success}
        </div>
      )}

      <form onSubmit={handleSaveSettings} style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: '30px' }}>
        
        {/* Navigation Sidebar */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'general' ? 'active' : ''}`}
            onClick={() => setActiveTab('general')}
            style={{ 
              display: 'inline-flex', alignItems: 'center', gap: '10px', 
              padding: '12px 16px', border: 'none', background: 'transparent', 
              cursor: 'pointer', fontWeight: 600, width: '100%', borderRadius: '6px',
              textAlign: 'left',
              color: activeTab === 'general' ? 'var(--primary-400)' : 'var(--text-secondary)'
            }}
          >
            <Calendar size={16} />
            Business Hours
          </button>
          <button
            type="button"
            className={`tab-btn ${activeTab === 'sla' ? 'active' : ''}`}
            onClick={() => setActiveTab('sla')}
            style={{ 
              display: 'inline-flex', alignItems: 'center', gap: '10px', 
              padding: '12px 16px', border: 'none', background: 'transparent', 
              cursor: 'pointer', fontWeight: 600, width: '100%', borderRadius: '6px',
              textAlign: 'left',
              color: activeTab === 'sla' ? 'var(--primary-400)' : 'var(--text-secondary)'
            }}
          >
            <Clock size={16} />
            SLA Parameters
          </button>
        </div>

        {/* Form Fields Card */}
        <div className="card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {activeTab === 'general' && (
            <>
              <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', margin: 0 }}>
                Operations Calendar
              </h3>
              
              <div className="form-group">
                <label>Operational Hours Start</label>
                <input
                  type="time"
                  className="form-input"
                  value={settingsForm['business_hours_start'] || '09:00'}
                  onChange={(e) => handleInputChange('business_hours_start', e.target.value)}
                  required
                />
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Tickets arriving before this time won't begin SLA consumption until start time.
                </span>
              </div>

              <div className="form-group" style={{ marginTop: '12px' }}>
                <label>Operational Hours End</label>
                <input
                  type="time"
                  className="form-input"
                  value={settingsForm['business_hours_end'] || '17:00'}
                  onChange={(e) => handleInputChange('business_hours_end', e.target.value)}
                  required
                />
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Operational business hours endpoint for calculation pauses.
                </span>
              </div>
            </>
          )}

          {activeTab === 'sla' && (
            <>
              <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '12px', margin: 0 }}>
                SLA Milestones
              </h3>

              <div className="form-group">
                <label>Critical Response SLA (minutes)</label>
                <input
                  type="number"
                  min="1"
                  className="form-input"
                  value={settingsForm['sla_response_critical'] || '15'}
                  onChange={(e) => handleInputChange('sla_response_critical', e.target.value)}
                  required
                />
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Minutes allowed for the first official support agent reply on Critical severity tickets.
                </span>
              </div>

              <div className="form-group" style={{ marginTop: '12px' }}>
                <label>Critical Resolution SLA (minutes)</label>
                <input
                  type="number"
                  min="1"
                  className="form-input"
                  value={settingsForm['sla_resolution_critical'] || '60'}
                  onChange={(e) => handleInputChange('sla_resolution_critical', e.target.value)}
                  required
                />
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  Total minutes permitted to resolve or close a Critical incident.
                </span>
              </div>
            </>
          )}

          {/* Action Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', borderTop: '1px solid var(--border-color)', paddingTop: '20px', marginTop: '10px' }}>
            <button
              type="submit"
              disabled={saving}
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
            >
              <Save size={16} />
              {saving ? 'Saving...' : 'Save Settings'}
            </button>
          </div>

        </div>

      </form>
    </div>
  );
}
