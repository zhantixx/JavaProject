import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('accessToken') || localStorage.getItem('token') || null);
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('refreshToken') || null);
  const [user, setUser] = useState(null);

  const isRefreshingRef = useRef(false);
  const refreshQueueRef = useRef([]);

  const saveTokens = useCallback((access, refresh) => {
    if (access) {
      localStorage.setItem('token', access);
      localStorage.setItem('accessToken', access);
      setAccessToken(access);
    }
    if (refresh) {
      localStorage.setItem('refreshToken', refresh);
      setRefreshToken(refresh);
    }
  }, []);

  const clearTokens = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
  }, []);

  const login = useCallback((access, refresh, me) => {
    saveTokens(access, refresh);
    if (me) setUser(me);
  }, [saveTokens]);

  const logout = useCallback(() => {
    clearTokens();
  }, [clearTokens]);

  const doRefresh = useCallback(async () => {
    if (isRefreshingRef.current) {
      // уже обновляем — вернём промис, который резолвится после текущего обновления
      return new Promise((resolve, reject) => {
        refreshQueueRef.current.push({ resolve, reject });
      });
    }

    isRefreshingRef.current = true;
    try {
      const resp = await fetch('/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });

      if (!resp.ok) {
        throw new Error(`Refresh failed: ${resp.status}`);
      }
      const data = await resp.json();
      // ожидаем, что бэкенд отдаёт { token, refreshToken? }
      const newAccess = data.token || data.accessToken;
      const newRefresh = data.refreshToken || refreshToken;
      if (!newAccess) throw new Error('No access token in refresh response');

      saveTokens(newAccess, newRefresh);

      // разбудим ожидающих
      refreshQueueRef.current.forEach(p => p.resolve(newAccess));
      refreshQueueRef.current = [];
      return newAccess;
    } catch (e) {
      // провалим ожидающих
      refreshQueueRef.current.forEach(p => p.reject(e));
      refreshQueueRef.current = [];
      clearTokens();
      throw e;
    } finally {
      isRefreshingRef.current = false;
    }
  }, [refreshToken, saveTokens, clearTokens]);

  const apiFetch = useCallback(async (input, init = {}) => {
    const withAuth = (token) => {
      const headers = new Headers(init.headers || {});
      if (token) headers.set('Authorization', `Bearer ${token}`);
      if (!headers.has('Content-Type') && !(init.body instanceof FormData)) {
        headers.set('Content-Type', 'application/json');
      }
      return { ...init, headers };
    };

    let resp = await fetch(input, withAuth(accessToken));
    if (resp.status !== 401) return resp;

    // попытка рефреша
    if (!refreshToken) return resp;

    try {
      const newAccess = await doRefresh();
      resp = await fetch(input, withAuth(newAccess));
      return resp;
    } catch {
      // рефреш провалился — отдадим 401 и почистим состояние
      logout();
      return resp;
    }
  }, [accessToken, refreshToken, doRefresh, logout]);

  // опционально подгружаем профиль, если есть токен
  useEffect(() => {
    (async () => {
      if (!accessToken) return;
      try {
        const r = await apiFetch('/api/users/me');
        if (r.ok) {
          const me = await r.json();
          setUser(me);
        }
      } catch {
        // ignore
      }
    })();
  }, [accessToken, apiFetch]);

  const value = useMemo(() => ({
    accessToken, refreshToken, user, setUser, login, logout, saveTokens, apiFetch
  }), [accessToken, refreshToken, user, login, logout, saveTokens, apiFetch]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
