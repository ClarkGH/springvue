import { useAuthStore } from '@/stores/auth'

const BASE = '/api'

function getHeaders(): HeadersInit {
  const auth = useAuthStore()
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  if (auth.token) {
    headers['Authorization'] = `Bearer ${auth.token}`
  }
  return headers
}

export async function apiFetch(path: string, opts: RequestInit = {}): Promise<Response> {
  const url = path.startsWith('http') ? path : `${BASE}${path.startsWith('/') ? path : '/' + path}`
  const headers = getHeaders()
  return fetch(url, {
    ...opts,
    headers: { ...headers, ...(opts.headers as Record<string, string>) },
  })
}

export async function apiJson<T>(path: string, opts: RequestInit = {}): Promise<T> {
  const res = await apiFetch(path, opts)
  const text = await res.text()
  if (!res.ok) {
    throw new Error(text || `HTTP ${res.status}`)
  }
  if (!text) return undefined as T
  return JSON.parse(text) as T
}
