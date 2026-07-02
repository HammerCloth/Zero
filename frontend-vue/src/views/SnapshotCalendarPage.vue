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
  <div class="page-stack">
    <section class="page-header">
      <div class="page-header__copy">
        <h2 class="page-header__title">快照日历</h2>
        <p class="page-header__desc">按月份查看已记录日期。点进已有快照，或直接在空白日期创建新记录。</p>
      </div>
      <div class="inline-control">
        <n-button @click="prevMonth">上月</n-button>
        <strong class="calendar-label">{{ year }} 年 {{ month + 1 }} 月</strong>
        <n-button @click="nextMonth">下月</n-button>
      </div>
    </section>

    <section class="calendar-shell">
      <div class="cal-grid">
        <div v-for="w in weekDays" :key="w" class="cal-head">{{ w }}</div>
        <template v-for="(c, i) in cells" :key="i">
          <div v-if="!c" class="cal-cell cal-empty" />
          <button
            v-else
            type="button"
            class="cal-cell cal-day"
            :class="{ 'has-snap': snapshotDates.has(c.dateStr) }"
            @click="onPick(c.dateStr)"
          >
            <span class="day-num">{{ c.day }}</span>
            <span class="day-state">{{ snapshotDates.has(c.dateStr) ? '已记录' : '新建' }}</span>
          </button>
        </template>
      </div>
    </section>
    <n-text depth="3">点击某日：有快照则进入详情，无快照则新建并预填日期。</n-text>
  </div>
</template>

<style scoped>
.calendar-shell {
  padding: 22px;
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-lg);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88), rgba(250, 251, 248, 0.78));
  box-shadow: var(--shadow-md);
}

.calendar-label {
  min-width: 120px;
  text-align: center;
  color: var(--text-1);
}

.cal-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 10px;
}

.cal-head {
  text-align: center;
  font-size: 13px;
  color: var(--text-3);
  padding: 6px;
}

.cal-cell {
  min-height: 90px;
  border-radius: 18px;
  border: 1px solid var(--line-soft);
}

.cal-empty {
  border: none;
}

.cal-day {
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
  width: 100%;
  padding: 14px;
  color: var(--text-1);
  background: rgba(255, 255, 255, 0.58);
  transition:
    background 0.15s,
    transform 0.15s,
    border-color 0.15s;
}

.cal-day:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: var(--line-strong);
  transform: translateY(-1px);
}

.cal-day.has-snap {
  border-color: rgba(31, 143, 120, 0.28);
  background: linear-gradient(180deg, rgba(31, 143, 120, 0.14), rgba(255, 255, 255, 0.86));
}

.day-num {
  font-weight: 600;
  font-size: 18px;
}

.day-state {
  font-size: 12px;
  color: var(--text-3);
}

@media (max-width: 640px) {
  .calendar-shell {
    padding: 16px;
  }

  .cal-grid {
    gap: 8px;
  }

  .cal-cell {
    min-height: 72px;
    border-radius: 14px;
  }

  .cal-day {
    padding: 10px;
  }

  .day-state {
    display: none;
  }
}
</style>
