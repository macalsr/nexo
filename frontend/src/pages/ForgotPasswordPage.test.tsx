import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { requestPasswordReset } from '../api/authApi'
import { ForgotPasswordPage } from './ForgotPasswordPage'

vi.mock('../api/authApi', () => ({
  requestPasswordReset: vi.fn(),
}))

const requestPasswordResetMock = vi.mocked(requestPasswordReset)

function renderForgotPasswordPage(initialPath = '/forgot-password') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('ForgotPasswordPage', () => {
  beforeEach(() => {
    requestPasswordResetMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('submits the email and shows the generic success message', async () => {
    requestPasswordResetMock.mockResolvedValue({ message: 'Check your email' })

    renderForgotPasswordPage()

    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'person@example.com' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Send reset link' }))

    await waitFor(() => {
      expect(screen.getByText('Check your email')).toBeInTheDocument()
    })

    expect(requestPasswordResetMock).toHaveBeenCalledWith('person@example.com')
  })

  it('validates the email before submitting the request', async () => {
    renderForgotPasswordPage()

    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'invalid-email' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Send reset link' }))

    await waitFor(() => {
      expect(screen.getByText('Enter a valid email address')).toBeInTheDocument()
    })

    expect(requestPasswordResetMock).not.toHaveBeenCalled()
  })
})
