import { httpClient } from './httpClient'

type AuthRequest = {
  email: string
  password: string
}

export type AuthenticatedUserResponse = {
  userId: string
  email: string
}

export type LoginResponse = {
  accessToken: string
  expiresAt: string
}

export type SignupResponse = LoginResponse
export type ForgotPasswordResponse = {
  message: string
}

export function login(request: AuthRequest, signal?: AbortSignal) {
  return httpClient.post<LoginResponse>('/auth/login', request, { signal })
}

export function signup(request: AuthRequest, signal?: AbortSignal) {
  return httpClient.post<SignupResponse>('/auth/signup', request, { signal })
}

export function getAuthenticatedUser(signal?: AbortSignal) {
  return httpClient.get<AuthenticatedUserResponse>('/me', { signal })
}

export function requestPasswordReset(email: string, signal?: AbortSignal) {
  return httpClient.post<ForgotPasswordResponse>('/auth/forgot-password', { email }, { signal })
}
