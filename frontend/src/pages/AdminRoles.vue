<template>
  <div class="container">
    <div class="header"></div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><User /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">角色数量</div>
          <div class="vea-stat-value">{{ roles.length }}</div>
          <div class="vea-stat-foot">系统角色数量</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Lock /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">权限数量</div>
          <div class="vea-stat-value">{{ permissions.length }}</div>
          <div class="vea-stat-foot">可分配权限数量</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">选中角色权限</div>
          <div class="vea-stat-value">{{ selectedRole?.permissionIds?.length || 0 }}</div>
          <div class="vea-stat-foot">当前角色权限数量</div>
        </div>
      </div>
    </div>

    <div class="card vea-panel">
      <div class="vea-panel-header">
        <div class="vea-panel-title">角色列表</div>
      </div>
      <div class="vea-filter-bar">
        <div class="vea-toolbar">
          <div class="vea-toolbar-left">
            <span class="filter-label">关键词：</span>
            <el-input v-model="filters.keyword" size="small" placeholder="搜索角色/代码" clearable style="width: 180px;" />
            <span class="filter-label">时间：</span>
            <el-date-picker
              v-model="filters.dateRange"
              size="small"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px;"
            />
          </div>
          <div class="vea-toolbar-right">
            <VeaButton type="primary" :icon="Search" @click="loadAll">查询</VeaButton>
            <VeaButton @click="resetFilters">重置</VeaButton>
          </div>
        </div>
      </div>
      <div class="vea-table-toolbar">
        <VeaButton v-if="can('admin.role.create')" type="primary" :icon="Plus" @click="openRoleModal">创建</VeaButton>
        <div class="vea-table-actions">
          <VeaButton type="danger" plain :disabled="!selectedRoleIds.length" @click="batchRemove">批量删除</VeaButton>
          <VeaButton :icon="Download" @click="exportRoles">导出</VeaButton>
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
              <th>角色</th>
              <th>代码</th>
              <th>权限数量</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(role, index) in pagedRoles" :key="role.id">
              <td><el-checkbox v-model="selectedRoleIds" :label="role.id" /></td>
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ role.name }}</td>
              <td>{{ role.code }}</td>
              <td>{{ role.permissionIds.length }}</td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton v-if="can('admin.role.assign')" type="primary" plain :icon="Key" @click="openPermissionDrawer(role)">
                    分配权限
                  </VeaButton>
                  <VeaButton v-if="can('admin.role.create')" type="success" plain :icon="Edit" @click="openEditRole(role)">
                    编辑
                  </VeaButton>
                  <VeaButton v-if="can('admin.role.create')" type="danger" plain :icon="Delete" @click="removeRole(role)">
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
            <tr v-if="!pagedRoles.length">
              <td colspan="6" class="notice">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="vea-pagination">
        <VeaButton :icon="Refresh" @click="loadAll">刷新</VeaButton>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="filteredRoles.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          @size-change="pageSize = $event"
          @current-change="currentPage = $event"
        />
      </div>
    </div>

    <div v-if="showRoleModal" class="drawer-backdrop" @click="closeRoleModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ roleMode === 'create' ? '创建角色' : '编辑角色' }}</div>
          <VeaButton text @click="closeRoleModal">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <el-form label-width="90px">
            <el-form-item label="角色代码">
              <el-input v-model="newRole.code" placeholder="例如:EDITOR" :disabled="roleMode === 'edit'" />
            </el-form-item>
            <el-form-item label="角色名称">
              <el-input v-model="newRole.name" placeholder="例如:数据管理员" />
            </el-form-item>
          </el-form>
        </div>
        <div class="drawer-footer">
          <VeaButton @click="closeRoleModal">取消</VeaButton>
          <VeaButton v-if="can('admin.role.create')" type="primary" :icon="Check" @click="saveRole">保存</VeaButton>
        </div>
      </div>
    </div>

    <div v-if="showPermissionDrawer" class="drawer-backdrop" @click="closePermissionDrawer">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div>
            <div class="drawer-title">权限分配</div>
            <div class="notice">{{ selectedRole?.name }} - {{ selectedRole?.code }}</div>
          </div>
          <VeaButton text @click="closePermissionDrawer">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice">请按树状结构选择权限节点</div>
          <el-tree
            ref="permissionTreeRef"
            class="permission-tree"
            :data="permissionTree"
            node-key="id"
            default-expand-all
            show-checkbox
            :props="{ label: 'label', children: 'children' }"
            @check="syncCheckedPermissions"
          />
        </div>
        <div class="drawer-footer">
          <VeaButton @click="closePermissionDrawer">取消</VeaButton>
          <VeaButton v-if="can('admin.role.assign')" type="primary" :icon="Check" @click="savePermissions">保存</VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { Plus, Key, Edit, Delete, Check, User, Lock, Checked, Search, Refresh, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can, setPermissions } from '../permissions'
import VeaButton from '../components/VeaButton.vue'

const roles = ref([])
const permissions = ref([])
const filters = ref({
  keyword: '',
  dateRange: []
})
const newRole = ref({ code: '', name: '' })
const roleMode = ref('create')
const editingRoleId = ref(null)
const selectedRole = ref(null)
const selectedPermissionIds = ref([])
const selectedRoleIds = ref([])
const permissionTreeRef = ref(null)

const pageRoleIds = computed(() => pagedRoles.value.map(r => r.id))
const pageSelectedCount = computed(() => pageRoleIds.value.filter(id => selectedRoleIds.value.includes(id)).length)
const allPageSelected = computed(() => pageRoleIds.value.length > 0 && pageSelectedCount.value === pageRoleIds.value.length)
const pageIndeterminate = computed(() => pageSelectedCount.value > 0 && pageSelectedCount.value < pageRoleIds.value.length)

const toggleSelectAll = (val) => {
  const set = new Set(selectedRoleIds.value)
  if (val) {
    pageRoleIds.value.forEach(id => set.add(id))
  } else {
    pageRoleIds.value.forEach(id => set.delete(id))
  }
  selectedRoleIds.value = Array.from(set)
}
const showRoleModal = ref(false)
const showPermissionDrawer = ref(false)
const pageSize = ref(10)
const currentPage = ref(1)

const actionLabelMap = {
  create: '创建',
  edit: '编辑',
  delete: '删除',
  assign: '分配',
  list: '查看',
  view: '查看',
  detail: '详情',
  get: '查看',
  order: '排序',
  auto_group: '自动分组',
  review: '审核',
  export: '导出',
  complete: '完成',
  redeem: '兑换'
}

const menuLabelMap = {
  'admin.role': '角色管理',
  'admin.user': '用户管理',
  'admin.permission': '权限管理',
  'admin.log': '日志管理',
  'parent.child': '孩子管理',
  'parent.task': '任务管理',
  'parent.reward': '奖励管理',
  'parent.redemption': '兑换审核',
  'parent.report': '报表统计',
  'child.task': '任务管理',
  'child.reward': '奖励管理'
}

const permissionTree = computed(() => {
  const map = new Map()
  permissions.value.forEach(permission => {
    const code = String(permission.code || '')
    const parts = code.split('.')
    const actionKey = parts.pop() || ''
    const menuKey = parts.join('.') || code
    const actionLabel = actionLabelMap[actionKey] || permission.name || actionKey
    let menuLabel = menuLabelMap[menuKey] || permission.name || menuKey
    if (!menuLabelMap[menuKey] && permission.name && actionLabel && permission.name.endsWith(actionLabel)) {
      menuLabel = permission.name.slice(0, -actionLabel.length) || menuKey
    }
    if (!map.has(menuKey)) {
      map.set(menuKey, {
        id: `menu:${menuKey}`,
        label: menuLabel,
        children: []
      })
    }
    map.get(menuKey).children.push({
      id: permission.id,
      label: `${actionLabel} (${permission.code})`
    })
  })
  return Array.from(map.values())
})

const loadAll = async () => {
  const params = {}
  const [startDate, endDate] = filters.value.dateRange || []
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  const [roleRes, permRes] = await Promise.all([
    api.get('/admin/roles', { params }),
    api.get('/admin/permissions', { params })
  ])
  roles.value = roleRes.data
  permissions.value = permRes.data
  clampPage()
}

const selectRole = (role) => {
  selectedRole.value = role
  selectedPermissionIds.value = [...role.permissionIds]
}

const openPermissionDrawer = (role) => {
  selectRole(role)
  showPermissionDrawer.value = true
  nextTick(() => {
    if (permissionTreeRef.value) {
      permissionTreeRef.value.setCheckedKeys(selectedPermissionIds.value)
    }
  })
}

const closePermissionDrawer = () => {
  showPermissionDrawer.value = false
}

const syncCheckedPermissions = () => {
  if (!permissionTreeRef.value) return
  selectedPermissionIds.value = permissionTreeRef.value.getCheckedKeys(true)
}

const saveRole = async () => {
  if (!newRole.value.code || !newRole.value.name) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await ElMessageBox.confirm('确认保存吗?', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  if (roleMode.value === 'create') {
    await api.post('/admin/roles', newRole.value)
    ElMessage.success('创建成功')
  } else {
    await api.put(`/admin/roles/${editingRoleId.value}`, newRole.value)
    ElMessage.success('修改成功')
  }
  newRole.value = { code: '', name: '' }
  roleMode.value = 'create'
  editingRoleId.value = null
  await loadAll()
  closeRoleModal()
}

const openRoleModal = () => {
  newRole.value = { code: '', name: '' }
  roleMode.value = 'create'
  editingRoleId.value = null
  showRoleModal.value = true
}

const openEditRole = (role) => {
  newRole.value = { code: role.code, name: role.name }
  roleMode.value = 'edit'
  editingRoleId.value = role.id
  showRoleModal.value = true
}

const closeRoleModal = () => {
  showRoleModal.value = false
}

const removeRole = async (role) => {
  try {
    await ElMessageBox.confirm(`确认删除角色 ${role.name} 吗?`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/admin/roles/${role.id}`)
  ElMessage.success('删除成功')
  await loadAll()
}

const batchRemove = async () => {
  if (!selectedRoleIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRoleIds.value.length} 个角色吗?`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  for (const id of selectedRoleIds.value) {
    await api.delete(`/admin/roles/${id}`)
  }
  ElMessage.success('删除成功')
  selectedRoleIds.value = []
  await loadAll()
}

const exportRoles = async () => {
  const header = '\u89d2\u8272\u540d\u79f0,\u7f16\u7801,\u6743\u9650\u6570\u91cf'
  const rows = filteredRoles.value.map(row => `${row.name},${row.code},${row.permissionIds.length}`)
  const csv = [header, ...rows].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `roles-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

const savePermissions = async () => {
  if (!selectedRole.value) return
  await api.put(`/admin/roles/${selectedRole.value.id}/permissions`, {
    permissionIds: selectedPermissionIds.value
  })
  ElMessage.success('保存成功')
  const currentRole = localStorage.getItem('role')
  if (currentRole && selectedRole.value.code === currentRole) {
    try {
      const { data } = await api.get('/auth/me')
      setPermissions(data.permissions || [])
    } catch {
      setPermissions([])
    }
  }
  await loadAll()
  closePermissionDrawer()
}

const filteredRoles = computed(() => {
  if (!filters.value.keyword) return roles.value
  const k = filters.value.keyword.toLowerCase()
  return roles.value.filter(r => {
    const name = String(r.name || '').toLowerCase()
    const code = String(r.code || '').toLowerCase()
    return name.includes(k) || code.includes(k)
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRoles.value.length / pageSize.value)))
const pagedRoles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRoles.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(filteredRoles.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

watch([roles, pageSize], () => {
  clampPage()
})

onMounted(loadAll)

const resetFilters = () => {
  filters.value.keyword = ''
  filters.value.dateRange = []
  loadAll()
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

.permission-tree {
  margin-top: 12px;
  border: 1px solid rgba(28, 27, 34, 0.12);
  border-radius: 12px;
  padding: 10px 12px;
  background: #fff;
}

.drawer-footer {
  padding: 12px 18px 18px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid rgba(28, 27, 34, 0.08);
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
