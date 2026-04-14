import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const NavBar: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={{
      background: '#1a1a2e', color: '#fff',
      padding: '0 2rem', height: '56px',
      display: 'flex', alignItems: 'center',
      justifyContent: 'space-between',
      position: 'sticky', top: 0, zIndex: 100,
      boxShadow: '0 2px 8px rgba(0,0,0,0.3)'
    }}>
      <Link to="/feed" style={{
        color: '#7c83fd', fontWeight: 700,
        fontSize: '1.2rem', textDecoration: 'none'
      }}>
        ContentCurator
      </Link>
      <div style={{ display: 'flex', gap: '1.5rem', alignItems: 'center' }}>
        <Link to="/feed" style={navLink}>Feed</Link>
        <Link to="/dashboard" style={navLink}>Dashboard</Link>
        <Link to="/profile" style={navLink}>Profile</Link>
        <Link to="/feeds" style={navLink}>Feeds</Link>
        <span style={{ color: '#aaa', fontSize: '0.85rem' }}>
          {user?.displayName}
        </span>
        <button onClick={handleLogout} style={{
          background: 'transparent', border: '1px solid #555',
          color: '#ccc', padding: '4px 12px', borderRadius: '6px',
          cursor: 'pointer', fontSize: '0.85rem'
        }}>
          Logout
        </button>
      </div>
    </nav>
  );
};

const navLink: React.CSSProperties = {
  color: '#ccc', textDecoration: 'none', fontSize: '0.9rem'
};

export default NavBar;
