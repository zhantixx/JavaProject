import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { auth, profile, saveAuthSession } from '../services/api';
import { useToast } from './Toast';
import { PrimaryButton, Surface } from './UiKit';

function Register({ siteName = 'SanaBot' }) {
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
      toast.success('Тіркелу сәтті аяқталды');
      navigate('/tasks');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Тіркелу қатесі');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto grid min-h-[calc(100vh-132px)] max-w-6xl items-center gap-6 lg:grid-cols-[0.92fr_1.08fr]">
      <Surface className="p-7 md:p-8">
        <h2 className="text-2xl font-semibold text-[var(--text-primary)]">Тіркелу</h2>
        <p className="mt-2 text-sm text-[var(--text-secondary)]">Жаңа аккаунт ашып, жеке жұмыс кеңістігін бастаңыз.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Аты-жөні</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="app-input"
              placeholder="Аты-жөніңіз"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
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
              onChange={(e) => setPassword(e.target.value)}
              className="app-input"
              placeholder="Кемінде 8 таңба"
              required
            />
          </div>
          <label className="flex items-start gap-3 rounded-xl border border-[var(--border-color)] bg-[var(--card-soft)] px-4 py-3 text-sm text-[var(--text-secondary)]">
            <input
              type="checkbox"
              checked={consentGiven}
              onChange={(e) => setConsentGiven(e.target.checked)}
              className="mt-1 h-4 w-4 accent-[var(--brand-strong)]"
            />
            <span>Мен деректерімді өңдеуге келісім беремін және сервисті жеке жоспарлау үшін қолданамын.</span>
          </label>
          <PrimaryButton type="submit" disabled={loading} className="w-full">
            {loading ? 'Тіркеліп жатыр...' : 'Аккаунт ашу'}
          </PrimaryButton>
        </form>

        <p className="mt-5 text-center text-sm text-[var(--text-secondary)]">
          Аккаунтыңыз бар ма?{' '}
          <Link to="/login" className="font-medium text-[var(--brand-strong)]">
            Кіру
          </Link>
        </p>
      </Surface>

      <Surface className="overflow-hidden">
        <div className="p-8 md:p-10">
          <p className="text-xs font-semibold uppercase tracking-[0.16em] text-[var(--muted-text)]">{siteName}</p>
          <h1 className="mt-3 max-w-lg text-4xl font-semibold leading-tight text-[var(--text-primary)]">
            Бір экранда жоспар, жазба және фокус
          </h1>
          <p className="mt-4 max-w-xl text-sm leading-6 text-[var(--text-secondary)]">
            Минималистік көк интерфейс күнделікті жұмысты тыныш ұстайды: не істеу керек, қашан істеу керек, бәрі анық.
          </p>

          <div className="mt-8 grid gap-4">
            {[
              ['Жеке кеңістік', 'Тапсырмалар, жазбалар және профиль бір логикамен байланысады.'],
              ['Екі режим', 'Жарық және қараңғы тақырып көзге жайлы орта береді.'],
              ['Қазақша интерфейс', 'Негізгі экрандар толық қазақ тілінде қайта құрылады.'],
            ].map(([title, text]) => (
              <div key={title} className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                <div className="text-sm font-medium text-[var(--text-primary)]">{title}</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">{text}</div>
              </div>
            ))}
          </div>
        </div>
      </Surface>
    </div>
  );
}

export default Register;
