/** 与后端 JSON（snake_case）对齐 */

export interface User {
  id: string
  username: string
  is_admin: boolean
  must_change_password: boolean
  created_at?: string
}

export interface Account {
  id: string
  user_id: string
  name: string
  type: string
  owner: string
  sort_order: number
  is_active: boolean
  created_at?: string
}

export interface SnapshotListItem {
  id: string
  date: string
  createdAt: string
  netWorth: number
}

export interface SnapshotDetail {
  id: string
  date: string
  note?: string | null
  createdAt: string
  createdBy: string
  items: Array<{
    accountId: string
    balance: number
    accountName?: string
    type?: string
    owner?: string
  }>
  events: Array<{
    id: string
    category: string
    description: string
    amount: number
    createdAt?: string
  }>
}


export interface DashboardSummary {
  netWorth: number
  monthlyChange: number
  annualChange: number
  annualizedReturn: number
}

