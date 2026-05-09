import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import TaskList from './components/TaskList';
import TaskForm from './components/TaskForm';
import Chat from './components/Chat';

function App() {
  const token = localStorage.getItem('token');

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
        <nav className="bg-white/80 backdrop-blur-sm shadow-sm sticky top-0 z-10">
          <div className="container mx-auto px-4 py-3 flex justify-between items-center">
            <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              Жеке Көмекші
            </h1>
            {token && (
              <button
                onClick={() => { localStorage.removeItem('token'); window.location.href = '/'; }}
                className="flex items-center gap-1 text-red-500 hover:text-red-700 transition"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                Шығу
              </button>
            )}
          </div>
        </nav>
        <main className="container mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<Navigate to={token ? "/tasks" : "/login"} />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/oauth2/redirect" element={<OAuth2Redirect />} />
            <Route path="/tasks" element={
              token ? (
                <div className="max-w-4xl mx-auto space-y-6">
                  <TaskForm onTaskCreated={() => window.location.reload()} />
                  <TaskList />
                  <Chat />
                </div>
              ) : <Navigate to="/login" />
            } />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;