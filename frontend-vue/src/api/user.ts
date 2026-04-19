import type { User } from '@/types/models'
import http from './http'

export async function listUsers() {
  const { data } = await http.get<{ users: User[] }>('/api/v1/users')
  return data.users
}

export async function createUser(body: { username: string; password: string; isAdmin: boolean }) {
  const { data } = await http.post<{ user: User }>('/api/v1/users', body)
  return data.user
}

export async function resetPassword(id: string, password: string) {
  await http.put(`/api/v1/users/${id}/password`, { password })
}
