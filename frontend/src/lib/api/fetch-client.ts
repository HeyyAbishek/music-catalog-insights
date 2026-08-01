const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'https://music-catalog-insights-production.up.railway.app';

// Normalize base URL to ensure it includes /api
const normalizedBaseUrl = API_BASE_URL.replace(/\/$/, '');
const BASE_API_URL = normalizedBaseUrl.endsWith('/api')
  ? normalizedBaseUrl
  : `${normalizedBaseUrl}/api`;

type FetchClientOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | null | Record<string, unknown>;
  query?: Record<string, string | number | boolean | null | undefined>;
};

function buildUrl(
  path: string,
  query?: FetchClientOptions["query"],
): string {
  // Ensure path starts with a single slash
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  const url = new URL(`${BASE_API_URL}${cleanPath}`);

  if (!query) {
    return url.toString();
  }

  Object.entries(query).forEach(([key, value]) => {
    if (value === null || value === undefined) {
      return;
    }

    url.searchParams.set(key, String(value));
  });

  return url.toString();
}

export async function fetchClient<T>(
  path: string,
  options: FetchClientOptions = {},
): Promise<T> {
  const { body, headers, query, ...init } = options;

  // 1. Retrieve JWT Token from localStorage (Browser only)
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;

  const isJsonBody =
    body !== null &&
    body !== undefined &&
    typeof body === "object" &&
    !(body instanceof FormData) &&
    !(body instanceof URLSearchParams) &&
    !(body instanceof Blob) &&
    !(body instanceof ArrayBuffer);

  const response = await fetch(buildUrl(path, query), {
    ...init,
    headers: {
      ...(isJsonBody ? { "Content-Type": "application/json" } : {}),
      ...(token ? { "Authorization": `Bearer ${token}` } : {}), // Auto-attach Bearer token
      ...headers,
    },
    body: isJsonBody ? JSON.stringify(body) : (body as BodyInit | null | undefined),
  });

  // 2. Handle expired/missing token automatically
  if (response.status === 401) {
    if (typeof window !== 'undefined') {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    throw new Error('Unauthorized. Redirecting to login...');
  }

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";

  if (contentType.includes("application/json")) {
    return (await response.json()) as T;
  }

  return (await response.text()) as T;
}