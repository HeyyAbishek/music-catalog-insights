'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/components/auth-provider';

export function SiteNav() {
  const { isAuthenticated, username, logout } = useAuth();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-14 max-w-screen-2xl items-center justify-between px-4">
        {/* Brand & Main Links */}
        <div className="flex items-center gap-6">
          <Link href="/" className="font-bold text-lg tracking-tight hover:opacity-90">
            🎵 Music Catalog
          </Link>
          <nav className="flex items-center gap-4 text-sm font-medium text-muted-foreground">
            <Link href="/search" className="transition-colors hover:text-foreground">
              Search
            </Link>
            <Link href="/library" className="transition-colors hover:text-foreground">
              Library
            </Link>
            <Link href="/analytics" className="transition-colors hover:text-foreground">
              Analytics
            </Link>
          </nav>
        </div>

        {/* Auth Action Buttons */}
        <div className="flex items-center gap-4">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm font-medium text-muted-foreground">
                Hi, <span className="text-foreground font-semibold">{username}</span>
              </span>
              <button
                onClick={handleLogout}
                className="rounded-md border border-input bg-background px-3 py-1.5 text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground focus:outline-none"
              >
                Logout
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                href="/login"
                className="rounded-md px-3 py-1.5 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground"
              >
                Sign In
              </Link>
              <Link
                href="/register"
                className="rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
              >
                Register
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}