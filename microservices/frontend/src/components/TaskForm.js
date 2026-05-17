import React, { useState } from 'react';
import { tasks } from '../services/api';
import { useToast } from './Toast';
import { PrimaryButton, SectionTitle, Surface } from './UiKit';

function TaskForm({
  onTaskCreated,
  defaultDate = '',
  title = 'Жаңа тапсырма',
  description = 'Аяқталу уақыты анық тапсырмаларды бөлек жоспарлаңыз.',
  submitLabel = 'Тапсырманы сақтау',
}) {
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
    if (!form.deadline) {
      toast.error('Орындау мерзімін көрсетіңіз');
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
      <SectionTitle title={title} description={description} />

      <form onSubmit={handleSubmit} className="grid gap-4 p-5 md:grid-cols-6">
        <div className="md:col-span-3">
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Атауы</label>
          <input
            name="title"
            value={form.title}
            onChange={onChange}
            placeholder="Мысалы: клиентке есеп жіберу"
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
            <option value="DONE">Дайын</option>
            <option value="CANCELED">Тоқтатылды</option>
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Орындау уақыты</label>
          <input type="datetime-local" name="deadline" value={form.deadline} onChange={onChange} className="app-input" />
        </div>

        <div className="md:col-span-6">
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Сипаттама</label>
          <textarea
            name="description"
            value={form.description}
            onChange={onChange}
            placeholder="Не істеу керек және нәтижесі қандай болуы тиіс"
            rows={5}
            className="app-input min-h-[140px] resize-none"
          />
        </div>

        <div className="md:col-span-6 flex justify-end">
          <PrimaryButton type="submit" disabled={saving}>
            {saving ? 'Сақталып жатыр...' : submitLabel}
          </PrimaryButton>
        </div>
      </form>
    </Surface>
  );
}

export default TaskForm;
