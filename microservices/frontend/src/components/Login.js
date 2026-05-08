import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../services/api';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await auth.login({ email, password });
      localStorage.setItem('token', res.data.token);
      navigate('/tasks');
    } catch (err) {
      alert('Кіру қатесі');
    }
  };

  return (
    <div>
      <h2>Кіру беті</h2>
      <form onSubmit={handleSubmit}>
        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="password" placeholder="Құпиясөз" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit">Кіру</button>
      </form>
      <button onClick={() => navigate('/register')}>Тіркелу</button>
    </div>
  );
}

export default Login;