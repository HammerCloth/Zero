import http from './http'

export interface OptionItem {
  key: string
  label: string
  sortOrder: number
  enabled: boolean
}

export type OptionsResponse = {
  account_type: OptionItem[]
  account_owner: OptionItem[]
  event_category: OptionItem[]
}

export async function fetchOptions() {
  const { data } = await http.get<OptionsResponse>('/api/v1/settings/options')
  return data
}

export async function putDimension(dimension: string, items: OptionItem[]) {
  await http.put(`/api/v1/settings/options/${dimension}`, { items })
}

export async function resetOptions() {
  await http.post('/api/v1/settings/options/reset')
}
