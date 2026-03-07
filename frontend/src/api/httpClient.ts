import { env } from '../config/env'
import { clearAccessToken, getAccessToken, replaceAccessToken } from '../auth/tokenStorage'

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

function canAttemptRefresh(path: string) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return !normalizedPath.startsWith('/auth/')
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

async function executeRequest(path: string, options: RequestOptions = {}) {
  const url = buildUrl(path)
  const headers = buildHeaders(options.headers)

  return fetch(url, {
    method: options.method ?? 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
    signal: options.signal,
    credentials: 'include',
  })
}

async function tryRefreshAccessToken() {
  const refreshUrl = buildUrl('/auth/refresh')
  const response = await fetch(refreshUrl, {
    method: 'POST',
    credentials: 'include',
    headers: new Headers({ Accept: 'application/json' }),
  })

  if (!response.ok) {
    return false
  }

  const refreshed = (await parseResponseBody(response)) as { accessToken?: string } | undefined
  if (!refreshed?.accessToken) {
    return false
  }

  replaceAccessToken(refreshed.accessToken)
  return true
}

export async function request<TResponse>(path: string, options: RequestOptions = {}) {
  const url = buildUrl(path)
  let response = await executeRequest(path, options)

  if (response.status === 401 && getAccessToken() && canAttemptRefresh(path)) {
    const refreshed = await tryRefreshAccessToken()
    if (refreshed) {
      response = await executeRequest(path, options)
    } else {
      clearAccessToken()
    }
  }

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
