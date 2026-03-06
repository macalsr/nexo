import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { verifyEmail } from '../api/authApi'

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const [status, setStatus] = useState<'verifying' | 'success' | 'failed'>(() =>
    searchParams.get('token') ? 'verifying' : 'failed'
  )
  const token = searchParams.get('token')

  useEffect(() => {
    if (!token) {
      return
    }

    const controller = new AbortController()

    verifyEmail(token, controller.signal)
      .then(() => setStatus('success'))
      .catch(() => setStatus('failed'))

    return () => controller.abort()
  }, [token])

  return (
    <main className="mobile-shell">
      <section className="hero-card">
        <p className="eyebrow">Email verification</p>
        <h1>
          {status === 'verifying'
            ? 'Verifying your email'
            : status === 'success'
              ? 'Email verified'
              : 'Verification failed'}
        </h1>
        <p className="lead">
          {status === 'verifying'
            ? 'Please wait while we confirm your verification token.'
            : status === 'success'
              ? 'Your email is now verified. You can return to the application.'
              : 'This verification link is invalid or expired. Request a new verification email from the app.'}
        </p>

        <div className="actions stacked-actions">
          <Link className="secondary-action" to="/login">
            Back to login
          </Link>
        </div>
      </section>
    </main>
  )
}
