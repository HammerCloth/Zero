<script setup lang="ts">
import { h, ref, watch } from 'vue'
import { useDialog, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type { Loan, LoanRepayment } from '@/types/models'
import * as loanApi from '@/api/loan'
import { formatMoney } from '@/lib/format'

const message = useMessage()
const dialog = useDialog()
const currentYear = new Date().getFullYear()
const year = ref(currentYear)
const keyword = ref('')
const status = ref<string>('all')
const loans = ref<Loan[]>([])
const stats = ref<loanApi.LoanStats>({
  year: currentYear,
  loanCount: 0,
  openCount: 0,
  settledCount: 0,
  principalTotal: 0,
  outstandingTotal: 0,
  repaidThisYear: 0,
})
const loading = ref(true)
const loanModalOpen = ref(false)
const editingLoan = ref<Loan | null>(null)
const detailLoan = ref<Loan | null>(null)
const editingRepayment = ref<LoanRepayment | null>(null)

const today = () => new Date().toISOString().slice(0, 10)

const loanForm = ref<loanApi.LoanBody>({
  borrowerName: '',
  relationship: '',
  amount: null,
  loanDate: today(),
  dueDate: null,
  note: '',
})
const repaymentForm = ref<loanApi.RepaymentBody>({
  amount: null,
  repayDate: today(),
  note: '',
})

const statusOptions = [
  { label: '全部', value: 'all' },
  { label: '未还清', value: 'open' },
  { label: '已还清', value: 'settled' },
]

const loanColumns: DataTableColumns<Loan> = [
  { title: '借款人', key: 'borrower_name', minWidth: 120 },
  { title: '关系', key: 'relationship', width: 90, render: (row) => row.relationship || '—' },
  { title: '借款日', key: 'loan_date', width: 112 },
  { title: '约定还日', key: 'due_date', width: 112, render: (row) => row.due_date || '—' },
  { title: '本金', key: 'amount', width: 110, render: (row) => formatMoney(row.amount) },
  { title: '已还', key: 'repaid_total', width: 110, render: (row) => formatMoney(row.repaid_total) },
  { title: '剩余', key: 'remaining', width: 110, render: (row) => formatMoney(row.remaining) },
  {
    title: '状态',
    key: 'settled',
    width: 90,
    render(row) {
      return h(
        'span',
        { class: row.settled ? 'loan-status loan-status--settled' : 'loan-status' },
        row.settled ? '已还清' : '未还清',
      )
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 188,
    render(row) {
      return h('div', { class: 'loan-table-actions' }, [
        h('button', { class: 'loan-text-button', onClick: () => openLoanDetail(row.id) }, '还款记录'),
        h('button', { class: 'loan-text-button', onClick: () => openEditLoan(row) }, '编辑'),
        h('button', { class: 'loan-text-button loan-text-button--danger', onClick: () => confirmDeleteLoan(row) }, '删除'),
      ])
    },
  },
]

async function load() {
  loading.value = true
  try {
    const [loanRows, statsData] = await Promise.all([
      loanApi.listLoans({ keyword: keyword.value || undefined, status: status.value }),
      loanApi.loanStats(year.value),
    ])
    loans.value = loanRows
    stats.value = statsData
    if (detailLoan.value) {
      const latest = loanRows.find((row) => row.id === detailLoan.value?.id)
      if (latest) {
        await refreshDetail(latest.id)
      }
    }
  } catch {
    message.error('加载借款数据失败')
  } finally {
    loading.value = false
  }
}

watch([year, status], load, { immediate: true })

function onSearch() {
  load()
}

function openCreateLoan() {
  editingLoan.value = null
  loanForm.value = {
    borrowerName: '',
    relationship: '',
    amount: null,
    loanDate: today(),
    dueDate: null,
    note: '',
  }
  loanModalOpen.value = true
}

function openEditLoan(row: Loan) {
  editingLoan.value = row
  loanForm.value = {
    borrowerName: row.borrower_name,
    relationship: row.relationship || '',
    amount: row.amount,
    loanDate: row.loan_date,
    dueDate: row.due_date || null,
    note: row.note || '',
  }
  loanModalOpen.value = true
}

async function saveLoan() {
  try {
    if (editingLoan.value) {
      await loanApi.updateLoan(editingLoan.value.id, loanForm.value)
      message.success('借款已更新')
    } else {
      await loanApi.createLoan(loanForm.value)
      message.success('借款已记录')
    }
    loanModalOpen.value = false
    await load()
  } catch (error) {
    message.error(apiMessage(error, '保存失败'))
  }
}

function confirmDeleteLoan(row: Loan) {
  dialog.warning({
    title: '删除借款',
    content: `确定删除借给 ${row.borrower_name} 的 ${formatMoney(row.amount)} 吗？关联的还款记录会一并删除。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await loanApi.deleteLoan(row.id)
        if (detailLoan.value?.id === row.id) {
          detailLoan.value = null
        }
        message.success('已删除')
        await load()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

async function openLoanDetail(id: string) {
  await refreshDetail(id)
  resetRepaymentForm()
}

async function refreshDetail(id: string) {
  try {
    detailLoan.value = await loanApi.getLoan(id)
  } catch {
    detailLoan.value = null
    message.error('加载还款记录失败')
  }
}

function resetRepaymentForm(repayment?: LoanRepayment) {
  editingRepayment.value = repayment ?? null
  repaymentForm.value = {
    amount: repayment ? repayment.amount : detailLoan.value && !detailLoan.value.settled ? detailLoan.value.remaining : null,
    repayDate: repayment?.repay_date || today(),
    note: repayment?.note || '',
  }
}

async function saveRepayment() {
  if (!detailLoan.value) return
  try {
    if (editingRepayment.value) {
      await loanApi.updateRepayment(detailLoan.value.id, editingRepayment.value.id, repaymentForm.value)
      message.success('还款记录已更新')
    } else {
      await loanApi.createRepayment(detailLoan.value.id, repaymentForm.value)
      message.success('已登记一笔还款')
    }
    await load()
    resetRepaymentForm()
  } catch (error) {
    message.error(apiMessage(error, '保存失败'))
  }
}

function confirmDeleteRepayment(row: LoanRepayment) {
  if (!detailLoan.value) return
  const loanId = detailLoan.value.id
  dialog.warning({
    title: '删除还款记录',
    content: `确定删除 ${row.repay_date} 的 ${formatMoney(row.amount)} 还款吗？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await loanApi.deleteRepayment(loanId, row.id)
        message.success('已删除')
        await load()
        resetRepaymentForm()
      } catch {
        message.error('删除失败')
      }
    },
  })
}

function apiMessage(error: unknown, fallback: string) {
  const data = (error as { response?: { data?: { error?: string } } })?.response?.data
  return typeof data?.error === 'string' ? data.error : fallback
}
</script>

<template>
  <div class="page-stack">
    <section class="page-header">
      <div class="page-header__copy">
        <h2 class="page-header__title">借款</h2>
        <p class="page-header__desc">记录谁向我们借了钱，并按批次登记对方还款，不与资产快照混用。</p>
      </div>
      <n-button type="primary" @click="openCreateLoan">新增借款</n-button>
    </section>

    <n-spin :show="loading">
      <div class="page-stack">
        <div class="loan-toolbar">
          <n-select v-model:value="status" :options="statusOptions" />
          <n-input v-model:value="keyword" clearable placeholder="搜索借款人、关系或备注" @keyup.enter="onSearch" />
          <n-input-number v-model:value="year" :min="2000" :max="2100" />
          <n-button @click="onSearch">搜索</n-button>
        </div>

        <section class="loan-summary-grid">
          <n-card class="surface-panel" size="small"><n-statistic label="未还总额" :value="formatMoney(stats.outstandingTotal)" /></n-card>
          <n-card class="surface-panel" size="small"><n-statistic label="未还清" :value="stats.openCount" suffix="笔" /></n-card>
          <n-card class="surface-panel" size="small"><n-statistic label="本年收回" :value="formatMoney(stats.repaidThisYear)" /></n-card>
        </section>

        <n-card class="surface-panel" title="借款明细">
          <n-data-table :columns="loanColumns" :data="loans" :row-key="(row: Loan) => row.id" :scroll-x="1080" />
          <n-empty v-if="!loading && !loans.length" style="padding: 28px 0" description="还没有符合条件的借款记录" />
        </n-card>
      </div>
    </n-spin>

    <n-modal v-model:show="loanModalOpen" preset="card" :title="editingLoan ? '编辑借款' : '新增借款'" style="width: 520px">
      <n-form label-placement="left" label-width="90">
        <n-form-item label="借款人" required>
          <n-input v-model:value="loanForm.borrowerName" placeholder="谁向我们借的钱" />
        </n-form-item>
        <n-form-item label="关系">
          <n-input v-model:value="loanForm.relationship" placeholder="例如：亲戚、同事、朋友" />
        </n-form-item>
        <n-form-item label="本金" required>
          <n-input-number v-model:value="loanForm.amount" :min="0.01" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="借款日" required>
          <n-date-picker v-model:formatted-value="loanForm.loanDate" value-format="yyyy-MM-dd" type="date" style="width: 100%" />
        </n-form-item>
        <n-form-item label="约定还日">
          <n-date-picker v-model:formatted-value="loanForm.dueDate" value-format="yyyy-MM-dd" type="date" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="loanForm.note" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </n-form-item>
      </n-form>
      <template #footer><n-button type="primary" @click="saveLoan">保存</n-button></template>
    </n-modal>

    <n-drawer :show="Boolean(detailLoan)" :width="460" placement="right" @update:show="(show) => { if (!show) detailLoan = null }">
      <n-drawer-content v-if="detailLoan" :title="detailLoan.borrower_name">
        <n-tag v-if="detailLoan.relationship" size="small">{{ detailLoan.relationship }}</n-tag>
        <n-tag size="small" :type="detailLoan.settled ? 'success' : 'warning'" style="margin-left: 8px">
          {{ detailLoan.settled ? '已还清' : '未还清' }}
        </n-tag>
        <p class="section-note">
          本金 {{ formatMoney(detailLoan.amount) }}，已还 {{ formatMoney(detailLoan.repaid_total) }}，剩余 {{ formatMoney(detailLoan.remaining) }}
        </p>
        <p v-if="detailLoan.due_date" class="section-note">约定还日 {{ detailLoan.due_date }}</p>
        <p v-if="detailLoan.note" class="loan-note">{{ detailLoan.note }}</p>

        <h3 class="loan-drawer-title">{{ editingRepayment ? '编辑还款' : '登记还款' }}</h3>
        <n-form label-placement="left" label-width="76">
          <n-form-item label="金额" required>
            <n-input-number v-model:value="repaymentForm.amount" :min="0.01" :precision="2" :max="detailLoan.remaining + (editingRepayment?.amount ?? 0)" style="width: 100%">
              <template #prefix>¥</template>
            </n-input-number>
          </n-form-item>
          <n-form-item label="还款日" required>
            <n-date-picker v-model:formatted-value="repaymentForm.repayDate" value-format="yyyy-MM-dd" type="date" style="width: 100%" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="repaymentForm.note" placeholder="例如：第一次还款、微信转账" />
          </n-form-item>
        </n-form>
        <n-space>
          <n-button type="primary" :disabled="detailLoan.settled && !editingRepayment" @click="saveRepayment">
            {{ editingRepayment ? '保存修改' : '登记还款' }}
          </n-button>
          <n-button v-if="editingRepayment" @click="resetRepaymentForm()">取消编辑</n-button>
        </n-space>

        <h3 class="loan-drawer-title">分批还款</h3>
        <div class="loan-repayment-list">
          <div v-for="item in detailLoan.repayments" :key="item.id" class="loan-repayment-row">
            <div>
              <strong>{{ formatMoney(item.amount) }}</strong>
              <span class="section-note">{{ item.repay_date }}</span>
              <p v-if="item.note" class="loan-note">{{ item.note }}</p>
            </div>
            <n-space size="small">
              <n-button size="small" @click="resetRepaymentForm(item)">编辑</n-button>
              <n-button size="small" type="warning" @click="confirmDeleteRepayment(item)">删除</n-button>
            </n-space>
          </div>
          <n-empty v-if="!detailLoan.repayments?.length" description="还没有还款记录，可按批次登记" style="padding: 20px 0" />
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<style scoped>
.loan-toolbar { display: grid; grid-template-columns: 124px minmax(180px, 1fr) 124px auto; gap: 12px; align-items: center; }
.loan-summary-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.loan-text-button { border: 0; background: none; color: #426879; cursor: pointer; font: inherit; padding: 0; }
.loan-text-button:hover { color: #b7791f; }
.loan-table-actions { display: flex; gap: 12px; }
.loan-text-button--danger { color: #b44c43; }
.loan-status { color: #b7791f; font-weight: 600; }
.loan-status--settled { color: #2f6f4e; }
.loan-drawer-title { margin: 24px 0 12px; font-size: 15px; }
.loan-note { margin: 5px 0 0; color: #62665f; font-size: 13px; }
.loan-repayment-list { display: grid; gap: 8px; }
.loan-repayment-row { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 12px; align-items: center; padding: 12px 0; border-bottom: 1px solid #e5e1d8; }
.loan-repayment-row:last-child { border-bottom: 0; }
.loan-repayment-row .section-note { margin-left: 8px; }
@media (max-width: 760px) {
  .loan-toolbar { grid-template-columns: 1fr 1fr; }
  .loan-toolbar :deep(.n-input) { grid-column: span 2; }
  .loan-summary-grid { grid-template-columns: 1fr; gap: 10px; }
}
</style>
