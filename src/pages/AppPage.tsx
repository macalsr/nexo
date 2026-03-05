import { Link } from 'react-router-dom'

export function AppPage() {
  return (
    <main className="page">
      <section className="card">
        <p className="eyebrow">/app placeholder</p>
        <h1>App Home</h1>
        <p className="muted">Main finance workspace will be built in upcoming cards.</p>
        <Link to="/login" className="button secondary">Back to Login</Link>
      </section>
    </main>
  )
}
