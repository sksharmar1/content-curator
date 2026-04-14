import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/client';

const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [newTopic, setNewTopic]   = useState('');
  const [interests, setInterests] = useState<string[]>(user?.interests || []);
  const [saving, setSaving]       = useState(false);
  const [message, setMessage]     = useState('');

  const addInterest = async () => {
    if (!newTopic.trim()) return;
    setSaving(true);
    try {
      await api.user.addInterest(newTopic.trim());
      setInterests(prev => [...prev, newTopic.trim().toLowerCase()]);
      setNewTopic('');
      setMessage('Interest added!');
      setTimeout(() => setMessage(''), 3000);
    } catch {
      setMessage('Failed to add interest');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={pageStyle}>
      <div style={card}>
        <h2 style={{ color: '#fff', marginBottom: '0.25rem' }}>
          {user?.displayName}
        </h2>
        <p style={{ color: '#888', marginBottom: '2rem' }}>{user?.email}</p>

        <h3 style={{ color: '#e2e8f0', marginBottom: '1rem' }}>
          Learning interests
        </h3>

        <div style={chipContainer}>
          {interests.map(topic => (
            <span key={topic} style={chip}>{topic}</span>
          ))}
          {interests.length === 0 && (
            <p style={{ color: '#666', fontSize: '0.85rem' }}>
              No interests yet — add some below
            </p>
          )}
        </div>

        <div style={addRow}>
          <input
            style={input}
            value={newTopic}
            onChange={e => setNewTopic(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addInterest()}
            placeholder="e.g. machine learning, kubernetes, spring boot"
          />
          <button style={btn} onClick={addInterest} disabled={saving}>
            {saving ? 'Adding...' : 'Add'}
          </button>
        </div>

        {message && (
          <p style={{ color: '#4ade80', fontSize: '0.85rem', marginTop: '0.5rem' }}>
            {message}
          </p>
        )}

        <div style={suggestSection}>
          <p style={{ color: '#888', fontSize: '0.85rem', marginBottom: '0.5rem' }}>
            Suggested topics:
          </p>
          <div style={chipContainer}>
            {['AWS', 'Java', 'Spring Boot', 'machine learning', 'Docker',
              'Kubernetes', 'OpenShift', 'Python', 'AI', 'cloud architecture']
              .filter(t => !interests.includes(t.toLowerCase()))
              .map(t => (
                <span key={t} style={suggestChip}
                  onClick={() => { setNewTopic(t); }}>
                  + {t}
                </span>
              ))
            }
          </div>
        </div>
      </div>
    </div>
  );
};

const pageStyle: React.CSSProperties = {
  maxWidth: '600px', margin: '0 auto', padding: '2rem 1rem'
};
const card: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '12px',
  padding: '2rem', border: '1px solid #2a2a4a'
};
const chipContainer: React.CSSProperties = {
  display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.5rem'
};
const chip: React.CSSProperties = {
  background: '#2a2a4a', color: '#7c83fd',
  padding: '4px 12px', borderRadius: '20px',
  fontSize: '0.85rem', border: '1px solid #7c83fd40'
};
const suggestChip: React.CSSProperties = {
  ...chip, color: '#888', cursor: 'pointer',
  border: '1px solid #333', background: '#1a1a2e'
};
const addRow: React.CSSProperties = {
  display: 'flex', gap: '0.75rem', marginBottom: '0.5rem'
};
const input: React.CSSProperties = {
  flex: 1, padding: '10px 14px', borderRadius: '8px',
  border: '1px solid #333', background: '#0f0f1a',
  color: '#fff', fontSize: '0.9rem'
};
const btn: React.CSSProperties = {
  background: '#7c83fd', color: '#fff', border: 'none',
  borderRadius: '8px', padding: '10px 1.25rem',
  cursor: 'pointer', fontWeight: 600
};
const suggestSection: React.CSSProperties = {
  marginTop: '1.5rem', paddingTop: '1.5rem',
  borderTop: '1px solid #2a2a4a'
};

export default ProfilePage;
