'use client';

import React, { useEffect, useState, useMemo } from 'react';
import { axiosClient } from '@/lib/api';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  CartesianGrid,
} from 'recharts';

interface Album {
  id: number;
  title: string;
  artistName: string;
  genre: string;
  userRating: number;
  releaseDate?: string;
  createdAt?: string;
}

interface AiSummary {
  persona: string;
  summary: string;
  insights: string[];
  recommendations: string[];
}

// Chart Color Palette
const COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#3b82f6'];

export default function AnalyticsPage() {
  const [albums, setAlbums] = useState<Album[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // AI Insights State
  const [aiSummary, setAiSummary] = useState<AiSummary | null>(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiError, setAiError] = useState<string | null>(null);

  useEffect(() => {
    fetchAnalyticsData();
  }, []);

  const fetchAnalyticsData = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axiosClient.get('/albums');
      const data = response.data.content || response.data.results || response.data;
      setAlbums(Array.isArray(data) ? data : []);
    } catch (err: any) {
      console.error('Failed to load library analytics data:', err);
      setError('Could not load library analytics. Please check your network connection.');
    } finally {
      setLoading(false);
    }
  };

  const generateAiSummary = async () => {
    setAiLoading(true);
    setAiError(null);
    try {
      const response = await axiosClient.get('/analytics/ai-summary');
      setAiSummary(response.data);
    } catch (err: any) {
      console.error('Failed to generate AI summary:', err);
      setAiError('Could not generate AI insights at this time.');
    } finally {
      setAiLoading(false);
    }
  };

  // ----------------------------------------------------
  // DATA PROCESSING FOR CHARTS
  // ----------------------------------------------------

  // 1. Genre Distribution (Bar Chart Data)
  const genreData = useMemo(() => {
    const counts: Record<string, number> = {};
    albums.forEach((album) => {
      const g = album.genre || 'Unknown';
      counts[g] = (counts[g] || 0) + 1;
    });
    return Object.keys(counts).map((genre) => ({
      genre,
      count: counts[genre],
    }));
  }, [albums]);

  // 2. Rating Breakdown (Pie / Donut Chart Data)
  const ratingData = useMemo(() => {
    const counts: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    albums.forEach((album) => {
      const r = album.userRating || 5;
      counts[r] = (counts[r] || 0) + 1;
    });
    return [
      { name: '5 Stars', value: counts[5] },
      { name: '4 Stars', value: counts[4] },
      { name: '3 Stars', value: counts[3] },
      { name: '2 Stars', value: counts[2] },
      { name: '1 Star', value: counts[1] },
    ].filter((item) => item.value > 0);
  }, [albums]);

  // 3. Releases by Year / Era (Horizontal Bar Chart Data)
  const releasesByYearData = useMemo(() => {
    const counts: Record<string, number> = {};
    albums.forEach((album) => {
      const year = album.releaseDate
        ? new Date(album.releaseDate).getFullYear().toString()
        : 'Unknown';
      counts[year] = (counts[year] || 0) + 1;
    });
    return Object.keys(counts)
      .sort()
      .map((year) => ({
        year,
        count: counts[year],
      }));
  }, [albums]);

  // 4. Library Cumulative Growth (Line Chart Data)
  const growthData = useMemo(() => {
    if (albums.length === 0) return [];
    let total = 0;
    return albums.map((_, idx) => {
      total += 1;
      return {
        itemIndex: `Track #${idx + 1}`,
        totalCount: total,
      };
    });
  }, [albums]);

  if (loading) {
    return (
      <div className="container max-w-screen-2xl px-4 py-16 text-center text-muted-foreground">
        Loading analytics engine...
      </div>
    );
  }

  if (error) {
    return (
      <div className="container max-w-screen-2xl px-4 py-16 text-center">
        <p className="text-destructive font-semibold">{error}</p>
        <button
          onClick={fetchAnalyticsData}
          className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm text-primary-foreground"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="container max-w-screen-2xl px-4 py-8 space-y-8">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Catalog Analytics & AI Insights</h1>
        <p className="text-muted-foreground">
          Visual metrics and automated trend analysis across your collection.
        </p>
      </div>

      {/* --- AI TREND SUMMARY CARD --- */}
      <div className="rounded-xl border border-indigo-500/30 bg-indigo-950/20 p-6 space-y-4 shadow-lg">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-indigo-600 text-xl font-bold text-white">
              ✨
            </span>
            <div>
              <h2 className="text-lg font-bold text-foreground">AI Trend Summary</h2>
              <p className="text-xs text-muted-foreground">
                Natural language insights generated from your catalog pattern
              </p>
            </div>
          </div>
          <button
            onClick={generateAiSummary}
            disabled={aiLoading || albums.length === 0}
            className="rounded-lg bg-indigo-600 px-4 py-2 text-xs font-semibold text-white transition-all hover:bg-indigo-500 disabled:opacity-50 cursor-pointer"
          >
            {aiLoading ? 'Analyzing Catalog...' : '✨ Generate AI Insights'}
          </button>
        </div>

        {aiError && (
          <p className="text-xs text-destructive pt-2 border-t border-indigo-500/20">{aiError}</p>
        )}

        {aiSummary && (
          <div className="space-y-4 pt-2 border-t border-indigo-500/20">
            <div className="flex items-center gap-2">
              <span className="text-xs uppercase font-bold tracking-wider text-indigo-400">
                Listener Archetype:
              </span>
              <span className="rounded bg-indigo-500/20 px-2.5 py-0.5 text-xs font-bold text-indigo-300 border border-indigo-500/40">
                {aiSummary.persona}
              </span>
            </div>

            <p className="text-sm text-foreground/90 leading-relaxed font-medium">
              {aiSummary.summary}
            </p>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-2">
              <div className="space-y-2 rounded-lg bg-background/50 p-4 border border-border">
                <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  Key Insights
                </h4>
                <ul className="space-y-1.5 text-xs text-foreground/80">
                  {aiSummary.insights.map((item, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <span className="text-indigo-400">•</span>
                      <span>{item}</span>
                    </li>
                  ))}
                </ul>
              </div>

              <div className="space-y-2 rounded-lg bg-background/50 p-4 border border-border">
                <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground">
                  AI Recommendations
                </h4>
                <ul className="space-y-1.5 text-xs text-foreground/80">
                  {aiSummary.recommendations.map((item, idx) => (
                    <li key={idx} className="flex items-start gap-2">
                      <span className="text-emerald-400">→</span>
                      <span>{item}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* KPI Banners */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-xl border border-border bg-card p-5">
          <p className="text-xs font-medium text-muted-foreground uppercase">Total Saved Releases</p>
          <p className="mt-2 text-3xl font-extrabold text-foreground">{albums.length}</p>
        </div>
        <div className="rounded-xl border border-border bg-card p-5">
          <p className="text-xs font-medium text-muted-foreground uppercase">Top Genre</p>
          <p className="mt-2 text-3xl font-extrabold text-indigo-400">
            {genreData.length > 0
              ? genreData.reduce((prev, curr) => (prev.count > curr.count ? prev : curr)).genre
              : 'N/A'}
          </p>
        </div>
        <div className="rounded-xl border border-border bg-card p-5">
          <p className="text-xs font-medium text-muted-foreground uppercase">Average Rating</p>
          <p className="mt-2 text-3xl font-extrabold text-emerald-400">
            {albums.length > 0
              ? (albums.reduce((acc, a) => acc + (a.userRating || 5), 0) / albums.length).toFixed(1)
              : '0.0'}{' '}
            ★
          </p>
        </div>
      </div>

      {/* Empty State Warning */}
      {albums.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-12 text-center text-muted-foreground">
          No releases saved in your library yet. Save tracks on the Search page to populate analytics charts!
        </div>
      ) : (
        /* 2x2 CHART GRID */
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* CHART 1: BAR CHART (Genres) */}
          <div className="rounded-xl border border-border bg-card p-6 space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-foreground">Genre Distribution</h3>
              <p className="text-xs text-muted-foreground">Breakdown of catalog items by music genre</p>
            </div>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={genreData}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.2} />
                  <XAxis dataKey="genre" stroke="#888888" fontSize={12} />
                  <YAxis stroke="#888888" fontSize={12} allowDecimals={false} />
                  <Tooltip contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', color: '#fff' }} />
                  <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* CHART 2: PIE / DONUT CHART (Ratings) */}
          <div className="rounded-xl border border-border bg-card p-6 space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-foreground">Rating Breakdown</h3>
              <p className="text-xs text-muted-foreground">Proportion of star ratings across collection</p>
            </div>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={ratingData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                    label={({ name, percent }) => `${name} (${((percent ?? 0) * 100).toFixed(0)}%)`}
                  >
                    {ratingData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', color: '#fff' }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* CHART 3: LINE CHART (Library Growth) */}
          <div className="rounded-xl border border-border bg-card p-6 space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-foreground">Library Cumulative Growth</h3>
              <p className="text-xs text-muted-foreground">Progression of total tracks added over time</p>
            </div>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={growthData}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.2} />
                  <XAxis dataKey="itemIndex" stroke="#888888" fontSize={12} />
                  <YAxis stroke="#888888" fontSize={12} allowDecimals={false} />
                  <Tooltip contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', color: '#fff' }} />
                  <Line type="monotone" dataKey="totalCount" stroke="#10b981" strokeWidth={3} dot={{ r: 4 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* CHART 4: HORIZONTAL BAR CHART (Releases by Year) */}
          <div className="rounded-xl border border-border bg-card p-6 space-y-4">
            <div>
              <h3 className="text-lg font-semibold text-foreground">Releases by Year</h3>
              <p className="text-xs text-muted-foreground">Original release date timeline distribution</p>
            </div>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart layout="vertical" data={releasesByYearData}>
                  <CartesianGrid strokeDasharray="3 3" opacity={0.2} />
                  <XAxis type="number" stroke="#888888" fontSize={12} allowDecimals={false} />
                  <YAxis type="category" dataKey="year" stroke="#888888" fontSize={12} />
                  <Tooltip contentStyle={{ backgroundColor: '#1f2937', borderColor: '#374151', color: '#fff' }} />
                  <Bar dataKey="count" fill="#f59e0b" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}