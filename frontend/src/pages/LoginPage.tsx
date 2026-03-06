import { Link } from 'react-router-dom'
import { env } from '../config/env'

export function LoginPage() {
  return (
    <main className="mobile-shell">
      <section className="hero-card">
        <p className="eyebrow">Mobile-first foundation</p>
        <h1>Nexo Finance</h1>
        <p className="lead">
          Personal finance app shell prepared for mobile navigation, PWA
          installability, and environment-based API configuration.
        </p>

        <div className="stack">
          <div className="info-tile">
            <span className="info-label">Route</span>
            <strong>/login</strong>
          </div>
          <div className="info-tile">
            <span className="info-label">API base URL</span>
            <strong>{env.apiBaseUrl}</strong>
          </div>
        </div>

        <div className="actions">
          <Link className="primary-action" to="/app">
            Open app placeholder
          </Link>
        </div>
      </section>
    </main>
  )
}
