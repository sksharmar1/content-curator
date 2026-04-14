import React from 'react';

const COLORS: Record<string, { bg: string; text: string }> = {
  'AI/ML':  { bg: '#1e1b4b', text: '#818cf8' },
  'Cloud':  { bg: '#1a2e3a', text: '#38bdf8' },
  'Java':   { bg: '#1a2e1a', text: '#4ade80' },
  'General':{ bg: '#2a2a2a', text: '#9ca3af' },
};

const CategoryBadge: React.FC<{ category: string }> = ({ category }) => {
  const color = COLORS[category] || COLORS.General;
  return (
    <span style={{
      background: color.bg, color: color.text,
      padding: '2px 8px', borderRadius: '10px',
      fontSize: '0.72rem', fontWeight: 600,
      border: `1px solid ${color.text}40`
    }}>
      {category}
    </span>
  );
};

export default CategoryBadge;
