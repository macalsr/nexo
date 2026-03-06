import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { login } from '../api/authApi'
import { storeAccessToken } from '../auth/tokenStorage'
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
    loginMock.mockReset()
  })

  afterEach(() => {
    cleanup()
  })

  it('keeps the login screen accessible even when a token is already stored', () => {
    storeAccessToken('stale-token')

    renderLoginPage()

    expect(screen.getByRole('heading', { name: 'Nexo Finance' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeInTheDocument()
    expect(screen.queryByText('Protected app content')).not.toBeInTheDocument()
  })
})
