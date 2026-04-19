<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NButton, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import type { User } from '@/types/models'
import * as userApi from '@/api/user'

const message = useMessage()
const rows = ref<User[]>([])
const loading = ref(true)
const showCreate = ref(false)
const showPwd = ref(false)
const pwdTarget = ref<User | null>(null)
const newUser = ref({ username: '', password: '', isAdmin: false })
const newPwd = ref('')

async function load() {
  rows.value = await userApi.listUsers()
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

async function create() {
  try {
    await userApi.createUser(newUser.value)
    message.success('已创建')
    showCreate.value = false
    newUser.value = { username: '', password: '', isAdmin: false }
    await load()
  } catch {
    message.error('创建失败')
  }
}

function openPwd(u: User) {
  pwdTarget.value = u
  newPwd.value = ''
  showPwd.value = true
}

async function savePwd() {
  if (!pwdTarget.value) {
    return
  }
  try {
    await userApi.resetPassword(pwdTarget.value.id, newPwd.value)
    message.success('密码已重置')
    showPwd.value = false
  } catch {
    message.error('失败')
  }
}

const columns: DataTableColumns<User> = [
  { title: '用户名', key: 'username' },
  {
    title: '管理员',
    key: 'is_admin',
    render(row) {
      return row.is_admin ? '是' : '否'
    },
  },
  {
    title: '须改密',
    key: 'must_change_password',
    render(row) {
      return row.must_change_password ? '是' : '否'
    },
  },
  {
    title: '操作',
    key: 'a',
    render(row) {
      return h(NButton, { size: 'small', onClick: () => openPwd(row) }, { default: () => '重置密码' })
    },
  },
]
</script>

<template>
  <n-space vertical size="large">
    <n-space justify="space-between">
      <n-h2 style="margin: 0">用户管理</n-h2>
      <n-button type="primary" @click="showCreate = true">新建用户</n-button>
    </n-space>
    <n-spin :show="loading">
      <n-data-table :columns="columns" :data="rows" :row-key="(r: User) => r.id" />
    </n-spin>
    <n-modal v-model:show="showCreate" preset="card" title="新建用户" style="width: 440px">
      <n-form>
        <n-form-item label="用户名">
          <n-input v-model:value="newUser.username" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="newUser.password" type="password" show-password-on="click" />
        </n-form-item>
        <n-form-item label="管理员">
          <n-switch v-model:value="newUser.isAdmin" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button type="primary" @click="create">创建</n-button>
      </template>
    </n-modal>
    <n-modal v-model:show="showPwd" preset="card" title="重置密码" style="width: 400px">
      <n-input v-model:value="newPwd" type="password" show-password-on="click" placeholder="新密码（至少 8 位）" />
      <template #footer>
        <n-button type="primary" @click="savePwd">保存</n-button>
      </template>
    </n-modal>
  </n-space>
</template>
