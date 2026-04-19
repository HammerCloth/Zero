import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

/** 无拦截器，仅用于 refresh，避免循环 */
const raw = axios.create({ withCredentials: true })

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE ?? '',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

let refreshing = false
const waitQueue: Array<(token: string | null) => void> = []

http.interceptors.request.use((config) => {
  const t = localStorage.getItem('access_token')
  if (t) {
    config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

http.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    if (!original || error.response?.status !== 401) {
      return Promise.reject(error)
    }
    const url = original.url ?? ''
    if (url.includes('/auth/refresh') || url.includes('/auth/login') || url.includes('/auth/setup')) {
      return Promise.reject(error)
    }
    if (original._retry) {
      return Promise.reject(error)
    }
    if (refreshing) {
      return new Promise((resolve, reject) => {
        waitQueue.push((token) => {
          if (!token) {
            reject(error)
            return
          }
          original.headers.Authorization = `Bearer ${token}`
          original._retry = true
          resolve(http(original))
        })
      })
    }
    refreshing = true
    try {
      const { data } = await raw.post<{ access_token: string }>('/api/v1/auth/refresh')
      const at = data.access_token
      localStorage.setItem('access_token', at)
      waitQueue.forEach((cb) => cb(at))
      waitQueue.length = 0
      original.headers.Authorization = `Bearer ${at}`
      original._retry = true
      return http(original)
    } catch (e) {
      waitQueue.forEach((cb) => cb(null))
      waitQueue.length = 0
      localStorage.removeItem('access_token')
      const path = window.location.pathname
      if (path !== '/login' && path !== '/setup') {
        window.location.href = '/login'
      }
      return Promise.reject(e)
    } finally {
      refreshing = false
    }
  },
)

export default http
