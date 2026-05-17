import React, { useState } from 'react';
import { tasks } from '../services/api';
import { useToast } from './Toast';

function TaskForm({ onTaskCreated, defaultDate = '' }) {
  const toast = useToast();
  const [form, setForm] = useState({
    title: '',
    priority: 'MEDIUM',
    status: 'PLANNED',
    description: '',
    deadline: defaultDate,
  });
  const [saving, setSaving] = useState(false);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) {
      toast.error('Тапсырма атауын жазыңыз');
      return;
    }
    setSaving(true);
    try {
      const response = await tasks.create({
        title: form.title.trim(),
        priority: form.priority,
        status: form.status,
        description: form.description.trim(),
        deadline: form.deadline || null,
        tagIds: null,
      });
      setForm({ title: '', priority: 'MEDIUM', status: 'PLANNED', description: '', deadline: defaultDate });
      toast.success('Тапсырма қосылды');
      onTaskCreated?.(response.data);
    } catch (err) {
      toast.error(err.response?.data || 'Тапсырма қосылмады');
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div className="mb-4">
        <h2 className="text-lg font-semibold text-slate-950">Жаңа тапсырма</h2>
        <p className="text-sm text-slate-500">Қарапайым түрде атау, уақыт және приоритет таңдаңыз.</p>
      </div>

      <div className="grid grid-cols-1 gap-3 md:grid-cols-6">
        <input
          name="title"
          value={form.title}
          onChange={onChange}
          placeholder="Тапсырма атауы"
          className="md:col-span-3 rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
          required
        />
        <select
          name="priority"
          value={form.priority}
          onChange={onChange}
          className="rounded-md border border-slate-300 bg-white px-3 py-2 outline-none focus:border-slate-900"
        >
          <option value="HIGH">Жоғары</option>
          <option value="MEDIUM">Орташа</option>
          <option value="LOW">Төмен</option>
        </select>
        <select
          name="status"
          value={form.status}
          onChange={onChange}
          className="rounded-md border border-slate-300 bg-white px-3 py-2 outline-none focus:border-slate-900"
        >
          <option value="PLANNED">Жоспар</option>
          <option value="IN_PROGRESS">Орындалуда</option>
          <option value="DONE">Дайын</option>
        </select>
        <input
          type="datetime-local"
          name="deadline"
          value={form.deadline}
          onChange={onChange}
          className="rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
        />
        <textarea
          name="description"
          value={form.description}
          onChange={onChange}
          placeholder="Сипаттама"
          rows={3}
          className="md:col-span-6 rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
        />
      </div>

      <div className="mt-4 flex justify-end">
        <button
          type="submit"
          disabled={saving}
          className="rounded-md bg-slate-900 px-4 py-2 font-medium text-white hover:bg-slate-800 disabled:opacity-60"
        >
          {saving ? 'Сақталып жатыр...' : 'Қосу'}
        </button>
      </div>
    </form>
  );
}

export default TaskForm;
