'use client';

import React, { useState, useEffect } from 'react';
import { TrackingMap } from '../components/TrackingMap';
import { DeliveryStatusCard } from '../components/DeliveryStatusCard';

export default function Home() {
  const [driverPos, setDriverPos] = useState({ lat: -23.563500, lng: -46.659000 });
  const [pingCount, setPingCount] = useState(1);

  // Simula atualização de coordenadas GPS em tempo real (fallback / WebSocket STOMP simulation)
  useEffect(() => {
    const interval = setInterval(() => {
      setDriverPos(prev => ({
        lat: prev.lat - 0.0003,
        lng: prev.lng - 0.0004
      }));
      setPingCount(p => p + 1);
    }, 2500);

    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg-main)', padding: '24px' }}>
      {/* Top Navbar */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', borderBottom: '1px solid var(--border)', paddingBottom: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ fontSize: '24px' }}>🚚</div>
          <div>
            <h1 style={{ fontSize: '20px', fontWeight: 800, letterSpacing: '-0.02em' }}>
              Rastreamento de Entregas <span style={{ color: 'var(--emerald-light)' }}>em Tempo Real</span>
            </h1>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
              Telemetria GPS ao vivo • Ingestão Kafka & Redis Geo • Mapas OpenStreetMap
            </p>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <span className="badge badge-cyan">
            Pings GPS Recebidos: <strong className="mono" style={{ marginLeft: '4px' }}>{pingCount}</strong>
          </span>
          <span className="badge badge-emerald">
            <div className="pulse-dot" /> WEBSOCKET CONECTADO
          </span>
        </div>
      </header>

      {/* Main Grid Content */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '24px' }}>
        {/* Left Column: Interactive Leaflet Tracking Map */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div className="card-glass" style={{ padding: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
              <div style={{ fontWeight: 700, fontSize: '14px' }}>
                📍 Mapa de Posição ao Vivo (OpenStreetMap / Leaflet)
              </div>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)' }} className="mono">
                Lat: {driverPos.lat.toFixed(6)}, Lng: {driverPos.lng.toFixed(6)}
              </span>
            </div>

            <TrackingMap
              driverPosition={driverPos}
              originPosition={{ lat: -23.561684, lng: -46.655981 }}
              destinationPosition={{ lat: -23.578500, lng: -46.686000 }}
            />
          </div>

          {/* Stats Bar */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
            <div className="card-glass" style={{ padding: '16px', textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Velocidade Média</div>
              <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--cyan-light)', marginTop: '4px' }} className="mono">
                35.5 km/h
              </div>
            </div>

            <div className="card-glass" style={{ padding: '16px', textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Distância Percorrida</div>
              <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--emerald-light)', marginTop: '4px' }} className="mono">
                2.4 / 4.8 km
              </div>
            </div>

            <div className="card-glass" style={{ padding: '16px', textAlign: 'center' }}>
              <div style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Partições Kafka</div>
              <div style={{ fontSize: '20px', fontWeight: 800, color: 'var(--purple)', marginTop: '4px' }} className="mono">
                12 Tópicos
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Status Card & Timeline */}
        <div>
          <DeliveryStatusCard
            orderId="d100e840-0000-4000-a000-000000000001"
            status="IN_TRANSIT"
            etaMinutes={12}
            delivererName="Carlos Silva"
            originAddress="Av. Paulista, 1578 (MASP)"
            destinationAddress="R. Faria Lima / Largo da Batata"
          />
        </div>
      </div>
    </div>
  );
}
