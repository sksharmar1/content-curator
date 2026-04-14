import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { Article, Note } from '../types';
import SentimentBadge from '../components/common/SentimentBadge';
import CategoryBadge from '../components/common/CategoryBadge';
import Spinner from '../components/common/Spinner';

const ArticleDetailPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const article = location.state?.article as Article;

  const [noteContent, setNoteContent] = useState('');
  const [savedNote, setSavedNote]     = useState<Note | null>(null);
  const [saving, setSaving]           = useState(false);

  if (!article) {
    navigate('/feed');
    return null;
  }

  const saveNote = async () => {
    if (!noteContent.trim()) return;
    setSaving(true);
    try {
      const res = await api.analytics.saveNote(article.articleId, noteContent);
      setSavedNote(res.data);
      setNoteContent('');
    } catch (e) {
      console.error('Failed to save note', e);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={pageStyle}>
      <button onClick={() => navigate(-1)} style={backBtn}>← Back to feed</button>

      <div style={articleCard}>
        <div style={{ display: 'flex', gap: '0.75rem', marginBottom: '1rem' }}>
          <CategoryBadge category={article.feedCategory} />
          <span style={{ color: '#555', fontSize: '0.8rem' }}>
            {Math.round(article.score * 100)}% match
          </span>
        </div>

        <h1 style={titleStyle}>{article.articleTitle}</h1>

        <div style={scoreSection}>
          <div style={scoreBar}>
            <div style={{
              ...scoreFill,
              width: `${Math.round(article.score * 100)}%`
            }} />
          </div>
          <span style={{ color: '#888', fontSize: '0.8rem' }}>
            Relevance score: {(article.score * 100).toFixed(1)}%
          </span>
        </div>
      </div>

      <div style={noteSection}>
        <h3 style={{ color: '#e2e8f0', marginBottom: '1rem' }}>Add a note</h3>
        <p style={{ color: '#888', fontSize: '0.85rem', marginBottom: '0.75rem' }}>
          Tip: include "90% useful" or "8/10" to track usefulness automatically.
        </p>
        <textarea
          style={textarea}
          value={noteContent}
          onChange={e => setNoteContent(e.target.value)}
          placeholder="What did you learn? Was it useful? e.g. 'Great overview of S3. 80% useful for my AWS cert prep.'"
          rows={4}
        />
        <button onClick={saveNote} disabled={saving || !noteContent.trim()} style={saveBtn}>
          {saving ? <Spinner size={16} /> : 'Save note + analyse sentiment'}
        </button>

        {savedNote && (
          <div style={noteResult}>
            <div style={{
              display: 'flex', gap: '0.75rem',
              alignItems: 'center', marginBottom: '0.5rem'
            }}>
              <span style={{ color: '#4ade80', fontSize: '0.9rem' }}>
                Note saved
              </span>
              <SentimentBadge
                label={savedNote.sentimentLabel}
                score={savedNote.sentimentScore}
              />
              {savedNote.usefulnessScore !== null && (
                <span style={{
                  color: '#fbbf24', fontSize: '0.8rem',
                  background: '#2a2a1a', padding: '2px 8px',
                  borderRadius: '10px', border: '1px solid #fbbf2440'
                }}>
                  {Math.round(savedNote.usefulnessScore * 100)}% useful
                </span>
              )}
            </div>
            <p style={{ color: '#ccc', fontSize: '0.85rem', margin: 0 }}>
              {savedNote.content}
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

const pageStyle: React.CSSProperties = {
  maxWidth: '720px', margin: '0 auto', padding: '2rem 1rem'
};
const backBtn: React.CSSProperties = {
  background: 'transparent', border: 'none',
  color: '#7c83fd', cursor: 'pointer',
  fontSize: '0.9rem', marginBottom: '1.5rem', padding: 0
};
const articleCard: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '12px',
  padding: '2rem', marginBottom: '1.5rem',
  border: '1px solid #2a2a4a'
};
const titleStyle: React.CSSProperties = {
  color: '#e2e8f0', fontSize: '1.4rem',
  fontWeight: 600, lineHeight: 1.4, margin: '0 0 1.5rem'
};
const scoreSection: React.CSSProperties = {
  display: 'flex', flexDirection: 'column', gap: '0.5rem'
};
const scoreBar: React.CSSProperties = {
  height: '6px', background: '#2a2a4a',
  borderRadius: '3px', overflow: 'hidden'
};
const scoreFill: React.CSSProperties = {
  height: '100%', background: '#7c83fd',
  borderRadius: '3px'
};
const noteSection: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '12px',
  padding: '2rem', border: '1px solid #2a2a4a'
};
const textarea: React.CSSProperties = {
  width: '100%', background: '#0f0f1a',
  border: '1px solid #333', borderRadius: '8px',
  color: '#e2e8f0', padding: '12px',
  fontSize: '0.9rem', resize: 'vertical',
  boxSizing: 'border-box', marginBottom: '1rem',
  fontFamily: 'inherit'
};
const saveBtn: React.CSSProperties = {
  background: '#7c83fd', color: '#fff',
  border: 'none', borderRadius: '8px',
  padding: '10px 1.5rem', cursor: 'pointer',
  fontSize: '0.9rem', fontWeight: 600,
  display: 'flex', alignItems: 'center', gap: '0.5rem'
};
const noteResult: React.CSSProperties = {
  marginTop: '1.25rem', padding: '1rem',
  background: '#0f0f1a', borderRadius: '8px',
  border: '1px solid #2a2a4a'
};

export default ArticleDetailPage;
