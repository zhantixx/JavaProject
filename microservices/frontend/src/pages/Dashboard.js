import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { notes, profile, tasks } from '../services/api';
import { useToast } from '../components/Toast';
import { GhostButton, PageIntro, PrimaryButton, Surface } from '../components/UiKit';

function formatDeadline(deadline) {
  return new Date(deadline).toLocaleString('kk-KZ', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function DashboardPage() {
  const toast = useToast();
  const [taskItems, setTaskItems] = useState([]);
  const [noteItems, setNoteItems] = useState([]);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const meRes = await profile.me();
        const me = meRes.data;
        setUser(me);
        if (me?.id) {
          localStorage.setItem('userId', me.id);
        }
        const [tasksRes, notesRes] = await Promise.all([
          tasks.getAll(),
          me?.id ? notes.getAll(me.id) : Promise.resolve({ data: [] }),
        ]);
        setTaskItems(tasksRes.data || []);
        setNoteItems(notesRes.data || []);
      } catch (err) {
        toast.error(err.response?.data || 'Басты бет деректері жүктелмеді');
      } finally {
        setLoading(false);
      }
    })();
  }, [toast]);

  const upcomingTasks = useMemo(
    () =>
      [...taskItems]
        .filter((task) => task.deadline && task.status !== 'DONE')
        .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
        .slice(0, 5),
    [taskItems]
  );

  const stats = useMemo(() => {
    const todayCount = taskItems.filter((task) => {
      if (!task.deadline) return false;
      return new Date(task.deadline).toDateString() === new Date().toDateString();
    }).length;
    const doneCount = taskItems.filter((task) => task.status === 'DONE').length;
    return [
      { label: 'Тапсырма', value: taskItems.length, hint: 'Жалпы жұмыс тізімі' },
      { label: 'Бүгін', value: todayCount, hint: 'Бүгінгі мерзімдер' },
      { label: 'Жазба', value: noteItems.length, hint: 'Сақталған жазбалар' },
      { label: 'Дайын', value: doneCount, hint: 'Аяқталған істер' },
    ];
  }, [noteItems.length, taskItems]);

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Басты бет"
        title={user?.name ? `${user.name}, бүгінгі фокус дайын` : 'Бүгінгі фокус дайын'}
        description="Бұл бет сізге алдағы мерзімдерді, негізгі әрекеттерді және қысқа шолуды береді. Тапсырма құру бөлек экранда орналасқан."
        stats={stats}
        actions={
          <>
            <Link to="/tasks/new">
              <PrimaryButton>Жаңа тапсырма</PrimaryButton>
            </Link>
            <Link to="/calendar">
              <GhostButton>Күнтізбені ашу</GhostButton>
            </Link>
          </>
        }
      />

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <Surface className="overflow-hidden">
          <div className="border-b border-[var(--border-color)] px-5 py-5">
            <h2 className="text-lg font-semibold text-[var(--text-primary)]">Жақын мерзімдер</h2>
            <p className="mt-1 text-sm text-[var(--text-secondary)]">Алдымен бітетін тапсырмалар осында жиналды.</p>
          </div>
          <div className="space-y-3 p-5">
            {loading ? (
              <div className="text-sm text-[var(--text-secondary)]">Жүктеліп жатыр...</div>
            ) : upcomingTasks.length === 0 ? (
              <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4 text-sm text-[var(--text-secondary)]">
                Алдағы мерзімі бар тапсырмалар жоқ.
              </div>
            ) : (
              upcomingTasks.map((task) => (
                <div key={task.id} className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <div className="font-medium text-[var(--text-primary)]">{task.title}</div>
                      <div className="mt-1 text-sm text-[var(--text-secondary)]">
                        {task.description || 'Қысқа сипаттама қосылмаған'}
                      </div>
                    </div>
                    <span className="app-badge">{task.priority || 'MEDIUM'}</span>
                  </div>
                  <div className="mt-3 text-sm text-[var(--text-secondary)]">
                    Аяқтау уақыты: <span className="font-medium text-[var(--text-primary)]">{formatDeadline(task.deadline)}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </Surface>

        <div className="space-y-6">
          <Surface className="overflow-hidden">
            <div className="border-b border-[var(--border-color)] px-5 py-5">
              <h2 className="text-lg font-semibold text-[var(--text-primary)]">Жылдам әрекеттер</h2>
            </div>
            <div className="grid gap-3 p-5">
              <Link to="/tasks/new" className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4 transition hover:border-[var(--brand-border)]">
                <div className="font-medium text-[var(--text-primary)]">Тапсырма құру</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">Уақытын көрсетіп, жеке жоспарға қосыңыз.</div>
              </Link>
              <Link to="/notes" className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4 transition hover:border-[var(--brand-border)]">
                <div className="font-medium text-[var(--text-primary)]">Жазбаларды ашу</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">Ойлар мен идеяларды реттеу.</div>
              </Link>
              <Link to="/profile" className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4 transition hover:border-[var(--brand-border)]">
                <div className="font-medium text-[var(--text-primary)]">Профильді баптау</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">Аватар, пароль және деректерді басқару.</div>
              </Link>
            </div>
          </Surface>

          <Surface className="overflow-hidden">
            <div className="border-b border-[var(--border-color)] px-5 py-5">
              <h2 className="text-lg font-semibold text-[var(--text-primary)]">Соңғы жазбалар</h2>
            </div>
            <div className="space-y-3 p-5">
              {noteItems.slice(0, 3).map((note) => (
                <div key={note.id} className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                  <div className="font-medium text-[var(--text-primary)]">{note.title || 'Жазба'}</div>
                  <div className="mt-2 text-sm text-[var(--text-secondary)]">{note.content}</div>
                </div>
              ))}
              {!noteItems.length && !loading ? (
                <div className="text-sm text-[var(--text-secondary)]">Жазба әлі қосылмаған.</div>
              ) : null}
            </div>
          </Surface>
        </div>
      </div>
    </div>
  );
}
