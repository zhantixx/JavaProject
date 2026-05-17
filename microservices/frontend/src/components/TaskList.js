import React, { useEffect, useMemo, useState } from 'react';
import TaskForm from './TaskForm';
import { tasks } from '../services/api';
import { useToast } from './Toast';

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
      const matchesQuery = !q ||
        (task.title || '').toLowerCase().includes(q) ||
        (task.description || '').toLowerCase().includes(q);
      const matchesStatus = !status || (task.status || '').toUpperCase() === status;
      return matchesQuery && matchesStatus;
    });
  }, [taskList, query, status]);

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
      setTaskList((prev) => prev.map((task) => task.id === id ? res.data : task));
    } catch (err) {
      toast.error(err.response?.data || 'Статус өзгермеді');
    }
  };

  const priorityClass = (priority) => {
    if (priority === 'HIGH') return 'bg-rose-50 text-rose-700 border-rose-200';
    if (priority === 'LOW') return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    return 'bg-amber-50 text-amber-700 border-amber-200';
  };

  return (
    <div className="space-y-5">
      <TaskForm onTaskCreated={handleCreated} />

      <section className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-col gap-3 border-b border-slate-200 p-5 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-xl font-semibold text-slate-950">Тапсырмалар</h1>
            <p className="text-sm text-slate-500">{visibleTasks.length} тапсырма көрсетіліп тұр</p>
          </div>
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Іздеу"
              className="rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
            />
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="rounded-md border border-slate-300 bg-white px-3 py-2 outline-none focus:border-slate-900"
            >
              <option value="">Барлық статус</option>
              <option value="PLANNED">Жоспар</option>
              <option value="IN_PROGRESS">Орындалуда</option>
              <option value="DONE">Дайын</option>
            </select>
          </div>
        </div>

        {loading ? (
          <div className="p-5 text-sm text-slate-500">Жүктеліп жатыр...</div>
        ) : visibleTasks.length === 0 ? (
          <div className="p-10 text-center text-slate-500">Әзірге тапсырма жоқ</div>
        ) : (
          <ul className="divide-y divide-slate-200">
            {visibleTasks.map((task) => (
              <li key={task.id} className="flex flex-col gap-3 p-5 md:flex-row md:items-start md:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-semibold text-slate-950">{task.title}</h3>
                    <span className={`rounded-full border px-2 py-0.5 text-xs ${priorityClass(task.priority)}`}>
                      {task.priority || 'MEDIUM'}
                    </span>
                    <span className="rounded-full border border-slate-200 bg-slate-50 px-2 py-0.5 text-xs text-slate-600">
                      {task.status || 'PLANNED'}
                    </span>
                  </div>
                  {task.description ? <p className="mt-1 text-sm text-slate-600">{task.description}</p> : null}
                  {task.deadline ? (
                    <p className="mt-2 text-xs text-slate-500">
                      Дедлайн: {new Date(task.deadline).toLocaleString()}
                    </p>
                  ) : null}
                </div>
                <div className="flex shrink-0 flex-wrap gap-2">
                  <button
                    onClick={() => handleStatus(task.id, task.status === 'DONE' ? 'PLANNED' : 'DONE')}
                    className="rounded-md border border-slate-300 px-3 py-2 text-sm hover:bg-slate-50"
                  >
                    {task.status === 'DONE' ? 'Қайтару' : 'Дайын'}
                  </button>
                  <button
                    onClick={() => handleDelete(task.id)}
                    className="rounded-md border border-rose-200 px-3 py-2 text-sm text-rose-700 hover:bg-rose-50"
                  >
                    Өшіру
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

export default TaskList;
