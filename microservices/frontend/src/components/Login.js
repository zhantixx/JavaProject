import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { auth, profile, saveAuthSession } from '../services/api';
import { useToast } from './Toast';
import { PrimaryButton, Surface } from './UiKit';

function Login({ siteName = 'SanaBot' }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleEnabled, setGoogleEnabled] = useState(false);
  const [githubEnabled, setGithubEnabled] = useState(false);
  const navigate = useNavigate();
  const toast = useToast();

  useEffect(() => {
    (async () => {
      try {
        const res = await auth.providers();
        setGoogleEnabled(Boolean(res.data?.googleEnabled));
        setGithubEnabled(Boolean(res.data?.githubEnabled));
      } catch {
        setGoogleEnabled(false);
        setGithubEnabled(false);
      }
    })();
  }, []);

  const loadMe = async () => {
    const res = await profile.me();
    if (res.data?.id) localStorage.setItem('userId', res.data.id);
    if (res.data?.email) localStorage.setItem('userEmail', res.data.email);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    try {
      const res = await auth.login({ email, password });
      saveAuthSession(res.data);
      await loadMe();
      toast.success('Жүйеге кіру сәтті аяқталды');
      navigate('/tasks');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Кіру қатесі');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto grid min-h-[calc(100vh-132px)] max-w-6xl items-center gap-6 lg:grid-cols-[1.15fr_0.85fr]">
      <Surface className="overflow-hidden">
        <div className="p-8 md:p-10">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[var(--muted-text)]">Күнделікті басқару</p>
          <h1 className="mt-3 max-w-lg text-4xl font-semibold leading-tight text-[var(--text-primary)]">
            {siteName} сіздің жұмыс ырғағыңызды тыныш әрі жинақы ұстайды
          </h1>
          <p className="mt-4 max-w-xl text-sm leading-6 text-[var(--text-secondary)]">
            Тапсырмалар, жазбалар, күнтізбе және AI көмек бір жерде. Артық элементсіз, тек қажет нәрсе.
          </p>

          <div className="mt-8 grid gap-3 sm:grid-cols-3">
            <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
              <div className="text-sm font-medium text-[var(--text-primary)]">Тапсырмалар</div>
              <div className="mt-1 text-xs text-[var(--text-secondary)]">Басымдық пен мерзімге қарай бақылау</div>
            </div>
            <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
              <div className="text-sm font-medium text-[var(--text-primary)]">AI көмек</div>
              <div className="mt-1 text-xs text-[var(--text-secondary)]">Күрделі істі қадамдарға бөліп алу</div>
            </div>
            <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
              <div className="text-sm font-medium text-[var(--text-primary)]">Күнтізбе</div>
              <div className="mt-1 text-xs text-[var(--text-secondary)]">Күндік жүктемені бір көріністе көру</div>
            </div>
          </div>
        </div>
      </Surface>

      <Surface className="p-7 md:p-8">
        <h2 className="text-2xl font-semibold text-[var(--text-primary)]">Кіру</h2>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">Аккаунтыңызға кіріп, жұмысты жалғастырыңыз.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Email</label>
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="app-input"
              placeholder="example@mail.com"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Құпиясөз</label>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="app-input"
              placeholder="••••••••"
              required
            />
          </div>
          <PrimaryButton type="submit" disabled={loading} className="w-full">
            {loading ? 'Кіріп жатыр...' : 'Жүйеге кіру'}
          </PrimaryButton>
        </form>

        {googleEnabled || githubEnabled ? (
          <div className="mt-3 grid gap-2">
            {googleEnabled ? (
              <a
                href="http://localhost:8080/oauth2/authorization/google"
                className="flex w-full items-center justify-center rounded-xl border border-[var(--border-color)] bg-[var(--card-strong)] px-4 py-3 text-sm font-medium text-[var(--text-primary)] transition hover:border-[var(--brand-border)] hover:text-[var(--brand-strong)]"
              >
                Google арқылы кіру
              </a>
            ) : null}
            {githubEnabled ? (
              <a
                href="http://localhost:8080/oauth2/authorization/github"
                className="flex w-full items-center justify-center rounded-xl border border-[var(--border-color)] bg-[var(--card-strong)] px-4 py-3 text-sm font-medium text-[var(--text-primary)] transition hover:border-[var(--brand-border)] hover:text-[var(--brand-strong)]"
              >
                GitHub арқылы кіру
              </a>
            ) : null}
          </div>
        ) : (
          <div className="mt-3 rounded-xl border border-[var(--border-color)] bg-[var(--card-soft)] px-4 py-3 text-sm text-[var(--text-secondary)]">
            Google және GitHub арқылы кіру бұл ортада бапталмаған.
          </div>
        )}

        <p className="mt-5 text-center text-sm text-[var(--text-secondary)]">
          Аккаунтыңыз жоқ па?{' '}
          <Link to="/register" className="font-medium text-[var(--brand-strong)]">
            Тіркелу
          </Link>
        </p>
      </Surface>
    </div>
  );
}

export default Login;
