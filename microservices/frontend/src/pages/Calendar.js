import React from 'react';
import CalendarEventForm from '../components/CalendarEventForm';

export default function CalendarPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4">
      <div className="max-w-3xl mx-auto">
        <div className="bg-white rounded-2xl shadow-sm p-8">
          <h1 className="text-2xl font-semibold text-gray-900 mb-2">Google Календарь</h1>
          <p className="text-gray-600 mb-6">
            Создайте событие в вашем Google Calendar. Для работы необходима авторизация и выданный доступ.
          </p>
          <CalendarEventForm />
          <div className="mt-8 text-sm text-gray-500">
            Формат времени: используйте локальный выбор даты/времени. Бэкенд конвертирует в RFC3339.
          </div>
        </div>
      </div>
    </div>
  );
}
