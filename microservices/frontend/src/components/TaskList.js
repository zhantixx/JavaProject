import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { tasks } from '../services/api';
import { useToast } from './Toast';
import { GhostButton, PageIntro, PrimaryButton, SectionTitle, Surface } from './UiKit';

const STATUS_LABELS = {
  PLANNED: 'Жоспар',
  DONE: 'Дайын',
  CANCELED: 'Тоқтатылды',
};

const PRIORITY_LABELS = {
  HIGH: 'Жоғары',
  MEDIUM: 'Орташа',
  LOW: 'Төмен',
};

function formatDeadline(deadline) {
  return new Date(deadline).toLocaleString('kk-KZ', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatRemaining(deadline) {
  const diffMs = new Date(deadline).getTime() - Date.now();
  if (Number.isNaN(diffMs)) return 'Мерзім белгісіз';
  if (diffMs <= 0) return 'Мерзімі өтіп кетті';

  const totalMinutes = Math.floor(diffMs / 60000);
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const minutes = totalMinutes % 60;

  if (days > 0) return `${days} күн ${hours} сағ қалды`;
  if (hours > 0) return `${hours} сағ ${minutes} мин қалды`;
  return `${minutes} мин қалды`;
}

function TaskList() {
  const toast = useToast();
  const [taskList, setTaskList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');

  const loadTasks = async () => {
    setLoading(true);
    try {
      const res = await tasks.getAll();
      setTaskList(res.data || []);
    } catch (err) {
      toast.error(err.response?.data || 'Тапсырмалар жүктелмеді');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const visibleTasks = useMemo(() => {
    const q = query.trim().toLowerCase();
    return [...taskList]
      .filter((task) => {
        const matchesQuery =
          !q || (task.title || '').toLowerCase().includes(q) || (task.description || '').toLowerCase().includes(q);
        const matchesStatus = !status || (task.status || '').toUpperCase() === status;
        return matchesQuery && matchesStatus;
      })
      .sort((a, b) => {
        if (!a.deadline) return 1;
        if (!b.deadline) return -1;
        return new Date(a.deadline) - new Date(b.deadline);
      });
  }, [taskList, query, status]);

  const stats = useMemo(() => {
    const planned = taskList.filter((task) => task.status === 'PLANNED').length;
    const withDeadline = taskList.filter((task) => Boolean(task.deadline)).length;
    const overdue = taskList.filter((task) => task.deadline && new Date(task.deadline) < new Date() && task.status !== 'DONE').length;

    return [
      { label: 'Барлығы', value: taskList.length, hint: 'Жалпы тапсырма саны' },
      { label: 'Жоспарда', value: planned, hint: 'Әлі аяқталмағандар' },
      { label: 'Мерзімі бар', value: withDeadline, hint: 'Уақыты көрсетілгендер' },
      { label: 'Кешіккен', value: overdue, hint: 'Назар сұрайтындар' },
    ];
  }, [taskList]);

  const handleDelete = async (id) => {
    try {
      await tasks.delete(id);
      setTaskList((prev) => prev.filter((task) => task.id !== id));
      toast.success('Тапсырма өшірілді');
    } catch (err) {
      toast.error(err.response?.data || 'Өшіру қатесі');
    }
  };

  const handleStatus = async (id, nextStatus) => {
    try {
      const res = await tasks.updateStatus(id, nextStatus);
      setTaskList((prev) => prev.map((task) => (task.id === id ? res.data : task)));
      toast.success('Күйі жаңартылды');
    } catch (err) {
      toast.error(err.response?.data || 'Күйі өзгермеді');
    }
  };

  const priorityClass = (priority) => {
    if (priority === 'HIGH') return 'border-rose-200 bg-[var(--danger-soft)] text-rose-500';
    if (priority === 'LOW') return 'border-emerald-200 bg-[var(--success-soft)] text-emerald-600';
    return 'border-amber-200 bg-[var(--warning-soft)] text-amber-600';
  };

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Бақылау тақтасы"
        title="Тапсырмалар"
        description="Тізім тек орындауға арналған болсын: не істеу керек, қашан бітуі тиіс және қайсысы кешігіп тұр — бәрі бір жерде."
        stats={stats}
        actions={
          <Link to="/tasks/new">
            <PrimaryButton>Жаңа тапсырма</PrimaryButton>
          </Link>
        }
      />

      <Surface className="overflow-hidden">
        <SectionTitle
          title="Тапсырмалар тізімі"
          description={`${visibleTasks.length} жазба көрініп тұр`}
          aside={
            <div className="grid gap-2 sm:grid-cols-2">
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Тапсырма іздеу"
                className="app-input min-w-[200px]"
              />
              <select value={status} onChange={(e) => setStatus(e.target.value)} className="app-input min-w-[180px]">
                <option value="">Барлық күйлер</option>
                <option value="PLANNED">Жоспар</option>
                <option value="DONE">Дайын</option>
                <option value="CANCELED">Тоқтатылды</option>
              </select>
            </div>
          }
        />

        {loading ? (
          <div className="p-5 text-sm text-[var(--text-secondary)]">Жүктеліп жатыр...</div>
        ) : visibleTasks.length === 0 ? (
          <div className="p-10 text-center text-[var(--text-secondary)]">Сәйкес тапсырма табылмады</div>
        ) : (
          <div className="grid gap-4 p-5 xl:grid-cols-2">
            {visibleTasks.map((task) => (
              <article key={task.id} className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-5">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="text-lg font-semibold text-[var(--text-primary)]">{task.title}</h3>
                      <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${priorityClass(task.priority)}`}>
                        {PRIORITY_LABELS[task.priority] || PRIORITY_LABELS.MEDIUM}
                      </span>
                    </div>
                    {task.description ? (
                      <p className="mt-3 text-sm leading-6 text-[var(--text-secondary)]">{task.description}</p>
                    ) : (
                      <p className="mt-3 text-sm text-[var(--muted-text)]">Сипаттама қосылмаған</p>
                    )}
                  </div>
                  <span className="app-badge">{STATUS_LABELS[task.status] || STATUS_LABELS.PLANNED}</span>
                </div>

                <div className="mt-4 grid gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--panel-bg)] p-3">
                    <div className="text-xs uppercase tracking-[0.12em] text-[var(--muted-text)]">Орындау уақыты</div>
                    <div className="mt-2 text-sm font-medium text-[var(--text-primary)]">
                      {task.deadline ? formatDeadline(task.deadline) : 'Көрсетілмеген'}
                    </div>
                  </div>
                  <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--panel-bg)] p-3">
                    <div className="text-xs uppercase tracking-[0.12em] text-[var(--muted-text)]">Қалған уақыт</div>
                    <div className="mt-2 text-sm font-medium text-[var(--text-primary)]">
                      {task.deadline ? formatRemaining(task.deadline) : 'Шектеу жоқ'}
                    </div>
                  </div>
                </div>

                <div className="mt-5 flex flex-wrap gap-2">
                  <GhostButton onClick={() => handleStatus(task.id, task.status === 'DONE' ? 'PLANNED' : 'DONE')}>
                    {task.status === 'DONE' ? 'Қайта ашу' : 'Дайын деп белгілеу'}
                  </GhostButton>
                  <button
                    onClick={() => handleStatus(task.id, 'CANCELED')}
                    className="rounded-xl border border-amber-200 bg-[var(--warning-soft)] px-4 py-2.5 text-sm font-medium text-amber-700 transition hover:border-amber-300"
                  >
                    Тоқтату
                  </button>
                  <button
                    onClick={() => handleDelete(task.id)}
                    className="rounded-xl border border-rose-200 bg-[var(--danger-soft)] px-4 py-2.5 text-sm font-medium text-rose-500 transition hover:border-rose-300"
                  >
                    Өшіру
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </Surface>
    </div>
  );
}

export default TaskList;
