import React, { useMemo, useState } from 'react';
import { useToast } from './Toast';
import { calendar } from '../services/api';

function toLocalDateTimeValue(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const adjusted = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return adjusted.toISOString().slice(0, 16);
}

function getMinDateTimeValue() {
  const now = new Date();
  now.setSeconds(0, 0);
  return toLocalDateTimeValue(now);
}

function isPastDateTime(value) {
  if (!value) {
    return false;
  }
  return new Date(value).getTime() < Date.now();
}

export default function CalendarEventForm() {
  const toast = useToast();
  const [form, setForm] = useState({
    title: '',
    description: '',
    startDateTime: '',
    endDateTime: '',
    timeZone: 'Asia/Almaty',
  });
  const [loading, setLoading] = useState(false);
  const minDateTime = useMemo(() => getMinDateTimeValue(), []);

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    if (!form.title || !form.startDateTime || !form.endDateTime) {
      toast.error('Міндетті өрістерді толтырыңыз');
      return;
    }

    if (isPastDateTime(form.startDateTime)) {
      toast.error('Өткен күнді таңдауға болмайды');
      return;
    }

    if (new Date(form.startDateTime) >= new Date(form.endDateTime)) {
      toast.error('Басталу уақыты аяқталу уақытынан бұрын болуы керек');
      return;
    }

    setLoading(true);
    try {
      const response = await calendar.createEvent({
        title: form.title,
        description: form.description,
        startDateTime: new Date(form.startDateTime).toISOString(),
        endDateTime: new Date(form.endDateTime).toISOString(),
        timeZone: form.timeZone,
      });

      if (response.status >= 200 && response.status < 300) {
        const data = response.data || {};
        toast.success(`Оқиға қосылды${data.eventId ? ` (id: ${data.eventId})` : ''}`);
        setForm({
          title: '',
          description: '',
          startDateTime: '',
          endDateTime: '',
          timeZone: form.timeZone,
        });
      } else {
        toast.error(`Оқиға қосу қатесі: ${response.status}`);
      }
    } catch (error) {
      const errorMessage = error.response?.data?.error || error.message || 'Оқиға жасауда қате';
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit} className="space-y-5">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Тақырып*</label>
          <input
            name="title"
            value={form.title}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
            placeholder="Мысалы, кездесу"
            required
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Уақыт белдеуі</label>
          <select
            name="timeZone"
            value={form.timeZone}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
          >
            <option value="UTC">UTC</option>
            <option value="Asia/Almaty">Asia/Almaty (GMT+6)</option>
            <option value="Asia/Astana">Asia/Astana (GMT+6)</option>
            <option value="Europe/Moscow">Europe/Moscow (GMT+3)</option>
            <option value="Asia/Tashkent">Asia/Tashkent (GMT+5)</option>
            <option value="Asia/Bishkek">Asia/Bishkek (GMT+6)</option>
            <option value="Asia/Dubai">Asia/Dubai (GMT+4)</option>
          </select>
        </div>
      </div>

      <div>
        <label className="mb-1 block text-sm font-medium text-gray-700">Сипаттама</label>
        <textarea
          name="description"
          value={form.description}
          onChange={onChange}
          rows={3}
          className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
          placeholder="Қосымша ақпарат"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Басталуы*</label>
          <input
            type="datetime-local"
            name="startDateTime"
            value={form.startDateTime}
            min={minDateTime}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-gray-700">Аяқталуы*</label>
          <input
            type="datetime-local"
            name="endDateTime"
            value={form.endDateTime}
            min={form.startDateTime || minDateTime}
            onChange={onChange}
            className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
            required
          />
        </div>
      </div>

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center rounded-lg bg-emerald-600 px-5 py-2.5 text-white hover:bg-emerald-700 disabled:opacity-60"
        >
          {loading ? 'Қосылуда...' : 'Оқиға қосу'}
        </button>
      </div>
    </form>
  );
}
