import React, { createContext, useContext, useState, useCallback } from 'react';
import { api, setToken, clearToken } from '../api/client';
import { User } from '../types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, displayName: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  const login = useCallback(async (email: string, password: string) => {
    const res = await api.auth.login(email, password);
    setToken(res.data.token);
    const userRes = await api.user.getMe();
    setUser(userRes.data);
  }, []);

  const register = useCallback(async (
    email: string, password: string, displayName: string
  ) => {
    await api.auth.register(email, password, displayName);
    await login(email, password);
  }, [login]);

  const logout = useCallback(() => {
    clearToken();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{
      user, isAuthenticated: !!user, login, register, logout
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
