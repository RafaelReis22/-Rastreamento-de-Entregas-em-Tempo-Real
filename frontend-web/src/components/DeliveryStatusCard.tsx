'use client';

import React from 'react';

interface DeliveryStatusCardProps {
  orderId: string;
  status: string;
  etaMinutes: number;
  delivererName: string;
  originAddress: string;
  destinationAddress: string;
}

export const DeliveryStatusCard: React.FC<DeliveryStatusCardProps> = ({
  orderId,
  status,
  etaMinutes,
  delivererName,
  originAddress,
  destinationAddress
}) => {
  return (
    <div className="card-glass" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Pedido ID: <span className="mono" style={{ color: 'var(--cyan-light)' }}>{orderId.substring(0, 8)}...</span>
          </div>
          <div style={{ fontSize: '18px', fontWeight: 800, marginTop: '2px' }}>
            Entrega em Andamento
          </div>
        </div>
        <span className="badge badge-emerald">
          <div className="pulse-dot" /> EM TRÂNSITO
        </span>
      </div>

      {/* ETA Banner */}
      <div style={{ background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.25)', borderRadius: '12px', padding: '14px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 700 }}>Estimativa de Chegada (ETA)</div>
          <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--emerald-light)', marginTop: '2px' }}>
            {etaMinutes} min
          </div>
        </div>
        <div style={{ fontSize: '32px' }}>🛵</div>
      </div>

      {/* Delivery Progress Timeline */}
      <div style={{ display: 'flex', justifyContent: 'space-between', position: 'relative', marginTop: '8px' }}>
        <div style={{ position: 'absolute', top: '12px', left: '10%', right: '10%', height: '3px', background: 'var(--emerald)', zIndex: 0 }} />

        <div style={{ zIndex: 1, textAlign: 'center' }}>
          <div style={{ width: '26px', height: '26px', borderRadius: '50%', background: 'var(--emerald)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto', fontSize: '12px', fontWeight: 700 }}>✓</div>
          <div style={{ fontSize: '11px', fontWeight: 700, marginTop: '6px' }}>Coletado</div>
        </div>

        <div style={{ zIndex: 1, textAlign: 'center' }}>
          <div style={{ width: '26px', height: '26px', borderRadius: '50%', background: 'var(--emerald)', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto', fontSize: '12px', fontWeight: 700 }}>2</div>
          <div style={{ fontSize: '11px', fontWeight: 700, marginTop: '6px', color: 'var(--emerald-light)' }}>Em Rota</div>
        </div>

        <div style={{ zIndex: 1, textAlign: 'center' }}>
          <div style={{ width: '26px', height: '26px', borderRadius: '50%', background: 'var(--border)', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto', fontSize: '12px', fontWeight: 700 }}>3</div>
          <div style={{ fontSize: '11px', fontWeight: 600, marginTop: '6px', color: 'var(--text-muted)' }}>Entregue</div>
        </div>
      </div>

      {/* Driver & Route Info */}
      <div style={{ borderTop: '1px solid var(--border)', paddingTop: '14px', display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '13px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: 'var(--purple)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>CS</div>
          <div>
            <div style={{ fontWeight: 700 }}>{delivererName}</div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Honda CG 160 • ABC-1234</div>
          </div>
        </div>

        <div style={{ background: 'var(--surface)', padding: '12px', borderRadius: '10px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
          <div>
            <span style={{ fontSize: '11px', color: 'var(--purple)', fontWeight: 700 }}>DE:</span> {originAddress}
          </div>
          <div>
            <span style={{ fontSize: '11px', color: 'var(--cyan-light)', fontWeight: 700 }}>PARA:</span> {destinationAddress}
          </div>
        </div>
      </div>
    </div>
  );
};
