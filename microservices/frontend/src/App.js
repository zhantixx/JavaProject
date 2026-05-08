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
      <div>
        <h1>Жеке Көмекші</h1>
        <nav>
          {token && <button onClick={() => { localStorage.removeItem('token'); window.location.href = '/'; }}>Шығу</button>}
        </nav>
        <Routes>
          <Route path="/" element={<Navigate to={token ? "/tasks" : "/login"} />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/tasks" element={
            token ? (
              <>
                <TaskForm onTaskCreated={() => window.location.reload()} />
                <TaskList />
                <Chat />
              </>
            ) : <Navigate to="/login" />
          } />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;