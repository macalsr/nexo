import { useEffect, useState } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { getAuthenticatedUser } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { clearAccessToken, getAccessToken } from '../auth/tokenStorage'

type SessionStatus = 'validating' | 'authenticated' | 'unauthenticated' | 'failed'

export function ProtectedRoute() {
  const location = useLocation()
  const accessToken = getAccessToken()
  const [sessionStatus, setSessionStatus] = useState<SessionStatus>(() =>
    accessToken ? 'validating' : 'unauthenticated'
  )

  useEffect(() => {
    if (!accessToken) {
      return
    }

    const controller = new AbortController()

    getAuthenticatedUser(controller.signal)
      .then(() => {
        setSessionStatus('authenticated')
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }

        if (error instanceof ApiError && error.status === 401) {
          clearAccessToken()
          setSessionStatus('unauthenticated')
          return
        }

        setSessionStatus('failed')
      })

    return () => controller.abort()
  }, [accessToken])

  if (!accessToken || sessionStatus === 'unauthenticated') {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (sessionStatus === 'validating') {
    return (
      <main className="mobile-shell">
        <section className="hero-card">
          <p className="eyebrow">Session validation</p>
          <h1>Checking your session</h1>
          <p className="lead">
            We are confirming your access before the application shell is rendered.
          </p>
        </section>
      </main>
    )
  }

  if (sessionStatus === 'failed') {
    return (
      <main className="mobile-shell">
        <section className="hero-card">
          <p className="eyebrow">Session validation</p>
          <h1>Session check failed</h1>
          <p className="lead">
            We could not validate the current session. Reload and try again.
          </p>
        </section>
      </main>
    )
  }

  return <Outlet />
}
