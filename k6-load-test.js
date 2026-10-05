import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '15s', target: 20 },  // Rampa de subida
    { duration: '30s', target: 100 }, // Alta carga constante (100 req/s)
    { duration: '15s', target: 0 },   // Rampa de descida
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% das requisições devem responder em menos de 200ms
  },
};

export default function () {
  const url = 'http://localhost:8081/api/v1/payments';
  const payload = JSON.stringify({
    customerId: 'cli-k6-stress',
    amount: 150.00,
    currency: 'BRL',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Correlation-ID': `k6-trace-${__VU}-${__ITER}`,
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status é 200 ou 201': (r) => r.status === 200 || r.status === 201,
  });

  sleep(0.05); // Intervalo leve entre disparos por usuário virtual
}