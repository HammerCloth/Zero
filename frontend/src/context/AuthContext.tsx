import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import * as api from '../api/api'
import { refreshAccessToken, setAccessToken } from '../api/client'
import type { User } from '../api/types'

type AuthState = {
  ready: boolean
  needsSetup: boolean | null
  user: User | null
}

type AuthContextValue = AuthState & {
  refreshUser: () => Promise<void>
  signIn: (username: string, password: string, remember: boolean) => Promise<void>
  signUp: (username: string, password: string) => Promise<void>
  signOut: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false)
  const [needsSetup, setNeedsSetup] = useState<boolean | null>(null)
  const [user, setUser] = useState<User | null>(null)

  const refreshUser = useCallback(async () => {
    const token = await refreshAccessToken()
    if (!token) {
      setUser(null)
      return
    }
    setAccessToken(token)
    const me = await api.fetchMe()
    setUser(me)
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const setup = await api.getAuthStatus()
        if (cancelled) {
          return
        }
        setNeedsSetup(setup)
        if (setup) {
          setUser(null)
          setAccessToken(null)
          return
        }
        await refreshUser()
      } catch {
        if (!cancelled) {
          setNeedsSetup(false)
          setUser(null)
          setAccessToken(null)
        }
      } finally {
        if (!cancelled) {
          setReady(true)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [refreshUser])

  const signIn = useCallback(async (username: string, password: string, remember: boolean) => {
    const data = await api.login(username, password, remember)
    setAccessToken(data.access_token)
    setUser(data.user)
    setNeedsSetup(false)
  }, [])

  const signUp = useCallback(async (username: string, password: string) => {
    const data = await api.setupAdmin(username, password)
    setAccessToken(data.access_token)
    setUser(data.user)
    setNeedsSetup(false)
  }, [])

  const signOut = useCallback(async () => {
    await api.logout()
    setUser(null)
    setAccessToken(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      ready,
      needsSetup,
      user,
      refreshUser,
      signIn,
      signUp,
      signOut,
    }),
    [ready, needsSetup, user, refreshUser, signIn, signUp, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
