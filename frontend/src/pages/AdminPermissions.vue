<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>权限管理</h2>
        <div class="notice">菜单与按钮权限树</div>
      </div>
      <div class="notice">基于菜单第一层、按钮第二层展示</div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Lock /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">权限数量</div>
          <div class="vea-stat-value">{{ permissionItems.length }}</div>
          <div class="vea-stat-foot">可分配权限</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><Menu /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">菜单数量</div>
          <div class="vea-stat-value">{{ menuRoots.length }}</div>
          <div class="vea-stat-foot">菜单层级</div>
        </div>
      </div>
    </div>

    <div class="permission-shell">
      <div class="card vea-panel permission-tree-panel">
        <div class="vea-panel-header">
          <div class="vea-panel-title">权限树</div>
        </div>
        <el-tree
          class="permission-tree"
          :data="treeData"
          node-key="id"
          show-checkbox
          default-expand-all
          highlight-current
          :expand-on-click-node="false"
          @node-click="onTreeClick"
        />
      </div>
      <div class="card vea-panel permission-table-panel">
        <div class="vea-panel-header">
          <div>
            <div class="vea-panel-title">权限列表</div>
            <div class="notice">选择左侧权限树或筛选条件查看</div>
          </div>
        </div>
        <div class="permission-filter-bar">
          <div class="permission-filter">
            <div class="filter-item">
              <span class="filter-label">权限类别:</span>
              <el-select v-model="selectedMenuId" size="small" clearable placeholder="请选择" style="width: 180px;">
                <el-option v-for="menu in menuCategoryOptions" :key="menu.id" :label="menu.name" :value="menu.id" />
              </el-select>
            </div>
            <div class="filter-item">
              <span class="filter-label">搜索:</span>
              <el-input v-model="filters.keyword" size="small" placeholder="名称/编码" clearable style="width: 220px;" />
            </div>
          </div>
          <div class="permission-filter-actions">
            <VeaButton type="primary" :icon="Search" @click="applyFilters">查询</VeaButton>
            <VeaButton @click="resetFilters">重置</VeaButton>
            <VeaButton type="primary" :icon="Plus" @click="openCreate">新增</VeaButton>
          </div>
        </div>
        <div class="table-wrap">
          <table class="table compact vea-table">
            <thead>
              <tr>
                <th style="width: 38%;">菜单</th>
                <th style="width: 22%;">功能</th>
                <th>权限编码</th>
                <th style="width: 140px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pagedRows" :key="row.id">
                <td>{{ row.menu }}</td>
                <td>{{ row.action }}</td>
                <td class="mono">{{ row.code }}</td>
                <td>
                  <div class="permission-actions">
                    <VeaButton type="primary" plain :icon="Edit" @click="openEdit(row)">编辑</VeaButton>
                    <VeaButton type="danger" plain :icon="Delete" @click="removeMenu(row)">删除</VeaButton>
                  </div>
                </td>
              </tr>
              <tr v-if="!pagedRows.length">
                <td colspan="4" class="notice">暂无权限</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="permission-footer">
          <VeaButton :icon="Refresh" @click="loadAll">刷新</VeaButton>
          <el-pagination
            small
            background
            layout="prev, pager, next"
            :total="filteredTableRows.length"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="currentPage = $event"
          />
          <VeaButton type="success" :icon="Check" @click="exportPermissions">导出</VeaButton>
        </div>
      </div>
    </div>

    <el-drawer
      v-model="showMenuModal"
      class="permission-drawer"
      direction="rtl"
      size="420px"
      :with-header="false"
    >
      <div class="permission-drawer-header">
        <div class="permission-drawer-title">{{ menuMode === 'create' ? '新增权限' : '编辑权限' }}</div>
        <VeaButton class="permission-drawer-close" text @click="closeMenuModal">关闭</VeaButton>
      </div>
      <div class="permission-drawer-body">
        <el-form class="permission-dialog-form" label-width="90px">
          <el-form-item label="名称">
            <el-input v-model="menuForm.name" placeholder="名称" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="menuForm.type" placeholder="选择类型">
              <el-option :value="1" label="目录" />
              <el-option :value="2" label="菜单" />
              <el-option :value="3" label="按钮" />
            </el-select>
          </el-form-item>
          <el-form-item label="父级">
            <el-select v-model="menuForm.parentId" placeholder="顶级">
              <el-option :value="0" label="顶级" />
              <el-option v-for="menu in parentOptions" :key="menu.id" :value="menu.id" :label="menu.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="路径">
            <el-input v-model="menuForm.path" placeholder="如 /admin/roles" />
          </el-form-item>
          <el-form-item label="权限编码">
            <el-input v-model="menuForm.permission" placeholder="如 admin.role.list" />
          </el-form-item>
          <el-form-item label="图标">
            <el-input v-model="menuForm.icon" placeholder="如 setting/role/user" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input v-model.number="menuForm.sortOrder" type="number" />
          </el-form-item>
          <el-form-item label="可见">
            <el-switch v-model="menuForm.visible" />
          </el-form-item>
          <el-form-item label="缓存">
            <el-switch v-model="menuForm.cache" />
          </el-form-item>
        </el-form>
      </div>
      <div class="permission-drawer-footer">
        <VeaButton @click="closeMenuModal">取消</VeaButton>
        <VeaButton type="primary" :icon="Check" @click="saveMenu">保存</VeaButton>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Lock, Menu, Search, Refresh, Plus, Edit, Delete, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'
import { downloadCsv } from '../utils'

const menus = ref([])
const filters = ref({
  keyword: ''
})
const selectedMenuId = ref('')
const selectedPermissionId = ref('')
const pageSize = ref(8)
const currentPage = ref(1)
const showMenuModal = ref(false)
const menuMode = ref('create')
const editingMenuId = ref(null)
const menuForm = ref({
  name: '',
  type: 2,
  parentId: 0,
  path: '',
  permission: '',
  icon: '',
  sortOrder: 0,
  visible: true,
  cache: true
})

const loadAll = async () => {
  const { data } = await api.get('/admin/menus')
  menus.value = data || []
}

const permissionItems = computed(() => menus.value.filter(menu => menu.permission))

const menuRoots = computed(() => menus.value.filter(menu => (menu.parentId ?? 0) === 0))

const menuCategoryOptions = computed(() => {
  const roots = menuRoots.value.filter(menu => menu.type === 1 || menu.type === 2)
  return roots.length ? roots : menuRoots.value
})

const actionLabelMap = {
  create: '新增',
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

const treeData = computed(() => {
  const childrenMap = new Map()
  menus.value.forEach(menu => {
    const parentId = menu.parentId ?? 0
    if (!childrenMap.has(parentId)) childrenMap.set(parentId, [])
    childrenMap.get(parentId).push(menu)
  })
  const buildNodes = (parentId) => {
    const children = (childrenMap.get(parentId) || [])
      .slice()
      .sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0) || a.id - b.id)
    return children.map(menu => ({
      id: menu.id,
      label: menu.name,
      type: menu.type,
      permission: menu.permission,
      parentId: menu.parentId ?? 0,
      children: buildNodes(menu.id)
    }))
  }
  return buildNodes(0)
})

const onTreeClick = (data) => {
  if (data.type === 3) {
    selectedPermissionId.value = data.id
    selectedMenuId.value = data.parentId || ''
  } else {
    selectedMenuId.value = data.id
    selectedPermissionId.value = ''
  }
  currentPage.value = 1
}

const childMenus = computed(() => {
  if (!selectedMenuId.value) return permissionItems.value
  return menus.value.filter(menu => (menu.parentId ?? 0) === selectedMenuId.value)
})

const filteredChildMenus = computed(() => {
  if (!filters.value.keyword) return childMenus.value
  const k = filters.value.keyword.toLowerCase()
  return childMenus.value.filter(menu => {
    const name = String(menu.name || '').toLowerCase()
    const code = String(menu.permission || '').toLowerCase()
    return name.includes(k) || code.includes(k)
  })
})

const tableRows = computed(() => {
  return filteredChildMenus.value.map(menu => {
    const code = String(menu.permission || '')
    const parts = code.split('.')
    const actionKey = parts.pop() || ''
    const actionLabel = menu.type === 3 ? (actionLabelMap[actionKey] || menu.name || actionKey) : '菜单'
    return {
      id: menu.id,
      parentId: menu.parentId ?? 0,
      type: menu.type,
      menu: menu.name,
      action: actionLabel,
      code,
      raw: menu
    }
  })
})

const filteredTableRows = computed(() => {
  let rows = tableRows.value
  if (selectedPermissionId.value) {
    rows = rows.filter(row => row.id === selectedPermissionId.value)
  }
  return rows
})

const pagedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredTableRows.value.slice(start, start + pageSize.value)
})

onMounted(loadAll)

const resetFilters = () => {
  filters.value.keyword = ''
  selectedMenuId.value = ''
  selectedPermissionId.value = ''
  currentPage.value = 1
}

const applyFilters = () => {
  currentPage.value = 1
}

const exportPermissions = async () => {
  const header = '菜单,功能,权限编码'
  const rows = filteredTableRows.value.map(row => `${row.menu},${row.action},${row.code}`)
  downloadCsv([header, ...rows], `permissions-${new Date().toISOString().slice(0, 10)}.csv`)
  ElMessage.success('导出成功')
}

watch(selectedMenuId, () => {
  currentPage.value = 1
  selectedPermissionId.value = ''
})

watch(filteredTableRows, () => {
  currentPage.value = 1
})

const parentOptions = computed(() => {
  return menus.value.filter(menu => menu.type === 1 || menu.type === 2)
})

const openCreate = () => {
  menuMode.value = 'create'
  editingMenuId.value = null
  const selected = menus.value.find(menu => menu.id === selectedMenuId.value)
  let nextType = 1
  if (selected) {
    nextType = selected.type === 1 ? 2 : 3
  }
  menuForm.value = {
    name: '',
    type: nextType,
    parentId: selected?.id || 0,
    path: '',
    permission: '',
    icon: selected?.icon || '',
    sortOrder: 0,
    visible: true,
    cache: true
  }
  showMenuModal.value = true
}

const openEdit = (row) => {
  const menu = row.raw
  if (!menu) return
  menuMode.value = 'edit'
  editingMenuId.value = menu.id
  menuForm.value = {
    name: menu.name || '',
    type: menu.type || 2,
    parentId: menu.parentId ?? 0,
    path: menu.path || '',
    permission: menu.permission || '',
    icon: menu.icon || '',
    sortOrder: menu.sortOrder ?? 0,
    visible: menu.visible ?? true,
    cache: menu.cache ?? true
  }
  showMenuModal.value = true
}

const closeMenuModal = () => {
  showMenuModal.value = false
}

const saveMenu = async () => {
  if (!menuForm.value.name || menuForm.value.type === null || menuForm.value.type === undefined) {
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
  if (menuMode.value === 'create') {
    await api.post('/admin/menus', menuForm.value)
    ElMessage.success('新增成功')
  } else if (editingMenuId.value) {
    await api.put(`/admin/menus/${editingMenuId.value}`, menuForm.value)
    ElMessage.success('修改成功')
  }
  showMenuModal.value = false
  await loadAll()
}

const removeMenu = async (row) => {
  if (!row?.id) return
  try {
    await ElMessageBox.confirm(`确定删除 ${row.menu} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/admin/menus/${row.id}`)
  ElMessage.success('删除成功')
  await loadAll()
}
</script>

<style scoped>
.permission-shell {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 16px;
}

.permission-tree-panel {
  padding: 12px 10px;
}

.permission-tree {
  margin-top: 6px;
}

.permission-table-panel {
  display: flex;
  flex-direction: column;
  min-height: 420px;
}

.permission-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.permission-filter {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  color: #606266;
}

.permission-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}

.permission-dialog-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.permission-dialog-form :deep(.el-input),
.permission-dialog-form :deep(.el-select) {
  width: 100%;
}

.permission-dialog-form :deep(.el-switch) {
  margin-right: 0;
}

.permission-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}

.permission-drawer :deep(.el-drawer__body) {
  padding: 0;
}

.permission-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.permission-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.permission-drawer-close {
  padding: 0;
}

.permission-drawer-body {
  padding: 16px;
}

.permission-drawer-footer {
  padding: 12px 16px 16px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-wrap {
  width: 100%;
  overflow: auto;
  flex: 1;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

.table th,
.table td {
  padding: 10px 12px;
  border-bottom: 1px solid rgba(28, 27, 34, 0.08);
  text-align: left;
  vertical-align: middle;
}

.table thead th {
  font-weight: 600;
  background: #f6f8fb;
}

.table.compact td,
.table.compact th {
  padding: 8px 10px;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  color: #2b3642;
}

.permission-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 4px 0;
}

@media (max-width: 960px) {
  .permission-shell {
    grid-template-columns: 1fr;
  }

  .permission-footer {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
