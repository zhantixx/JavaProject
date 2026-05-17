import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || '1m',
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.ACCESS_TOKEN || '';

export default function () {
  const headers = TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {};
  const response = http.get(`${BASE_URL}/api/tasks`, { headers });

  check(response, {
    'status is successful or unauthorized for smoke runs': (r) => [200, 401].includes(r.status),
    'response time is below 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
