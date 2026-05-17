import React, { useEffect, useMemo, useState } from 'react';
import { notes, profile } from '../services/api';
import { useToast } from '../components/Toast';
import { GhostButton, PageIntro, PrimaryButton, Surface } from '../components/UiKit';

export default function NotesPage() {
  const toast = useToast();
  const [userId, setUserId] = useState(localStorage.getItem('userId'));
  const [items, setItems] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState({ title: '', content: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const selected = useMemo(() => items.find((note) => note.id === selectedId) || null, [items, selectedId]);

  const loadUser = async () => {
    if (userId) return userId;
    const res = await profile.me();
    const id = res.data?.id;
    if (id) {
      localStorage.setItem('userId', id);
      setUserId(id);
    }
    return id;
  };

  const loadNotes = async () => {
    setLoading(true);
    try {
      const id = await loadUser();
      if (!id) throw new Error('User id not found');
      const res = await notes.getAll(id);
      const list = res.data || [];
      setItems(list);
      if (list.length && !selectedId) {
        setSelectedId(list[0].id);
        setForm({ title: list[0].title || '', content: list[0].content || '' });
      }
    } catch (err) {
      toast.error(err.response?.data || 'Жазбалар жүктелмеді');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (selected) {
      setForm({ title: selected.title || '', content: selected.content || '' });
    }
  }, [selected]);

  const filtered = items.filter((note) => {
    const q = query.trim().toLowerCase();
    if (!q) return true;
    return (note.title || '').toLowerCase().includes(q) || (note.content || '').toLowerCase().includes(q);
  });

  const createNote = () => {
    setSelectedId(null);
    setForm({ title: '', content: '' });
  };

  const saveNote = async (e) => {
    e.preventDefault();
    if (!form.content.trim()) {
      toast.error('Жазба мәтінін енгізіңіз');
      return;
    }
    setSaving(true);
    try {
      const id = await loadUser();
      const payload = {
        title: form.title.trim() || 'Жаңа жазба',
        content: form.content.trim(),
      };
      if (selectedId) {
        const res = await notes.update(id, selectedId, payload);
        setItems((prev) => prev.map((note) => (note.id === selectedId ? res.data : note)));
        toast.success('Жазба сақталды');
      } else {
        const res = await notes.create(id, payload);
        setItems((prev) => [res.data, ...prev]);
        setSelectedId(res.data.id);
        toast.success('Жазба қосылды');
      }
    } catch (err) {
      toast.error(err.response?.data || 'Жазба сақталмады');
    } finally {
      setSaving(false);
    }
  };

  const deleteNote = async () => {
    if (!selectedId) return;
    try {
      const id = await loadUser();
      await notes.delete(id, selectedId);
      const rest = items.filter((note) => note.id !== selectedId);
      setItems(rest);
      setSelectedId(rest[0]?.id || null);
      setForm(rest[0] ? { title: rest[0].title || '', content: rest[0].content || '' } : { title: '', content: '' });
      toast.success('Жазба өшірілді');
    } catch (err) {
      toast.error(err.response?.data || 'Өшіру қатесі');
    }
  };

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Жеке архив"
        title="Жазбалар"
        description="Ой, жоспар және қысқа белгілерді тез сақтап, кейін оңай табыңыз."
        stats={[
          { label: 'Барлығы', value: items.length, hint: 'Жалпы жазба саны' },
          { label: 'Көрсетіліп тұр', value: filtered.length, hint: 'Іздеу нәтижесі' },
          { label: 'Таңдалған', value: selected ? '1' : '0', hint: 'Ашық жазба' },
          { label: 'Пішім', value: 'Қарапайым', hint: 'Таза мәтін көрінісі' },
        ]}
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[340px_1fr]">
        <Surface className="overflow-hidden">
          <div className="border-b border-[var(--border-color)] px-5 py-5">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-lg font-semibold text-[var(--text-primary)]">Тізім</h2>
              <PrimaryButton onClick={createNote}>Жаңа</PrimaryButton>
            </div>
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Жазба іздеу"
              className="app-input mt-4"
            />
          </div>

          <div className="max-h-[640px] overflow-auto">
            {loading ? (
              <div className="p-5 text-sm text-[var(--text-secondary)]">Жүктеліп жатыр...</div>
            ) : filtered.length === 0 ? (
              <div className="p-5 text-sm text-[var(--text-secondary)]">Жазба табылмады</div>
            ) : (
              filtered.map((note) => (
                <button
                  key={note.id}
                  onClick={() => setSelectedId(note.id)}
                  className={`block w-full border-b border-[var(--border-color)] px-5 py-4 text-left transition ${
                    selectedId === note.id ? 'bg-[var(--brand-soft)]' : 'bg-transparent hover:bg-[var(--card-soft)]'
                  }`}
                >
                  <div className="truncate font-medium text-[var(--text-primary)]">{note.title || 'Жазба'}</div>
                  <div className="mt-2 max-h-[72px] overflow-hidden text-sm leading-6 text-[var(--text-secondary)]">{note.content}</div>
                </button>
              ))
            )}
          </div>
        </Surface>

        <Surface className="overflow-hidden">
          <form onSubmit={saveNote} className="flex min-h-[640px] flex-col">
            <div className="border-b border-[var(--border-color)] px-5 py-5">
              <input
                value={form.title}
                onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                placeholder="Жазба тақырыбы"
                className="app-input text-lg font-semibold"
              />
            </div>

            <div className="flex-1 p-5">
              <textarea
                value={form.content}
                onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
                placeholder="Ойыңызды осында жазыңыз..."
                className="app-input min-h-[420px] h-full resize-none leading-7"
              />
            </div>

            <div className="flex justify-between gap-3 border-t border-[var(--border-color)] px-5 py-5">
              <GhostButton type="button" onClick={deleteNote} disabled={!selectedId}>
                Өшіру
              </GhostButton>
              <PrimaryButton type="submit" disabled={saving}>
                {saving ? 'Сақталып жатыр...' : 'Сақтау'}
              </PrimaryButton>
            </div>
          </form>
        </Surface>
      </div>
    </div>
  );
}
