import { httpClient } from './httpClient'

type AuthRequest = {
  email: string
  password: string
}

export type AuthenticatedUserResponse = {
  userId: string
  email: string
  emailVerified: boolean
}

export type LoginResponse = {
  accessToken: string
  expiresAt: string
}

export type SignupResponse = LoginResponse
export type ForgotPasswordResponse = {
  message: string
}

export type ResendVerificationResponse = ForgotPasswordResponse

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

export function resendVerificationEmail(signal?: AbortSignal) {
  return httpClient.post<ResendVerificationResponse>('/auth/resend-verification', undefined, { signal })
}

export function verifyEmail(token: string, signal?: AbortSignal) {
  return httpClient.post<void>('/auth/verify-email', { token }, { signal })
}
