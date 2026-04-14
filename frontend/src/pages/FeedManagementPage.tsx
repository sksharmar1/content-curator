import React, { useState } from 'react';
import { api } from '../api/client';

const PRESET_FEEDS = [
  { name: 'Towards Data Science', url: 'https://towardsdatascience.com/feed', category: 'AI/ML' },
  { name: 'Spring Blog', url: 'https://spring.io/blog.atom', category: 'Java' },
  { name: 'AWS News', url: 'https://aws.amazon.com/blogs/aws/feed/', category: 'Cloud' },
  { name: 'KDnuggets', url: 'https://www.kdnuggets.com/feed', category: 'AI/ML' },
  { name: 'InfoQ Java', url: 'https://feed.infoq.com/java', category: 'Java' },
];

const FeedManagementPage: React.FC = () => {
  const [form, setForm]     = useState({ name: '', url: '', category: 'General' });
  const [polling, setPolling] = useState(false);
  const [message, setMessage] = useState('');

  const set = (k: string) => (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => setForm(f => ({ ...f, [k]: e.target.value }));

  const registerFeed = async () => {
    try {
      await api.feeds.register(form.name, form.url, form.category);
      setMessage(`Feed "${form.name}" registered successfully`);
      setForm({ name: '', url: '', category: 'General' });
      setTimeout(() => setMessage(''), 4000);
    } catch {
      setMessage('Failed to register feed');
    }
  };

  const triggerPoll = async () => {
    setPolling(true);
    try {
      await api.feeds.poll();
      setMessage('Feed poll triggered — new articles will appear shortly');
      setTimeout(() => setMessage(''), 5000);
    } catch {
      setMessage('Failed to trigger poll');
    } finally {
      setPolling(false);
    }
  };

  const addPreset = (preset: typeof PRESET_FEEDS[0]) => {
    setForm({ name: preset.name, url: preset.url, category: preset.category });
  };

  return (
    <div style={pageStyle}>
      <h2 style={{ color: '#fff', marginBottom: '1.5rem' }}>Feed Management</h2>

      {/* Poll trigger */}
      <div style={card}>
        <h3 style={sectionTitle}>Trigger RSS poll</h3>
        <p style={{ color: '#888', fontSize: '0.85rem', marginBottom: '1rem' }}>
          Manually fetch new articles from all active feeds.
          Polling also runs automatically every 30 minutes.
        </p>
        <button style={primaryBtn} onClick={triggerPoll} disabled={polling}>
          {polling ? 'Polling...' : 'Poll all feeds now'}
        </button>
      </div>

      {/* Register feed */}
      <div style={card}>
        <h3 style={sectionTitle}>Register new feed</h3>

        <p style={{ color: '#888', fontSize: '0.85rem', marginBottom: '0.75rem' }}>
          Preset feeds:
        </p>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.25rem' }}>
          {PRESET_FEEDS.map(p => (
            <button key={p.url} style={presetBtn} onClick={() => addPreset(p)}>
              {p.name}
            </button>
          ))}
        </div>

        <input style={input} placeholder="Feed name"
          value={form.name} onChange={set('name')} />
        <input style={input} placeholder="RSS URL"
          value={form.url} onChange={set('url')} />
        <select style={input} value={form.category} onChange={set('category')}>
          <option>AI/ML</option>
          <option>Java</option>
          <option>Cloud</option>
          <option>General</option>
        </select>
        <button style={primaryBtn} onClick={registerFeed}
          disabled={!form.name || !form.url}>
          Register feed
        </button>
      </div>

      {message && (
        <div style={messageBanner}>
          {message}
        </div>
      )}
    </div>
  );
};

const pageStyle: React.CSSProperties = {
  maxWidth: '700px', margin: '0 auto', padding: '2rem 1rem'
};
const card: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '12px',
  padding: '1.75rem', marginBottom: '1.5rem',
  border: '1px solid #2a2a4a'
};
const sectionTitle: React.CSSProperties = {
  color: '#e2e8f0', marginBottom: '0.75rem', fontSize: '1rem'
};
const input: React.CSSProperties = {
  width: '100%', padding: '10px 14px',
  marginBottom: '0.75rem', borderRadius: '8px',
  border: '1px solid #333', background: '#0f0f1a',
  color: '#fff', fontSize: '0.9rem', boxSizing: 'border-box'
};
const primaryBtn: React.CSSProperties = {
  background: '#7c83fd', color: '#fff', border: 'none',
  borderRadius: '8px', padding: '10px 1.5rem',
  cursor: 'pointer', fontWeight: 600, fontSize: '0.9rem'
};
const presetBtn: React.CSSProperties = {
  background: '#2a2a4a', color: '#7c83fd',
  border: '1px solid #7c83fd40', borderRadius: '20px',
  padding: '4px 12px', cursor: 'pointer', fontSize: '0.8rem'
};
const messageBanner: React.CSSProperties = {
  background: '#1a3a2a', color: '#4ade80',
  padding: '1rem 1.5rem', borderRadius: '8px',
  border: '1px solid #4ade8040', fontSize: '0.9rem'
};

export default FeedManagementPage;
