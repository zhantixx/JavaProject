import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

export function saveAuthSession(data) {
  const accessToken = data?.token || data?.accessToken;
  if (accessToken) {
    localStorage.setItem('token', accessToken);
    localStorage.setItem('accessToken', accessToken);
  }
  if (data?.refreshToken) {
    localStorage.setItem('refreshToken', data.refreshToken);
  }
}

export function clearAuthSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userEmail');
  localStorage.removeItem('userId');
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

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
  if (!refreshToken) {
    throw new Error('No refresh token');
  }

  const resp = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken }, {
    headers: { 'Content-Type': 'application/json' },
  });

  const newAccess = resp.data?.token || resp.data?.accessToken;
  const newRefresh = resp.data?.refreshToken;

  if (!newAccess) {
    throw new Error('No access token in refresh response');
  }

  localStorage.setItem('token', newAccess);
  localStorage.setItem('accessToken', newAccess);
  if (newRefresh) {
    localStorage.setItem('refreshToken', newRefresh);
  }

  return newAccess;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (!originalRequest || error.response?.status !== 401 ||
      originalRequest.url?.includes('/auth/refresh') ||
      originalRequest.url?.includes('/auth/login') ||
      originalRequest.url?.includes('/auth/register')) {
      return Promise.reject(error);
    }

    if (originalRequest._retry) {
      return Promise.reject(error);
    }
    originalRequest._retry = true;

    if (!isRefreshing) {
      isRefreshing = true;
      try {
        const newToken = await refreshTokenRequest();
        isRefreshing = false;
        onRefreshed(newToken);
      } catch (e) {
        isRefreshing = false;
        clearAuthSession();
        window.location.href = '/login';
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
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: clearAuthSession,
};

export const tasks = {
  getAll: (params = {}) => api.get('/tasks', { params }),
  getById: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  updateStatus: (id, status) => api.patch(`/tasks/${id}/status`, { status }),
  delete: (id) => api.delete(`/tasks/${id}`),
};

export const profile = {
  me: () => api.get('/users/me'),
  updateMe: (data) => api.patch('/users/me', data),
  exportData: () => api.get('/users/me/export', { responseType: 'blob' }),
  deleteAccount: () => api.delete('/users/me'),
};

export const calendar = {
  icsUrl: (userId) => {
    const uid = userId || localStorage.getItem('userId') || 1;
    return `${API_BASE}/calendar/ics?userId=${uid}`;
  },
  getIcs: (userId) => {
    const uid = userId || localStorage.getItem('userId') || 1;
    return api.get(`/calendar/ics?userId=${uid}`, { responseType: 'blob' });
  },
};

export const chat = {
  send: (message) => api.post('/chat', { message }),
};

export const analytics = {
  getStats: () => api.get('/analytics/stats'),
  getUserStats: (userId) => api.get(`/analytics/stats/${userId}`),
};

export const notes = {
  getAll: (userId) => api.get(`/users/${userId}/notes`),
  getById: (userId, noteId) => api.get(`/users/${userId}/notes/${noteId}`),
  create: (userId, data) => api.post(`/users/${userId}/notes`, data),
  update: (userId, noteId, data) => api.put(`/users/${userId}/notes/${noteId}`, data),
  delete: (userId, noteId) => api.delete(`/users/${userId}/notes/${noteId}`),
};

export default api;
