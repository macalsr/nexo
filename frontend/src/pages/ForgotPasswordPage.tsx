import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { requestPasswordReset } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { env } from '../config/env'

type FieldErrors = {
  email?: string
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const genericSuccessMessage = 'Check your email'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  function validate(nextEmail: string) {
    if (!nextEmail.trim()) {
      return { email: 'Email is required' }
    }

    if (!emailPattern.test(nextEmail.trim())) {
      return { email: 'Enter a valid email address' }
    }

    return {}
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const nextErrors = validate(email)
    setFieldErrors(nextErrors)
    setErrorMessage(null)
    setSuccessMessage(null)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    setIsSubmitting(true)

    try {
      const response = await requestPasswordReset(email.trim())
      setSuccessMessage(response.message || genericSuccessMessage)
    } catch (error: unknown) {
      if (error instanceof ApiError && error.status === 400) {
        setErrorMessage('Unable to submit reset request')
      } else {
        setErrorMessage('Unable to submit reset request')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mobile-shell">
      <section className="hero-card">
        <p className="eyebrow">Password reset</p>
        <h1>Forgot your password?</h1>
        <p className="lead">
          Enter your email address and we will send password reset instructions if the
          account exists.
        </p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="field">
            <span className="field-label">Email</span>
            <input
              className="field-input"
              type="email"
              name="email"
              autoComplete="email"
              value={email}
              onChange={(event) => {
                setEmail(event.target.value)
                setFieldErrors({})
                setErrorMessage(null)
                setSuccessMessage(null)
              }}
              aria-invalid={fieldErrors.email ? 'true' : 'false'}
              aria-describedby={fieldErrors.email ? 'forgot-password-email-error' : undefined}
            />
          </label>
          {fieldErrors.email ? (
            <p className="field-error" id="forgot-password-email-error">
              {fieldErrors.email}
            </p>
          ) : null}

          {successMessage ? (
            <p className="success-message" role="status">
              {successMessage}
            </p>
          ) : null}

          {errorMessage ? (
            <p className="form-error" role="alert">
              {errorMessage}
            </p>
          ) : null}

          <button className="primary-action" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Submitting...' : 'Send reset link'}
          </button>
        </form>

        <div className="actions stacked-actions">
          <Link className="secondary-action" to="/login">
            Back to login
          </Link>
        </div>

        <div className="stack compact-stack">
          <div className="info-tile">
            <span className="info-label">Route</span>
            <strong>/forgot-password</strong>
          </div>
          <div className="info-tile">
            <span className="info-label">API base URL</span>
            <strong>{env.apiBaseUrl}</strong>
          </div>
        </div>
      </section>
    </main>
  )
}
