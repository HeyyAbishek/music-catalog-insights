'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/components/auth-provider';

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    // Only redirect if loading has finished and the user is NOT authenticated
    if (!loading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isAuthenticated, loading, router]);

  // Show a loading state while checking localStorage for the token
  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center py-12">
        <p className="text-sm text-muted-foreground animate-pulse">
          Checking authentication...
        </p>
      </div>
    );
  }

  // Prevent flash of protected content before redirect completes
  if (!isAuthenticated) {
    return null;
  }

  return <>{children}</>;
}