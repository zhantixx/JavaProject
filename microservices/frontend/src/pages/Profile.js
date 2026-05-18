import React, { useEffect, useMemo, useRef, useState } from 'react';
import { clearAuthSession, profile } from '../services/api';
import { useToast } from '../components/Toast';
import { GhostButton, PageIntro, PrimaryButton, Surface } from '../components/UiKit';
import { formatLongDate } from '../utils/date';

function formatCreatedAt(createdAt) {
  if (!createdAt) return 'Көрсетілмеген';
  return formatLongDate(createdAt);
}

async function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

function InfoCard({ label, value, hint }) {
  return (
    <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
      <div className="text-xs font-medium uppercase tracking-[0.12em] text-[var(--muted-text)]">{label}</div>
      <div className="mt-2 break-words text-sm font-medium leading-6 text-[var(--text-primary)]">{value || '—'}</div>
      {hint ? <div className="mt-1 text-xs text-[var(--text-secondary)]">{hint}</div> : null}
    </div>
  );
}

function ToggleRow({ label, hint, checked, onChange }) {
  return (
    <label className="flex items-start justify-between gap-4 rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
      <div className="min-w-0">
        <div className="text-sm font-medium text-[var(--text-primary)]">{label}</div>
        {hint ? <div className="mt-1 text-sm leading-6 text-[var(--text-secondary)]">{hint}</div> : null}
      </div>
      <input
        type="checkbox"
        checked={Boolean(checked)}
        onChange={(event) => onChange(event.target.checked)}
        className="mt-1 h-5 w-5 rounded border-[var(--border-color)] text-[var(--brand-strong)]"
      />
    </label>
  );
}

export default function Profile() {
  const toast = useToast();
  const avatarInputRef = useRef(null);
  const [loading, setLoading] = useState(false);
  const [me, setMe] = useState(null);
  const [form, setForm] = useState({
    name: '',
    email: '',
    avatarUrl: '',
    phone: '',
    emailNotificationsEnabled: true,
    pushNotificationsEnabled: true,
    reminderWindowMinutes: 60,
  });
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [deleting, setDeleting] = useState(false);
  const [exporting, setExporting] = useState(false);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const response = await profile.me();
      const next = response.data || {};
      setMe(next);
      setForm({
        name: next.name || '',
        email: next.email || '',
        avatarUrl: next.avatarUrl || '',
        phone: next.phone || '',
        emailNotificationsEnabled: next.emailNotificationsEnabled !== false,
        pushNotificationsEnabled: next.pushNotificationsEnabled !== false,
        reminderWindowMinutes: next.reminderWindowMinutes || 60,
      });
    } catch {
      toast.error('Профильді жүктеу қатесі');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const initials = useMemo(() => {
    const base = form.name || form.email || 'S';
    return base.substring(0, 1).toUpperCase();
  }, [form.email, form.name]);

  const accountType = me?.passwordChangeAvailable ? 'Жергілікті аккаунт' : 'OAuth аккаунт';
  const securityMode = me?.passwordChangeAvailable ? 'Парольмен қорғалған' : 'Сыртқы провайдер арқылы';
  const displayName = form.name?.trim() || 'Аты көрсетілмеген';

  const onSaveProfile = async (event) => {
    event.preventDefault();

    try {
      setLoading(true);
      const response = await profile.updateMe({
        name: form.name,
        avatarUrl: form.avatarUrl,
        phone: form.phone,
        emailNotificationsEnabled: form.emailNotificationsEnabled,
        pushNotificationsEnabled: form.pushNotificationsEnabled,
        reminderWindowMinutes: Number(form.reminderWindowMinutes) || 60,
      });

      setMe(response.data);
      toast.success('Профиль жаңартылды');
    } catch {
      toast.error('Профильді жаңарту мүмкін болмады');
    } finally {
      setLoading(false);
    }
  };

  const onAvatarChange = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!['image/png', 'image/jpeg'].includes(file.type)) {
      toast.error('Аватар тек JPG немесе PNG болуы керек');
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      toast.error('Аватар өлшемі 2 MB-тан аспауы керек');
      return;
    }

    try {
      const dataUrl = await fileToDataUrl(file);
      setForm((prev) => ({ ...prev, avatarUrl: dataUrl }));
      toast.success('Аватар сақтауға дайын');
    } catch {
      toast.error('Файлды оқу мүмкін болмады');
    }
  };

  const onRemoveAvatar = () => {
    setForm((prev) => ({ ...prev, avatarUrl: '' }));
    if (avatarInputRef.current) {
      avatarInputRef.current.value = '';
    }
  };

  const onChangePassword = async (event) => {
    event.preventDefault();

    if (!me?.passwordChangeAvailable) {
      toast.info('Бұл аккаунт үшін пароль ауыстыру қолжетімсіз');
      return;
    }

    if (passwordForm.newPassword.length < 8) {
      toast.error('Жаңа пароль кемінде 8 таңба болуы керек');
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.error('Жаңа парольдер сәйкес келмейді');
      return;
    }

    try {
      setLoading(true);
      await profile.changePassword({
        currentPassword: passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      toast.success('Пароль жаңартылды');
    } catch {
      toast.error('Парольді өзгерту мүмкін болмады');
    } finally {
      setLoading(false);
    }
  };

  const onExportData = async () => {
    try {
      setExporting(true);
      const response = await profile.exportData();
      const blob = new Blob([response.data], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'user_data.json';
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success('Деректер экспорты дайын');
    } catch {
      toast.error('Экспорт жасау мүмкін болмады');
    } finally {
      setExporting(false);
    }
  };

  const onDeleteAccount = async () => {
    const confirmed = window.confirm('Аккаунтты жою сұранысын жібересіз бе? Деректер 30 күннен кейін толық өшіріледі.');
    if (!confirmed) return;

    try {
      setDeleting(true);
      await profile.deleteAccount();
      clearAuthSession();
      toast.success('Аккаунт жою кезегіне қойылды');
      window.location.href = '/login';
    } catch {
      toast.error('Аккаунтты жою мүмкін болмады');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Жеке баптау"
        title="Профиль және қауіпсіздік"
        description="Негізгі деректерді жаңартып, аватарды реттеп, пароль, хабарлама арналары және GDPR әрекеттерін осы жерден басқарасыз."
        stats={[
          { label: 'Email', value: form.email || '—', hint: 'Негізгі кіру адресі' },
          { label: 'Рөл', value: me?.role || 'USER', hint: 'Аккаунт мәртебесі' },
          { label: 'Тіркелген', value: formatCreatedAt(me?.createdAt), hint: 'Аккаунт жасалған күн' },
          { label: 'Қауіпсіздік', value: me?.passwordChangeAvailable ? 'Жергілікті' : 'OAuth', hint: 'Кіру тәсілі' },
        ]}
      />

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[0.95fr_1.05fr]">
        <Surface className="overflow-hidden">
          <div className="border-b border-[var(--border-color)] px-6 py-5">
            <h2 className="text-xl font-semibold text-[var(--text-primary)]">Жеке ақпарат</h2>
            <p className="mt-1 text-sm text-[var(--text-secondary)]">
              Аккаунттың негізгі деректері, көріну форматы және профиль суреті бір жерде жиналған.
            </p>
          </div>

          <form onSubmit={onSaveProfile} className="space-y-6 p-6">
            <div className="grid gap-4 lg:grid-cols-[240px_minmax(0,1fr)]">
              <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                <div className="flex flex-col items-center text-center">
                  {form.avatarUrl ? (
                    <img src={form.avatarUrl} alt="avatar" className="h-24 w-24 rounded-[28px] object-cover shadow-sm" />
                  ) : (
                    <div className="flex h-24 w-24 items-center justify-center rounded-[28px] bg-[linear-gradient(135deg,#2563eb,#60a5fa)] text-3xl font-semibold text-white shadow-sm">
                      {initials}
                    </div>
                  )}

                  <div className="mt-4 text-base font-semibold text-[var(--text-primary)]">{displayName}</div>
                  <div className="mt-1 text-sm text-[var(--text-secondary)]">{accountType}</div>
                  <div className="mt-3 inline-flex rounded-full bg-[var(--brand-soft)] px-3 py-1 text-xs font-medium text-[var(--brand-strong)]">
                    Белсенді аккаунт
                  </div>

                  <input
                    ref={avatarInputRef}
                    type="file"
                    accept="image/png,image/jpeg"
                    onChange={onAvatarChange}
                    className="hidden"
                  />

                  <div className="mt-4 flex w-full flex-col gap-2">
                    <GhostButton type="button" className="w-full" onClick={() => avatarInputRef.current?.click()}>
                      Сурет таңдау
                    </GhostButton>
                    <button
                      type="button"
                      onClick={onRemoveAvatar}
                      className="rounded-xl px-3 py-2 text-sm font-medium text-[var(--brand-strong)] transition hover:bg-[var(--brand-soft)]"
                    >
                      Аватарды алып тастау
                    </button>
                  </div>

                  <div className="mt-3 text-xs leading-5 text-[var(--text-secondary)]">JPG немесе PNG, 2 MB дейін</div>
                </div>
              </div>

              <div className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <InfoCard label="Аты" value={displayName} hint="Интерфейсте көрінетін атау" />
                  <InfoCard label="Рөл" value={me?.role || 'USER'} hint="Қолжетімділік деңгейі" />
                  <InfoCard label="Тіркелген күні" value={formatCreatedAt(me?.createdAt)} hint="Аккаунт ашылған уақыт" />
                  <InfoCard label="Қауіпсіздік түрі" value={securityMode} hint="Кіруді растау тәсілі" />
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Email</label>
                  <div className="rounded-xl border border-[var(--border-color)] bg-[var(--card-strong)] px-3.5 py-3 text-sm text-[var(--text-primary)]">
                    <div className="break-all leading-6">{form.email || '—'}</div>
                  </div>
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Көрінетін аты</label>
                  <input
                    type="text"
                    value={form.name}
                    onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
                    className="app-input"
                    placeholder="Сіздің атыңыз"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Телефон</label>
                  <input
                    type="text"
                    value={form.phone}
                    onChange={(event) => setForm((prev) => ({ ...prev, phone: event.target.value }))}
                    className="app-input"
                    placeholder="+7 777 000 00 00"
                  />
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold text-[var(--text-primary)]">Еске салғыш баптаулары</h3>
                <p className="mt-1 text-sm text-[var(--text-secondary)]">
                  Қай арна арқылы ескерту алатыныңызды және тапсырма қанша минут бұрын еске салынатынын таңдаңыз.
                </p>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                <ToggleRow
                  label="Email арқылы ескерту"
                  hint="Мерзімі жақындаған және кешіккен тапсырмалар почтаға жіберіледі."
                  checked={form.emailNotificationsEnabled}
                  onChange={(value) => setForm((prev) => ({ ...prev, emailNotificationsEnabled: value }))}
                />
                <ToggleRow
                  label="In-app push ескерту"
                  hint="Хабарламалар бетінде push түрінде сақталып тұрады."
                  checked={form.pushNotificationsEnabled}
                  onChange={(value) => setForm((prev) => ({ ...prev, pushNotificationsEnabled: value }))}
                />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Еске салу терезесі (минут)</label>
                <input
                  type="number"
                  min="5"
                  max="1440"
                  value={form.reminderWindowMinutes}
                  onChange={(event) => setForm((prev) => ({ ...prev, reminderWindowMinutes: event.target.value }))}
                  className="app-input max-w-xs"
                />
              </div>
            </div>

            <div className="flex justify-end">
              <PrimaryButton type="submit" disabled={loading}>
                {loading ? 'Сақталып жатыр...' : 'Профильді сақтау'}
              </PrimaryButton>
            </div>
          </form>
        </Surface>

        <div className="space-y-6">
          <Surface className="overflow-hidden">
            <div className="border-b border-[var(--border-color)] px-6 py-5">
              <h2 className="text-xl font-semibold text-[var(--text-primary)]">Парольді өзгерту</h2>
              <p className="mt-1 text-sm text-[var(--text-secondary)]">
                {me?.passwordChangeAvailable
                  ? 'Қауіпсіздік үшін ағымдағы парольді енгізіп, жаңасын сақтаңыз.'
                  : 'OAuth аккаунттары үшін бұл бөлім қолжетімсіз.'}
              </p>
            </div>

            <form onSubmit={onChangePassword} className="space-y-4 p-6">
              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Ағымдағы пароль</label>
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(event) => setPasswordForm((prev) => ({ ...prev, currentPassword: event.target.value }))}
                  className="app-input"
                  disabled={!me?.passwordChangeAvailable}
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Жаңа пароль</label>
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(event) => setPasswordForm((prev) => ({ ...prev, newPassword: event.target.value }))}
                  className="app-input"
                  disabled={!me?.passwordChangeAvailable}
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Жаңа парольді қайталау</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(event) => setPasswordForm((prev) => ({ ...prev, confirmPassword: event.target.value }))}
                  className="app-input"
                  disabled={!me?.passwordChangeAvailable}
                />
              </div>
              <div className="flex justify-end">
                <PrimaryButton type="submit" disabled={loading || !me?.passwordChangeAvailable}>
                  Парольді жаңарту
                </PrimaryButton>
              </div>
            </form>
          </Surface>

          <Surface className="overflow-hidden">
            <div className="border-b border-[var(--border-color)] px-6 py-5">
              <h2 className="text-xl font-semibold text-[var(--text-primary)]">GDPR және аккаунт</h2>
              <p className="mt-1 text-sm text-[var(--text-secondary)]">
                Жеке деректерді экспорттау немесе аккаунтты жоюға қатысты әрекеттер осында орналасқан.
              </p>
            </div>

            <div className="space-y-4 p-6">
              <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                <div className="font-medium text-[var(--text-primary)]">Деректер экспорты</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">Жеке деректерді JSON форматында жүктеп алуға болады.</div>
                <div className="mt-4">
                  <GhostButton onClick={onExportData} disabled={exporting}>
                    {exporting ? 'Дайындалып жатыр...' : 'Экспортты жүктеу'}
                  </GhostButton>
                </div>
              </div>

              <div className="rounded-2xl border border-rose-200 bg-[var(--danger-soft)] p-4">
                <div className="font-medium text-rose-600">Аккаунтты жою</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">
                  Жою сұранысынан кейін аккаунт бірден бұғатталады, ал барлық деректер 30 күннен соң толық өшіріледі.
                </div>
                <div className="mt-4">
                  <button
                    type="button"
                    onClick={onDeleteAccount}
                    disabled={deleting}
                    className="rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-sm font-medium text-rose-600 transition hover:border-rose-300 disabled:opacity-60"
                  >
                    {deleting ? 'Жіберіліп жатыр...' : 'Жою сұранысын беру'}
                  </button>
                </div>
              </div>
            </div>
          </Surface>
        </div>
      </div>
    </div>
  );
}
