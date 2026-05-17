import React, { useEffect, useMemo, useState } from 'react';
import TaskForm from '../components/TaskForm';
import { tasks } from '../services/api';
import { useToast } from '../components/Toast';
import { GhostButton, PageIntro, Surface } from '../components/UiKit';

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
    <div className="space-y-6">
      <PageIntro
        eyebrow="Күндік көрініс"
        title="Күнтізбелік жоспар"
        description="Ай ішіндегі мерзімдерді бақылап, таңдалған күнге жаңа тапсырма қосыңыз."
        stats={[
          { label: 'Ай', value: monthLabel(current), hint: 'Ағымдағы көрініс' },
          { label: 'Таңдалған күн', value: new Date(selected).toLocaleDateString('kk-KZ'), hint: 'Негізгі фокус' },
          { label: 'Бүгінгі жазбалар', value: byDay[dateKey(new Date())]?.length || 0, hint: 'Бүгінгі мерзімдер' },
          { label: 'Барлығы', value: taskList.length, hint: 'Күнтізбедегі барлық тапсырма' },
        ]}
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <Surface className="overflow-hidden">
          <div className="flex items-center justify-between border-b border-[var(--border-color)] px-5 py-5">
            <GhostButton onClick={() => setCurrent(new Date(current.getFullYear(), current.getMonth() - 1, 1))}>
              Алдыңғы
            </GhostButton>
            <h1 className="text-xl font-semibold capitalize text-[var(--text-primary)]">{monthLabel(current)}</h1>
            <GhostButton onClick={() => setCurrent(new Date(current.getFullYear(), current.getMonth() + 1, 1))}>
              Келесі
            </GhostButton>
          </div>

          <div className="px-5 pb-5 pt-4">
            <div className="grid grid-cols-7 gap-2 text-center text-xs font-medium uppercase tracking-[0.12em] text-[var(--muted-text)]">
              {['Дс', 'Сс', 'Ср', 'Бс', 'Жм', 'Сб', 'Жс'].map((day) => (
                <div key={day} className="py-2">
                  {day}
                </div>
              ))}
            </div>

            <div className="mt-2 grid grid-cols-7 gap-2">
              {days.map((day, index) => {
                if (!day) return <div key={`empty-${index}`} className="min-h-28" />;
                const key = dateKey(day);
                const list = byDay[key] || [];
                const isSelected = key === selected;
                const isToday = key === dateKey(new Date());
                return (
                  <button
                    key={key}
                    onClick={() => setSelected(key)}
                    className={`min-h-28 rounded-2xl border p-3 text-left transition ${
                      isSelected
                        ? 'border-[var(--brand-strong)] bg-[var(--brand-strong)] text-white shadow-[0_18px_36px_rgba(37,99,235,0.22)]'
                        : isToday
                          ? 'border-[var(--brand-border)] bg-[var(--brand-soft)]'
                          : 'border-[var(--border-color)] bg-[var(--card-strong)] hover:border-[var(--brand-border)]'
                    }`}
                  >
                    <div className="text-sm font-semibold">{day.getDate()}</div>
                    <div className="mt-3 space-y-1">
                      {list.slice(0, 2).map((task) => (
                        <div
                          key={task.id}
                          className={`truncate rounded-xl px-2 py-1 text-xs ${
                            isSelected ? 'bg-white/15 text-white' : 'bg-[var(--card-soft)] text-[var(--text-secondary)]'
                          }`}
                        >
                          {task.title}
                        </div>
                      ))}
                      {list.length > 2 ? (
                        <div className={`text-xs ${isSelected ? 'text-white/80' : 'text-[var(--muted-text)]'}`}>
                          +{list.length - 2}
                        </div>
                      ) : null}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </Surface>

        <div className="space-y-6">
          <Surface className="overflow-hidden">
            <div className="border-b border-[var(--border-color)] px-5 py-5">
              <h2 className="text-lg font-semibold text-[var(--text-primary)]">Таңдалған күн</h2>
              <p className="mt-1 text-sm text-[var(--text-secondary)]">
                {new Date(selected).toLocaleDateString('kk-KZ')}
              </p>
            </div>

            <div className="space-y-3 p-5">
              {loading ? (
                <div className="text-sm text-[var(--text-secondary)]">Жүктеліп жатыр...</div>
              ) : selectedTasks.length === 0 ? (
                <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4 text-sm text-[var(--text-secondary)]">
                  Бұл күнге тапсырма жоқ
                </div>
              ) : (
                selectedTasks.map((task) => (
                  <div key={task.id} className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                    <div className="font-medium text-[var(--text-primary)]">{task.title}</div>
                    <div className="mt-1 text-xs text-[var(--muted-text)]">
                      {new Date(task.deadline).toLocaleTimeString('kk-KZ', { hour: '2-digit', minute: '2-digit' })}
                    </div>
                    {task.description ? (
                      <p className="mt-3 text-sm leading-6 text-[var(--text-secondary)]">{task.description}</p>
                    ) : null}
                  </div>
                ))
              )}
            </div>
          </Surface>

          <TaskForm defaultDate={defaultDateTime} onTaskCreated={handleCreated} />
        </div>
      </div>
    </div>
  );
}
