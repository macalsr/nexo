import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError, httpClient } from './httpClient'
import { clearAccessToken, getAccessToken, storeAccessToken } from '../auth/tokenStorage'

describe('httpClient refresh flow', () => {
  beforeEach(() => {
    clearAccessToken()
    vi.restoreAllMocks()
  })

  it('refreshes access token once and retries the protected request', async () => {
    storeAccessToken('expired-token', true)

    const fetchMock = vi
      .spyOn(window, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ message: 'Access token expired' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        })
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ accessToken: 'new-token', expiresAt: '2026-03-08T00:00:00Z' }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ email: 'person@example.com' }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      )

    const response = await httpClient.get<{ email: string }>('/me')

    expect(response.email).toBe('person@example.com')
    expect(getAccessToken()).toBe('new-token')
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(fetchMock.mock.calls[1][0]).toBe('http://localhost:8080/auth/refresh')
  })

  it('clears local token and throws when refresh fails', async () => {
    storeAccessToken('expired-token', false)

    vi.spyOn(window, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ message: 'Access token expired' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        })
      )
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ message: 'Invalid refresh token' }), {
          status: 401,
          headers: { 'Content-Type': 'application/json' },
        })
      )

    await expect(httpClient.get('/me')).rejects.toBeInstanceOf(ApiError)
    expect(getAccessToken()).toBeNull()
  })
})
