import React from 'react';
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

function TasksPage() {
  return (
    <div className="mx-auto max-w-7xl">
      <TaskList />
    </div>
  );
}

function ChatPage() {
  return (
    <div className="mx-auto max-w-4xl rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
      <div className="mb-5">
        <h1 className="text-2xl font-semibold text-slate-900">AI көмекші</h1>
        <p className="mt-1 text-sm text-slate-500">Жоспар, тапсырма және идея бойынша қысқа кеңес алыңыз.</p>
      </div>
      <Chat />
    </div>
  );
}

function AppShell() {
  const token = localStorage.getItem('token') || localStorage.getItem('accessToken');
  const toast = useToast();
  const location = useLocation();

  const logout = () => {
    clearAuthSession();
    toast.info('Сессия аяқталды');
    window.location.href = '/login';
  };

  const navItems = [
    { to: '/tasks', label: 'Тапсырмалар' },
    { to: '/calendar', label: 'Күнтізбе' },
    { to: '/notes', label: 'Заметки' },
    { to: '/chat', label: 'AI чат' },
    { to: '/profile', label: 'Профиль' },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-3 md:flex-row md:items-center md:justify-between">
          <Link to={token ? '/tasks' : '/login'} className="text-xl font-bold text-slate-950">
            Жеке Көмекші
          </Link>

          {token ? (
            <div className="flex flex-wrap items-center gap-2">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `rounded-md px-3 py-2 text-sm font-medium transition ${
                      isActive || location.pathname.startsWith(item.to)
                        ? 'bg-slate-900 text-white'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
              <button
                onClick={logout}
                className="rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
              >
                Шығу
              </button>
            </div>
          ) : null}
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-6">
        <Routes>
          <Route path="/" element={<Navigate to={token ? '/tasks' : '/login'} replace />} />
          <Route path="/login" element={token ? <Navigate to="/tasks" replace /> : <Login />} />
          <Route path="/register" element={token ? <Navigate to="/tasks" replace /> : <Register />} />
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
