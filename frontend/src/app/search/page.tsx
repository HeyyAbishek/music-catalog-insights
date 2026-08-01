'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { axiosClient } from '@/lib/api';

interface SearchResultItem {
  id?: number | string;
  externalId?: string;
  title: string;
  artistName: string;
  genre?: string;
  releaseDate?: string;
  imageUrl?: string;
}

const PAGE_SIZE = 10;

export default function SearchPage() {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [results, setResults] = useState<SearchResultItem[]>([]);
  
  // Pagination State
  const [page, setPage] = useState(0); // 0-based for Spring Boot Pageable
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // UX States
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [savedIds, setSavedIds] = useState<Set<string | number>>(new Set());
  const [savingId, setSavingId] = useState<string | number | null>(null);

  // ----------------------------------------------------
  // 1. DEBOUNCE LOGIC
  // ----------------------------------------------------
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedQuery(query.trim());
      setPage(0); // Reset back to page 0 whenever query changes
    }, 400);

    return () => clearTimeout(handler);
  }, [query]);

  // ----------------------------------------------------
  // 2. FETCH SEARCH RESULTS
  // ----------------------------------------------------
  const fetchSearchResults = useCallback(async () => {
    if (!debouncedQuery) {
      setResults([]);
      setTotalPages(0);
      setTotalElements(0);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Endpoint handles Spring Boot Pageable params: ?q=...&page=0&size=10
      const response = await axiosClient.get('/albums/search', {
        params: {
          q: debouncedQuery,
          page,
          size: PAGE_SIZE,
        },
      });

      const data = response.data;

      // Handle Spring Boot Page<T> or standard list responses
      if (data.content) {
        setResults(data.content);
        setTotalPages(data.totalPages || 0);
        setTotalElements(data.totalElements || 0);
      } else if (Array.isArray(data)) {
        setResults(data);
        setTotalPages(Math.ceil(data.length / PAGE_SIZE));
        setTotalElements(data.length);
      } else {
        setResults(data.results || []);
        setTotalPages(data.totalPages || 1);
        setTotalElements(data.totalElements || (data.results?.length || 0));
      }
    } catch (err: any) {
      console.error('Failed to search catalog:', err);
      setError('Failed to fetch search results. Please check your backend connection.');
    } finally {
      setLoading(false);
    }
  }, [debouncedQuery, page]);

  useEffect(() => {
    fetchSearchResults();
  }, [fetchSearchResults]);

  // ----------------------------------------------------
  // 3. SAVE ALBUM TO USER LIBRARY
  // ----------------------------------------------------
  const handleSaveToLibrary = async (item: SearchResultItem) => {
    const itemId = item.id || item.externalId;
    if (!itemId) return;

    setSavingId(itemId);
    try {
      await axiosClient.post('/albums', {
        title: item.title,
        artistName: item.artistName,
        genre: item.genre || 'General',
        releaseDate: item.releaseDate,
        imageUrl: item.imageUrl,
        userRating: 5, // Default rating on quick save
      });

      setSavedIds((prev) => new Set(prev).add(itemId));
    } catch (err: any) {
      console.error('Failed to save release to library:', err);
      alert('Could not save album to library.');
    } finally {
      setSavingId(null);
    }
  };

  return (
    <div className="container max-w-screen-xl px-4 py-8 space-y-8">
      {/* Search Header */}
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Live Catalog Search</h1>
        <p className="text-muted-foreground text-sm">
          Type to instantly search across artists, titles, and genres.
        </p>
      </div>

      {/* --- LIVE SEARCH BAR --- */}
      <div className="relative max-w-2xl">
        <div className="absolute inset-y-0 left-0 flex items-center pl-4 pointer-events-none text-muted-foreground">
          🔍
        </div>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by artist, album, or genre..."
          className="w-full rounded-xl border border-border bg-card py-3.5 pl-11 pr-10 text-sm font-medium text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-sm transition-all"
        />
        {query && (
          <button
            onClick={() => setQuery('')}
            className="absolute inset-y-0 right-0 flex items-center pr-3 text-muted-foreground hover:text-foreground text-xs"
          >
            ✕
          </button>
        )}
      </div>

      {/* --- STATUS & RESULTS COUNTER --- */}
      <div className="flex items-center justify-between text-xs text-muted-foreground">
        {loading ? (
          <span className="flex items-center gap-2 text-indigo-400 font-medium">
            <span className="animate-spin">⏳</span> Searching catalog...
          </span>
        ) : debouncedQuery ? (
          <span>
            Found <strong className="text-foreground">{totalElements}</strong> results for &quot;{debouncedQuery}&quot;
          </span>
        ) : (
          <span>Enter a search term above to begin.</span>
        )}
      </div>

      {error && (
        <div className="rounded-xl border border-destructive/50 bg-destructive/10 p-4 text-xs font-semibold text-destructive">
          {error}
        </div>
      )}

      {/* --- RESULTS GRID --- */}
      {!loading && debouncedQuery && results.length === 0 && !error && (
        <div className="rounded-xl border border-dashed border-border bg-card p-12 text-center text-muted-foreground space-y-2">
          <p className="font-semibold text-foreground">No matches found</p>
          <p className="text-xs">Try searching for a different keyword or genre.</p>
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {results.map((item, index) => {
          const itemId = item.id || item.externalId || index;
          const isSaved = savedIds.has(itemId);
          const isSaving = savingId === itemId;

          return (
            <div
              key={itemId}
              className="flex flex-col justify-between rounded-xl border border-border bg-card p-5 shadow-sm transition-all hover:border-indigo-500/50 hover:shadow-md"
            >
              <div className="space-y-3">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-bold text-foreground text-base line-clamp-1">{item.title}</h3>
                    <p className="text-xs text-muted-foreground font-medium">{item.artistName}</p>
                  </div>
                  {item.genre && (
                    <span className="rounded-md bg-indigo-500/10 px-2 py-0.5 text-[10px] font-bold text-indigo-400 border border-indigo-500/20">
                      {item.genre}
                    </span>
                  )}
                </div>

                {item.releaseDate && (
                  <p className="text-[11px] text-muted-foreground">
                    Released: {new Date(item.releaseDate).getFullYear() || item.releaseDate}
                  </p>
                )}
              </div>

              <button
                onClick={() => handleSaveToLibrary(item)}
                disabled={isSaved || isSaving}
                className={`mt-4 w-full rounded-lg py-2 text-xs font-semibold transition-all ${
                  isSaved
                    ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 cursor-default'
                    : 'bg-indigo-600 text-white hover:bg-indigo-500 disabled:opacity-50 cursor-pointer'
                }`}
              >
                {isSaving ? 'Saving...' : isSaved ? '✓ Saved to Library' : '+ Save to Library'}
              </button>
            </div>
          );
        })}
      </div>

      {/* --- PAGINATION FOOTER --- */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-border pt-6">
          <button
            onClick={() => setPage((p) => Math.max(p - 1, 0))}
            disabled={page === 0 || loading}
            className="rounded-lg border border-border bg-card px-4 py-2 text-xs font-semibold text-foreground hover:bg-accent disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
          >
            ← Previous
          </button>

          <span className="text-xs text-muted-foreground font-medium">
            Page <strong className="text-foreground">{page + 1}</strong> of <strong className="text-foreground">{totalPages}</strong>
          </span>

          <button
            onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
            disabled={page >= totalPages - 1 || loading}
            className="rounded-lg border border-border bg-card px-4 py-2 text-xs font-semibold text-foreground hover:bg-accent disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
          >
            Next →
          </button>
        </div>
      )}
    </div>
  );
}