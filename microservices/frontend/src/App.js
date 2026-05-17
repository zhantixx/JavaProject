import React, { useEffect, useMemo, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, NavLink, Link, useLocation } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import TaskList from './components/TaskList';
import Chat from './components/Chat';
import OAuth2Redirect from './components/OAuth2Redirect';
import Profile from './pages/Profile';
import CalendarPage from './pages/Calendar';
import NotesPage from './pages/Notes';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ToastProvider, useToast } from './components/Toast';
import { clearAuthSession } from './services/api';
import { GhostButton } from './components/UiKit';

const SITE_NAME = 'SanaBot';

function TasksPage() {
  return (
    <div className="mx-auto max-w-7xl">
      <TaskList />
    </div>
  );
}

function ChatPage() {
  return (
    <div className="mx-auto max-w-5xl">
      <div className="mb-6">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[var(--muted-text)]">Смарт көмек</p>
        <h1 className="mt-2 text-3xl font-semibold text-[var(--text-primary)]">AI көмекші</h1>
        <p className="mt-2 max-w-2xl text-sm text-[var(--text-secondary)]">
          Күндік жоспар, ой жинау және қысқа кеңес алу үшін сұрағыңызды жазыңыз.
        </p>
      </div>
      <Chat />
    </div>
  );
}

function ThemeToggle({ theme, onToggle }) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className="inline-flex items-center gap-2 rounded-xl border border-[var(--border-color)] bg-[var(--card-strong)] px-3.5 py-2.5 text-sm font-medium text-[var(--text-primary)] transition hover:border-[var(--brand-border)] hover:text-[var(--brand-strong)]"
    >
      <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-[var(--brand-soft)] text-[var(--brand-strong)]">
        {theme === 'dark' ? '☾' : '☼'}
      </span>
      {theme === 'dark' ? 'Қараңғы' : 'Жарық'}
    </button>
  );
}

function AppShell() {
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  const toast = useToast();
  const location = useLocation();
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
    localStorage.setItem('theme', theme);
  }, [theme]);

  useEffect(() => {
    document.title = `${SITE_NAME} — Жеке ассистент`;
  }, []);

  const logout = () => {
    clearAuthSession();
    toast.info('Сессия аяқталды');
    window.location.href = '/login';
  };

  const navItems = useMemo(
    () => [
      { to: '/tasks', label: 'Тапсырмалар' },
      { to: '/calendar', label: 'Күнтізбе' },
      { to: '/notes', label: 'Жазбалар' },
      { to: '/chat', label: 'AI чат' },
      { to: '/profile', label: 'Профиль' },
    ],
    []
  );

  return (
    <div className="min-h-screen bg-transparent text-[var(--text-primary)]">
      <header className="sticky top-0 z-30 border-b border-[var(--border-color)] bg-[rgba(248,251,255,0.72)] backdrop-blur dark:bg-[rgba(8,17,32,0.72)]">
        <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-4 md:flex-row md:items-center md:justify-between md:px-6">
          <div className="flex items-center gap-4">
            <Link to={token ? '/tasks' : '/login'} className="flex items-center gap-3">
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#2563eb,#60a5fa)] text-base font-semibold text-white shadow-[0_12px_24px_rgba(37,99,235,0.28)]">
                K
              </div>
              <div>
                <div className="text-lg font-semibold text-[var(--text-primary)]">{SITE_NAME}</div>
                <div className="text-xs text-[var(--text-secondary)]">Жеке ассистент</div>
              </div>
            </Link>
          </div>

          <div className="flex flex-col gap-3 md:items-end">
            <div className="flex flex-wrap items-center gap-2">
              <ThemeToggle
                theme={theme}
                onToggle={() => setTheme((prev) => (prev === 'dark' ? 'light' : 'dark'))}
              />
              {token ? <GhostButton onClick={logout}>Шығу</GhostButton> : null}
            </div>
            {token ? (
              <nav className="flex flex-wrap gap-2">
                {navItems.map((item) => {
                  const isActive = location.pathname === item.to || location.pathname.startsWith(`${item.to}/`);
                  return (
                    <NavLink
                      key={item.to}
                      to={item.to}
                      className={`rounded-xl px-4 py-2 text-sm font-medium transition ${
                        isActive
                          ? 'bg-[var(--brand-strong)] text-white shadow-[0_14px_26px_rgba(37,99,235,0.22)]'
                          : 'bg-[var(--card-strong)] text-[var(--text-secondary)] hover:text-[var(--brand-strong)]'
                      }`}
                    >
                      {item.label}
                    </NavLink>
                  );
                })}
              </nav>
            ) : null}
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-6 md:px-6 md:py-8">
        <Routes>
          <Route path="/" element={<Navigate to={token ? '/tasks' : '/login'} replace />} />
          <Route path="/login" element={token ? <Navigate to="/tasks" replace /> : <Login siteName={SITE_NAME} />} />
          <Route path="/register" element={token ? <Navigate to="/tasks" replace /> : <Register siteName={SITE_NAME} />} />
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
          <Route path="/tasks" element={token ? <TasksPage /> : <Navigate to="/login" replace />} />
          <Route path="/calendar" element={token ? <CalendarPage /> : <Navigate to="/login" replace />} />
          <Route path="/notes" element={token ? <NotesPage /> : <Navigate to="/login" replace />} />
          <Route path="/chat" element={token ? <ChatPage /> : <Navigate to="/login" replace />} />
          <Route path="/profile" element={token ? <Profile /> : <Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to={token ? '/tasks' : '/login'} replace />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <ErrorBoundary>
        <ToastProvider>
          <AppShell />
        </ToastProvider>
      </ErrorBoundary>
    </BrowserRouter>
  );
}

export default App;
