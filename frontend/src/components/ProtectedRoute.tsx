import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Loading } from './ui/Loading'

export function ProtectedRoute() {
  const { ready, needsSetup, user } = useAuth()
  const location = useLocation()

  if (!ready) {
    return <Loading />
  }

  if (needsSetup) {
    return <Navigate to="/setup" replace state={{ from: location }} />
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (user.must_change_password && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />
  }

  return <Outlet />
}
