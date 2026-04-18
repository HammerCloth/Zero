export type User = {
  id: string
  username: string
  is_admin: boolean
  must_change_password: boolean
}

export type AccountType =
  | 'cash'
  | 'deposit'
  | 'fund'
  | 'pension'
  | 'housing_fund'
  | 'credit'

export type AccountOwner = 'A' | 'B' | 'shared'

export type Account = {
  id: string
  user_id: string
  name: string
  type: AccountType
  owner: AccountOwner
  sort_order: number
  is_active: boolean
  created_at: string
}

export type SnapshotItem = {
  id?: string
  snapshot_id?: string
  account_id: string
  balance: number
}

export type EventCategory =
  | 'rent'
  | 'travel'
  | 'medical'
  | 'appliance'
  | 'social'
  | 'other'

export type SnapshotEvent = {
  id?: string
  snapshot_id?: string
  category: EventCategory
  description: string
  amount: number
  created_at?: string
}

export type Snapshot = {
  id: string
  user_id: string
  date: string
  note?: string
  created_at: string
  created_by: string
  items?: SnapshotItem[]
  events?: SnapshotEvent[]
}

export type SnapshotListItem = {
  id: string
  user_id: string
  date: string
  note?: string
  created_at: string
  created_by: string
  net_worth: number
}

export type DashboardSummary = {
  net_worth: number
  monthly_change: number
  annual_change: number
  annualized_return: number
}

export type TrendPoint = { date: string; net_worth: number }

export type Composition = {
  by_type: Record<string, number>
  by_owner: Record<string, number>
}

export type MonthlyGrowthPoint = { month: string; change: number }

export type EventStatRow = {
  category: EventCategory
  total: number
  count: number
}
