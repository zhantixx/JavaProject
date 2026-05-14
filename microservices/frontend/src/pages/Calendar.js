import React, { useEffect, useMemo, useState } from 'react';
import CalendarEventForm from '../components/CalendarEventForm';
import { useToast } from '../components/Toast';
import { calendar, tasks } from '../services/api';

function startOfMonth(date) {
  const d = new Date(date.getFullYear(), date.getMonth(), 1);
  return d;
}
function endOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}
function formatISODate(d) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function CalendarPage() {
  const toast = useToast();
  const [current, setCurrent] = useState(new Date());
  const [items, setItems] = useState([]); // задачи
  const [selectedDate, setSelectedDate] = useState(null);

  const icsUrl = calendar.icsUrl();

  useEffect(() => {
    (async () => {
      try {
        const r = await tasks.getAll();
        setItems(r.data || []);
      } catch {
        toast.error('Тапсырмаларды жүктеу қатесі');
      }
    })();
  }, [toast]);

  const monthDays = useMemo(() => {
    const start = startOfMonth(current);
    const end = endOfMonth(current);
    const days = [];
    // пустые ячейки до первого дня
    const offset = (start.getDay() + 6) % 7; // Пн=0
    for (let i = 0; i < offset; i++) days.push(null);
    for (let d = 1; d <= end.getDate(); d++) {
      days.push(new Date(current.getFullYear(), current.getMonth(), d));
    }
    return days;
  }, [current]);

  const itemsByDay = useMemo(() => {
    const map = {};
    (items || []).forEach(t => {
      if (!t.deadline) return;
      const d = new Date(t.deadline);
      const key = formatISODate(new Date(d.getFullYear(), d.getMonth(), d.getDate()));
      if (!map[key]) map[key] = [];
      map[key].push(t);
    });
    return map;
  }, [items]);

  const prevMonth = () => setCurrent(new Date(current.getFullYear(), current.getMonth() - 1, 1));
  const nextMonth = () => setCurrent(new Date(current.getFullYear(), current.getMonth() + 1, 1));

  const copyIcs = async () => {
    try {
      await navigator.clipboard.writeText(icsUrl);
      toast.success('ICS сілтемесі көшірілді. Күнтізбеге URL арқылы қосыңыз.');
    } catch {
      toast.error('Сілтемені көшіру мүмкін болмады');
    }
  };

  const weekdayLabels = ['Дс', 'Сс', 'Ср', 'Бс', 'Жм', 'Сб', 'Жс'];

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white rounded-2xl shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <button onClick={prevMonth} className="px-3 py-1.5 rounded-lg bg-gray-100 hover:bg-gray-200">←</button>
            <h1 className="text-xl font-semibold text-gray-900">
              {current.toLocaleString('kk-KZ', { month: 'long', year: 'numeric' })}
            </h1>
            <button onClick={nextMonth} className="px-3 py-1.5 rounded-lg bg-gray-100 hover:bg-gray-200">→</button>
          </div>

          <div className="grid grid-cols-7 text-center text-gray-500 text-sm mb-2">
            {weekdayLabels.map((w) => <div key={w} className="py-1">{w}</div>)}
          </div>

          <div className="grid grid-cols-7 gap-1">
            {monthDays.map((d, idx) => {
              if (!d) return <div key={`e-${idx}`} className="h-24 bg-transparent" />;
              const key = formatISODate(d);
              const list = itemsByDay[key] || [];
              const isToday = key === formatISODate(new Date());
              return (
                <button
                  key={key}
                  onClick={() => setSelectedDate({ key, date: d, list })}
                  className={`h-24 text-left p-2 rounded-lg border transition ${
                    isToday ? 'border-indigo-400 bg-indigo-50' : 'border-gray-200 bg-white hover:bg-gray-50'
                  }`}
                >
                  <div className="text-xs text-gray-500">{d.getDate()}</div>
                  {list.slice(0, 3).map(t => (
                    <div key={t.id} className="mt-1 truncate text-xs rounded px-1 py-0.5
                      bg-slate-100 text-slate-700">
                      {t.status === 'OVERDUE' ? '⏰ ' : ''}{t.title}
                    </div>
                  ))}
                  {list.length > 3 && (
                    <div className="mt-1 text-xs text-gray-500">+{list.length - 3} тағы</div>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white rounded-2xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">ICS сілтеме</h2>
            <div className="flex items-center gap-2">
              <input readOnly value={icsUrl} className="flex-1 rounded-lg border border-gray-300 px-3 py-2 bg-gray-50 text-gray-700" />
              <button onClick={copyIcs} className="px-3 py-2 rounded-lg bg-slate-700 text-white hover:bg-slate-800">
                Көшіру
              </button>
            </div>
            <p className="text-sm text-gray-500 mt-2">
              Google/Outlook‑қа «URL арқылы жазылу» ретінде қосыңыз.
            </p>
          </div>

          <div className="bg-white rounded-2xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Оқиға қосу</h2>
            <CalendarEventForm />
          </div>

          <div className="bg-white rounded-2xl shadow-sm p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-2">Күн бойынша тапсырмалар</h2>
            {selectedDate ? (
              <div>
                <div className="text-sm text-gray-600 mb-2">
                  Таңдалған күн: {selectedDate.date.toLocaleDateString('kk-KZ')}
                </div>
                <ul className="divide-y">
                  {selectedDate.list.map(t => (
                    <li key={t.id} className="py-2">
                      <div className="font-medium text-gray-900">
                        {t.status === 'OVERDUE' ? '⏰ ' : ''}{t.title}
                      </div>
                      {t.description && <div className="text-sm text-gray-600">{t.description}</div>}
                    </li>
                  ))}
                  {selectedDate.list.length === 0 && (
                    <li className="py-2 text-sm text-gray-500">Бұл күнге тапсырма жоқ</li>
                  )}
                </ul>
              </div>
            ) : (
              <div className="text-sm text-gray-500">Сол жақтағы күннен таңдаңыз</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
