import { useEffect, useState } from 'react';
import { assetService } from '../../services/dataService';
import { Laptop, Search, Plus, Filter, MoreVertical } from 'lucide-react';
import './Assets.css';

export default function AssetsPage() {
  const [assets, setAssets] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAssets();
  }, []);

  const loadAssets = async () => {
    setLoading(true);
    try {
      const data = await assetService.getAssets();
      setAssets(data.content);
    } catch (err) {
      console.error('Failed to load assets:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'IN_USE': return <span className="badge badge-success">In Use</span>;
      case 'IN_STOCK': return <span className="badge badge-primary">In Stock</span>;
      case 'REPAIR': return <span className="badge badge-warning">Repair</span>;
      case 'RETIRED': return <span className="badge badge-danger">Retired</span>;
      case 'LOST': return <span className="badge badge-danger">Lost</span>;
      default: return <span className="badge">{status}</span>;
    }
  };

  return (
    <div className="assets-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Asset Inventory</h1>
          <p className="page-subtitle">Track and manage hardware devices and their lifecycle.</p>
        </div>
        <button className="btn btn-primary">
          <Plus size={18} />
          Add Asset
        </button>
      </div>

      <div className="card">
        <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)', display: 'flex', gap: '12px' }}>
          <div className="toolbar-search">
            <Search size={16} className="toolbar-search-icon" />
            <input
              type="text"
              placeholder="Search by name, tag, or serial..."
              className="form-input toolbar-search-input"
            />
          </div>
          <button className="btn btn-secondary">
             <Filter size={16}/> Filter
          </button>
        </div>
        
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Asset Tag</th>
                <th>Name</th>
                <th>Category</th>
                <th>Status</th>
                <th>Purchase Date</th>
                <th className="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6} className="text-center" style={{ padding: '40px 0' }}>Loading assets...</td>
                </tr>
              ) : assets.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center" style={{ padding: '60px 0' }}>
                    <Laptop size={40} style={{ color: 'var(--text-muted)', marginBottom: 12, opacity: 0.5 }} />
                    <p style={{ color: 'var(--text-secondary)' }}>No assets found in inventory.</p>
                  </td>
                </tr>
              ) : (
                assets.map((asset) => (
                  <tr key={asset.id}>
                    <td><span className="text-primary font-medium">{asset.assetTag}</span></td>
                    <td>{asset.name}</td>
                    <td>{asset.category || '-'}</td>
                    <td>{getStatusBadge(asset.status)}</td>
                    <td>{asset.purchaseDate || '-'}</td>
                    <td className="text-right">
                      <button className="btn btn-ghost btn-icon">
                        <MoreVertical size={16} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
