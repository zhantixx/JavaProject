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
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-2xl shadow-sm p-8">
          <h1 className="text-2xl font-semibold text-gray-900 mb-6">Пайдаланушы профилі</h1>
          {loading && <div className="text-gray-500 mb-4">Жүктелуде...</div>}
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
              <label className="block text-sm font-medium text-gray-700 mb-1">Аты</label>
              <input
                type="text"
                value={form.name}
                onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
                className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Сіздің атыңыз"
              />
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={loading}
                className="inline-flex items-center px-5 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-60"
              >
                Сақтау
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
