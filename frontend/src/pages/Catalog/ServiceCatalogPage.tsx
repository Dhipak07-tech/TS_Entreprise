import React, { useEffect, useState } from 'react';
import { catalogService } from '../../services/dataService';
import { ShoppingCart, Laptop, Terminal, Layers, Armchair, ChevronRight, Check } from 'lucide-react';
import './ServiceCatalogPage.css';

export default function ServiceCatalogPage() {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedItem, setSelectedItem] = useState<any | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    loadCatalog();
  }, []);

  const loadCatalog = async () => {
    setLoading(true);
    try {
      const data = await catalogService.getCatalog();
      setItems(data);
    } catch (err) {
      console.error('Failed to load service catalog:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenRequest = (item: any) => {
    setSelectedItem(item);
    setQuantity(1);
    setSuccessMessage(null);
  };

  const handleSubmitRequest = async () => {
    if (!selectedItem) return;
    setSubmitting(true);
    try {
      await catalogService.raiseRequest({
        itemId: selectedItem.id,
        quantity: quantity
      });
      setSuccessMessage(`Successfully ordered ${quantity}x ${selectedItem.name}! Request ticket has been raised.`);
      setTimeout(() => {
        setSelectedItem(null);
        setSuccessMessage(null);
      }, 3000);
    } catch (err) {
      console.error('Order failed:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const getCategoryIcon = (category: string) => {
    switch (category?.toLowerCase()) {
      case 'hardware':
        return <Laptop size={20} />;
      case 'cloud infrastructure':
        return <Terminal size={20} />;
      case 'office supply':
        return <Armchair size={20} />;
      default:
        return <Layers size={20} />;
    }
  };

  return (
    <div className="catalog-page animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Service Catalog</h1>
          <p className="page-subtitle">Request hardware, software licenses, infrastructure, or account access with automatic approval flows.</p>
        </div>
      </div>

      {loading ? (
        <div className="catalog-grid">
          {[1, 2, 3, 4].map(n => (
            <div key={n} className="catalog-card card skeleton" style={{ height: 220 }}></div>
          ))}
        </div>
      ) : (
        <div className="catalog-grid">
          {items.map(item => (
            <div key={item.id} className="catalog-card card hover-lift">
              <div className="catalog-card-icon">
                {getCategoryIcon(item.category)}
              </div>
              <div className="catalog-card-body">
                <span className="badge badge-primary">{item.category}</span>
                <h3 className="item-title">{item.name}</h3>
                <p className="item-description">{item.description}</p>
                <div className="item-footer">
                  <span className="item-cost">${item.cost.toFixed(2)}</span>
                  <button className="btn btn-primary btn-sm btn-icon" onClick={() => handleOpenRequest(item)}>
                    Order Now <ChevronRight size={14} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {selectedItem && (
        <div className="modal-backdrop" onClick={() => !submitting && setSelectedItem(null)}>
          <div className="modal-content glassmorphism-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Fulfill Service Request</h2>
              <button className="close-btn-x" onClick={() => setSelectedItem(null)} disabled={submitting}>&times;</button>
            </div>
            
            {successMessage ? (
              <div className="success-state animate-scale-in">
                <div className="success-icon-circle">
                  <Check size={40} />
                </div>
                <h3>Order Submitted</h3>
                <p>{successMessage}</p>
              </div>
            ) : (
              <div className="modal-body">
                <div className="order-details-card">
                  <div className="order-header">
                    {getCategoryIcon(selectedItem.category)}
                    <div>
                      <h4>{selectedItem.name}</h4>
                      <p>{selectedItem.description}</p>
                    </div>
                  </div>
                  <div className="order-pricing">
                    <span>Unit Price:</span>
                    <strong>${selectedItem.cost.toFixed(2)}</strong>
                  </div>
                </div>

                <div className="form-group quantity-selector">
                  <label htmlFor="quantity">Quantity</label>
                  <div className="quantity-controls">
                    <button 
                      type="button" 
                      onClick={() => setQuantity(q => Math.max(1, q - 1))}
                      disabled={quantity <= 1}
                    >-</button>
                    <input 
                      type="number" 
                      id="quantity" 
                      value={quantity} 
                      readOnly
                    />
                    <button 
                      type="button" 
                      onClick={() => setQuantity(q => q + 1)}
                    >+</button>
                  </div>
                </div>

                <div className="total-cost-panel">
                  <span>Total Est. Cost:</span>
                  <span className="total-price">${(selectedItem.cost * quantity).toFixed(2)}</span>
                </div>

                <div className="modal-actions">
                  <button 
                    className="btn btn-secondary" 
                    onClick={() => setSelectedItem(null)}
                    disabled={submitting}
                  >
                    Cancel
                  </button>
                  <button 
                    className="btn btn-primary" 
                    onClick={handleSubmitRequest}
                    disabled={submitting}
                  >
                    {submitting ? 'Ordering...' : 'Confirm Checkout'}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
