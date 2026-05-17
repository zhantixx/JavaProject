import React, { useEffect, useState } from 'react';
import { useToast } from '../components/Toast';
import { profile } from '../services/api';
import { PageIntro, PrimaryButton, Surface } from '../components/UiKit';

export default function Profile() {
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ name: '', email: '' });

  useEffect(() => {
    (async () => {
      try {
        setLoading(true);
        const r = await profile.me();
        if (r.status >= 200 && r.status < 300) {
          const me = r.data || {};
          setForm({ name: me.name || '', email: me.email || '' });
        } else {
          toast.error('Профильді жүктеу мүмкін болмады');
        }
      } catch {
        toast.error('Профильді жүктеу қатесі');
      } finally {
        setLoading(false);
      }
    })();
  }, [toast]);

  const onSave = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const r = await profile.updateMe({ name: form.name });
      if (r.status >= 200 && r.status < 300) {
        toast.success('Профиль жаңартылды');
      } else {
        toast.error(`Жаңарту мүмкін болмады: ${r.status}`);
      }
    } catch {
      toast.error('Профильді жаңарту қатесі');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Жеке баптау"
        title="Профиль"
        description="Негізгі деректерді жаңартып, аккаунт күйін осы жерден бақылаңыз."
        stats={[
          { label: 'Email', value: form.email || '—', hint: 'Негізгі кіру адресі' },
          { label: 'Аты', value: form.name || 'Көрсетілмеген', hint: 'Профиль атауы' },
          { label: 'Күйі', value: loading ? 'Жүктелуде' : 'Белсенді', hint: 'Аккаунт мәртебесі' },
          { label: 'Тақырып', value: 'Жарық/Қараңғы', hint: 'Жоғарғы панельден ауысады' },
        ]}
      />

      <Surface className="overflow-hidden">
        <div className="border-b border-[var(--border-color)] px-6 py-5">
          <div className="flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-3xl bg-[linear-gradient(135deg,#2563eb,#60a5fa)] text-2xl font-semibold text-white">
              {(form.name || form.email || 'U').substring(0, 1).toUpperCase()}
            </div>
            <div>
              <h2 className="text-xl font-semibold text-[var(--text-primary)]">Пайдаланушы профилі</h2>
              <p className="mt-1 text-sm text-[var(--text-secondary)]">Негізгі ақпаратты бір қалыпта ұстаңыз.</p>
            </div>
          </div>
        </div>

        <form onSubmit={onSave} className="grid gap-6 p-6 md:grid-cols-2">
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Email</label>
            <input type="email" disabled value={form.email} className="app-input opacity-80" />
          </div>
          <div className="md:col-span-2">
            <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Аты</label>
            <input
              type="text"
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              className="app-input"
              placeholder="Сіздің атыңыз"
            />
          </div>
          <div className="md:col-span-2 flex justify-end">
            <PrimaryButton type="submit" disabled={loading}>
              {loading ? 'Сақталып жатыр...' : 'Сақтау'}
            </PrimaryButton>
          </div>
        </form>
      </Surface>
    </div>
  );
}
