import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ApiError } from '../api/httpClient'
import { getHealth, type HealthResponse } from '../api/healthApi'
import { env } from '../config/env'

export function AppPage() {
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    getHealth(controller.signal)
      .then((response) => {
        setHealth(response)
        setErrorMessage(null)
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }

        if (error instanceof ApiError) {
          setErrorMessage(`Backend request failed (${error.status})`)
          return
        }

        setErrorMessage('Unable to reach the backend')
      })

    return () => controller.abort()
  }, [])

  return (
    <main className="mobile-shell">
      <section className="hero-card">
        <p className="eyebrow">Application placeholder</p>
        <h1>Finance dashboard shell</h1>
        <p className="lead">
          This route exists to validate post-login navigation and app-shell
          behavior before feature implementation.
        </p>

        <div className="stack">
          <div className="info-tile">
            <span className="info-label">Route</span>
            <strong>/app</strong>
          </div>
          <div className="info-tile">
            <span className="info-label">Environment API</span>
            <strong>{env.apiBaseUrl}</strong>
          </div>
          <div className="info-tile">
            <span className="info-label">Health check</span>
            <strong>
              {health
                ? `${health.service}: ${health.status}`
                : errorMessage ?? 'Loading backend status...'}
            </strong>
          </div>
        </div>

        <div className="actions">
          <Link className="secondary-action" to="/login">
            Back to login placeholder
          </Link>
        </div>
      </section>
    </main>
  )
}
