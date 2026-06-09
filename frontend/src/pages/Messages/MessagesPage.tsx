import { useState } from 'react';
import { Send, Hash, Search, MoreVertical, MessageSquare } from 'lucide-react';
import './Messages.css';

export default function MessagesPage() {
  const [messages, setMessages] = useState<any[]>([
    { id: 1, sender: 'John Doe', text: 'Hey team, any update on INC-2026-00001?', time: '10:30 AM', isMe: false },
    { id: 2, sender: 'Me', text: 'I am looking into the database logs now. Looks like a deadlock.', time: '10:32 AM', isMe: true },
    { id: 3, sender: 'Jane Smith', text: 'Let me know if you need help with the DBA access.', time: '10:35 AM', isMe: false }
  ]);
  const [newMessage, setNewMessage] = useState('');

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMessage.trim()) return;

    setMessages([...messages, {
      id: Date.now(),
      sender: 'Me',
      text: newMessage,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isMe: true
    }]);
    setNewMessage('');
  };

  return (
    <div className="messages-page animate-fade-in">
      <div className="messages-layout card">
        
        {/* Sidebar */}
        <div className="messages-sidebar">
          <div className="messages-sidebar-header">
            <h3>Conversations</h3>
            <div className="toolbar-search">
              <Search size={16} className="toolbar-search-icon" />
              <input type="text" placeholder="Search..." className="form-input toolbar-search-input" />
            </div>
          </div>
          
          <div className="channel-list">
            <div className="channel-item active">
              <div className="channel-icon"><Hash size={18} /></div>
              <div className="channel-info">
                <h4>INC-2026-00001 Triage</h4>
                <p>Jane Smith: Let me know if...</p>
              </div>
            </div>
            <div className="channel-item">
              <div className="channel-icon"><MessageSquare size={18} /></div>
              <div className="channel-info">
                <h4>IT Ops General</h4>
                <p>System: Weekly backup com...</p>
              </div>
            </div>
          </div>
        </div>

        {/* Chat Area */}
        <div className="chat-area">
          <div className="chat-header">
            <div className="chat-header-info">
              <Hash size={20} className="text-primary" />
              <h2>INC-2026-00001 Triage</h2>
            </div>
            <button className="btn btn-ghost btn-icon"><MoreVertical size={18} /></button>
          </div>

          <div className="chat-messages">
            {messages.map(msg => (
              <div key={msg.id} className={`message-bubble ${msg.isMe ? 'me' : 'them'}`}>
                {!msg.isMe && <div className="message-sender">{msg.sender}</div>}
                <div className="message-content">{msg.text}</div>
                <div className="message-time">{msg.time}</div>
              </div>
            ))}
          </div>

          <div className="chat-input-container">
            <form onSubmit={handleSend} className="chat-input-form">
              <input 
                type="text" 
                placeholder="Type a message..." 
                value={newMessage}
                onChange={(e) => setNewMessage(e.target.value)}
                className="form-input"
              />
              <button type="submit" className="btn btn-primary btn-icon" disabled={!newMessage.trim()}>
                <Send size={18} />
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
