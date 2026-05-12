import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Link } from 'react-router-dom';
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

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    toast.info('Сессия аяқталды');
    window.location.href = '/';
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <nav className="bg-white/80 backdrop-blur-sm shadow-sm sticky top-0 z-10">
        <div className="container mx-auto px-4 py-3 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            Жеке Көмекші
          </Link>
          <div className="flex items-center gap-4">
            {token ? (
              <>
                <Link to="/tasks" className="text-gray-700 hover:text-gray-900">Тапсырмалар</Link>
                <Link to="/calendar" className="text-gray-700 hover:text-gray-900">Күнтізбе</Link>
                <Link to="/profile" className="text-gray-700 hover:text-gray-900">Пайдаланушы</Link>
                <button
                  onClick={logout}
                  className="flex items-center gap-1 text-red-500 hover:text-red-700 transition"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
                  Шығу
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-700 hover:text-gray-900">Кіру</Link>
                <Link to="/register" className="text-gray-700 hover:text-gray-900">Тіркелу</Link>
              </>
            )}
          </div>
        </div>
      </nav>
      <main className="container mx-auto px-4 py-8">
        <Routes>
          <Route path="/" element={<Navigate to={token ? "/tasks" : "/login"} />} />
          <Route path="/login" element={token ? <Navigate to="/tasks" replace /> : <Login />} />
          <Route path="/register" element={token ? <Navigate to="/tasks" replace /> : <Register />} />
          <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />

          <Route path="/tasks" element={
            token ? (
              <div className="max-w-5xl mx-auto space-y-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                  <div className="lg:col-span-2 space-y-6">
                    <TaskForm onTaskCreated={() => window.location.reload()} />
                    <TaskList />
                  </div>
                  <div className="space-y-6">
                    <div className="bg-white rounded-2xl shadow-sm p-6">
                      <h2 className="text-lg font-semibold text-gray-900 mb-4">Жедел оқиға (Күнтізбе)</h2>
                      <CalendarEventForm />
                    </div>
                    <div className="bg-white rounded-2xl shadow-sm p-6">
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