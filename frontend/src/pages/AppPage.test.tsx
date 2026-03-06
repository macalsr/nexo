import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { getAuthenticatedUser, resendVerificationEmail } from '../api/authApi'
import { getHealth } from '../api/healthApi'
import { getAccessToken, storeAccessToken } from '../auth/tokenStorage'
import { AppPage } from './AppPage'

vi.mock('../api/authApi', () => ({
  getAuthenticatedUser: vi.fn(),
  resendVerificationEmail: vi.fn(),
}))

vi.mock('../api/healthApi', () => ({
  getHealth: vi.fn(),
}))

const getAuthenticatedUserMock = vi.mocked(getAuthenticatedUser)
const resendVerificationEmailMock = vi.mocked(resendVerificationEmail)
const getHealthMock = vi.mocked(getHealth)

function renderAppPage(initialPath = '/app') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/login" element={<div>Login page</div>} />
        <Route path="/app" element={<AppPage />} />
      </Routes>
    </MemoryRouter>
  )
}

describe('AppPage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    getAuthenticatedUserMock.mockReset()
    resendVerificationEmailMock.mockReset()
    getHealthMock.mockReset()
    getAuthenticatedUserMock.mockResolvedValue({
      userId: 'user-1',
      email: 'person@example.com',
      emailVerified: false,
    })
    resendVerificationEmailMock.mockResolvedValue({
      message: 'Check your email',
    })
    getHealthMock.mockResolvedValue({
      status: 'UP',
      service: 'nexo-api',
      timestamp: '2026-03-06T18:00:00Z',
    })
  })

  afterEach(() => {
    cleanup()
  })

  it('logs out by clearing the token and navigating to /login', async () => {
    storeAccessToken('active-token')

    renderAppPage()

    await screen.findByText('Finance dashboard shell')

    fireEvent.click(screen.getByRole('button', { name: 'Logout' }))

    await waitFor(() => {
      expect(screen.getByText('Login page')).toBeInTheDocument()
    })

    expect(getAccessToken()).toBeNull()
  })

  it('shows unverified state and lets the user resend verification email', async () => {
    renderAppPage()

    expect(await screen.findByText('Verification required')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Resend verification email' }))

    await waitFor(() => {
      expect(screen.getByText('Check your email')).toBeInTheDocument()
    })

    expect(resendVerificationEmailMock).toHaveBeenCalledTimes(1)
  })
})
