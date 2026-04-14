import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RegisterPage: React.FC = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', displayName: '' });
  const [error, setError]   = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      await register(form.email, form.password, form.displayName);
      navigate('/feed');
    } catch {
      setError('Registration failed. Email may already be in use.');
    } finally {
      setLoading(false);
    }
  };

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }));

  return (
    <div style={container}>
      <div style={card}>
        <h1 style={{ color: '#7c83fd', marginBottom: '0.5rem' }}>Create Account</h1>
        <p style={{ color: '#888', marginBottom: '2rem' }}>Start your learning journey</p>
        <form onSubmit={handleSubmit}>
          <input style={input} placeholder="Display name"
            value={form.displayName} onChange={set('displayName')} required />
          <input style={input} type="email" placeholder="Email"
            value={form.email} onChange={set('email')} required />
          <input style={input} type="password" placeholder="Password (8+ chars)"
            value={form.password} onChange={set('password')} minLength={8} required />
          {error && <p style={{ color: '#f87171', fontSize: '0.85rem' }}>{error}</p>}
          <button style={btn} type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Account'}
          </button>
        </form>
        <p style={{ color: '#888', fontSize: '0.85rem', marginTop: '1rem' }}>
          Have an account? <Link to="/login" style={{ color: '#7c83fd' }}>Sign in</Link>
        </p>
      </div>
    </div>
  );
};

const container: React.CSSProperties = {
  minHeight: '100vh', display: 'flex',
  alignItems: 'center', justifyContent: 'center',
  background: '#0f0f1a'
};
const card: React.CSSProperties = {
  background: '#1a1a2e', padding: '2.5rem',
  borderRadius: '12px', width: '360px',
  textAlign: 'center', border: '1px solid #2a2a4a'
};
const input: React.CSSProperties = {
  width: '100%', padding: '10px 14px',
  marginBottom: '1rem', borderRadius: '8px',
  border: '1px solid #333', background: '#0f0f1a',
  color: '#fff', fontSize: '0.95rem', boxSizing: 'border-box'
};
const btn: React.CSSProperties = {
  width: '100%', padding: '10px',
  background: '#7c83fd', color: '#fff',
  border: 'none', borderRadius: '8px',
  fontSize: '1rem', cursor: 'pointer', fontWeight: 600
};

export default RegisterPage;
