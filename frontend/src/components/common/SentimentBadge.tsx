import React from 'react';

interface Props {
  label: string | null;
  score: number | null;
}

const SentimentBadge: React.FC<Props> = ({ label, score }) => {
  if (!label) return null;

  const colors: Record<string, { bg: string; text: string }> = {
    POSITIVE: { bg: '#1a3a2a', text: '#4ade80' },
    NEGATIVE: { bg: '#3a1a1a', text: '#f87171' },
    NEUTRAL:  { bg: '#2a2a1a', text: '#fbbf24' },
    PENDING:  { bg: '#1a1a3a', text: '#818cf8' },
  };

  const color = colors[label] || colors.NEUTRAL;

  return (
    <span style={{
      background: color.bg, color: color.text,
      padding: '2px 10px', borderRadius: '12px',
      fontSize: '0.75rem', fontWeight: 600,
      border: `1px solid ${color.text}40`
    }}>
      {label} {score !== null ? `(${score.toFixed(2)})` : ''}
    </span>
  );
};

export default SentimentBadge;
