import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { auth } from '../services/api';

function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [consentGiven, setConsentGiven] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!consentGiven) {
      alert('Деректерді өңдеуге келісім беру қажет');
      return;
    }
    try {
      const res = await auth.register({ name, email, password, consentGiven });
      localStorage.setItem('token', res.data.token);
      navigate('/tasks');
    } catch (err) {
      alert('Тіркелу қатесі');
    }
  };

  return (
    <div className="min-h-screen px-4 flex items-center justify-center">
      <div className="max-w-md w-full bg-white rounded-2xl shadow-xl p-8 space-y-6">
        <div className="flex justify-center">
          <div className="relative w-full max-w-sm">
            <div className="absolute -inset-1 rounded-2xl bg-gradient-to-r from-emerald-500 via-teal-500 to-cyan-500 opacity-20 blur"></div>
            <div className="relative rounded-2xl bg-white/70 backdrop-blur p-1 shadow ring-1 ring-gray-100">
              <div className="absolute inset-y-1 right-1 w-1/2 rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 transition-all"></div>
              <div className="relative z-10 grid grid-cols-2 gap-1">
                <Link
                  to="/login"
                  className="flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-gray-700 hover:bg-white"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 7a4 4 0 10-6 0 4 4 0 006 0zM5 21a7 7 0 0114 0H5z" />
                  </svg>
                  <span className="font-semibold">Кіру</span>
                </Link>
                <Link
                  to="/register"
                  aria-current="page"
                  className="flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-white"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4a4 4 0 110 8 4 4 0 010-8zm8 12h-3m0 0h-3m3 0v-3m0 3v3M6 18a6 6 0 1112 0H6z" />
                  </svg>
                  <span className="font-semibold">Тіркелу</span>
                </Link>
              </div>
            </div>
          </div>
        </div>
        <div className="text-center">
          <div className="mx-auto w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <h2 className="mt-4 text-2xl font-bold text-gray-800">Тіркелу</h2>
          <p className="text-gray-500">Жаңа аккаунт жасау</p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Аты-жөні</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
              placeholder="Аты-жөні"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
              placeholder="example@mail.com"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Құпиясөз</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500"
              placeholder="••••••••"
              required
            />
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              id="consent"
              checked={consentGiven}
              onChange={(e) => setConsentGiven(e.target.checked)}
              className="mr-2"
              required
            />
            <label htmlFor="consent" className="text-sm text-gray-600">
              Менің деректерімді өңдеуге келісемін (GDPR)
            </label>
          </div>
          <button
            type="submit"
            className="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-2 rounded-lg transition"
          >
            Тіркелу
          </button>
        </form>
        <div className="text-center">
          <button
            onClick={() => navigate('/login')}
            className="text-green-600 hover:text-green-800 text-sm font-medium"
          >
            Аккаунтыңыз бар? Кіру
          </button>
        </div>
      </div>
    </div>
  );
}

export default Register;