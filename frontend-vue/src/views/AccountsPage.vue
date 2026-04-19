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
  <n-space vertical size="large">
    <n-space justify="space-between">
      <n-h2 style="margin: 0">账户</n-h2>
      <n-button type="primary" @click="openCreate">新建账户</n-button>
    </n-space>
    <n-spin :show="loading">
      <draggable v-model="list" item-key="id" handle=".drag-handle" @end="onDragEnd">
        <template #item="{ element }">
          <n-card size="small" style="margin-bottom: 8px">
            <n-space justify="space-between" align="center">
              <n-space align="center">
                <span class="drag-handle" style="cursor: grab; opacity: 0.6">⋮⋮</span>
                <strong>{{ element.name }}</strong>
                <n-tag size="small">{{ settings.label(DIM_ACCOUNT_TYPE, element.type) }}</n-tag>
                <n-tag size="small" type="info">{{ settings.label(DIM_ACCOUNT_OWNER, element.owner) }}</n-tag>
                <span style="opacity: 0.6">排序 {{ element.sort_order }}</span>
              </n-space>
              <n-space>
                <n-button size="small" @click="openEdit(element)">编辑</n-button>
                <n-button size="small" type="warning" @click="deactivate(element)">停用</n-button>
              </n-space>
            </n-space>
          </n-card>
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
  </n-space>
</template>
