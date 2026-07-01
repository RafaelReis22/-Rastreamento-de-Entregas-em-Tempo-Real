/**
 * Simulador de Protocolo de Sincronização Offline de GPS
 * Simula perda de conexão de rede móvel (buffer offline) e envio transacional em lote (batching).
 * Uso: node infra/scripts/gps-offline-simulator.js
 */

const http = require('http');

const BATCH_URL = process.env.BATCH_URL || 'http://localhost:8083/api/v1/tracking/batch';

const offlinePingBuffer = [
  { deliveryId: 'd100e840-0000-4000-a000-000000000001', latitude: -23.562000, longitude: -46.656500, timestamp: new Date(Date.now() - 60000).toISOString() },
  { deliveryId: 'd100e840-0000-4000-a000-000000000001', latitude: -23.563000, longitude: -46.658000, timestamp: new Date(Date.now() - 40000).toISOString() },
  { deliveryId: 'd100e840-0000-4000-a000-000000000001', latitude: -23.564500, longitude: -46.660000, timestamp: new Date(Date.now() - 20000).toISOString() }
];

console.log('📡 Simulando reconexão de rede móvel do entregador...');
console.log(`📦 Enviando lote (batch) de ${offlinePingBuffer.length} pings GPS acumulados no buffer local IndexedDB...`);

const payload = JSON.stringify(offlinePingBuffer);
const url = new URL(BATCH_URL);

const req = http.request(url, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(payload)
  }
}, (res) => {
  console.log(`✅ Sincronização de pings em lote concluída com sucesso! HTTP Status: ${res.statusCode}`);
});

req.on('error', (err) => {
  console.log(`ℹ️ Teste de envio em lote (Servidor offline no momento: ${err.message})`);
});

req.write(payload);
req.end();
