import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { login } from '../api/authApi'
import { getAccessToken, storeAccessToken } from '../auth/tokenStorage'
import { LoginPage } from './LoginPage'

vi.mock('../api/authApi', () => ({
  login: vi.fn(),
}))

const loginMock = vi.mocked(login)

function renderLoginPage(initialPath = '/login') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/app" element={<div>Protected app content</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    window.sessionStorage.clear()
    loginMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('keeps the login screen accessible even when a token is already stored', () => {
    storeAccessToken('stale-token', true)

    renderLoginPage()

    expect(screen.getByRole('heading', { name: 'Nexo Finance' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument()
    expect(screen.queryByText('Protected app content')).not.toBeInTheDocument()
  })

  it('shows the remember me toggle enabled by default', () => {
    renderLoginPage()

    expect(screen.getByRole('checkbox', { name: 'Remember me' })).toBeChecked()
  })

  it('stores the session in sessionStorage when remember me is disabled', async () => {
    loginMock.mockResolvedValue({
      accessToken: 'session-only-token',
      expiresAt: '2026-03-07T18:00:00Z',
    })

    renderLoginPage()

    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'user@example.com' },
    })
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'Password123!' },
    })
    fireEvent.click(screen.getByRole('checkbox', { name: 'Remember me' }))
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }))

    await waitFor(() => {
      expect(screen.getByText('Protected app content')).toBeInTheDocument()
    })

    expect(window.sessionStorage.getItem('nexo.accessToken')).toBe('session-only-token')
    expect(window.localStorage.getItem('nexo.accessToken')).toBeNull()
    expect(getAccessToken()).toBe('session-only-token')
  })
})
