import { Navigate, Route, Routes } from 'react-router-dom'
import { AppPage } from './pages/AppPage'
import { LoginPage } from './pages/LoginPage'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/app" element={<AppPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
