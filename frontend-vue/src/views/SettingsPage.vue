<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useDialog, useMessage } from 'naive-ui'
import type { OptionItem } from '@/api/settings'
import { DIM_ACCOUNT_TYPE, DIM_ACCOUNT_OWNER, DIM_EVENT_CATEGORY, useSettingsStore } from '@/stores/settings'

const message = useMessage()
const dialog = useDialog()
const settings = useSettingsStore()

const editing = ref<Record<string, OptionItem[]>>({})

function cloneRows(dim: string): OptionItem[] {
  const src = settings.options?.[dim as keyof typeof settings.options] ?? []
  return src.map((r) => ({ ...r }))
}

async function refresh() {
  await settings.load()
  editing.value = {
    [DIM_ACCOUNT_TYPE]: cloneRows(DIM_ACCOUNT_TYPE),
    [DIM_ACCOUNT_OWNER]: cloneRows(DIM_ACCOUNT_OWNER),
    [DIM_EVENT_CATEGORY]: cloneRows(DIM_EVENT_CATEGORY),
  }
}

onMounted(() => {
  refresh().catch(() => message.error('加载失败'))
})

async function saveDim(dim: string) {
  try {
    await settings.saveDimension(dim, editing.value[dim] ?? [])
    message.success('已保存')
  } catch (e: unknown) {
    const data = (e as { response?: { data?: { error?: string } } })?.response?.data
    const msg = data?.error
    message.error(typeof msg === 'string' ? msg : '保存失败（key 重复或仍有引用等）')
  }
}

function addRow(dim: string) {
  const list = editing.value[dim] ?? []
  list.push({
    key: '',
    label: '',
    sortOrder: list.length,
    enabled: true,
  })
  editing.value[dim] = list
}

function removeRow(dim: string, index: number) {
  editing.value[dim]?.splice(index, 1)
}

function onReset() {
  dialog.warning({
    title: '恢复默认',
    content: '将覆盖当前所有自定义选项，确定继续？',
    positiveText: '恢复',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await settings.reset()
        await refresh()
        message.success('已恢复默认')
      } catch {
        message.error('操作失败')
      }
    },
  })
}
</script>

<template>
  <n-space vertical size="large">
    <n-space justify="space-between" align="center">
      <n-h2 style="margin: 0">设置</n-h2>
      <n-space>
        <n-button @click="refresh">刷新</n-button>
        <n-button type="warning" @click="onReset">恢复默认</n-button>
      </n-space>
    </n-space>
    <n-spin :show="settings.loading">
      <n-space vertical size="large">
        <n-card title="账户类型">
          <n-table v-if="editing[DIM_ACCOUNT_TYPE]" :single-line="false" size="small">
            <thead>
              <tr>
                <th>Key</th>
                <th>显示名称</th>
                <th>排序</th>
                <th>启用</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in editing[DIM_ACCOUNT_TYPE]" :key="i">
                <td><n-input v-model:value="row.key" size="small" placeholder="英文 key" /></td>
                <td><n-input v-model:value="row.label" size="small" /></td>
                <td style="width: 100px">
                  <n-input-number v-model:value="row.sortOrder" size="small" :show-button="false" />
                </td>
                <td><n-switch v-model:value="row.enabled" /></td>
                <td><n-button size="tiny" quaternary @click="removeRow(DIM_ACCOUNT_TYPE, i)">删</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <n-space style="margin-top: 12px">
            <n-button size="small" dashed @click="addRow(DIM_ACCOUNT_TYPE)">新增</n-button>
            <n-button size="small" type="primary" @click="saveDim(DIM_ACCOUNT_TYPE)">保存本组</n-button>
          </n-space>
        </n-card>

        <n-card title="账户归属">
          <n-table v-if="editing[DIM_ACCOUNT_OWNER]" :single-line="false" size="small">
            <thead>
              <tr>
                <th>Key</th>
                <th>显示名称</th>
                <th>排序</th>
                <th>启用</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in editing[DIM_ACCOUNT_OWNER]" :key="i">
                <td><n-input v-model:value="row.key" size="small" /></td>
                <td><n-input v-model:value="row.label" size="small" /></td>
                <td style="width: 100px">
                  <n-input-number v-model:value="row.sortOrder" size="small" :show-button="false" />
                </td>
                <td><n-switch v-model:value="row.enabled" /></td>
                <td><n-button size="tiny" quaternary @click="removeRow(DIM_ACCOUNT_OWNER, i)">删</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <n-space style="margin-top: 12px">
            <n-button size="small" dashed @click="addRow(DIM_ACCOUNT_OWNER)">新增</n-button>
            <n-button size="small" type="primary" @click="saveDim(DIM_ACCOUNT_OWNER)">保存本组</n-button>
          </n-space>
        </n-card>

        <n-card title="大事记分类">
          <n-table v-if="editing[DIM_EVENT_CATEGORY]" :single-line="false" size="small">
            <thead>
              <tr>
                <th>Key</th>
                <th>显示名称</th>
                <th>排序</th>
                <th>启用</th>
                <th />
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, i) in editing[DIM_EVENT_CATEGORY]" :key="i">
                <td><n-input v-model:value="row.key" size="small" /></td>
                <td><n-input v-model:value="row.label" size="small" /></td>
                <td style="width: 100px">
                  <n-input-number v-model:value="row.sortOrder" size="small" :show-button="false" />
                </td>
                <td><n-switch v-model:value="row.enabled" /></td>
                <td><n-button size="tiny" quaternary @click="removeRow(DIM_EVENT_CATEGORY, i)">删</n-button></td>
              </tr>
            </tbody>
          </n-table>
          <n-space style="margin-top: 12px">
            <n-button size="small" dashed @click="addRow(DIM_EVENT_CATEGORY)">新增</n-button>
            <n-button size="small" type="primary" @click="saveDim(DIM_EVENT_CATEGORY)">保存本组</n-button>
          </n-space>
        </n-card>
      </n-space>
    </n-spin>
  </n-space>
</template>
