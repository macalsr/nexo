import { httpClient } from './httpClient'

type LoginRequest = {
  email: string
  password: string
}

export type LoginResponse = {
  accessToken: string
  expiresAt: string
}

export function login(request: LoginRequest, signal?: AbortSignal) {
  return httpClient.post<LoginResponse>('/auth/login', request, { signal })
}
