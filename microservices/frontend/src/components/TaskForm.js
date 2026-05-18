import React, { useMemo, useState } from 'react';
import { chat, tasks } from '../services/api';
import { useToast } from './Toast';
import { GhostButton, PrimaryButton, SectionTitle, Surface } from './UiKit';

function toLocalDateTimeValue(value) {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const adjusted = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return adjusted.toISOString().slice(0, 16);
}

function getMinDateTimeValue() {
  const now = new Date();
  now.setSeconds(0, 0);
  return toLocalDateTimeValue(now);
}

function isPastDateTime(value) {
  if (!value) {
    return false;
  }
  return new Date(value).getTime() < Date.now();
}

function TaskForm({
  onTaskCreated,
  defaultDate = '',
  title = 'Жаңа тапсырма',
  description = 'Нақты мерзімі бар тапсырмаларды жоспарлап, қадамдарға бөліңіз.',
  submitLabel = 'Тапсырманы сақтау',
}) {
  const toast = useToast();
  const [form, setForm] = useState({
    title: '',
    priority: 'MEDIUM',
    status: 'PLANNED',
    description: '',
    deadline: toLocalDateTimeValue(defaultDate),
  });
  const [saving, setSaving] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiSuggestion, setAiSuggestion] = useState('');
  const [aiInserted, setAiInserted] = useState(false);
  const minDeadline = useMemo(() => getMinDateTimeValue(), []);

  const resetForm = () => {
    setForm({
      title: '',
      priority: 'MEDIUM',
      status: 'PLANNED',
      description: '',
      deadline: toLocalDateTimeValue(defaultDate),
    });
    setAiSuggestion('');
    setAiInserted(false);
  };

  const onChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.title.trim()) {
      toast.error('Тапсырма атауын жазыңыз');
      return;
    }

    if (!form.deadline) {
      toast.error('Соңғы мерзімді көрсетіңіз');
      return;
    }

    if (isPastDateTime(form.deadline)) {
      toast.error('Өткен күнді таңдауға болмайды');
      return;
    }

    setSaving(true);
    try {
      const response = await tasks.create({
        title: form.title.trim(),
        priority: form.priority,
        status: form.status,
        description: form.description.trim(),
        deadline: form.deadline,
        tagIds: null,
      });

      resetForm();
      toast.success('Тапсырма қосылды');
      onTaskCreated?.(response.data);
    } catch (error) {
      toast.error(error.response?.data || 'Тапсырма қосылмады');
    } finally {
      setSaving(false);
    }
  };

  const requestAiPlan = async () => {
    if (!form.title.trim()) {
      toast.error('Алдымен тапсырма атауын жазыңыз');
      return;
    }

    setAiLoading(true);
    setAiInserted(false);

    try {
      const prompt = [
        'Сен жеке ассистентсің.',
        'Пайдаланушының күрделі тапсырмасын шағын жоспарға бөл.',
        'Жауап тек қазақ тілінде болсын.',
        'Құрылым:',
        '1) Қысқа түсінік',
        '2) 3-7 нақты қадам',
        '3) Ұсынылған орындау реті',
        '4) Ықтимал тәуекелдер',
        '5) Қысқа кеңес',
        '',
        `Тапсырма атауы: ${form.title}`,
        `Сипаттама: ${form.description || 'Жоқ'}`,
        `Басымдық: ${form.priority}`,
        `Мерзімі: ${form.deadline || 'Көрсетілмеген'}`,
      ].join('\n');

      const response = await chat.send(prompt);
      setAiSuggestion(response.data.reply || '');
      toast.success('AI жоспар дайындалды');
    } catch {
      toast.error('AI кеңесін алу мүмкін болмады');
    } finally {
      setAiLoading(false);
    }
  };

  const applyAiSuggestion = () => {
    if (!aiSuggestion) {
      return;
    }

    setForm((prev) => ({
      ...prev,
      description: prev.description.trim()
        ? `${prev.description.trim()}\n\nAI жоспары:\n${aiSuggestion}`
        : `AI жоспары:\n${aiSuggestion}`,
    }));
    setAiInserted(true);
    toast.success('AI жоспары сипаттамаға қосылды');
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
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Соңғы мерзім</label>
          <input
            type="datetime-local"
            name="deadline"
            value={form.deadline}
            min={minDeadline}
            onChange={onChange}
            className="app-input"
          />
        </div>

        <div className="md:col-span-6">
          <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Сипаттама</label>
          <textarea
            name="description"
            value={form.description}
            onChange={onChange}
            placeholder="Не істеу керек және нәтиже қандай болуы тиіс"
            rows={5}
            className="app-input min-h-[140px] resize-none"
          />
        </div>

        <div className="md:col-span-6 rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
          <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
            <div className="max-w-2xl">
              <div className="font-medium text-[var(--text-primary)]">AI көмек</div>
              <div className="mt-1 text-sm text-[var(--text-secondary)]">
                Егер тапсырма күрделі болса, ассистент оны қадамдарға бөліп, кеңес береді. Дайын жоспарды бір
                батырмамен сипаттамаға енгізуге болады.
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              <GhostButton type="button" onClick={requestAiPlan} disabled={aiLoading}>
                {aiLoading ? 'Талдап жатыр...' : 'AI жоспар сұрау'}
              </GhostButton>
              <PrimaryButton type="button" onClick={applyAiSuggestion} disabled={!aiSuggestion || aiInserted}>
                {aiInserted ? 'Сипаттамаға қосылды' : 'Сипаттамаға қосу'}
              </PrimaryButton>
            </div>
          </div>

          <div className="mt-4 rounded-2xl border border-[var(--border-color)] bg-[var(--panel-bg)] p-4">
            <div className="flex items-center justify-between gap-3">
              <div className="text-sm font-medium text-[var(--text-primary)]">AI ұсынған жоспар</div>
              {aiSuggestion ? (
                <span className="rounded-full bg-[var(--brand-soft)] px-3 py-1 text-xs font-medium text-[var(--brand-strong)]">
                  Дайын ұсыныс
                </span>
              ) : null}
            </div>
            <div className="mt-3 whitespace-pre-line text-sm leading-7 text-[var(--text-secondary)]">
              {aiSuggestion || 'Мұнда AI ұсынған қадамдық жоспар мен қысқа кеңес көрінеді.'}
            </div>
          </div>
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
