import React, { useState } from 'react';
import { useToast } from './Toast';
import { calendar } from '../services/api';

export default function CalendarEventForm() {
    const toast = useToast();
    const [form, setForm] = useState({
        title: '',          // ← изменил с summary на title
        description: '',
        startDateTime: '',
        endDateTime: '',
        timeZone: 'Asia/Almaty'  // ← часовой пояс по умолчанию
    });
    const [loading, setLoading] = useState(false);

    const onChange = (e) => {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        if (!form.title || !form.startDateTime || !form.endDateTime) {
            toast.error('Міндетті өрістерді толтырыңыз');
            return;
        }

        // Валидация времени
        if (new Date(form.startDateTime) >= new Date(form.endDateTime)) {
            toast.error('Басталу уақыты аяқталу уақытынан кейін болуы керек');
            return;
        }

        setLoading(true);
        try {
            // Преобразуем datetime-local в ISO строку
            const startDateTime = new Date(form.startDateTime).toISOString();
            const endDateTime = new Date(form.endDateTime).toISOString();

            const r = await calendar.createEvent({
                title: form.title,           // ← изменено
                description: form.description,
                startDateTime: startDateTime,
                endDateTime: endDateTime,
                timeZone: form.timeZone
            });

            if (r.status >= 200 && r.status < 300) {
                const data = r.data || {};
                toast.success(`Оқиға қосылды${data.eventId ? ` (id: ${data.eventId})` : ''}`);
                // Сброс формы
                setForm({
                    title: '',
                    description: '',
                    startDateTime: '',
                    endDateTime: '',
                    timeZone: form.timeZone
                });
            } else {
                toast.error(`Оқиға қосу қатесі: ${r.status}`);
            }
        } catch (err) {
            console.error('Calendar error:', err);
            const errorMsg = err.response?.data?.error || err.message || 'Оқиға жасауда қате';
            toast.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={onSubmit} className="space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Тақырып*</label>
                    <input
                        name="title"  // ← изменено с summary на title
                        value={form.title}
                        onChange={onChange}
                        className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                        placeholder="Мысалы, Кездесу"
                        required
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Уақыт белдеуі</label>
                    <select
                        name="timeZone"
                        value={form.timeZone}
                        onChange={onChange}
                        className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                    >
                        <option value="UTC">UTC</option>
                        <option value="Asia/Almaty">Asia/Almaty (GMT+6)</option>
                        <option value="Asia/Astana">Asia/Astana (GMT+6)</option>
                        <option value="Europe/Moscow">Europe/Moscow (GMT+3)</option>
                        <option value="Asia/Tashkent">Asia/Tashkent (GMT+5)</option>
                        <option value="Asia/Bishkek">Asia/Bishkek (GMT+6)</option>
                        <option value="Asia/Dubai">Asia/Dubai (GMT+4)</option>
                    </select>
                </div>
            </div>

            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Сипаттама</label>
                <textarea
                    name="description"
                    value={form.description}
                    onChange={onChange}
                    rows={3}
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                    placeholder="Қосымша ақпарат (міндетті емес)"
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Басталуы*</label>
                    <input
                        type="datetime-local"
                        name="startDateTime"
                        value={form.startDateTime}
                        onChange={onChange}
                        className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                        required
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Аяқталуы*</label>
                    <input
                        type="datetime-local"
                        name="endDateTime"
                        value={form.endDateTime}
                        onChange={onChange}
                        className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-indigo-500"
                        required
                    />
                </div>
            </div>

            <div className="flex justify-end">
                <button
                    type="submit"
                    disabled={loading}
                    className="inline-flex items-center px-5 py-2.5 rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-60"
                >
                    {loading ? 'Қосылуда...' : 'Оқиға қосу'}
                </button>
            </div>
        </form>
    );
}