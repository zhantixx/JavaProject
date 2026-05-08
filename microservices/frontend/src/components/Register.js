import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../services/api';

function Register() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await auth.register({ email, password, name });
      localStorage.setItem('token', res.data.token);
      navigate('/tasks');
    } catch (err) {
      alert('Тіркелу қатесі');
    }
  };

  return (
    <div>
      <h2>Тіркелу</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" placeholder="Аты-жөні" value={name} onChange={(e) => setName(e.target.value)} required />
        <input type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input type="password" placeholder="Құпиясөз" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit">Тіркелу</button>
      </form>
      <button onClick={() => navigate('/login')}>Кіру</button>
    </div>
  );
}

export default Register;