import http from './http'

export function downloadCsv() {
  return http.get('/api/v1/export/csv', { responseType: 'blob' })
}
