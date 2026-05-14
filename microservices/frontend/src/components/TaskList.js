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

  const priorityColor = (priority) => {
    switch(priority) {
      case 'HIGH': return 'bg-red-100 text-red-800';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-green-100 text-green-800';
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-md p-6">
      <h2 className="text-xl font-bold text-gray-800 mb-4 flex items-center gap-2">
        <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        Менің тапсырмаларым
      </h2>
      {taskList.length === 0 ? (
        <p className="text-gray-500 text-center py-8">Әзірге тапсырма жоқ. Жоғарыдан қосыңыз!</p>
      ) : (
        <div className="divide-y">
          {taskList.map(task => (
            <div key={task.id} className="py-4 flex flex-wrap justify-between items-center gap-2">
              <div className="flex-1">
                <p className="font-medium text-gray-800">{task.title}</p>
                <div className="flex gap-2 mt-1">
                  <span className={`text-xs px-2 py-0.5 rounded-full ${priorityColor(task.priority)}`}>
                    {task.priority === 'HIGH' ? 'Жоғары' : task.priority === 'MEDIUM' ? 'Орташа' : 'Төмен'}
                  </span>
                </div>
              </div>
              <button
                onClick={() => handleDelete(task.id)}
                className="text-red-500 hover:text-red-700 transition"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default TaskList;