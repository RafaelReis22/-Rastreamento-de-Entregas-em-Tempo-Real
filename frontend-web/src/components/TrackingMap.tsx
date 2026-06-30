'use client';

import React, { useEffect, useState } from 'react';

interface Position {
  lat: number;
  lng: number;
}

interface TrackingMapProps {
  driverPosition: Position;
  originPosition: Position;
  destinationPosition: Position;
}

export const TrackingMap: React.FC<TrackingMapProps> = ({
  driverPosition,
  originPosition,
  destinationPosition
}) => {
  const [isClient, setIsClient] = useState(false);
  const [MapComponents, setMapComponents] = useState<any>(null);

  useEffect(() => {
    setIsClient(true);
    Promise.all([
      import('react-leaflet'),
      import('leaflet')
    ]).then(([reactLeaflet, L]) => {
      // Fix default Leaflet marker icon URLs
      delete (L.Icon.Default.prototype as any)._getIconUrl;
      L.Icon.Default.mergeOptions({
        iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
        iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
        shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      });

      const customDriverIcon = L.divIcon({
        className: 'custom-driver-icon',
        html: `<div style="background-color: #10b981; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 3px solid #ffffff; box-shadow: 0 4px 12px rgba(16,185,129,0.5); font-size: 16px;">🛵</div>`,
        iconSize: [32, 32],
        iconAnchor: [16, 16]
      });

      const customOriginIcon = L.divIcon({
        className: 'custom-origin-icon',
        html: `<div style="background-color: #8b5cf6; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid #ffffff; font-size: 14px;">🏬</div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });

      const customDestIcon = L.divIcon({
        className: 'custom-dest-icon',
        html: `<div style="background-color: #06b6d4; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid #ffffff; font-size: 14px;">🏠</div>`,
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });

      setMapComponents({
        MapContainer: reactLeaflet.MapContainer,
        TileLayer: reactLeaflet.TileLayer,
        Marker: reactLeaflet.Marker,
        Popup: reactLeaflet.Popup,
        Polyline: reactLeaflet.Polyline,
        icons: { customDriverIcon, customOriginIcon, customDestIcon }
      });
    });
  }, []);

  if (!isClient || !MapComponents) {
    return (
      <div style={{ width: '100%', height: '420px', background: 'var(--card)', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        Carregando Mapa de Telemetria OpenStreetMap...
      </div>
    );
  }

  const { MapContainer, TileLayer, Marker, Popup, Polyline, icons } = MapComponents;

  const routePolyline = [
    [originPosition.lat, originPosition.lng],
    [driverPosition.lat, driverPosition.lng],
    [destinationPosition.lat, destinationPosition.lng]
  ];

  return (
    <div style={{ width: '100%', height: '420px', borderRadius: '16px', overflow: 'hidden', border: '1px solid var(--border)' }}>
      <MapContainer
        center={[driverPosition.lat, driverPosition.lng]}
        zoom={14}
        scrollWheelZoom={false}
        style={{ width: '100%', height: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Origem */}
        <Marker position={[originPosition.lat, originPosition.lng]} icon={icons.customOriginIcon}>
          <Popup>Ponto de Coleta: Loja Paulista</Popup>
        </Marker>

        {/* Entregador em Movimento */}
        <Marker position={[driverPosition.lat, driverPosition.lng]} icon={icons.customDriverIcon}>
          <Popup>
            <b>Entregador Carlos Silva</b><br />
            Velocidade: 35.5 km/h<br />
            Status: Em Trânsito
          </Popup>
        </Marker>

        {/* Destino */}
        <Marker position={[destinationPosition.lat, destinationPosition.lng]} icon={icons.customDestIcon}>
          <Popup>Destino: Residência do Cliente</Popup>
        </Marker>

        {/* Linha de Rota */}
        <Polyline positions={routePolyline} color="#10b981" weight={4} opacity={0.7} dashArray="8, 8" />
      </MapContainer>
    </div>
  );
};
