import React, { useEffect, useMemo, useState } from 'react';
import { clearAuthSession, profile } from '../services/api';
import { useToast } from '../components/Toast';
import { GhostButton, PageIntro, PrimaryButton, Surface } from '../components/UiKit';

function formatCreatedAt(createdAt) {
  if (!createdAt) return 'Көрсетілмеген';
  return new Date(createdAt).toLocaleString('kk-KZ', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

async function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export default function Profile() {
  const toast = useToast();
  const [loading, setLoading] = useState(false);
  const [me, setMe] = useState(null);
  const [form, setForm] = useState({ name: '', email: '', avatarUrl: '' });
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
      const r = await profile.me();
      if (r.status >= 200 && r.status < 300) {
        const next = r.data || {};
        setMe(next);
        setForm({
          name: next.name || '',
          email: next.email || '',
          avatarUrl: next.avatarUrl || '',
        });
      } else {
        toast.error('Профильді жүктеу мүмкін болмады');
      }
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

  const onSaveProfile = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      const r = await profile.updateMe({ name: form.name, avatarUrl: form.avatarUrl });
      if (r.status >= 200 && r.status < 300) {
        setMe(r.data);
        toast.success('Профиль жаңартылды');
      } else {
        toast.error('Жаңарту мүмкін болмады');
      }
    } catch {
      toast.error('Профильді жаңарту қатесі');
    } finally {
      setLoading(false);
    }
  };

  const onAvatarChange = async (e) => {
    const file = e.target.files?.[0];
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
      toast.success('Аватар жүктеуге дайын');
    } catch {
      toast.error('Файлды оқу мүмкін болмады');
    }
  };

  const onChangePassword = async (e) => {
    e.preventDefault();
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
    const confirmed = window.confirm('Аккаунтты толық жоюды растайсыз ба? Бұл әрекет қайтарылмайды.');
    if (!confirmed) return;

    try {
      setDeleting(true);
      await profile.deleteAccount();
      clearAuthSession();
      toast.success('Аккаунт жойылды');
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
        description="Бұл жерде негізгі деректер, аватар, пароль және GDPR әрекеттері бір жүйеге жиналған."
        stats={[
          { label: 'Email', value: form.email || '—', hint: 'Негізгі кіру адресі' },
          { label: 'Рөл', value: me?.role || 'USER', hint: 'Аккаунт мәртебесі' },
          { label: 'Тіркелген', value: formatCreatedAt(me?.createdAt), hint: 'Аккаунт жасалған күн' },
          { label: 'Пароль', value: me?.passwordChangeAvailable ? 'Жергілікті' : 'OAuth', hint: 'Қауіпсіздік түрі' },
        ]}
      />

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[0.95fr_1.05fr]">
        <Surface className="overflow-hidden">
          <div className="border-b border-[var(--border-color)] px-6 py-5">
            <h2 className="text-xl font-semibold text-[var(--text-primary)]">Жеке ақпарат</h2>
            <p className="mt-1 text-sm text-[var(--text-secondary)]">Аватарды, атты және көрінетін профиль деректерін жаңартыңыз.</p>
          </div>

          <form onSubmit={onSaveProfile} className="space-y-6 p-6">
            <div className="flex flex-col gap-4 md:flex-row md:items-center">
              {form.avatarUrl ? (
                <img src={form.avatarUrl} alt="avatar" className="h-20 w-20 rounded-3xl object-cover" />
              ) : (
                <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-[linear-gradient(135deg,#2563eb,#60a5fa)] text-3xl font-semibold text-white">
                  {initials}
                </div>
              )}
              <div className="space-y-3">
                <div>
                  <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Аватар жүктеу</label>
                  <input type="file" accept="image/png,image/jpeg" onChange={onAvatarChange} className="block text-sm text-[var(--text-secondary)]" />
                </div>
                <button
                  type="button"
                  onClick={() => setForm((prev) => ({ ...prev, avatarUrl: '' }))}
                  className="text-sm font-medium text-[var(--brand-strong)]"
                >
                  Аватарды алып тастау
                </button>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="md:col-span-2">
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Email</label>
                <input type="email" disabled value={form.email} className="app-input opacity-80" />
              </div>
              <div className="md:col-span-2">
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Аты</label>
                <input
                  type="text"
                  value={form.name}
                  onChange={(e) => setForm((prev) => ({ ...prev, name: e.target.value }))}
                  className="app-input"
                  placeholder="Сіздің атыңыз"
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
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, currentPassword: e.target.value }))}
                  className="app-input"
                  disabled={!me?.passwordChangeAvailable}
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Жаңа пароль</label>
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, newPassword: e.target.value }))}
                  className="app-input"
                  disabled={!me?.passwordChangeAvailable}
                />
              </div>
              <div>
                <label className="mb-1 block text-sm font-medium text-[var(--text-secondary)]">Жаңа парольді қайталау</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm((prev) => ({ ...prev, confirmPassword: e.target.value }))}
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
              <p className="mt-1 text-sm text-[var(--text-secondary)]">Деректерді экспорттау немесе аккаунтты толық жою әрекеттері.</p>
            </div>

            <div className="space-y-4 p-6">
              <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-4">
                <div className="font-medium text-[var(--text-primary)]">Деректер экспорты</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">JSON форматында жеке деректерді жүктеп алыңыз.</div>
                <div className="mt-4">
                  <GhostButton onClick={onExportData} disabled={exporting}>
                    {exporting ? 'Дайындалып жатыр...' : 'Экспортты жүктеу'}
                  </GhostButton>
                </div>
              </div>

              <div className="rounded-2xl border border-rose-200 bg-[var(--danger-soft)] p-4">
                <div className="font-medium text-rose-600">Аккаунтты толық жою</div>
                <div className="mt-1 text-sm text-[var(--text-secondary)]">
                  Бұл әрекет профильді және байланысты негізгі деректерді өшіреді.
                </div>
                <div className="mt-4">
                  <button
                    type="button"
                    onClick={onDeleteAccount}
                    disabled={deleting}
                    className="rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-sm font-medium text-rose-600 transition hover:border-rose-300 disabled:opacity-60"
                  >
                    {deleting ? 'Жойылып жатыр...' : 'Аккаунтты жою'}
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
