import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { auth, profile, saveAuthSession } from '../services/api';
import { useToast } from './Toast';

function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [consentGiven, setConsentGiven] = useState(true);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const toast = useToast();

  const loadMe = async () => {
    const res = await profile.me();
    if (res.data?.id) localStorage.setItem('userId', res.data.id);
    if (res.data?.email) localStorage.setItem('userEmail', res.data.email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!consentGiven) {
      toast.error('Деректерді өңдеуге келісім беру қажет');
      return;
    }
    setLoading(true);
    try {
      const res = await auth.register({ name, email, password, consentGiven });
      saveAuthSession(res.data);
      await loadMe();
      toast.success('Тіркелу сәтті өтті');
      navigate('/tasks');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Тіркелу қатесі');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[calc(100vh-120px)] max-w-md items-center">
      <div className="w-full rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-semibold text-slate-950">Тіркелу</h1>
        <p className="mt-1 text-sm text-slate-500">Жаңа аккаунт жасаңыз.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700">Аты-жөні</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">Құпиясөз</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
              required
            />
          </div>
          <label className="flex items-start gap-2 text-sm text-slate-600">
            <input
              type="checkbox"
              checked={consentGiven}
              onChange={(e) => setConsentGiven(e.target.checked)}
              className="mt-1"
            />
            Деректерімді өңдеуге келісемін
          </label>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-slate-900 px-4 py-2 font-medium text-white hover:bg-slate-800 disabled:opacity-60"
          >
            {loading ? 'Тіркеліп жатыр...' : 'Тіркелу'}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500">
          Аккаунт бар ма?{' '}
          <Link to="/login" className="font-medium text-slate-950 underline">
            Кіру
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Register;
