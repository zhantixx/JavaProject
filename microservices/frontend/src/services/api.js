import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// --- Request interceptor: добавляем access token ---
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token'); // access token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- Response interceptor: авто-refresh при 401 ---
let isRefreshing = false;
let refreshSubscribers = [];

function onRefreshed(newToken) {
  refreshSubscribers.forEach((cb) => cb(newToken));
  refreshSubscribers = [];
}

function subscribeTokenRefresh(cb) {
  refreshSubscribers.push(cb);
}

async function refreshTokenRequest() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) throw new Error('No refresh token');
  const resp = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken }, { headers: { 'Content-Type': 'application/json' } });
  // ожидается { token, refreshToken? }
  const newAccess = resp.data?.token || resp.data?.accessToken;
  const newRefresh = resp.data?.refreshToken || refreshToken;
  if (!newAccess) throw new Error('No access token in refresh response');
  localStorage.setItem('token', newAccess);
  if (newRefresh) localStorage.setItem('refreshToken', newRefresh);
  return newAccess;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (!originalRequest || error.response?.status !== 401) {
      return Promise.reject(error);
    }

    if (originalRequest.__isRetry) {
      // уже пытались — выходим и отдаём 401
      return Promise.reject(error);
    }
    originalRequest.__isRetry = true;

    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await refreshTokenRequest();
        isRefreshing = false;
        onRefreshed(newToken);
      } catch (e) {
        isRefreshing = false;
        // refresh провалился — чистим и уводим на логин
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        return Promise.reject(error);
      }
    }

    return new Promise((resolve) => {
      subscribeTokenRefresh((newToken) => {
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        resolve(api(originalRequest));
      });
    });
  }
);

export const auth = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  refresh: (refreshToken) => axios.post(`${API_BASE}/auth/refresh`, { refreshToken }),
};

export const tasks = {
  getAll: () => api.get('/tasks'),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  delete: (id) => api.delete(`/tasks/${id}`),
};

export const profile = {
  me: () => api.get('/users/me'),
  updateMe: (data) => api.patch('/users/me', data),
};

export const calendar = {
  createEvent: (data) => api.post('/calendar/events', data),
  icsUrl: () => `${API_BASE}/calendar/ics`,
};

export const chat = {
  send: (message) => api.post('/chat', { message }),
};

export default api;