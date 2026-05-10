import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthProvider';
import { useToast } from '../components/Toast';

export default function Profile() {
  const { apiFetch, user, setUser } = useAuth();
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ name: '', email: '' });

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const r = await apiFetch('/api/users/me');
        if (r.ok) {
          const me = await r.json();
          setUser(me);
          setForm({ name: me.name || '', email: me.email || '' });
        } else {
          toast.error('Не удалось загрузить профиль');
        }
      } catch {
        toast.error('Ошибка при загрузке профиля');
      } finally {
        setLoading(false);
      }
    })();
  }, [apiFetch, setUser, toast]);

  const onSave = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const r = await apiFetch('/api/users/me', {
        method: 'PATCH',
        body: JSON.stringify({ name: form.name })
      });
      if (r.ok) {
        toast.success('Профиль обновлён');
        const updated = { ...(user || {}), name: form.name };
        setUser(updated);
      } else {
        const txt = await r.text();
        toast.error(`Не удалось обновить: ${txt || r.status}`);
      }
    } catch {
      toast.error('Ошибка при обновлении профиля');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-2xl shadow-sm p-8">
          <h1 className="text-2xl font-semibold text-gray-900 mb-6">Профиль</h1>
          {loading && <div className="text-gray-500 mb-4">Загрузка...</div>}
          <form onSubmit={onSave} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                disabled
                value={form.email}
                className="w-full rounded-lg border border-gray-300 bg-gray-100 px-3 py-2 text-gray-700"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Имя</label>
              <input
                type="text"
                value={form.name}
                onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Ваше имя"
              />
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="inline-flex items-center px-5 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-60"
              >
                Сохранить
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
