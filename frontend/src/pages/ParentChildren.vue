<template>
  <div class="task-page">
    <div class="vea-stat-grid task-stats">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">孩子数量</div>
          <div class="vea-stat-value">{{ stats.children }}</div>
          <div class="vea-stat-foot">家庭账号数</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">任务数量</div>
          <div class="vea-stat-value">{{ stats.tasks }}</div>
          <div class="vea-stat-foot">近 {{ days }} 天新增 {{ stats.tasksInRange }}</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">奖励数量</div>
          <div class="vea-stat-value">{{ stats.rewards }}</div>
          <div class="vea-stat-foot">可兑换奖励</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-red"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待审核兑换</div>
          <div class="vea-stat-value">{{ stats.pending }}</div>
          <div class="vea-stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-head">
        <div class="table-info">
          <div class="table-title">孩子列表</div>
          <div class="notice">当前 {{ filteredChildren.length }} 个孩子账号</div>
        </div>
      </div>

      <div class="permission-filter-bar">
        <div class="permission-filter">
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input
              v-model="filters.keyword"
              size="small"
              placeholder="搜索姓名/账号"
              clearable
              style="width: 220px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">开始:</span>
            <el-date-picker
              v-model="filters.startDate"
              size="small"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
              clearable
              :disabled-date="disableStartDate"
              style="width: 150px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">结束:</span>
            <el-date-picker
              v-model="filters.endDate"
              size="small"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              clearable
              :disabled-date="disableEndDate"
              style="width: 150px;"
            />
          </div>
        </div>
        <div class="permission-filter-actions">
          <VeaButton class="btn-sm btn-soft info" @click="loadAll">
            <span class="btn-icon"><el-icon><Search /></el-icon></span>
            查询
          </VeaButton>
          <VeaButton class="btn-sm btn-soft neutral" @click="resetFilters">
            <span class="btn-icon"><el-icon><Refresh /></el-icon></span>
            重置
          </VeaButton>
        </div>
      </div>

      <div class="vea-table-toolbar">
        <VeaButton v-if="can('parent.child.create')" class="btn-sm btn-soft success" @click="openChildModal('create')">
          <span class="btn-icon"><el-icon><Plus /></el-icon></span>
          新增
        </VeaButton>
      </div>

      <div class="table-wrap">
        <table class="table vea-table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>名称</th>
              <th>账号</th>
              <th>积分</th>
              <th style="width: 180px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(child, index) in pagedChildren" :key="child.id">
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ child.displayName }}</td>
              <td>{{ child.username }}</td>
              <td>{{ child.points }}</td>
              <td>
                <div class="action-buttons">
                  <VeaButton class="btn-sm btn-soft info" @click="openChildModal('edit', child)">
                    <span class="btn-icon"><el-icon><Edit /></el-icon></span>
                    修改
                  </VeaButton>
                  <VeaButton class="btn-sm btn-soft danger" @click="removeChild(child)">
                    <span class="btn-icon"><el-icon><Delete /></el-icon></span>
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
            <tr v-if="!pagedChildren.length">
              <td colspan="5" class="notice">暂无孩子</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <select v-model.number="pageSize">
          <option :value="10">每页 10 条</option>
          <option :value="20">每页 20 条</option>
          <option :value="50">每页 50 条</option>
        </select>
        <VeaButton class="secondary" :disabled="currentPage === 1" @click="currentPage--" text>上一页</VeaButton>
        <div class="notice">第 {{ currentPage }} / {{ totalPages }} 页</div>
        <VeaButton class="secondary" :disabled="currentPage >= totalPages" @click="currentPage++" text>下一页</VeaButton>
      </div>
    </div>

    <div v-if="showChildModal" class="drawer-backdrop" @click="closeChildModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ formMode === 'create' ? '新增孩子' : '修改孩子' }}</div>
          <VeaButton class="modal-close" @click="closeChildModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <label>账号</label>
          <input v-model="childForm.username" placeholder="孩子账号" />
          <label style="margin-top: 10px;">密码</label>
          <input
            v-model="childForm.password"
            type="password"
            :placeholder="formMode === 'create' ? '孩子密码' : '留空则不修改密码'"
          />
          <label style="margin-top: 10px;">显示名</label>
          <input v-model="childForm.displayName" placeholder="例如：小明" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeChildModal" text>取消</VeaButton>
          <VeaButton v-if="can('parent.child.create')" @click="saveChild">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import {
  Plus,
  Check,
  UserFilled,
  Calendar,
  Present,
  Checked,
  Search,
  Refresh,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'

const children = ref([])
const filters = ref({
  keyword: '',
  startDate: '',
  endDate: ''
})
const childForm = ref({ id: null, username: '', password: '', displayName: '' })
const { stats, days, notifyUpdate } = useParentStats()
const showChildModal = ref(false)
const formMode = ref('create')
const pageSize = ref(10)
const currentPage = ref(1)

const disableStartDate = (date) => {
  if (!filters.value.endDate) return false
  return date.getTime() > new Date(filters.value.endDate).getTime()
}

const disableEndDate = (date) => {
  if (!filters.value.startDate) return false
  return date.getTime() < new Date(filters.value.startDate).getTime()
}

const loadAll = async () => {
  const params = {}
  const { startDate, endDate } = filters.value
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  const { data } = await api.get('/parent/children', { params })
  children.value = Array.isArray(data) ? data : []
  clampPage()
}

const filteredChildren = computed(() => {
  if (!filters.value.keyword) return children.value
  const keyword = filters.value.keyword.toLowerCase()
  return children.value.filter((child) => {
    const displayName = String(child.displayName || '').toLowerCase()
    const username = String(child.username || '').toLowerCase()
    return displayName.includes(keyword) || username.includes(keyword)
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredChildren.value.length / pageSize.value)))
const pagedChildren = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredChildren.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(filteredChildren.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

const openChildModal = (mode, child = null) => {
  formMode.value = mode
  if (mode === 'edit' && child) {
    childForm.value = {
      id: child.id,
      username: child.username || '',
      password: '',
      displayName: child.displayName || ''
    }
  } else {
    childForm.value = { id: null, username: '', password: '', displayName: '' }
  }
  showChildModal.value = true
}

const closeChildModal = () => {
  showChildModal.value = false
  childForm.value = { id: null, username: '', password: '', displayName: '' }
}

const saveChild = async () => {
  if (!childForm.value.username.trim() || !childForm.value.displayName.trim()) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (formMode.value === 'create' && !childForm.value.password.trim()) {
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

  if (formMode.value === 'create') {
    await api.post('/parent/children', {
      username: childForm.value.username.trim(),
      password: childForm.value.password,
      displayName: childForm.value.displayName.trim()
    })
    ElMessage.success('新增成功')
  } else {
    await api.put(`/parent/children/${childForm.value.id}`, {
      username: childForm.value.username.trim(),
      password: childForm.value.password,
      displayName: childForm.value.displayName.trim()
    })
    ElMessage.success('修改成功')
  }

  await loadAll()
  notifyUpdate()
  closeChildModal()
}

const removeChild = async (child) => {
  try {
    await ElMessageBox.confirm(`确定删除孩子 ${child.displayName} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/parent/children/${child.id}`)
  ElMessage.success('删除成功')
  await loadAll()
  notifyUpdate()
}

const resetFilters = async () => {
  filters.value.keyword = ''
  filters.value.startDate = ''
  filters.value.endDate = ''
  currentPage.value = 1
  await loadAll()
}

watch([filteredChildren, pageSize], () => {
  clampPage()
})

watch(filteredChildren, () => {
  currentPage.value = 1
})

onMounted(loadAll)
</script>

<style scoped>
.task-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.task-stats {
  margin-top: 0;
}

.table-card {
  display: grid;
  gap: 12px;
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
  gap: 14px 18px;
  flex-wrap: wrap;
  flex: 1;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.filter-label {
  font-size: 12px;
  color: #606266;
}

.permission-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex: 0 0 auto;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-info {
  display: grid;
  gap: 6px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.table-wrap {
  margin-top: 0;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

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

@media (max-width: 960px) {
  .permission-filter-bar {
    align-items: stretch;
  }

  .permission-filter {
    width: 100%;
  }

  .permission-filter-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .action-buttons {
    flex-wrap: wrap;
    white-space: normal;
  }
}
</style>
