import React, { useState } from 'react';
import { chat } from '../services/api';

function Chat() {
  const [message, setMessage] = useState('');
  const [reply, setReply] = useState('');

  const handleSend = async () => {
    const res = await chat.send(message);
    setReply(res.data.reply);
    setMessage('');
  };

  return (
    <div>
      <h2>AI Көмекші</h2>
      <textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Сұрағыңызды жазыңыз..." />
      <button onClick={handleSend}>Жіберу</button>
      {reply && <div><strong>Жауап:</strong> {reply}</div>}
    </div>
  );
}

export default Chat;