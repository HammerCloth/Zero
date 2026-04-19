<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import type { Account } from '@/types/models'
import * as accountApi from '@/api/account'
import * as snapshotApi from '@/api/snapshot'
import { DIM_EVENT_CATEGORY, useSettingsStore } from '@/stores/settings'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const settings = useSettingsStore()

const idParam = computed(() => (route.params.id as string) || '')
const isEdit = computed(() => route.name === 'snapshot-edit')

const date = ref<string | null>(null)
const note = ref('')
const accounts = ref<Account[]>([])
const balances = ref<Record<string, number | null>>({})
type Flow = 'in' | 'out'
const events = ref<
  { category: string; description: string; absAmount: number | null; flow: Flow }[]
>([])
const loading = ref(false)

const categorySelectOptions = computed(() => settings.selectOptions(DIM_EVENT_CATEGORY))

function defaultCategory() {
  const opts = categorySelectOptions.value
  return opts[0]?.value ?? 'other'
}

async function load() {
  await settings.load()
  const accs = await accountApi.listAccounts()
  accounts.value = accs
  for (const a of accs) {
    balances.value[a.id] = null
  }

  if (isEdit.value && idParam.value) {
    const s = await snapshotApi.getSnapshot(idParam.value)
    date.value = s.date
    note.value = s.note ?? ''
    for (const row of s.items) {
      balances.value[row.accountId] = row.balance
    }
    events.value =
      s.events?.map((e) => ({
        category: e.category,
        description: e.description,
        absAmount: Math.abs(e.amount),
        flow: e.amount < 0 ? ('out' as const) : ('in' as const),
      })) ?? []
  } else {
    const latest = await snapshotApi.latestSnapshot()
    if (latest?.items?.length) {
      for (const row of latest.items) {
        balances.value[row.accountId] = row.balance
      }
    }
    const q = route.query.date
    if (typeof q === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(q)) {
      date.value = q
    } else {
      const d = new Date()
      date.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    }
  }
}

function addEvent() {
  events.value.push({
    category: defaultCategory(),
    description: '',
    absAmount: null,
    flow: 'out',
  })
}

function removeEvent(i: number) {
  events.value.splice(i, 1)
}

function signedAmount(row: { absAmount: number | null; flow: Flow }) {
  if (row.absAmount == null || Number.isNaN(row.absAmount)) {
    return 0
  }
  const v = Math.abs(row.absAmount)
  return row.flow === 'out' ? -v : v
}

async function submit() {
  if (!date.value) {
    message.error('请选择日期')
    return
  }
  const items = accounts.value.map((a) => ({
    accountId: a.id,
    balance: balances.value[a.id],
  }))
  if (items.some((i) => i.balance === null || Number.isNaN(i.balance as number))) {
    message.error('请为每个账户填写余额')
    return
  }
  const evs = events.value
    .filter((e) => e.description.trim())
    .map((e) => ({
      category: e.category,
      description: e.description.trim(),
      amount: signedAmount(e),
    }))
  loading.value = true
  try {
    if (isEdit.value && idParam.value) {
      await snapshotApi.updateSnapshot(idParam.value, {
        date: date.value,
        note: note.value || null,
        items: items.map((i) => ({ accountId: i.accountId, balance: i.balance as number })),
        events: evs,
      })
      message.success('已保存')
      await router.replace(`/snapshots/${idParam.value}`)
    } else {
      const s = await snapshotApi.createSnapshot({
        date: date.value,
        note: note.value || null,
        items: items.map((i) => ({ accountId: i.accountId, balance: i.balance as number })),
        events: evs,
      })
      message.success('已创建')
      await router.replace(`/snapshots/${s.id}`)
    }
  } catch {
    message.error('保存失败（日期重复或未填余额等）')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load().catch(() => message.error('加载失败'))
})
</script>

<template>
  <n-space vertical size="large">
    <n-h2>{{ isEdit ? '编辑快照' : '新建快照' }}</n-h2>
    <n-form label-placement="left" label-width="100">
      <n-form-item label="日期">
        <n-date-picker
          v-model:formatted-value="date"
          type="date"
          value-format="yyyy-MM-dd"
          clearable
        />
      </n-form-item>
      <n-form-item label="备注">
        <n-input v-model:value="note" type="textarea" />
      </n-form-item>
    </n-form>

    <n-h3>账户余额（必填）</n-h3>
    <n-table :single-line="false" size="small">
      <thead>
        <tr>
          <th>账户</th>
          <th>类型</th>
          <th>归属</th>
          <th>余额</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in accounts" :key="a.id">
          <td>{{ a.name }}</td>
          <td>{{ settings.label('account_type', a.type) }}</td>
          <td>{{ settings.label('account_owner', a.owner) }}</td>
          <td>
            <n-input-number v-model:value="balances[a.id]" style="width: 160px" :show-button="false" />
          </td>
        </tr>
      </tbody>
    </n-table>

    <n-h3>大事记</n-h3>
    <n-space vertical>
      <n-space v-for="(ev, i) in events" :key="i" align="center" style="flex-wrap: wrap">
        <n-select
          v-model:value="ev.category"
          :options="categorySelectOptions"
          style="width: 140px"
        />
        <n-input v-model:value="ev.description" placeholder="说明" style="flex: 1; min-width: 120px" />
        <n-radio-group v-model:value="ev.flow" size="small">
          <n-radio value="out">支出</n-radio>
          <n-radio value="in">收入</n-radio>
        </n-radio-group>
        <n-input-number
          v-model:value="ev.absAmount"
          placeholder="金额"
          :min="0"
          :show-button="false"
          style="width: 120px"
        />
        <n-button quaternary @click="removeEvent(i)">删</n-button>
      </n-space>
      <n-button dashed block @click="addEvent">添加大事记</n-button>
    </n-space>

    <n-button type="primary" :loading="loading" @click="submit">保存</n-button>
  </n-space>
</template>
