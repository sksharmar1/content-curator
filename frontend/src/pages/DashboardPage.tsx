import React, { useEffect, useState } from 'react';
import { api } from '../api/client';
import { Dashboard } from '../types';
import {
  BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts';
import SentimentBadge from '../components/common/SentimentBadge';
import Spinner from '../components/common/Spinner';

const COLORS = ['#7c83fd', '#38bdf8', '#4ade80', '#fbbf24', '#f87171'];

const DashboardPage: React.FC = () => {
  const [data, setData]   = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.analytics.getDashboard()
      .then(res => setData(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div style={center}><Spinner size={48} /></div>
  );

  if (!data) return (
    <div style={center}>
      <p style={{ color: '#888' }}>Failed to load dashboard</p>
    </div>
  );

  const categoryData = Object.entries(data.categoryBreakdown)
    .map(([name, value]) => ({ name, value }));

  const sentimentColor = data.averageSentiment > 0.2 ? '#4ade80'
    : data.averageSentiment < -0.2 ? '#f87171' : '#fbbf24';

  return (
    <div style={pageStyle}>
      <h2 style={{ color: '#fff', marginBottom: '1.5rem' }}>Progress Dashboard</h2>

      {/* Stat cards */}
      <div style={statGrid}>
        <div style={statCard}>
          <p style={statLabel}>Articles read</p>
          <p style={statValue}>{data.totalArticlesRead}</p>
        </div>
        <div style={statCard}>
          <p style={statLabel}>Notes saved</p>
          <p style={statValue}>{data.totalNotes}</p>
        </div>
        <div style={statCard}>
          <p style={statLabel}>Avg sentiment</p>
          <p style={{ ...statValue, color: sentimentColor }}>
            {data.averageSentiment.toFixed(2)}
          </p>
        </div>
        <div style={statCard}>
          <p style={statLabel}>Avg usefulness</p>
          <p style={statValue}>
            {data.averageUsefulness > 0
              ? `${Math.round(data.averageUsefulness * 100)}%`
              : '—'}
          </p>
        </div>
        <div style={statCard}>
          <p style={statLabel}>Days to cert</p>
          <p style={statValue}>
            {data.predictedDaysToCert ?? '—'}
          </p>
        </div>
      </div>

      {/* Category breakdown */}
      {categoryData.length > 0 && (
        <div style={chartCard}>
          <h3 style={chartTitle}>Reading by category</h3>
          <div style={{ display: 'flex', gap: '2rem', flexWrap: 'wrap' }}>
            <ResponsiveContainer width="50%" height={200}>
              <BarChart data={categoryData}>
                <XAxis dataKey="name" stroke="#888" tick={{ fill: '#888', fontSize: 12 }} />
                <YAxis stroke="#888" tick={{ fill: '#888', fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ background: '#1a1a2e', border: '1px solid #2a2a4a' }}
                  labelStyle={{ color: '#e2e8f0' }}
                />
                <Bar dataKey="value" fill="#7c83fd" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
            <ResponsiveContainer width="40%" height={200}>
              <PieChart>
                <Pie data={categoryData} dataKey="value"
                  nameKey="name" cx="50%" cy="50%" outerRadius={80}>
                  {categoryData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Legend
                  formatter={(v) => <span style={{ color: '#ccc' }}>{v}</span>}
                />
                <Tooltip
                  contentStyle={{ background: '#1a1a2e', border: '1px solid #2a2a4a' }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}

      {/* Recent reads */}
      {data.recentReads.length > 0 && (
        <div style={chartCard}>
          <h3 style={chartTitle}>Recent reads</h3>
          {data.recentReads.map(r => (
            <div key={r.articleId} style={readRow}>
              <span style={{ color: '#e2e8f0', fontSize: '0.9rem', flex: 1 }}>
                {r.articleTitle}
              </span>
              <span style={{ color: '#666', fontSize: '0.75rem' }}>
                {new Date(r.readAt).toLocaleDateString()}
              </span>
            </div>
          ))}
        </div>
      )}

      {/* Notes with sentiment */}
      {data.notes.length > 0 && (
        <div style={chartCard}>
          <h3 style={chartTitle}>Your notes</h3>
          {data.notes.map(n => (
            <div key={n.id} style={noteRow}>
              <div style={{
                display: 'flex', gap: '0.5rem',
                alignItems: 'center', marginBottom: '0.4rem'
              }}>
                <SentimentBadge label={n.sentimentLabel} score={n.sentimentScore} />
                {n.usefulnessScore !== null && (
                  <span style={usefulBadge}>
                    {Math.round(n.usefulnessScore * 100)}% useful
                  </span>
                )}
              </div>
              <p style={{ color: '#ccc', fontSize: '0.85rem', margin: 0 }}>
                {n.content}
              </p>
            </div>
          ))}
        </div>
      )}

      {data.totalArticlesRead === 0 && data.totalNotes === 0 && (
        <div style={emptyState}>
          <p style={{ color: '#888' }}>No activity yet.</p>
          <p style={{ color: '#666', fontSize: '0.85rem' }}>
            Start reading articles from your feed to see progress here.
          </p>
        </div>
      )}
    </div>
  );
};

const pageStyle: React.CSSProperties = {
  maxWidth: '900px', margin: '0 auto', padding: '2rem 1rem'
};
const center: React.CSSProperties = {
  display: 'flex', justifyContent: 'center',
  alignItems: 'center', height: '60vh'
};
const statGrid: React.CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
  gap: '1rem', marginBottom: '1.5rem'
};
const statCard: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '10px',
  padding: '1.25rem', border: '1px solid #2a2a4a',
  textAlign: 'center'
};
const statLabel: React.CSSProperties = {
  color: '#888', fontSize: '0.8rem',
  margin: '0 0 0.5rem', textTransform: 'uppercase',
  letterSpacing: '0.05em'
};
const statValue: React.CSSProperties = {
  color: '#7c83fd', fontSize: '2rem',
  fontWeight: 700, margin: 0
};
const chartCard: React.CSSProperties = {
  background: '#1a1a2e', borderRadius: '12px',
  padding: '1.5rem', marginBottom: '1.5rem',
  border: '1px solid #2a2a4a'
};
const chartTitle: React.CSSProperties = {
  color: '#e2e8f0', marginBottom: '1rem', fontSize: '1rem'
};
const readRow: React.CSSProperties = {
  display: 'flex', justifyContent: 'space-between',
  alignItems: 'center', padding: '0.6rem 0',
  borderBottom: '1px solid #2a2a4a'
};
const noteRow: React.CSSProperties = {
  padding: '0.75rem 0', borderBottom: '1px solid #2a2a4a'
};
const usefulBadge: React.CSSProperties = {
  color: '#fbbf24', fontSize: '0.75rem',
  background: '#2a2a1a', padding: '2px 8px',
  borderRadius: '10px', border: '1px solid #fbbf2440'
};
const emptyState: React.CSSProperties = {
  textAlign: 'center', padding: '3rem'
};

export default DashboardPage;
