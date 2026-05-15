import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import TaskList from './components/TaskList';
import TaskForm from './components/TaskForm';
import Chat from './components/Chat';
import OAuth2Redirect from './components/OAuth2Redirect';
import Profile from './pages/Profile';
import CalendarPage from './pages/Calendar';
import CalendarEventForm from './components/CalendarEventForm';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ToastProvider, useToast } from './components/Toast';

function AppShell() {
  const token = localStorage.getItem('token');
  const toast = useToast();
  const location = useLocation();

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    toast.info('Сессия аяқталды');
    window.location.href = '/';
  };

  const NavLink = ({ to, label }) => {
    const active = location.pathname.startsWith(to);
    return (
      <Link
        to={to}
        className={`px-3 py-1.5 rounded-lg transition ${
          active
            ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-sm'
            : 'text-gray-700 hover:text-gray-900 hover:bg-gray-100'
        }`}
      >
        {label}
      </Link>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50">
      <header className="sticky top-0 z-20 border-b border-white/40 bg-white/70 backdrop-blur-xl">
        <div className="container mx-auto px-4 py-3 flex items-center justify-between">
          <Link to="/" className="text-2xl font-extrabold tracking-tight bg-gradient-to-r from-blue-700 via-indigo-600 to-purple-600 bg-clip-text text-transparent">
            Жеке Көмекші
          </Link>
          <div className="flex items-center gap-2">
            {token ? (
              <>
                <NavLink to="/tasks" label="Тапсырмалар" />
                <NavLink to="/calendar" label="Күнтізбе" />
                <NavLink to="/profile" label="Пайдаланушы" />
                <button
                  onClick={logout}
                  className="ml-2 px-3 py-1.5 rounded-lg text-white bg-gradient-to-r from-rose-500 to-red-600 hover:from-rose-600 hover:to-red-700 transition shadow-sm"
                >
                  Шығу
                </button>
              </>
            ) : null}
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Navigate to={token ? "/tasks" : "/login"} />} />
          <Route path="/login" element={token ? <Navigate to="/tasks" replace /> : <Login />} />
          <Route path="/register" element={token ? <Navigate to="/tasks" replace /> : <Register />} />
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />

          <Route path="/tasks" element={
            token ? (
              <div className="max-w-6xl mx-auto space-y-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-7">
                  <div className="lg:col-span-2 space-y-7">
                    <div className="rounded-2xl bg-white/80 backdrop-blur p-6 ring-1 ring-gray-100 shadow-sm">
                      <TaskForm onTaskCreated={() => window.location.reload()} />
                    </div>
                    <div className="rounded-2xl bg-white/80 backdrop-blur p-0 ring-1 ring-gray-100 shadow-sm">
                      <TaskList />
                    </div>
                  </div>
                  <div className="space-y-7">
                    <div className="rounded-2xl bg-white/80 backdrop-blur p-6 ring-1 ring-gray-100 shadow-sm">
                      <h2 className="text-lg font-semibold text-gray-900 mb-4">Жедел оқиға</h2>
                      <CalendarEventForm />
                    </div>
                    <div className="rounded-2xl bg-white/80 backdrop-blur p-6 ring-1 ring-gray-100 shadow-sm">
                      <h2 className="text-lg font-semibold text-gray-900 mb-4">Чат‑көмекші</h2>
                      <Chat />
                    </div>
                  </div>
                </div>
              </div>
            ) : <Navigate to="/login" />
          } />

          <Route path="/profile" element={token ? <Profile /> : <Navigate to="/login" />} />
          <Route path="/calendar" element={token ? <CalendarPage /> : <Navigate to="/login" />} />
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