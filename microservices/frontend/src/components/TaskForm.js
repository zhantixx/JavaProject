import React, { useState } from 'react';
import { tasks } from '../services/api';
import { useToast } from './Toast';
import { PrimaryButton, SectionTitle, Surface } from './UiKit';

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
    <Surface className="overflow-hidden">
      <SectionTitle
        title="Жаңа тапсырма"
        description="Атауы, күйі және мерзімі бар ықшам жазба жасаңыз."
      />

      <form onSubmit={handleSubmit} className="grid gap-4 p-5 md:grid-cols-6">
        <div className="md:col-span-3">
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Атауы</label>
          <input
            name="title"
            value={form.title}
            onChange={onChange}
            placeholder="Мысалы: апталық есепті аяқтау"
            className="app-input"
            required
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Басымдық</label>
          <select name="priority" value={form.priority} onChange={onChange} className="app-input">
            <option value="HIGH">Жоғары</option>
            <option value="MEDIUM">Орташа</option>
            <option value="LOW">Төмен</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Күйі</label>
          <select name="status" value={form.status} onChange={onChange} className="app-input">
            <option value="PLANNED">Жоспар</option>
            <option value="IN_PROGRESS">Орындалуда</option>
            <option value="DONE">Дайын</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Мерзімі</label>
          <input type="datetime-local" name="deadline" value={form.deadline} onChange={onChange} className="app-input" />
        </div>

        <div className="md:col-span-5">
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Сипаттама</label>
          <textarea
            name="description"
            value={form.description}
            onChange={onChange}
            placeholder="Тапсырманың қысқа мазмұны"
            rows={4}
            className="app-input min-h-[120px] resize-none"
          />
        </div>

        <div className="flex items-end justify-end md:col-span-1">
          <PrimaryButton type="submit" disabled={saving} className="w-full md:w-auto">
            {saving ? 'Сақталып жатыр...' : 'Қосу'}
          </PrimaryButton>
        </div>
      </form>
    </Surface>
  );
}

export default TaskForm;
