import React, { useEffect, useState } from 'react';
import { notifications, profile } from '../services/api';
import { PageIntro, GhostButton, Surface, SectionTitle } from '../components/UiKit';
import { useToast } from '../components/Toast';
import { formatLongDateTime } from '../utils/date';

function typeLabel(type) {
  return type === 'PUSH' ? 'Push' : 'Email';
}

function statusLabel(status) {
  if (status === 'SENT') return 'Жіберілді';
  if (status === 'FAILED') return 'Қате';
  return 'Кезекте';
}

export default function NotificationsPage() {
  const toast = useToast();
  const [loading, setLoading] = useState(true);
  const [userId, setUserId] = useState(null);
  const [items, setItems] = useState([]);
  const [summary, setSummary] = useState({ total: 0, unread: 0 });

  const loadData = async () => {
    try {
      setLoading(true);
      const meResponse = await profile.me();
      const me = meResponse.data || {};
      if (!me.id) {
        throw new Error('Missing user id');
      }
      setUserId(me.id);

      const [listResponse, summaryResponse] = await Promise.all([
        notifications.list(me.id),
        notifications.summary(me.id),
      ]);

      setItems(listResponse.data || []);
      setSummary(summaryResponse.data || { total: 0, unread: 0 });
    } catch (error) {
      toast.error(error.response?.data || 'Хабарламаларды жүктеу мүмкін болмады');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleMarkAsRead = async (notificationId) => {
    if (!userId) return;
    try {
      const response = await notifications.markAsRead(userId, notificationId);
      const updated = response.data;
      setItems((prev) => prev.map((item) => (item.id === notificationId ? updated : item)));
      setSummary((prev) => ({
        ...prev,
        unread: Math.max(0, prev.unread - 1),
      }));
    } catch (error) {
      toast.error(error.response?.data || 'Хабарламаны жаңарту мүмкін болмады');
    }
  };

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Еске салғыштар"
        title="Хабарламалар"
        description="Мерзімі жақындаған және кешіккен тапсырмаларға қатысты ескертулер осы жерде сақталады."
        stats={[
          { label: 'Барлығы', value: summary.total, hint: 'Жиналған хабарлама саны' },
          { label: 'Оқылмаған', value: summary.unread, hint: 'Әлі ашылмаған ескертулер' },
        ]}
      />

      <Surface className="overflow-hidden">
        <SectionTitle
          title="Соңғы ескертулер"
          description={loading ? 'Жүктеліп жатыр...' : `${items.length} хабарлама көрініп тұр`}
        />

        {loading ? (
          <div className="p-5 text-sm text-[var(--text-secondary)]">Хабарламалар жүктеліп жатыр...</div>
        ) : items.length === 0 ? (
          <div className="p-10 text-center text-[var(--text-secondary)]">Қазір ескертулер жоқ</div>
        ) : (
          <div className="grid gap-4 p-5">
            {items.map((item) => (
              <article
                key={item.id}
                className={`rounded-2xl border p-4 ${
                  item.readAt
                    ? 'border-[var(--border-color)] bg-[var(--card-strong)]'
                    : 'border-[var(--brand-border)] bg-[var(--brand-soft)]'
                }`}
              >
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="text-base font-semibold text-[var(--text-primary)]">{item.title}</h3>
                      <span className="app-badge">{typeLabel(item.type)}</span>
                      <span className="rounded-full border border-[var(--border-color)] px-2.5 py-1 text-xs text-[var(--text-secondary)]">
                        {statusLabel(item.status)}
                      </span>
                    </div>
                    <p className="mt-2 whitespace-pre-wrap break-words text-sm leading-6 text-[var(--text-secondary)]">
                      {item.content}
                    </p>
                    <div className="mt-3 text-xs text-[var(--muted-text)]">
                      Құрылған уақыты: {item.createdAt ? formatLongDateTime(item.createdAt) : 'Белгісіз'}
                    </div>
                  </div>

                  {!item.readAt ? (
                    <GhostButton onClick={() => handleMarkAsRead(item.id)}>Оқылды деп белгілеу</GhostButton>
                  ) : (
                    <div className="text-xs text-[var(--muted-text)]">
                      Оқылды: {formatLongDateTime(item.readAt)}
                    </div>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
      </Surface>
    </div>
  );
}
