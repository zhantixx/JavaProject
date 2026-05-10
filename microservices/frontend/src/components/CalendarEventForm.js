import React, { useState } from 'react';
import { useAuth } from '../context/AuthProvider';
import { useToast } from './Toast';

export default function CalendarEventForm() {
  const { apiFetch } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({
    summary: '',
    description: '',
    startDateTime: '',
    endDateTime: '',
    timeZone: 'UTC'
  });
  const [loading, setLoading] = useState(false);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!form.summary || !form.startDateTime || !form.endDateTime) {
      toast.error('Заполните обязательные поля');
      return;
    }
    setLoading(true);
    try {
      const r = await apiFetch('/api/calendar/events', {
        method: 'POST',
        body: JSON.stringify({
          summary: form.summary,
          description: form.description,
          startDateTime: form.startDateTime,
          endDateTime: form.endDateTime,
          timeZone: form.timeZone
        })
      });
      if (r.ok) {
        const data = await r.json().catch(() => ({}));
        toast.success(`Событие создано${data.id ? ` (id: ${data.id})` : ''}`);
        setForm({
          summary: '',
          description: '',
          startDateTime: '',
          endDateTime: '',
          timeZone: form.timeZone
        });
      } else if (r.status === 401) {
        toast.error('Авторизация истекла. Войдите заново.');
      } else {
        const txt = await r.text();
        toast.error(`Ошибка создания события: ${txt || r.status}`);
      }
    } catch (err) {
      toast.error('Сетевая ошибка при создании события');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit} className="space-y-5">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Заголовок*</label>
          <input
            name="summary"
            value={form.summary}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
            placeholder="Напр. Встреча"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Часовой пояс</label>
          <input
            name="timeZone"
            value={form.timeZone}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
            placeholder="UTC, Europe/Moscow ..."
          />
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
        <textarea
          name="description"
          value={form.description}
          onChange={onChange}
          rows={3}
          className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
          placeholder="Опционально"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Начало (RFC3339)*</label>
          <input
            type="datetime-local"
            name="startDateTime"
            value={form.startDateTime}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Конец (RFC3339)*</label>
          <input
            type="datetime-local"
            name="endDateTime"
            value={form.endDateTime}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
          />
        </div>
      </div>

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center px-5 py-2.5 rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-60"
        >
          {loading ? 'Создание...' : 'Создать событие'}
        </button>
      </div>
    </form>
  );
}
