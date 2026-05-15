import React, { useState } from 'react';
import { tasks } from '../services/api';

function TaskForm({ onTaskCreated }) {
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState('MEDIUM');
  const [description, setDescription] = useState('');
  const [deadline, setDeadline] = useState('');
  const [tagIds, setTagIds] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    const ids = tagIds
      .split(',')
      .map(s => s.trim())
      .filter(Boolean)
      .map(n => Number(n))
      .filter(n => !Number.isNaN(n));

    await tasks.create({
      title,
      priority,
      description,
      deadline: deadline || null,
      tagIds: ids.length ? ids : null
    });
    setTitle('');
    setPriority('MEDIUM');
    setDescription('');
    setDeadline('');
    setTagIds('');
    if (onTaskCreated) onTaskCreated();
  };

  return (
    <div>
      <h3 className="text-xl font-semibold text-gray-900 mb-4">Жаңа тапсырма</h3>
      <form onSubmit={handleSubmit} className="grid grid-cols-1 md:grid-cols-6 gap-3">
        <input
          type="text"
          placeholder="Атауы"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="md:col-span-3 px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
          required
        />
        <select
          value={priority}
          onChange={(e) => setPriority(e.target.value)}
          className="md:col-span-1 px-4 py-2.5 rounded-lg border border-gray-200 bg-white focus:ring-2 focus:ring-blue-500"
        >
          <option value="HIGH">Жоғары</option>
          <option value="MEDIUM">Орташа</option>
          <option value="LOW">Төмен</option>
        </select>
        <input
          type="datetime-local"
          value={deadline}
          onChange={(e) => setDeadline(e.target.value)}
          className="md:col-span-2 px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
        />
        <textarea
          placeholder="Сипаттама"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={3}
          className="md:col-span-4 px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
        />
        <input
          type="text"
          placeholder="Tag IDs (мысалы: 1,2,3)"
          value={tagIds}
          onChange={(e) => setTagIds(e.target.value)}
          className="md:col-span-2 px-4 py-2.5 rounded-lg border border-gray-200 focus:ring-2 focus:ring-blue-500"
        />
        <div className="md:col-span-6 flex justify-end">
          <button
            type="submit"
            className="px-6 py-2.5 rounded-lg text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 shadow-sm"
          >
            Қосу
          </button>
        </div>
      </form>
    </div>
  );
}

export default TaskForm;