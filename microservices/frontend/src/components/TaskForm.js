import React, { useState } from 'react';
import { tasks } from '../services/api';

function TaskForm({ onTaskCreated }) {
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState('MEDIUM');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    await tasks.create({ title, priority, description: '' });
    setTitle('');
    if (onTaskCreated) onTaskCreated();
  };

  return (
    <div className="bg-white rounded-xl shadow-md p-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-3">➕ Жаңа тапсырма</h3>
      <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-3">
        <input
          type="text"
          placeholder="Тапсырма атауы..."
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
          required
        />
        <select
          value={priority}
          onChange={(e) => setPriority(e.target.value)}
          className="px-4 py-2 border border-gray-300 rounded-lg bg-white"
        >
          <option value="HIGH">Жоғары</option>
          <option value="MEDIUM">Орташа</option>
          <option value="LOW">Төмен</option>
        </select>
        <button
          type="submit"
          className="bg-blue-600 hover:bg-blue-700 text-white font-medium px-6 py-2 rounded-lg transition"
        >
          Қосу
        </button>
      </form>
    </div>
  );
}

export default TaskForm;