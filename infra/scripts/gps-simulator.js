/**
 * Simulador de Telemetria GPS em Tempo Real
 * Simula a movimentação de um entregador em São Paulo enviando pings de GPS a cada 2 segundos.
 * Uso: node infra/scripts/gps-simulator.js
 */

const http = require('http');

const TRACKING_URL = process.env.TRACKING_URL || 'http://localhost:8083/api/v1/tracking/location';
const DELIVERY_ID = process.env.DELIVERY_ID || 'd100e840-0000-4000-a000-000000000001';
const DELIVERER_ID = process.env.DELIVERER_ID || 'u200e840-0000-4000-a000-000000000002';

// Rota simulada (Av. Paulista -> Jardins -> Pinheiros, SP)
const ROUTE_POINTS = [
  { lat: -23.561684, lng: -46.655981 }, // Av. Paulista (MASP)
  { lat: -23.563500, lng: -46.659000 },
  { lat: -23.565800, lng: -46.663200 },
  { lat: -23.568200, lng: -46.667500 }, // R. Oscar Freire
  { lat: -23.571000, lng: -46.672000 },
  { lat: -23.573500, lng: -46.676500 },
  { lat: -23.576000, lng: -46.681000 }, // Av. Rebouças
  { lat: -23.578500, lng: -46.686000 },
  { lat: -23.581000, lng: -46.691000 }, // Faria Lima / Largo da Batata
];

let currentIndex = 0;

console.log('🚀 Iniciando simulador de GPS para a entrega:', DELIVERY_ID);

function sendPing() {
  const point = ROUTE_POINTS[currentIndex];
  const payload = JSON.stringify({
    deliveryId: DELIVERY_ID,
    delivererId: DELIVERER_ID,
    latitude: point.lat + (Math.random() - 0.5) * 0.0001,
    longitude: point.lng + (Math.random() - 0.5) * 0.0001,
    speed: 35.5,
    bearing: 240.0,
    timestamp: new Date().toISOString()
  });

  const url = new URL(TRACKING_URL);
  const req = http.request(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(payload)
    }
  }, (res) => {
    console.log(`[Ping #${currentIndex + 1}] Lat: ${point.lat}, Lng: ${point.lng} | HTTP Status: ${res.statusCode}`);
  });

  req.on('error', (err) => {
    console.log(`[Ping #${currentIndex + 1}] Envio simulado (Servidor indisponível no momento: ${err.message})`);
  });

  req.write(payload);
  req.end();

  currentIndex = (currentIndex + 1) % ROUTE_POINTS.length;
}

setInterval(sendPing, 2000);
sendPing();
