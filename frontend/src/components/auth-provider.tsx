'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { axiosClient } from '@/lib/api';

interface AuthContextType {
  token: string | null;
  username: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  login: (loginData: { username: string; password: string }) => Promise<void>;
  register: (registerData: { username: string; password: string }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [username, setUsername] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    // Rehydrate auth state from localStorage on browser load
    if (typeof window !== 'undefined') {
      const storedToken = localStorage.getItem('token');
      const storedUsername = localStorage.getItem('username');
      if (storedToken && storedUsername) {
        setToken(storedToken);
        setUsername(storedUsername);
      }
    }
    setLoading(false);
  }, []);

  const login = async (loginData: { username: string; password: string }) => {
    const response = await axiosClient.post('/auth/login', loginData);
    const authToken = response.data.jwt || response.data.token;
    const user = response.data.username || loginData.username;

    localStorage.setItem('token', authToken);
    localStorage.setItem('username', user);
    setToken(authToken);
    setUsername(user);
  };

  const register = async (registerData: { username: string; password: string }) => {
    const response = await axiosClient.post('/auth/register', registerData);
    const authToken = response.data.jwt || response.data.token;
    const user = response.data.username || registerData.username;

    if (authToken) {
      localStorage.setItem('token', authToken);
      localStorage.setItem('username', user);
      setToken(authToken);
      setUsername(user);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setToken(null);
    setUsername(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        username,
        isAuthenticated: !!token,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};