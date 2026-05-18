import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  scenarios: {
    sustained_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 250 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 750 },
        { duration: '2m', target: 1000 },
        { duration: '3m', target: 1000 },
        { duration: '2m', target: 0 },
      ],
      gracefulRampDown: '30s',
    },
  },
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
    'status is successful or unauthorized for baseline runs': (r) => [200, 401].includes(r.status),
    'response time is below 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}
