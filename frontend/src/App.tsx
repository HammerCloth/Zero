import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { AdminRoute } from './components/AdminRoute'
import { AppLayout } from './components/layout/AppLayout'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AccountsPage } from './pages/AccountsPage'
import { ChangePasswordPage } from './pages/ChangePasswordPage'
import { DashboardPage } from './pages/DashboardPage'
import { EventStatsPage } from './pages/EventStatsPage'
import { LoginPage } from './pages/LoginPage'
import { ProfilePage } from './pages/ProfilePage'
import { SetupPage } from './pages/SetupPage'
import { SnapshotDetailPage } from './pages/SnapshotDetailPage'
import { SnapshotFormPage } from './pages/SnapshotFormPage'
import { SnapshotsPage } from './pages/SnapshotsPage'
import { UsersPage } from './pages/UsersPage'

export default function App() {
  return (
    <Routes>
      <Route path="/setup" element={<SetupPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/change-password" element={<ChangePasswordPage />} />
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} handle={{ crumb: '总览' }} />
          <Route path="/accounts" element={<AccountsPage />} handle={{ crumb: '账户' }} />
          <Route path="/snapshots" element={<Outlet />} handle={{ crumb: '快照' }}>
            <Route index element={<SnapshotsPage />} />
            <Route path="new" element={<SnapshotFormPage />} handle={{ crumb: '新建快照' }} />
            <Route path=":id/edit" element={<SnapshotFormPage />} handle={{ crumb: '编辑快照' }} />
            <Route path=":id" element={<SnapshotDetailPage />} handle={{ crumb: '详情' }} />
          </Route>
          <Route path="/profile" element={<ProfilePage />} handle={{ crumb: '我的' }} />
          <Route path="/events/stats" element={<EventStatsPage />} handle={{ crumb: '大事记统计' }} />
          <Route element={<AdminRoute />}>
            <Route path="/users" element={<UsersPage />} handle={{ crumb: '用户管理' }} />
          </Route>
        </Route>
      </Route>
      <Route path="/*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
