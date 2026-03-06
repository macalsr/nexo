import { env } from '../config/env'
import { getAccessToken } from '../auth/tokenStorage'

type Primitive = string | number | boolean | null
type JsonValue = Primitive | JsonValue[] | { [key: string]: JsonValue }

export class ApiError extends Error {
  readonly status: number
  readonly url: string
  readonly details?: JsonValue

  constructor(message: string, status: number, url: string, details?: JsonValue) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.url = url
    this.details = details
  }
}

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: JsonValue
  headers?: HeadersInit
  signal?: AbortSignal
}

function buildUrl(path: string) {
  const normalizedBaseUrl = env.apiBaseUrl.replace(/\/+$/, '')
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  return `${normalizedBaseUrl}${normalizedPath}`
}

function buildHeaders(customHeaders?: HeadersInit) {
  const headers = new Headers(customHeaders)

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const accessToken = getAccessToken()

  if (accessToken && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${accessToken}`)
  }

  return headers
}

async function parseResponseBody(response: Response): Promise<JsonValue | undefined> {
  if (response.status === 204) {
    return undefined
  }

  const contentType = response.headers.get('content-type') ?? ''

  if (!contentType.includes('application/json')) {
    return undefined
  }

  return (await response.json()) as JsonValue
}

export async function request<TResponse>(path: string, options: RequestOptions = {}) {
  const url = buildUrl(path)
  const headers = buildHeaders(options.headers)

  const response = await fetch(url, {
    method: options.method ?? 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
    signal: options.signal,
  })

  const responseBody = await parseResponseBody(response)

  if (!response.ok) {
    throw new ApiError(
      `Request failed with status ${response.status}`,
      response.status,
      url,
      responseBody
    )
  }

  return responseBody as TResponse
}

export const httpClient = {
  get: <TResponse>(path: string, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<TResponse>(path, { ...options, method: 'GET' }),
  post: <TResponse>(path: string, body?: JsonValue, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<TResponse>(path, { ...options, method: 'POST', body }),
  put: <TResponse>(path: string, body?: JsonValue, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<TResponse>(path, { ...options, method: 'PUT', body }),
  patch: <TResponse>(path: string, body?: JsonValue, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<TResponse>(path, { ...options, method: 'PATCH', body }),
  delete: <TResponse>(path: string, options?: Omit<RequestOptions, 'method' | 'body'>) =>
    request<TResponse>(path, { ...options, method: 'DELETE' }),
} as const
