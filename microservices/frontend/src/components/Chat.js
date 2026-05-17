import React, { useState } from 'react';
import { chat } from '../services/api';
import { PrimaryButton, Surface } from './UiKit';

function Chat() {
  const [message, setMessage] = useState('');
  const [reply, setReply] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSend = async () => {
    if (!message.trim()) return;
    setLoading(true);
    try {
      const res = await chat.send(message);
      setReply(res.data.reply);
      setMessage('');
    } catch (err) {
      setReply('Қате шықты. Сәл кейінірек қайта көріңіз.');
    }
    setLoading(false);
  };

  return (
    <Surface className="overflow-hidden">
      <div className="border-b border-[var(--border-color)] px-6 py-5">
        <h3 className="text-xl font-semibold text-[var(--text-primary)]">Сұрақ жіберу</h3>
        <p className="mt-1 text-sm text-[var(--text-secondary)]">Қысқа, нақты сұрақтар жақсы нәтиже береді.</p>
      </div>

      <div className="space-y-4 p-6">
        <textarea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Мысалы: ертеңгі жұмысымды 3 блокқа бөліп бер"
          rows={5}
          className="app-input min-h-[160px] resize-none"
        />

        <div className="flex justify-end">
          <PrimaryButton onClick={handleSend} disabled={loading}>
            {loading ? 'Жіберіліп жатыр...' : 'Жіберу'}
          </PrimaryButton>
        </div>

        <div className="rounded-2xl border border-[var(--border-color)] bg-[var(--card-strong)] p-5">
          <div className="text-sm font-medium text-[var(--text-primary)]">Жауап</div>
          <div className="mt-3 whitespace-pre-line text-sm leading-7 text-[var(--text-secondary)]">
            {reply || 'Жауап осы жерде көрінеді.'}
          </div>
        </div>
      </div>
    </Surface>
  );
}

export default Chat;
