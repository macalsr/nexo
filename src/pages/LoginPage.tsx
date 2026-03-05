import { Link } from 'react-router-dom'
import { apiBaseUrl } from '../config/env'

export function LoginPage() {
  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">Mobile First PWA</p>
        <h1>Welcome to Nexo</h1>
        <p className="muted">Login flow placeholder. Authentication will be implemented in a future card.</p>
        <p><strong>API base URL:</strong> {apiBaseUrl}</p>
        <Link to="/app" className="button">Continue to App</Link>
      </section>
    </main>
  )
}
