import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { getAuthenticatedUser } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { getAccessToken, storeAccessToken } from '../auth/tokenStorage'
import { ProtectedRoute } from './ProtectedRoute'

vi.mock('../api/authApi', () => ({
  getAuthenticatedUser: vi.fn(),
}))

const getAuthenticatedUserMock = vi.mocked(getAuthenticatedUser)

function renderProtectedRoute(initialPath = '/app') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/login" element={<div>Login page</div>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/app" element={<div>Protected app content</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    window.localStorage.clear()
    getAuthenticatedUserMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('redirects unauthenticated access to /login', () => {
    renderProtectedRoute()

    expect(screen.getByText('Login page')).toBeInTheDocument()
    expect(getAuthenticatedUserMock).not.toHaveBeenCalled()
  })

  it('shows a loading state while validating an existing session', async () => {
    let resolveRequest:
      | ((value: { userId: string; email: string; emailVerified: boolean }) => void)
      | undefined

    getAuthenticatedUserMock.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveRequest = resolve
        })
    )
    storeAccessToken('valid-token', true)

    renderProtectedRoute()

    expect(screen.getByText('Checking your session')).toBeInTheDocument()
    expect(screen.queryByText('Protected app content')).not.toBeInTheDocument()
    expect(getAuthenticatedUserMock).toHaveBeenCalledTimes(1)

    resolveRequest?.({ userId: 'user-1', email: 'user@example.com', emailVerified: false })

    expect(await screen.findByText('Protected app content')).toBeInTheDocument()
  })

  it('clears the token and redirects to /login when /me returns 401', async () => {
    getAuthenticatedUserMock.mockRejectedValue(
      new ApiError('Request failed with status 401', 401, 'http://localhost:8080/me')
    )
    storeAccessToken('expired-token', true)

    renderProtectedRoute()

    await waitFor(() => {
      expect(screen.getByText('Login page')).toBeInTheDocument()
    })

    expect(getAccessToken()).toBeNull()
    expect(screen.queryByText('Protected app content')).not.toBeInTheDocument()
  })
})
