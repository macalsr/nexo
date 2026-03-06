import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { login } from '../api/authApi'
import { ApiError } from '../api/httpClient'
import { storeAccessToken } from '../auth/tokenStorage'
import { env } from '../config/env'

type FormValues = {
  email: string
  password: string
}

type FieldErrors = Partial<Record<keyof FormValues, string>>

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [formValues, setFormValues] = useState<FormValues>({
    email: '',
    password: '',
  })
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const redirectTo =
    (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/app'

  function validate(values: FormValues) {
    const nextErrors: FieldErrors = {}

    if (!values.email.trim()) {
      nextErrors.email = 'Email is required'
    } else if (!emailPattern.test(values.email.trim())) {
      nextErrors.email = 'Enter a valid email address'
    }

    if (!values.password) {
      nextErrors.password = 'Password is required'
    }

    return nextErrors
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const nextErrors = validate(formValues)
    setFieldErrors(nextErrors)
    setErrorMessage(null)

    if (Object.keys(nextErrors).length > 0) {
      return
    }

    setIsSubmitting(true)

    try {
      const response = await login({
        email: formValues.email.trim(),
        password: formValues.password,
      })

      storeAccessToken(response.accessToken)
      navigate(redirectTo, { replace: true })
    } catch (error: unknown) {
      if (error instanceof ApiError && error.status === 401) {
        setErrorMessage('Invalid credentials')
      } else {
        setErrorMessage('Invalid credentials')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="mobile-shell">
      <section className="hero-card">
        <p className="eyebrow">Session access</p>
        <h1>Nexo Finance</h1>
        <p className="lead">
          Sign in with your email and password to start an authenticated session
          and continue to the application shell.
        </p>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <label className="field">
            <span className="field-label">Email</span>
            <input
              className="field-input"
              type="email"
              name="email"
              autoComplete="email"
              value={formValues.email}
              onChange={(event) => {
                const email = event.target.value
                setFormValues((current) => ({ ...current, email }))
                setFieldErrors((current) => ({ ...current, email: undefined }))
                setErrorMessage(null)
              }}
              aria-invalid={fieldErrors.email ? 'true' : 'false'}
              aria-describedby={fieldErrors.email ? 'email-error' : undefined}
            />
          </label>
          {fieldErrors.email ? (
            <p className="field-error" id="email-error">
              {fieldErrors.email}
            </p>
          ) : null}

          <label className="field">
            <span className="field-label">Password</span>
            <input
              className="field-input"
              type="password"
              name="password"
              autoComplete="current-password"
              value={formValues.password}
              onChange={(event) => {
                const password = event.target.value
                setFormValues((current) => ({ ...current, password }))
                setFieldErrors((current) => ({ ...current, password: undefined }))
                setErrorMessage(null)
              }}
              aria-invalid={fieldErrors.password ? 'true' : 'false'}
              aria-describedby={fieldErrors.password ? 'password-error' : undefined}
            />
          </label>
          {fieldErrors.password ? (
            <p className="field-error" id="password-error">
              {fieldErrors.password}
            </p>
          ) : null}

          {errorMessage ? (
            <p className="form-error" role="alert">
              {errorMessage}
            </p>
          ) : null}

          <button className="primary-action" type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="actions stacked-actions">
          <Link className="secondary-action" to="/forgot-password">
            Forgot password?
          </Link>
          <Link className="secondary-action" to="/signup">
            Create a new account
          </Link>
        </div>

        <div className="stack compact-stack">
          <div className="info-tile">
            <span className="info-label">Route</span>
            <strong>/login</strong>
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
