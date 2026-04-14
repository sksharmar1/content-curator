import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import NavBar from './components/common/NavBar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import FeedPage from './pages/FeedPage';
import ArticleDetailPage from './pages/ArticleDetailPage';
import ProfilePage from './pages/ProfilePage';
import DashboardPage from './pages/DashboardPage';
import FeedManagementPage from './pages/FeedManagementPage';

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return (
    <div style={{ minHeight: '100vh', background: '#0f0f1a' }}>
      {isAuthenticated && <NavBar />}
      {children}
    </div>
  );
};

const AppRoutes: React.FC = () => (
  <AppLayout>
    <Routes>
      <Route path="/login"    element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/feed" element={
        <PrivateRoute><FeedPage /></PrivateRoute>
      } />
      <Route path="/article/:id" element={
        <PrivateRoute><ArticleDetailPage /></PrivateRoute>
      } />
      <Route path="/profile" element={
        <PrivateRoute><ProfilePage /></PrivateRoute>
      } />
      <Route path="/dashboard" element={
        <PrivateRoute><DashboardPage /></PrivateRoute>
      } />
      <Route path="/feeds" element={
        <PrivateRoute><FeedManagementPage /></PrivateRoute>
      } />
      <Route path="*" element={<Navigate to="/feed" replace />} />
    </Routes>
  </AppLayout>
);

const App: React.FC = () => (
  <BrowserRouter>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </BrowserRouter>
);

export default App;
