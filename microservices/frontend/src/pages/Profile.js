import React, { useEffect, useState } from 'react';
import { useToast } from '../components/Toast';
import { profile } from '../services/api';

export default function Profile() {
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ name: '', email: '' });

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const r = await profile.me();
        if (r.status >= 200 && r.status < 300) {
          const me = r.data || {};
          setForm({ name: me.name || '', email: me.email || '' });
        } else {
          toast.error('Профильді жүктеу мүмкін болмады');
        }
      } catch {
        toast.error('Профильді жүктеу қатесі');
      } finally {
        setLoading(false);
      }
    })();
  }, [toast]);

  const onSave = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const r = await profile.updateMe({ name: form.name });
      if (r.status >= 200 && r.status < 300) {
        toast.success('Профиль жаңартылды');
      } else {
        toast.error(`Жаңарту мүмкін болмады: ${r.status}`);
      }
    } catch {
      toast.error('Профильді жаңарту қатесі');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-indigo-50 py-10 px-4">
      <div className="max-w-3xl mx-auto">
        <div className="rounded-2xl bg-white/80 backdrop-blur p-8 ring-1 ring-gray-100 shadow-sm">
          <div className="flex items-center gap-4 mb-8">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-indigo-600 to-blue-600 text-white flex items-center justify-center">
              <span className="text-xl font-bold">{(form.name || form.email || 'U').substring(0,1).toUpperCase()}</span>
            </div>
            <div>
              <h1 className="text-2xl font-semibold text-gray-900">Пайдаланушы профилі</h1>
              <p className="text-sm text-gray-500">Деректеріңізді жаңартыңыз</p>
            </div>
          </div>

          {loading && (
            <div className="mb-6">
              <div className="h-3 w-24 rounded bg-gray-100 mb-2 animate-pulse"></div>
              <div className="h-10 w-full rounded-lg bg-gray-100 animate-pulse"></div>
            </div>
          )}

          <form onSubmit={onSave} className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                disabled
                value={form.email}
                className="w-full rounded-xl border border-gray-200 bg-gray-100 px-3 py-2 text-gray-700"
              />
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Аты</label>
              <input
                type="text"
                value={form.name}
                onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
                className="w-full rounded-xl border border-gray-200 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Сіздің атыңыз"
              />
            </div>
            <div className="md:col-span-2 flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl text-white bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-700 hover:to-blue-700 disabled:opacity-60"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                </svg>
                Сақтау
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
