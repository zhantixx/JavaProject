import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { auth, profile, saveAuthSession } from '../services/api';
import { useToast } from './Toast';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleEnabled, setGoogleEnabled] = useState(false);
  const navigate = useNavigate();
  const toast = useToast();

  useEffect(() => {
    (async () => {
      try {
        const res = await auth.providers();
        setGoogleEnabled(Boolean(res.data?.googleEnabled));
      } catch {
        setGoogleEnabled(false);
      }
    })();
  }, []);

  const loadMe = async () => {
    const res = await profile.me();
    if (res.data?.id) localStorage.setItem('userId', res.data.id);
    if (res.data?.email) localStorage.setItem('userEmail', res.data.email);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await auth.login({ email, password });
      saveAuthSession(res.data);
      await loadMe();
      toast.success('Login successful');
      navigate('/tasks');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-[calc(100vh-120px)] max-w-md items-center">
      <div className="w-full rounded-lg border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-semibold text-slate-950">Personal Assistant</h1>
        <p className="mt-1 text-sm text-slate-500">
          Sign in to manage tasks, notes, and your daily workflow.
        </p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
              placeholder="example@mail.com"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
              required
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-slate-900 px-4 py-2 font-medium text-white hover:bg-slate-800 disabled:opacity-60"
          >
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        {googleEnabled ? (
          <a
            href="http://localhost:8080/oauth2/authorization/google"
            className="mt-3 flex w-full items-center justify-center rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Sign in with Google
          </a>
        ) : (
          <div className="mt-3 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
            Google sign-in is not configured for this environment.
          </div>
        )}

        <p className="mt-5 text-center text-sm text-slate-500">
          No account yet?{' '}
          <Link to="/register" className="font-medium text-slate-950 underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
