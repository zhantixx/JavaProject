import React, { useState } from 'react';
import { tasks } from '../services/api';

function TaskForm({ onTaskCreated }) {
  const [title, setTitle] = useState('');
  const [priority, setPriority] = useState('MEDIUM');

  const handleSubmit = async (e) => {
    e.preventDefault();
    await tasks.create({ title, priority, description: '' });
    setTitle('');
    if (onTaskCreated) onTaskCreated();
  };

  return (
    <form onSubmit={handleSubmit}>
      <input type="text" placeholder="Тапсырма атауы" value={title} onChange={(e) => setTitle(e.target.value)} required />
      <select value={priority} onChange={(e) => setPriority(e.target.value)}>
        <option value="HIGH">Жоғары</option>
        <option value="MEDIUM">Орташа</option>
        <option value="LOW">Төмен</option>
      </select>
      <button type="submit">Қосу</button>
    </form>
  );
}

export default TaskForm;