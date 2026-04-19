import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as settingsApi from '@/api/settings'
import type { OptionItem } from '@/api/settings'

export const DIM_ACCOUNT_TYPE = 'account_type'
export const DIM_ACCOUNT_OWNER = 'account_owner'
export const DIM_EVENT_CATEGORY = 'event_category'

export const useSettingsStore = defineStore('settings', () => {
  const options = ref<settingsApi.OptionsResponse | null>(null)
  const loading = ref(false)

  async function load() {
    loading.value = true
    try {
      options.value = await settingsApi.fetchOptions()
    } finally {
      loading.value = false
    }
  }

  function label(dim: string, key: string) {
    const rows = options.value?.[dim as keyof settingsApi.OptionsResponse] ?? []
    return rows.find((x) => x.key === key)?.label ?? key
  }

  function selectOptions(dim: string): { label: string; value: string }[] {
    const rows = (options.value?.[dim as keyof settingsApi.OptionsResponse] ?? []) as OptionItem[]
    return [...rows]
      .filter((x) => x.enabled)
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((x) => ({ label: x.label, value: x.key }))
  }

  async function saveDimension(dim: string, items: OptionItem[]) {
    await settingsApi.putDimension(dim, items)
    await load()
  }

  async function reset() {
    await settingsApi.resetOptions()
    await load()
  }

  return { options, loading, load, label, selectOptions, saveDimension, reset }
})
