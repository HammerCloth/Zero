import http from './http'

export interface OAuthAuthorization {
  id: string
  clientId: string
  clientName: string
  scope: string
  createdAt: string
  lastUsedAt: string | null
  expiresAt: string
  revokedAt: string | null
}

export async function listAuthorizations() {
  const { data } = await http.get<{ authorizations: OAuthAuthorization[] }>(
    '/api/v1/oauth/authorizations',
  )
  return data.authorizations
}

export async function revokeAuthorization(id: string) {
  await http.delete(`/api/v1/oauth/authorizations/${id}`)
}

export async function revokeAllAuthorizations() {
  await http.delete('/api/v1/oauth/authorizations')
}
