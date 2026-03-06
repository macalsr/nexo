import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { signup } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { getAccessToken } from '../auth/tokenStorage'
import { SignupPage } from './SignupPage'

vi.mock('../api/authApi', () => ({
  signup: vi.fn(),
}))

const signupMock = vi.mocked(signup)

function renderSignupPage(initialPath = '/signup') {
  return render(
    <MemoryRouter
      initialEntries={[initialPath]}
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/app" element={<div>Protected app content</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('SignupPage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    signupMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('creates an account, stores the token, and navigates to /app', async () => {
    signupMock.mockResolvedValue({
      accessToken: 'signup-token',
      expiresAt: '2026-03-07T12:00:00Z',
    })

    renderSignupPage()

    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'person@example.com' },
    })
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'secret123' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }))

    expect(await screen.findByText('Protected app content')).toBeInTheDocument()
    expect(signupMock).toHaveBeenCalledWith({
      email: 'person@example.com',
      password: 'secret123',
    })
    expect(getAccessToken()).toBe('signup-token')
  })

  it('renders backend field errors when signup validation fails', async () => {
    signupMock.mockRejectedValue(
      new ApiError('Request failed with status 400', 400, 'http://localhost:8080/auth/signup', {
        message: 'Validation failed',
        errors: {
          email: 'Enter a valid email address',
          password: 'Password must be at least 8 characters',
        },
      })
    )

    renderSignupPage()

    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'person@example.com' },
    })
    fireEvent.change(screen.getByLabelText('Password'), {
      target: { value: 'secret123' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }))

    await waitFor(() => {
      expect(screen.getByText('Enter a valid email address')).toBeInTheDocument()
    })

    expect(screen.getByText('Password must be at least 8 characters')).toBeInTheDocument()
    expect(getAccessToken()).toBeNull()
  })
})
