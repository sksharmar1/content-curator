import React from 'react';

const Spinner: React.FC<{ size?: number }> = ({ size = 32 }) => (
  <div style={{
    width: size, height: size,
    border: `3px solid #333`,
    borderTop: `3px solid #7c83fd`,
    borderRadius: '50%',
    animation: 'spin 0.8s linear infinite',
  }}>
    <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
  </div>
);

export default Spinner;
