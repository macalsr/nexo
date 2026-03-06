import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { hasAccessToken } from '../auth/tokenStorage'

export function ProtectedRoute() {
  const location = useLocation()

  if (!hasAccessToken()) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}
