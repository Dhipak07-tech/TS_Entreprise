import { useEffect, useState } from 'react';
import { billingService } from '../../services/dataService';
import { CreditCard, CheckCircle2, AlertCircle, Zap, Shield } from 'lucide-react';
import './Billing.css';

export default function BillingPage() {
  const [subscription, setSubscription] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [upgrading, setUpgrading] = useState(false);

  useEffect(() => {
    loadSubscription();
  }, []);

  const loadSubscription = async () => {
    try {
      const data = await billingService.getSubscription();
      setSubscription(data);
    } catch (err) {
      console.error('Failed to load subscription:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpgrade = async (tier: string) => {
    setUpgrading(true);
    try {
      await billingService.upgradePlan(tier);
      await loadSubscription();
    } catch (err) {
      console.error('Failed to upgrade:', err);
    } finally {
      setUpgrading(false);
    }
  };

  const isCurrentPlan = (tier: string) => subscription?.planTier === tier;

  return (
    <div className="billing-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Billing & Subscription</h1>
          <p className="page-subtitle">Manage your SaaS plan, invoices, and payment methods.</p>
        </div>
      </div>

      {loading ? (
        <div className="text-center p-40">Loading billing details...</div>
      ) : (
        <div className="billing-content">
          
          <div className="current-plan-card card">
            <div className="plan-header">
              <div className="plan-title-wrapper">
                <CreditCard size={24} className="text-primary" />
                <div>
                  <h2 className="text-xl font-bold">Current Plan: {subscription.planTier}</h2>
                  <p className="text-muted text-sm">Status: <span className="badge badge-success">{subscription.status}</span></p>
                </div>
              </div>
              <div className="text-right">
                <h3 className="text-2xl font-bold">
                  {subscription.planTier === 'FREE' ? '$0' : subscription.planTier === 'STARTER' ? '$49' : subscription.planTier === 'PROFESSIONAL' ? '$99' : 'Custom'}
                  <span className="text-sm text-muted font-normal"> / month</span>
                </h3>
              </div>
            </div>
            
            <div className="plan-details mt-20 p-20 bg-surface rounded border">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm text-secondary font-medium mb-4">Billing Cycle</p>
                  <p>{subscription.billingCycle}</p>
                </div>
                <div>
                  <p className="text-sm text-secondary font-medium mb-4">Next Payment Date</p>
                  <p>{subscription.currentPeriodEnd ? new Date(subscription.currentPeriodEnd).toLocaleDateString() : 'N/A'}</p>
                </div>
                <button className="btn btn-secondary">Manage Payment Methods</button>
              </div>
            </div>
          </div>

          <h2 className="text-xl font-bold mt-24 mb-16">Available Plans</h2>
          <div className="pricing-grid">
            
            {/* Starter Plan */}
            <div className={`pricing-card card ${isCurrentPlan('STARTER') ? 'active-plan' : ''}`}>
              {isCurrentPlan('STARTER') && <div className="active-badge">Current Plan</div>}
              <div className="pricing-header">
                <h3>Starter</h3>
                <div className="price"><span>$49</span>/mo</div>
                <p>Perfect for small IT teams getting started.</p>
              </div>
              <ul className="feature-list">
                <li><CheckCircle2 size={16} className="text-success" /> Up to 5 Agent Seats</li>
                <li><CheckCircle2 size={16} className="text-success" /> Core ITSM Ticketing</li>
                <li><CheckCircle2 size={16} className="text-success" /> Basic Knowledge Base</li>
              </ul>
              <button 
                className="btn btn-block mt-24"
                disabled={isCurrentPlan('STARTER') || upgrading}
                onClick={() => handleUpgrade('STARTER')}
              >
                {isCurrentPlan('STARTER') ? 'Active' : 'Upgrade to Starter'}
              </button>
            </div>

            {/* Professional Plan */}
            <div className={`pricing-card card popular ${isCurrentPlan('PROFESSIONAL') ? 'active-plan' : ''}`}>
              <div className="popular-badge"><Zap size={14} /> Most Popular</div>
              {isCurrentPlan('PROFESSIONAL') && <div className="active-badge">Current Plan</div>}
              <div className="pricing-header">
                <h3>Professional</h3>
                <div className="price"><span>$99</span>/mo</div>
                <p>Advanced features for growing organizations.</p>
              </div>
              <ul className="feature-list">
                <li><CheckCircle2 size={16} className="text-primary" /> Up to 20 Agent Seats</li>
                <li><CheckCircle2 size={16} className="text-primary" /> Approval Workflows</li>
                <li><CheckCircle2 size={16} className="text-primary" /> Full CMDB & Asset Tracking</li>
                <li><CheckCircle2 size={16} className="text-primary" /> White-label Portal</li>
              </ul>
              <button 
                className={`btn btn-block mt-24 ${isCurrentPlan('PROFESSIONAL') ? 'btn-secondary' : 'btn-primary'}`}
                disabled={isCurrentPlan('PROFESSIONAL') || upgrading}
                onClick={() => handleUpgrade('PROFESSIONAL')}
              >
                {isCurrentPlan('PROFESSIONAL') ? 'Active' : 'Upgrade to Professional'}
              </button>
            </div>

            {/* Enterprise Plan */}
            <div className={`pricing-card card ${isCurrentPlan('ENTERPRISE') ? 'active-plan' : ''}`}>
              {isCurrentPlan('ENTERPRISE') && <div className="active-badge">Current Plan</div>}
              <div className="pricing-header">
                <h3>Enterprise</h3>
                <div className="price"><span>Custom</span></div>
                <p>Maximum security and scale for large orgs.</p>
              </div>
              <ul className="feature-list">
                <li><Shield size={16} className="text-danger" /> Unlimited Seats</li>
                <li><Shield size={16} className="text-danger" /> GRC & SecOps Module</li>
                <li><Shield size={16} className="text-danger" /> Custom Integrations</li>
              </ul>
              <button className="btn btn-secondary btn-block mt-24">Contact Sales</button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
}
