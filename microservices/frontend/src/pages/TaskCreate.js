import React from 'react';
import { useNavigate } from 'react-router-dom';
import TaskForm from '../components/TaskForm';
import { PageIntro } from '../components/UiKit';

export default function TaskCreatePage() {
  const navigate = useNavigate();

  return (
    <div className="space-y-6">
      <PageIntro
        eyebrow="Жоспарлау"
        title="Жаңа тапсырма құру"
        description="Бұл жерде тапсырманың аяқталу уақыты нақты көрсетіледі. Сол себепті кейін тізімде мерзімдер анық көрінеді."
        stats={[
          { label: 'Уақыт', value: 'Міндетті', hint: 'Орындау мерзімін көрсету қажет' },
          { label: 'Күйі', value: '3 түр', hint: 'Жоспар, дайын, тоқтатылды' },
          { label: 'Басымдық', value: '3 деңгей', hint: 'Маңызын бөлу үшін' },
          { label: 'Нәтиже', value: 'Тізімге қосылады', hint: 'Сақталған соң тапсырмалар бетіне өтеді' },
        ]}
      />

      <TaskForm
        submitLabel="Тапсырманы құру"
        onTaskCreated={() => navigate('/tasks')}
      />
    </div>
  );
}
