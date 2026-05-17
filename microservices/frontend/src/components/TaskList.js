import React, { useEffect, useMemo, useState } from 'react';
import TaskForm from './TaskForm';
import { tasks } from '../services/api';
import { useToast } from './Toast';
import { GhostButton, PageIntro, SectionTitle, Surface } from './UiKit';

const STATUS_LABELS = {
  PLANNED: 'Жоспар',
  IN_PROGRESS: 'Орындалуда',
  DONE: 'Дайын',
  CANCELED: 'Тоқтатылды',
};

const PRIORITY_LABELS = {
  HIGH: 'Жоғары',
  MEDIUM: 'Орташа',
  LOW: 'Төмен',
};

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
    return taskList.filter((task) => {
      const matchesQuery =
        !q || (task.title || '').toLowerCase().includes(q) || (task.description || '').toLowerCase().includes(q);
      const matchesStatus = !status || (task.status || '').toUpperCase() === status;
      return matchesQuery && matchesStatus;
    });
  }, [taskList, query, status]);

  const stats = useMemo(() => {
    const done = taskList.filter((task) => task.status === 'DONE').length;
    const planned = taskList.filter((task) => task.status === 'PLANNED').length;
    const high = taskList.filter((task) => task.priority === 'HIGH').length;
    const dueToday = taskList.filter((task) => {
      if (!task.deadline) return false;
      const current = new Date();
      const deadline = new Date(task.deadline);
      return deadline.toDateString() === current.toDateString();
    }).length;

    return [
      { label: 'Барлығы', value: taskList.length, hint: 'Жалпы тапсырма саны' },
      { label: 'Дайын', value: done, hint: 'Аяқталғандар' },
      { label: 'Жоспарда', value: planned, hint: 'Алдағы орындаулар' },
      { label: 'Бүгін', value: dueToday + high, hint: 'Жоғары басымдық пен бүгінгі мерзім' },
    ];
  }, [taskList]);

  const handleCreated = (task) => {
    setTaskList((prev) => [task, ...prev]);
  };

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
        eyebrow="Жеке жоспар"
        title="Күндік ағынды басқару"
        description="Маңызды істерді бөліп алып, орындалу күйін бір беттен бақылаңыз. Фокус тек қажет нәрсеге түседі."
        stats={stats}
      />

      <TaskForm onTaskCreated={handleCreated} />

      <Surface className="overflow-hidden">
        <SectionTitle
          title="Тапсырмалар тізімі"
          description={`${visibleTasks.length} жазба көрініп тұр`}
          aside={
            <div className="grid gap-2 sm:grid-cols-2">
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Іздеу"
                className="app-input min-w-[180px]"
              />
              <select value={status} onChange={(e) => setStatus(e.target.value)} className="app-input min-w-[170px]">
                <option value="">Барлық күйлер</option>
                <option value="PLANNED">Жоспар</option>
                <option value="IN_PROGRESS">Орындалуда</option>
                <option value="DONE">Дайын</option>
              </select>
            </div>
          }
        />

        {loading ? (
          <div className="p-5 text-sm text-[var(--text-secondary)]">Жүктеліп жатыр...</div>
        ) : visibleTasks.length === 0 ? (
          <div className="p-10 text-center text-[var(--text-secondary)]">Әзірге бұл сүзгіге сай тапсырма жоқ</div>
        ) : (
          <div className="grid gap-4 p-5 xl:grid-cols-2">
            {visibleTasks.map((task) => (
              <article
                key={task.id}
                className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-5"
              >
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

                <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-[var(--text-secondary)]">
                  <span className="app-badge">ID #{task.id}</span>
                  {task.deadline ? (
                    <span className="app-badge">
                      Мерзімі: {new Date(task.deadline).toLocaleString('kk-KZ')}
                    </span>
                  ) : (
                    <span className="app-badge">Мерзімі көрсетілмеген</span>
                  )}
                </div>

                <div className="mt-5 flex flex-wrap gap-2">
                  <GhostButton onClick={() => handleStatus(task.id, task.status === 'DONE' ? 'PLANNED' : 'DONE')}>
                    {task.status === 'DONE' ? 'Қайтару' : 'Дайын деп белгілеу'}
                  </GhostButton>
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
