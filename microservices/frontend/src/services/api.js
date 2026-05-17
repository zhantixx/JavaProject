import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE,
    headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
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
    if (!refreshToken) throw new Error('No refresh token');

    // Исправлено: отправляем refresh token в теле запроса
    const resp = await axios.post(`${API_BASE}/auth/refresh`, { refreshToken }, {
        headers: { 'Content-Type': 'application/json' }
    });

    const newAccess = resp.data?.token || resp.data?.accessToken;
    const newRefresh = resp.data?.refreshToken;

    if (!newAccess) throw new Error('No access token in refresh response');

    localStorage.setItem('token', newAccess);
    if (newRefresh) localStorage.setItem('refreshToken', newRefresh);

    return newAccess;
}

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Не пытаемся обновить токен для запросов к /auth/refresh и /auth/login
        if (!originalRequest || error.response?.status !== 401 ||
            originalRequest.url?.includes('/auth/refresh') ||
            originalRequest.url?.includes('/auth/login')) {
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
                // Очищаем токены при ошибке обновления
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('userEmail');
                // Redirect to the login page.
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
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userEmail');
        localStorage.removeItem('userId');
    }
};

export const tasks = {
    getAll: (params) => api.get('/tasks', { params }),
    getById: (id) => api.get(`/tasks/${id}`),
    create: (data) => api.post('/tasks', data),
    update: (id, data) => api.put(`/tasks/${id}`, data),
    updateStatus: (id, status) => api.patch(`/tasks/${id}/status`, { status }),
    delete: (id) => api.delete(`/tasks/${id}`),
    getByUser: (userId) => api.get(`/tasks/user/${userId}`),
    deleteAllUser: (userId) => api.delete(`/tasks/user/${userId}`)
};

export const profile = {
    me: () => api.get('/users/me'),
    updateMe: (data) => api.patch('/users/me', data),
    exportData: () => api.get('/users/me/export', { responseType: 'blob' }),
    deleteAccount: () => api.delete('/users/me')
};

export const calendar = {
    // Создание события в Google Calendar
    createEvent: (data) => api.post('/calendar/events', data),

    // Получение событий (если добавите эндпоинт)
    getEvents: (params) => api.get('/calendar/events', { params }),

    // Обновление события
    updateEvent: (id, data) => api.put(`/calendar/events/${id}`, data),

    // Удаление события
    deleteEvent: (id) => api.delete(`/calendar/events/${id}`),

    // ICS ссылка для подписки (Task Service)
    icsUrl: (userId) => {
        const uid = userId || localStorage.getItem('userId') || 1;
        return `${API_BASE}/calendar/ics?userId=${uid}`;
    },

    // Получение ICS файла
    getIcs: (userId) => {
        const uid = userId || localStorage.getItem('userId') || 1;
        return api.get(`/calendar/ics?userId=${uid}`, { responseType: 'blob' });
    }
};

export const chat = {
    send: (message) => api.post('/chat', { message }),
    // Для streaming (если нужно)
    sendStream: (message) => `${API_BASE}/chat/stream?message=${encodeURIComponent(message)}`
};

export const analytics = {
    getStats: () => api.get('/analytics/stats'),
    getUserStats: (userId) => api.get(`/analytics/stats/${userId}`)
};

export const admin = {
    getAllUsers: () => api.get('/admin/users/all'),
    getUsers: (page, size, sort) => api.get('/admin/users', { params: { page, size, sort } }),
    getUserById: (id) => api.get(`/admin/users/${id}`),
    changeRole: (id, role) => api.patch(`/admin/users/${id}/role`, { role }),
    blockUser: (id) => api.post(`/admin/users/${id}/block`),
    unblockUser: (id) => api.post(`/admin/users/${id}/unblock`),
    getAuditLogs: (userId, page, size) => api.get('/admin/audit', { params: { userId, page, size } }),
    getAnalyticsStats: () => api.get('/admin/analytics/stats'),
    getUserAnalyticsStats: (userId) => api.get(`/admin/analytics/stats/${userId}`)
};

export const tags = {
    getAll: () => api.get('/tags'),
    getById: (id) => api.get(`/tags/${id}`),
    create: (data) => api.post('/tags', data),
    update: (id, data) => api.put(`/tags/${id}`, data),
    delete: (id) => api.delete(`/tags/${id}`)
};

export const notes = {
    getAll: (userId) => api.get(`/users/${userId}/notes`),
    getById: (userId, noteId) => api.get(`/users/${userId}/notes/${noteId}`),
    create: (userId, data) => api.post(`/users/${userId}/notes`, data),
    update: (userId, noteId, data) => api.put(`/users/${userId}/notes/${noteId}`, data),
    delete: (userId, noteId) => api.delete(`/users/${userId}/notes/${noteId}`)
};

export default api;
