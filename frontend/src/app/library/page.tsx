'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { axiosClient } from '@/lib/api';
import { ProtectedRoute } from '@/components/protected-route';

interface Album {
  id: number;
  title: string;
  artist: string;
  genre: string;
  releaseYear: number;
  artworkUrl?: string;
  userRating?: number;
}

export default function LibraryPage() {
  const [albums, setAlbums] = useState<Album[]>([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchLibrary = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axiosClient.get('/albums');
      setAlbums(Array.isArray(response.data) ? response.data : []);
    } catch (err: any) {
      console.error('Failed to fetch library:', err);
      setError('Failed to load your library. Make sure the backend server is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLibrary();
  }, []);

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to remove this album from your library?')) {
      return;
    }

    setDeletingId(id);
    try {
      await axiosClient.delete(`/albums/${id}`);
      setAlbums((prev) => prev.filter((album) => album.id !== id));
    } catch (err: any) {
      console.error('Failed to delete album:', err);
      alert('Failed to remove album. Please try again.');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <ProtectedRoute>
      <div className="container max-w-screen-2xl px-4 py-8">
        <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold tracking-tight">My Library</h1>
            <p className="text-muted-foreground">
              Manage your saved releases, track ratings, and collection details.
            </p>
          </div>
          <Link
            href="/search"
            className="inline-flex h-10 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90"
          >
            + Add New Albums
          </Link>
        </div>

        {error && (
          <div className="mb-6 rounded-md bg-destructive/15 p-4 text-sm text-destructive border border-destructive/30">
            {error}
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {[...Array(4)].map((_, i) => (
              <div
                key={i}
                className="h-72 rounded-xl border border-border bg-card/50 p-4 animate-pulse"
              />
            ))}
          </div>
        ) : albums.length === 0 ? (
          <div className="flex min-h-[300px] flex-col items-center justify-center rounded-2xl border border-dashed border-border p-8 text-center">
            <div className="mb-3 text-4xl">🎧</div>
            <h3 className="text-lg font-semibold">Your library is empty</h3>
            <p className="mb-4 max-w-sm text-sm text-muted-foreground">
              Start searching for artists and albums in the catalog to build your collection.
            </p>
            <Link
              href="/search"
              className="rounded-md bg-secondary px-4 py-2 text-sm font-medium text-secondary-foreground hover:bg-secondary/80"
            >
              Explore Catalog Search
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {albums.map((album) => (
              <div
                key={album.id}
                className="group relative flex flex-col justify-between overflow-hidden rounded-xl border border-border bg-card p-4 transition-all hover:border-muted-foreground/30 hover:shadow-md"
              >
                <div className="space-y-3">
                  {album.artworkUrl ? (
                    <img
                      src={album.artworkUrl.replace('100x100bb', '300x300bb')}
                      alt={album.title}
                      className="h-48 w-full rounded-lg object-cover"
                    />
                  ) : (
                    <div className="flex h-48 w-full items-center justify-center rounded-lg bg-muted text-muted-foreground">
                      🎵 No Artwork
                    </div>
                  )}

                  <div>
                    <h3 className="font-semibold tracking-tight text-foreground line-clamp-1">
                      {album.title}
                    </h3>
                    <p className="text-sm text-muted-foreground line-clamp-1">{album.artist}</p>
                  </div>

                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <span className="rounded bg-secondary px-2 py-0.5 text-secondary-foreground">
                      {album.genre || 'General'}
                    </span>
                    {album.releaseYear && <span>{album.releaseYear}</span>}
                  </div>

                  {album.userRating && (
                    <div className="flex items-center gap-1 text-amber-400 text-sm">
                      {'★'.repeat(album.userRating)}
                      {'☆'.repeat(5 - album.userRating)}
                    </div>
                  )}
                </div>

                <div className="mt-4 pt-3 border-t border-border flex items-center justify-end">
                  <button
                    onClick={() => handleDelete(album.id)}
                    disabled={deletingId === album.id}
                    className="text-xs text-destructive hover:underline disabled:opacity-50"
                  >
                    {deletingId === album.id ? 'Removing...' : 'Remove'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </ProtectedRoute>
  );
}