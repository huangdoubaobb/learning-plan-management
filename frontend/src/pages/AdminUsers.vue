<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>用户列表</h2>
        <div class="notice">查看系统中的账号信息</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><User /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">用户数量</div>
          <div class="vea-stat-value">{{ users.length }}</div>
          <div class="vea-stat-foot">当前用户总数</div>
        </div>
      </div>
    </div>

    <div class="card vea-panel">
      <div class="vea-panel-header">
        <div class="vea-panel-title">用户列表</div>
      </div>
      <div class="vea-filter-bar">
        <div class="vea-toolbar">
          <div class="vea-toolbar-left">
            <span class="filter-label">关键词：</span>
            <el-input v-model="filters.keyword" placeholder="搜索用户名/显示名" clearable style="width: 200px;" />
            <span class="filter-label">角色：</span>
            <el-select v-model="filters.roleCode" class="role-filter-select" clearable placeholder="全部角色" style="width: 160px;">
              <el-option v-for="role in roles" :key="role.code" :label="role.name" :value="role.code" />
            </el-select>
            <span class="filter-label">开始：</span>
            <el-date-picker
              v-model="filters.startDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
              clearable
              :disabled-date="disableStartDate"
              style="width: 150px;"
            />
            <span class="filter-label">结束：</span>
            <el-date-picker
              v-model="filters.endDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              clearable
              :disabled-date="disableEndDate"
              style="width: 150px;"
            />
          </div>
          <div class="vea-toolbar-right">
            <VeaButton type="primary" :icon="Search" @click="loadAll">查询</VeaButton>
            <VeaButton @click="resetFilters">重置</VeaButton>
          </div>
        </div>
      </div>
      <div class="vea-table-toolbar">
        <VeaButton v-if="can('admin.user.create')" type="primary" :icon="Plus" @click="openCreate">新增</VeaButton>
        <div class="vea-table-actions">
          <VeaButton type="danger" plain :disabled="!selectedUserIds.length" @click="batchRemove">批量删除</VeaButton>
          <VeaButton :icon="Download" @click="exportUsers">导出</VeaButton>
        </div>
      </div>
      <div class="table-wrap">
        <table class="table vea-table">
          <thead>
            <tr>
              <th style="width: 44px;">
                <el-checkbox
                  :model-value="allPageSelected"
                  :indeterminate="pageIndeterminate"
                  @change="toggleSelectAll"
                />
              </th>
              <th style="width: 60px;">序号</th>
              <th>用户名</th>
              <th>显示名</th>
              <th>角色</th>
              <th>积分</th>
              <th>状态</th>
              <th>最近登录</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, index) in pagedUsers" :key="user.id">
              <td><el-checkbox v-model="selectedUserIds" :label="user.id" /></td>
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.displayName }}</td>
              <td>
                <el-tag size="small" effect="plain">{{ user.role }}</el-tag>
              </td>
              <td>{{ user.points }}</td>
              <td>
                <el-tag size="small" :type="user.enabled ? 'success' : 'info'">
                  {{ user.enabled ? '启用' : '禁用' }}
                </el-tag>
              </td>
              <td>{{ formatDateTime(user.lastLoginAt) }}</td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton type="success" plain :icon="Edit" v-if="can('admin.user.edit')" @click="editUser(user)">
                    编辑
                  </VeaButton>
                  <VeaButton type="danger" plain :icon="Delete" v-if="can('admin.user.delete')" @click="removeUser(user)">
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
            <tr v-if="!pagedUsers.length">
              <td colspan="9" class="notice">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="vea-pagination">
        <VeaButton :icon="Refresh" @click="loadAll">刷新</VeaButton>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="filteredUsers.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          @size-change="pageSize = $event"
          @current-change="currentPage = $event"
        />
      </div>
    </div>

    <div v-if="showUserModal" class="drawer-backdrop" @click="closeModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ formMode === 'create' ? '新增用户' : '编辑用户' }}</div>
          <VeaButton text @click="closeModal">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <el-form label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="form.username" :disabled="formMode === 'edit'" placeholder="用户名" />
            </el-form-item>
            <el-form-item label="显示名">
              <el-input v-model="form.displayName" placeholder="显示名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" placeholder="密码，编辑时留空则不修改" type="password" />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="form.roleCode" placeholder="选择角色">
                <el-option v-for="role in roles" :key="role.code" :label="role.name" :value="role.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="积分">
              <el-input v-model.number="form.points" placeholder="积分" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="form.enabled">
                <el-option :value="true" label="启用" />
                <el-option :value="false" label="禁用" />
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <div class="drawer-footer">
          <VeaButton @click="closeModal">取消</VeaButton>
          <VeaButton v-if="formMode === 'create' ? can('admin.user.create') : can('admin.user.edit')" type="primary" :icon="Check" @click="saveUser">
            保存
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { Plus, Edit, Delete, Check, User, Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import VeaButton from '../components/VeaButton.vue'
import { downloadCsv, formatDateTime } from '../utils'

const users = ref([])
const roles = ref([])
const filters = ref({
  keyword: '',
  roleCode: '',
  startDate: '',
  endDate: ''
})
const formMode = ref('create')
const showUserModal = ref(false)
const pageSize = ref(10)
const currentPage = ref(1)
const selectedUserIds = ref([])

const disableStartDate = (date) => {
  if (!filters.value.endDate) return false
  return date.getTime() > new Date(filters.value.endDate).getTime()
}

const disableEndDate = (date) => {
  if (!filters.value.startDate) return false
  return date.getTime() < new Date(filters.value.startDate).getTime()
}

const pageUserIds = computed(() => pagedUsers.value.map(u => u.id))
const pageSelectedCount = computed(() => pageUserIds.value.filter(id => selectedUserIds.value.includes(id)).length)
const allPageSelected = computed(() => pageUserIds.value.length > 0 && pageSelectedCount.value === pageUserIds.value.length)
const pageIndeterminate = computed(() => pageSelectedCount.value > 0 && pageSelectedCount.value < pageUserIds.value.length)

const toggleSelectAll = (val) => {
  const set = new Set(selectedUserIds.value)
  if (val) {
    pageUserIds.value.forEach(id => set.add(id))
  } else {
    pageUserIds.value.forEach(id => set.delete(id))
  }
  selectedUserIds.value = Array.from(set)
}

const form = reactive({
  id: null,
  username: '',
  displayName: '',
  password: '',
  roleCode: '',
  points: 0,
  enabled: true
})

const syncSelectedUsers = () => {
  const validIds = new Set(filteredUsers.value.map(user => user.id))
  selectedUserIds.value = selectedUserIds.value.filter(id => validIds.has(id))
}

const loadAll = async () => {
  const params = {}
  const { startDate, endDate } = filters.value
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  const { data } = await api.get('/admin/users', { params })
  users.value = data || []
  clampPage()
  syncSelectedUsers()
}

const loadRoles = async () => {
  const { data } = await api.get('/admin/roles')
  roles.value = data
}

const resetForm = () => {
  formMode.value = 'create'
  form.id = null
  form.username = ''
  form.displayName = ''
  form.password = ''
  form.roleCode = roles.value[0]?.code || ''
  form.points = 0
  form.enabled = true
}

const openCreate = () => {
  resetForm()
  showUserModal.value = true
}

const saveUser = async () => {
  if (!form.username.trim() && formMode.value === 'create') {
    ElMessage.warning('请填写必填项')
    return
  }
  if (!form.displayName.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (!form.roleCode) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (formMode.value === 'create') {
    if (!form.password.trim()) {
      ElMessage.warning('请填写必填项')
      return
    }
    try {
      await ElMessageBox.confirm('确认保存吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    await api.post('/admin/users', {
      username: form.username.trim(),
      password: form.password,
      displayName: form.displayName.trim(),
      roleCode: form.roleCode,
      points: form.points,
      enabled: form.enabled
    })
    ElMessage.success('新增成功')
  } else {
    try {
      await ElMessageBox.confirm('确认保存吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    await api.put(`/admin/users/${form.id}`, {
      displayName: form.displayName.trim(),
      roleCode: form.roleCode,
      points: form.points,
      enabled: form.enabled,
      password: form.password
    })
    ElMessage.success('修改成功')
  }
  await loadAll()
  closeModal()
}

const editUser = (user) => {
  formMode.value = 'edit'
  form.id = user.id
  form.username = user.username
  form.displayName = user.displayName
  form.password = ''
  form.roleCode = user.role
  form.points = user.points
  form.enabled = user.enabled
  showUserModal.value = true
}

const removeUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 ${user.username} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/admin/users/${user.id}`)
  ElMessage.success('删除成功')
  await loadAll()
  if (form.id === user.id) {
    resetForm()
  }
}

const batchRemove = async () => {
  if (!selectedUserIds.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedUserIds.value.length} 个用户吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  for (const id of selectedUserIds.value) {
    await api.delete(`/admin/users/${id}`)
  }
  ElMessage.success('删除成功')
  selectedUserIds.value = []
  await loadAll()
}

const closeModal = () => {
  showUserModal.value = false
  resetForm()
}

const filteredUsers = computed(() => {
  let list = users.value
  if (filters.value.keyword) {
    const k = filters.value.keyword.toLowerCase()
    list = list.filter(u => {
      const name = String(u.username || '').toLowerCase()
      const display = String(u.displayName || '').toLowerCase()
      return name.includes(k) || display.includes(k)
    })
  }
  if (filters.value.roleCode) {
    list = list.filter(u => u.role === filters.value.roleCode)
  }
  return list
})

const pagedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

watch([users, pageSize], () => {
  clampPage()
})

watch(filteredUsers, () => {
  currentPage.value = 1
  syncSelectedUsers()
})

onMounted(async () => {
  await loadRoles()
  await loadAll()
  resetForm()
})

const resetFilters = () => {
  filters.value.keyword = ''
  filters.value.roleCode = ''
  filters.value.startDate = ''
  filters.value.endDate = ''
  loadAll()
}

const exportUsers = async () => {
  const header = '用户名,显示名,角色,积分,状态,最近登录'
  const rows = filteredUsers.value.map(row => {
    const status = row.enabled ? '启用' : '禁用'
    return `${row.username},${row.displayName},${row.role},${row.points},${status},${formatDateTime(row.lastLoginAt)}`
  })
  downloadCsv([header, ...rows], `users-${new Date().toISOString().slice(0, 10)}.csv`)
  ElMessage.success('导出成功')
}

</script>

<style scoped>
.drawer-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(20, 24, 31, 0.45);
  z-index: 999;
  display: flex;
  justify-content: flex-end;
}

.drawer-panel {
  width: min(520px, 92vw);
  height: 100vh;
  background: var(--card);
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  animation: drawerIn 0.25s ease both;
}

.drawer-header {
  padding: 16px 18px;
  border-bottom: 1px solid rgba(28, 27, 34, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.drawer-title {
  font-weight: 700;
  font-size: 16px;
}

.drawer-body {
  padding: 16px 18px;
  overflow: auto;
  flex: 1;
}

.drawer-footer {
  padding: 12px 18px 18px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid rgba(28, 27, 34, 0.08);
}

.drawer-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.drawer-form-grid .span-2 {
  grid-column: span 2;
}

.drawer-form-grid label {
  display: block;
  font-size: 12px;
  margin-bottom: 6px;
  color: var(--muted);
  font-weight: 600;
}

@keyframes drawerIn {
  from {
    transform: translateX(12px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
