import React, { useEffect, useMemo, useState } from 'react';
import TaskForm from '../components/TaskForm';
import { tasks } from '../services/api';
import { useToast } from '../components/Toast';

function dateKey(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function monthLabel(date) {
  return date.toLocaleDateString('kk-KZ', { month: 'long', year: 'numeric' });
}

export default function CalendarPage() {
  const toast = useToast();
  const [current, setCurrent] = useState(new Date());
  const [selected, setSelected] = useState(dateKey(new Date()));
  const [taskList, setTaskList] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadTasks = async () => {
    setLoading(true);
    try {
      const res = await tasks.getAll();
      setTaskList(res.data || []);
    } catch (err) {
      toast.error(err.response?.data || 'Күнтізбе жүктелмеді');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const days = useMemo(() => {
    const start = new Date(current.getFullYear(), current.getMonth(), 1);
    const end = new Date(current.getFullYear(), current.getMonth() + 1, 0);
    const offset = (start.getDay() + 6) % 7;
    const result = [];
    for (let i = 0; i < offset; i += 1) result.push(null);
    for (let d = 1; d <= end.getDate(); d += 1) {
      result.push(new Date(current.getFullYear(), current.getMonth(), d));
    }
    return result;
  }, [current]);

  const byDay = useMemo(() => {
    const map = {};
    taskList.forEach((task) => {
      if (!task.deadline) return;
      const key = dateKey(new Date(task.deadline));
      map[key] = map[key] || [];
      map[key].push(task);
    });
    return map;
  }, [taskList]);

  const selectedTasks = byDay[selected] || [];
  const defaultDateTime = `${selected}T09:00`;

  const handleCreated = (task) => {
    setTaskList((prev) => [task, ...prev]);
    if (task.deadline) {
      setSelected(dateKey(new Date(task.deadline)));
    }
  };

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-[1fr_380px]">
      <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div className="mb-5 flex items-center justify-between">
          <button
            onClick={() => setCurrent(new Date(current.getFullYear(), current.getMonth() - 1, 1))}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
          >
            Алдыңғы
          </button>
          <h1 className="text-xl font-semibold capitalize text-slate-950">{monthLabel(current)}</h1>
          <button
            onClick={() => setCurrent(new Date(current.getFullYear(), current.getMonth() + 1, 1))}
            className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
          >
            Келесі
          </button>
        </div>

        <div className="grid grid-cols-7 gap-2 text-center text-xs font-medium text-slate-500">
          {['Дс', 'Сс', 'Ср', 'Бс', 'Жм', 'Сб', 'Жс'].map((day) => (
            <div key={day} className="py-2">{day}</div>
          ))}
        </div>

        <div className="mt-2 grid grid-cols-7 gap-2">
          {days.map((day, index) => {
            if (!day) return <div key={`empty-${index}`} className="min-h-24" />;
            const key = dateKey(day);
            const list = byDay[key] || [];
            const isSelected = key === selected;
            const isToday = key === dateKey(new Date());
            return (
              <button
                key={key}
                onClick={() => setSelected(key)}
                className={`min-h-24 rounded-md border p-2 text-left transition ${
                  isSelected
                    ? 'border-slate-900 bg-slate-900 text-white'
                    : isToday
                      ? 'border-slate-400 bg-slate-50'
                      : 'border-slate-200 bg-white hover:bg-slate-50'
                }`}
              >
                <div className="text-sm font-semibold">{day.getDate()}</div>
                <div className="mt-2 space-y-1">
                  {list.slice(0, 2).map((task) => (
                    <div
                      key={task.id}
                      className={`truncate rounded px-1.5 py-0.5 text-xs ${
                        isSelected ? 'bg-white/15 text-white' : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {task.title}
                    </div>
                  ))}
                  {list.length > 2 ? (
                    <div className={`text-xs ${isSelected ? 'text-white/80' : 'text-slate-500'}`}>
                      +{list.length - 2}
                    </div>
                  ) : null}
                </div>
              </button>
            );
          })}
        </div>
      </section>

      <aside className="space-y-5">
        <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-950">Таңдалған күн</h2>
          <p className="mt-1 text-sm text-slate-500">{new Date(selected).toLocaleDateString('kk-KZ')}</p>
          <div className="mt-4 space-y-3">
            {loading ? (
              <div className="text-sm text-slate-500">Жүктеліп жатыр...</div>
            ) : selectedTasks.length === 0 ? (
              <div className="text-sm text-slate-500">Бұл күнге тапсырма жоқ</div>
            ) : (
              selectedTasks.map((task) => (
                <div key={task.id} className="rounded-md border border-slate-200 p-3">
                  <div className="font-medium text-slate-950">{task.title}</div>
                  <div className="mt-1 text-xs text-slate-500">{new Date(task.deadline).toLocaleTimeString()}</div>
                  {task.description ? <p className="mt-2 text-sm text-slate-600">{task.description}</p> : null}
                </div>
              ))
            )}
          </div>
        </section>

        <TaskForm defaultDate={defaultDateTime} onTaskCreated={handleCreated} />
      </aside>
    </div>
  );
}
