import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAuthenticatedUser, resendVerificationEmail } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { clearAccessToken } from '../auth/tokenStorage'
import { getHealth, type HealthResponse } from '../api/healthApi'
import { env } from '../config/env'

export function AppPage() {
  const navigate = useNavigate()
  const [health, setHealth] = useState<HealthResponse | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [userEmail, setUserEmail] = useState<string | null>(null)
  const [emailVerified, setEmailVerified] = useState<boolean | null>(null)
  const [verificationMessage, setVerificationMessage] = useState<string | null>(null)
  const [isResendingVerification, setIsResendingVerification] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    getAuthenticatedUser(controller.signal)
      .then((response) => {
        setUserEmail(response.email)
        setEmailVerified(response.emailVerified)
      })
      .catch(() => {
        setUserEmail(null)
        setEmailVerified(null)
      })

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

  function handleLogout() {
    clearAccessToken()
    navigate('/login', { replace: true })
  }

  async function handleResendVerification() {
    setIsResendingVerification(true)
    setVerificationMessage(null)

    try {
      const response = await resendVerificationEmail()
      setVerificationMessage(response.message)
    } catch {
      setVerificationMessage('Unable to resend verification email')
    } finally {
      setIsResendingVerification(false)
    }
  }

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
          <div className="info-tile">
            <span className="info-label">Email verification</span>
            <strong>
              {emailVerified === null
                ? 'Loading verification state...'
                : emailVerified
                  ? 'Verified'
                  : 'Verification required'}
            </strong>
          </div>
          <div className="info-tile">
            <span className="info-label">Authenticated email</span>
            <strong>{userEmail ?? 'Loading account...'}</strong>
          </div>
        </div>

        {emailVerified === false ? (
          <div className="stack compact-stack">
            <p className="lead compact-lead">
              Confirm your email address before relying on recovery and other future
              account-protection features.
            </p>
            {verificationMessage ? (
              <p className={verificationMessage === 'Check your email' ? 'success-message' : 'form-error'}>
                {verificationMessage}
              </p>
            ) : null}
            <button
              className="secondary-action"
              type="button"
              onClick={handleResendVerification}
              disabled={isResendingVerification}
            >
              {isResendingVerification ? 'Resending...' : 'Resend verification email'}
            </button>
          </div>
        ) : null}

        <div className="actions">
          <button className="secondary-action" type="button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </section>
    </main>
  )
}
