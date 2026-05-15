import React, { useEffect, useState } from 'react';
import { tasks } from '../services/api';

function TaskList() {
  const [taskList, setTaskList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState('');
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [tagId, setTagId] = useState('');

  const loadTasks = async () => {
    setLoading(true);
    const params = {};
    if (from) params.from = from;
    if (to) params.to = to;
    if (tagId) params.tagId = tagId;
    const res = await tasks.getAll({ params });
    let list = res.data || [];
    if (query.trim()) {
      const q = query.trim().toLowerCase();
      list = list.filter(t =>
        (t.title || '').toLowerCase().includes(q) ||
        (t.description || '').toLowerCase().includes(q)
      );
    }
    setTaskList(list);
    setLoading(false);
  };

  useEffect(() => {
    loadTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleApply = async () => {
    await loadTasks();
  };

  const handleReset = async () => {
    setQuery('');
    setFrom('');
    setTo('');
    setTagId('');
    await loadTasks();
  };

  const handleDelete = async (id) => {
    await tasks.delete(id);
    await loadTasks();
  };

  const priorityBadge = (priority) => {
    switch(priority) {
      case 'HIGH': return 'bg-rose-100 text-rose-800 ring-1 ring-rose-200';
      case 'MEDIUM': return 'bg-amber-100 text-amber-800 ring-1 ring-amber-200';
      default: return 'bg-emerald-100 text-emerald-800 ring-1 ring-emerald-200';
    }
  };

  const statusBadge = (status) => {
    switch((status || '').toUpperCase()) {
      case 'IN_PROGRESS': return 'bg-indigo-100 text-indigo-800 ring-1 ring-indigo-200';
      case 'DONE': return 'bg-emerald-100 text-emerald-800 ring-1 ring-emerald-200';
      case 'BLOCKED': return 'bg-slate-200 text-slate-800 ring-1 ring-slate-300';
      case 'OVERDUE': return 'bg-red-100 text-red-800 ring-1 ring-red-200';
      case 'CANCELED': return 'bg-gray-200 text-gray-800 ring-1 ring-gray-300';
      default: return 'bg-blue-100 text-blue-800 ring-1 ring-blue-200';
    }
  };

  return (
    <div className="p-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between mb-4">
        <div className="flex-1 grid grid-cols-1 md:grid-cols-4 gap-3">
          <input
            type="text"
            placeholder="Іздеу..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="datetime-local"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="datetime-local"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
          />
          <input
            type="number"
            min="1"
            placeholder="Tag ID"
            value={tagId}
            onChange={(e) => setTagId(e.target.value)}
            className="px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div className="flex gap-2">
          <button onClick={handleApply} className="px-4 py-2.5 rounded-lg text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700">
            Қолдану
          </button>
          <button onClick={handleReset} className="px-4 py-2.5 rounded-lg border border-gray-300 hover:bg-gray-50">
            Тазалау
          </button>
        </div>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-16 rounded-xl bg-gray-100 animate-pulse" />
          ))}
        </div>
      ) : taskList.length === 0 ? (
        <div className="text-center py-10 text-gray-500">Тапсырмалар табылмады</div>
      ) : (
        <ul className="divide-y">
          {taskList.map(task => (
            <li key={task.id} className="group p-4 sm:p-5 flex items-center justify-between gap-4 hover:bg-gray-50 transition">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-3">
                  <p className="font-semibold text-gray-900 truncate">{task.title}</p>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${priorityBadge(task.priority)}`}>
                    {task.priority === 'HIGH' ? 'Жоғары' : task.priority === 'MEDIUM' ? 'Орташа' : 'Төмен'}
                  </span>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${statusBadge(task.status)}`}>
                    {task.status || 'PLANNED'}
                  </span>
                </div>
                {task.description ? (
                  <p className="text-sm text-gray-600 mt-1 line-clamp-2">{task.description}</p>
                ) : null}
                {task.deadline ? (
                  <p className="text-xs text-gray-500 mt-1">Дедлайн: {new Date(task.deadline).toLocaleString()}</p>
                ) : null}
              </div>
              <button
                onClick={() => handleDelete(task.id)}
                className="shrink-0 inline-flex items-center justify-center w-9 h-9 rounded-lg text-red-600 hover:bg-red-50 transition"
                aria-label="Delete"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default TaskList;