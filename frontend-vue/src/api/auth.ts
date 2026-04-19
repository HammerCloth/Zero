import type { User } from '@/types/models'
import http from './http'

export async function getStatus() {
  const { data } = await http.get<{ needs_setup: boolean }>('/api/v1/auth/status')
  return data
}

export async function setup(username: string, password: string) {
  const { data } = await http.post<{ user: User; access_token: string }>('/api/v1/auth/setup', {
    username,
    password,
  })
  return data
}

export async function login(username: string, password: string, rememberMe: boolean) {
  const { data } = await http.post<{ user: User; access_token: string }>('/api/v1/auth/login', {
    username,
    password,
    remember_me: rememberMe,
  })
  return data
}

export async function refresh() {
  const { data } = await http.post<{ user: User; access_token: string }>('/api/v1/auth/refresh')
  return data
}

export async function logout() {
  await http.post('/api/v1/auth/logout')
}

export async function me() {
  const { data } = await http.get<User>('/api/v1/auth/me')
  return data
}

export async function changePassword(currentPassword: string, newPassword: string) {
  await http.put('/api/v1/auth/password', {
    current_password: currentPassword,
    new_password: newPassword,
  })
}
