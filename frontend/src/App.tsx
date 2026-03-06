import { Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import { AppPage } from './pages/AppPage'
import { LoginPage } from './pages/LoginPage'
import { ProtectedRoute } from './routing/ProtectedRoute'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/app" element={<AppPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
