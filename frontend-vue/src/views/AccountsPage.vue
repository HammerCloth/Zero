<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import draggable from 'vuedraggable'
import { useMessage } from 'naive-ui'
import type { Account } from '@/types/models'
import * as accountApi from '@/api/account'
import { DIM_ACCOUNT_OWNER, DIM_ACCOUNT_TYPE, useSettingsStore } from '@/stores/settings'

const message = useMessage()
const settings = useSettingsStore()
const list = ref<Account[]>([])
const loading = ref(true)
const showModal = ref(false)
const editing = ref<Account | null>(null)
const form = ref({ name: '', type: 'cash', owner: 'A' })

const typeOptions = computed(() => settings.selectOptions(DIM_ACCOUNT_TYPE))
const ownerOptions = computed(() => settings.selectOptions(DIM_ACCOUNT_OWNER))

async function load() {
  await settings.load()
  list.value = await accountApi.listAccounts()
}

onMounted(async () => {
  try {
    await load()
  } catch {
    message.error('加载失败')
  } finally {
    loading.value = false
  }
})

async function onDragEnd() {
  try {
    await accountApi.reorderAccounts(list.value.map((a) => a.id))
    message.success('排序已保存')
  } catch {
    message.error('排序失败')
    await load()
  }
}

function openCreate() {
  editing.value = null
  form.value = { name: '', type: 'cash', owner: 'A' }
  showModal.value = true
}

function openEdit(row: Account) {
  editing.value = row
  form.value = { name: row.name, type: row.type, owner: row.owner }
  showModal.value = true
}

async function saveAccount() {
  try {
    if (editing.value) {
      await accountApi.updateAccount(editing.value.id, form.value)
      message.success('已更新')
    } else {
      await accountApi.createAccount(form.value)
      message.success('已创建')
    }
    showModal.value = false
    await load()
  } catch {
    message.error('保存失败')
  }
}

async function deactivate(row: Account) {
  try {
    await accountApi.deactivateAccount(row.id)
    message.success('已停用')
    await load()
  } catch {
    message.error('操作失败')
  }
}
</script>

<template>
  <div class="page-stack">
    <section class="page-header">
      <div class="page-header__copy">
        <h2 class="page-header__title">账户</h2>
        <p class="page-header__desc">通过拖拽整理展示顺序，保持移动端和桌面端都能快速辨认账户结构。</p>
      </div>
      <n-button type="primary" @click="openCreate">新建账户</n-button>
    </section>

    <n-spin :show="loading">
      <draggable v-model="list" item-key="id" handle=".drag-handle" class="draggable-list" @end="onDragEnd">
        <template #item="{ element }">
          <div class="account-row">
            <div class="account-row__main">
              <span class="drag-handle">⋮⋮</span>
              <strong>{{ element.name }}</strong>
              <n-tag size="small">{{ settings.label(DIM_ACCOUNT_TYPE, element.type) }}</n-tag>
              <n-tag size="small" type="info">{{ settings.label(DIM_ACCOUNT_OWNER, element.owner) }}</n-tag>
            </div>
            <div class="account-row__meta">
              <span class="section-note">排序 {{ element.sort_order }}</span>
            </div>
            <div class="account-row__actions">
              <n-button size="small" @click="openEdit(element)">编辑</n-button>
              <n-button size="small" type="warning" @click="deactivate(element)">停用</n-button>
            </div>
          </div>
        </template>
      </draggable>
    </n-spin>
    <n-modal v-model:show="showModal" preset="card" :title="editing ? '编辑账户' : '新建账户'" style="width: 480px">
      <n-form>
        <n-form-item label="名称">
          <n-input v-model:value="form.name" />
        </n-form-item>
        <n-form-item label="类型">
          <n-select v-model:value="form.type" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="归属">
          <n-select v-model:value="form.owner" :options="ownerOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" @click="saveAccount">保存</n-button>
      </template>
    </n-modal>
  </div>
</template>
