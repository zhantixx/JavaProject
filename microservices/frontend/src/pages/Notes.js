import React, { useEffect, useMemo, useState } from 'react';
import { notes, profile } from '../services/api';
import { useToast } from '../components/Toast';

export default function NotesPage() {
  const toast = useToast();
  const [userId, setUserId] = useState(localStorage.getItem('userId'));
  const [items, setItems] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState({ title: '', content: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const selected = useMemo(
    () => items.find((note) => note.id === selectedId) || null,
    [items, selectedId]
  );

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
      toast.error(err.response?.data || 'Заметки жүктелмеді');
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
    return (note.title || '').toLowerCase().includes(q) ||
      (note.content || '').toLowerCase().includes(q);
  });

  const createNote = () => {
    setSelectedId(null);
    setForm({ title: '', content: '' });
  };

  const saveNote = async (e) => {
    e.preventDefault();
    if (!form.content.trim()) {
      toast.error('Заметка мәтінін жазыңыз');
      return;
    }
    setSaving(true);
    try {
      const id = await loadUser();
      const payload = {
        title: form.title.trim() || 'Жаңа заметка',
        content: form.content.trim(),
      };
      if (selectedId) {
        const res = await notes.update(id, selectedId, payload);
        setItems((prev) => prev.map((note) => note.id === selectedId ? res.data : note));
        toast.success('Заметка сақталды');
      } else {
        const res = await notes.create(id, payload);
        setItems((prev) => [res.data, ...prev]);
        setSelectedId(res.data.id);
        toast.success('Заметка қосылды');
      }
    } catch (err) {
      toast.error(err.response?.data || 'Заметка сақталмады');
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
      toast.success('Заметка өшірілді');
    } catch (err) {
      toast.error(err.response?.data || 'Өшіру қатесі');
    }
  };

  return (
    <div className="grid grid-cols-1 gap-5 lg:grid-cols-[320px_1fr]">
      <aside className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 p-4">
          <div className="flex items-center justify-between">
            <h1 className="text-lg font-semibold text-slate-950">Заметки</h1>
            <button
              onClick={createNote}
              className="rounded-md bg-slate-900 px-3 py-2 text-sm font-medium text-white hover:bg-slate-800"
            >
              Жаңа
            </button>
          </div>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Іздеу"
            className="mt-3 w-full rounded-md border border-slate-300 px-3 py-2 outline-none focus:border-slate-900"
          />
        </div>

        <div className="max-h-[620px] overflow-auto">
          {loading ? (
            <div className="p-4 text-sm text-slate-500">Жүктеліп жатыр...</div>
          ) : filtered.length === 0 ? (
            <div className="p-4 text-sm text-slate-500">Заметка жоқ</div>
          ) : (
            filtered.map((note) => (
              <button
                key={note.id}
                onClick={() => setSelectedId(note.id)}
                className={`block w-full border-b border-slate-100 p-4 text-left hover:bg-slate-50 ${
                  selectedId === note.id ? 'bg-slate-100' : 'bg-white'
                }`}
              >
                <div className="truncate font-medium text-slate-950">{note.title || 'Заметка'}</div>
                <div className="mt-1 line-clamp-2 text-sm text-slate-500">{note.content}</div>
              </button>
            ))
          )}
        </div>
      </aside>

      <section className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <form onSubmit={saveNote} className="flex h-full min-h-[560px] flex-col">
          <input
            value={form.title}
            onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
            placeholder="Тақырып"
            className="rounded-md border border-slate-300 px-3 py-2 text-lg font-semibold outline-none focus:border-slate-900"
          />
          <textarea
            value={form.content}
            onChange={(e) => setForm((prev) => ({ ...prev, content: e.target.value }))}
            placeholder="Заметка мәтіні..."
            className="mt-3 min-h-[420px] flex-1 resize-none rounded-md border border-slate-300 px-3 py-3 outline-none focus:border-slate-900"
          />
          <div className="mt-4 flex justify-between gap-2">
            <button
              type="button"
              onClick={deleteNote}
              disabled={!selectedId}
              className="rounded-md border border-rose-200 px-4 py-2 text-sm font-medium text-rose-700 hover:bg-rose-50 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Өшіру
            </button>
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800 disabled:opacity-60"
            >
              {saving ? 'Сақталып жатыр...' : 'Сақтау'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
