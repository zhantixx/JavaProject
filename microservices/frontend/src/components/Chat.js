import React, { useState } from 'react';
import { chat } from '../services/api';

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
      setReply('Қате кетті, кейінірек қайталаңыз.');
    }
    setLoading(false);
  };

  return (
    <div className="rounded-2xl bg-white/80 backdrop-blur p-6 ring-1 ring-gray-100 shadow-sm">
      <div className="flex items-center gap-2 mb-4">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-600 text-white flex items-center justify-center">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
          </svg>
        </div>
        <h3 className="text-lg font-semibold text-gray-900">AI Көмекші</h3>
      </div>
      <div className="space-y-4">
        <textarea
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Сұрағыңызды жазыңыз..."
          rows="3"
          className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-purple-500 focus:outline-none"
        />
        <button
          onClick={handleSend}
          disabled={loading}
          className="inline-flex items-center gap-2 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-medium px-5 py-2.5 rounded-xl transition disabled:opacity-60"
        >
          {loading ? (
            <>
              <span className="inline-block w-4 h-4 rounded-full border-2 border-white border-t-transparent animate-spin" />
              Жіберілуде...
            </>
          ) : (
            <>
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 12h14M12 5l7 7-7 7" />
              </svg>
              Жіберу
            </>
          )}
        </button>
        {reply && (
          <div className="mt-2 rounded-xl border border-gray-100 bg-gray-50 p-4">
            <div className="text-sm font-semibold text-gray-700 mb-1">Жауап</div>
            <div className="prose prose-sm max-w-none text-gray-800 whitespace-pre-line">
              {reply}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Chat;