<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import * as snapshotApi from '@/api/snapshot'

const router = useRouter()
const message = useMessage()

const viewMonth = ref(new Date())

const year = computed(() => viewMonth.value.getFullYear())
const month = computed(() => viewMonth.value.getMonth())

const snapshotDates = ref<Set<string>>(new Set())

function ymd(y: number, m: number, d: number) {
  return `${y}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}

async function loadMonth() {
  const y = year.value
  const m = month.value
  const from = ymd(y, m, 1)
  const lastDate = new Date(y, m + 1, 0).getDate()
  const to = ymd(y, m, lastDate)
  const dates = await snapshotApi.snapshotDatesInRange(from, to)
  snapshotDates.value = new Set(dates)
}

const cells = computed(() => {
  const y = year.value
  const m = month.value
  const firstDow = new Date(y, m, 1).getDay()
  const lastDate = new Date(y, m + 1, 0).getDate()
  const out: ({ dateStr: string; day: number } | null)[] = []
  for (let i = 0; i < firstDow; i++) {
    out.push(null)
  }
  for (let d = 1; d <= lastDate; d++) {
    out.push({ dateStr: ymd(y, m, d), day: d })
  }
  while (out.length % 7 !== 0) {
    out.push(null)
  }
  return out
})

const weekDays = ['日', '一', '二', '三', '四', '五', '六']

async function onPick(dateStr: string) {
  try {
    const snap = await snapshotApi.snapshotForDate(dateStr)
    if (snap) {
      await router.push(`/snapshots/${snap.id}`)
    } else {
      await router.push({ path: '/snapshots/new', query: { date: dateStr } })
    }
  } catch {
    message.error('加载失败')
  }
}

function prevMonth() {
  const d = new Date(viewMonth.value)
  d.setMonth(d.getMonth() - 1)
  viewMonth.value = d
}

function nextMonth() {
  const d = new Date(viewMonth.value)
  d.setMonth(d.getMonth() + 1)
  viewMonth.value = d
}

watch(
  viewMonth,
  () => {
    loadMonth().catch(() => message.error('加载日历失败'))
  },
  { immediate: true },
)
</script>

<template>
  <n-space vertical size="large">
    <n-space justify="space-between" align="center">
      <n-h2 style="margin: 0">快照日历</n-h2>
      <n-space align="center">
        <n-button @click="prevMonth">上月</n-button>
        <span>{{ year }} 年 {{ month + 1 }} 月</span>
        <n-button @click="nextMonth">下月</n-button>
      </n-space>
    </n-space>
    <div class="cal-grid">
      <div v-for="w in weekDays" :key="w" class="cal-head">{{ w }}</div>
      <template v-for="(c, i) in cells" :key="i">
        <div v-if="!c" class="cal-cell cal-empty" />
        <div
          v-else
          class="cal-cell cal-day"
          :class="{ 'has-snap': snapshotDates.has(c.dateStr) }"
          @click="onPick(c.dateStr)"
        >
          <span class="day-num">{{ c.day }}</span>
        </div>
      </template>
    </div>
    <n-text depth="3">点击某日：有快照则进入详情，无快照则新建并预填日期。</n-text>
  </n-space>
</template>

<style scoped>
.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  max-width: 560px;
}
.cal-head {
  text-align: center;
  font-size: 12px;
  opacity: 0.7;
  padding: 4px;
}
.cal-cell {
  min-height: 44px;
  border-radius: 8px;
  border: 1px solid var(--n-border-color);
}
.cal-empty {
  border: none;
}
.cal-day {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
}
.cal-day:hover {
  background: var(--n-color-hover);
}
.cal-day.has-snap {
  border-color: var(--n-color-target);
  background: rgba(99, 102, 241, 0.08);
}
.day-num {
  font-weight: 600;
}
</style>
