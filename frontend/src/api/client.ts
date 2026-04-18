import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const baseURL = import.meta.env.VITE_API_BASE ?? ''

export const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

let accessToken: string | null = null
let refreshPromise: Promise<string | null> | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

async function refreshAccessToken(): Promise<string | null> {
  if (refreshPromise) {
    return refreshPromise
  }
  refreshPromise = (async () => {
    try {
      const { data } = await api.post<{ access_token: string }>(
        '/api/v1/auth/refresh',
        {},
        { skipAuth: true } as InternalAxiosRequestConfig & { skipAuth?: boolean },
      )
      accessToken = data.access_token
      return data.access_token
    } catch {
      accessToken = null
      return null
    } finally {
      refreshPromise = null
    }
  })()
  return refreshPromise
}

api.interceptors.request.use((config) => {
  const skip = (config as InternalAxiosRequestConfig & { skipAuth?: boolean }).skipAuth
  if (!skip && accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { skipAuth?: boolean; _retry?: boolean }) | undefined
    if (!original || original.skipAuth) {
      return Promise.reject(error)
    }
    if (error.response?.status !== 401 || original._retry) {
      return Promise.reject(error)
    }
    original._retry = true
    const token = await refreshAccessToken()
    if (!token) {
      return Promise.reject(error)
    }
    original.headers = original.headers ?? {}
    original.headers.Authorization = `Bearer ${token}`
    return api(original)
  },
)

export { refreshAccessToken }
