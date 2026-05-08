import React, { useEffect, useState } from 'react';
import { tasks } from '../services/api';

function TaskList() {
  const [taskList, setTaskList] = useState([]);

  const loadTasks = async () => {
    const res = await tasks.getAll();
    setTaskList(res.data);
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleDelete = async (id) => {
    await tasks.delete(id);
    loadTasks();
  };

  return (
    <div>
      <h2>Менің тапсырмаларым</h2>
      <ul>
        {taskList.map(task => (
          <li key={task.id}>
            {task.title} (басымдылық: {task.priority})
            <button onClick={() => handleDelete(task.id)}>Өшіру</button>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default TaskList;