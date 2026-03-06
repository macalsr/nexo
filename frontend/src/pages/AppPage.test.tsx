import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { getHealth } from '../api/healthApi'
import { getAccessToken, storeAccessToken } from '../auth/tokenStorage'
import { AppPage } from './AppPage'

vi.mock('../api/healthApi', () => ({
  getHealth: vi.fn(),
}))

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
    getHealthMock.mockReset()
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
})
