const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || 'https://music-catalog-insights-production.up.railway.app';

type FetchClientOptions = Omit<RequestInit, "body"> & {
  body?: BodyInit | null | Record<string, unknown>;
  query?: Record<string, string | number | boolean | null | undefined>;
};

function buildUrl(
  path: string,
  query?: FetchClientOptions["query"],
): string {
  const url = new URL(path, API_BASE_URL.endsWith("/") ? API_BASE_URL : `${API_BASE_URL}/`);

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
      ...headers,
    },
    body: isJsonBody ? JSON.stringify(body) : (body as BodyInit | null | undefined),
  });

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
