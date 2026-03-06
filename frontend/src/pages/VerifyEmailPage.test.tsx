import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { verifyEmail } from '../api/authApi'
import { VerifyEmailPage } from './VerifyEmailPage'

vi.mock('../api/authApi', () => ({
  verifyEmail: vi.fn(),
}))

const verifyEmailMock = vi.mocked(verifyEmail)

function renderVerifyEmailPage(initialPath = '/verify-email?token=verify-token') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/verify-email" element={<VerifyEmailPage />} />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('VerifyEmailPage', () => {
  beforeEach(() => {
    verifyEmailMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('shows success after verifying a valid token', async () => {
    verifyEmailMock.mockResolvedValue(undefined)

    renderVerifyEmailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Email verified' })).toBeInTheDocument()
    })
  })

  it('shows failure when verification token is invalid', async () => {
    verifyEmailMock.mockRejectedValue(new Error('invalid token'))

    renderVerifyEmailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Verification failed' })).toBeInTheDocument()
    })
  })
})
