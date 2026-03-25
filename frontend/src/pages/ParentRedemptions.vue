<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>兑换审核</h2>
        <div class="notice">处理孩子的兑换申请</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-red"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待审核兑换</div>
          <div class="vea-stat-value">{{ stats.pending }}</div>
          <div class="vea-stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
        </div>
      </div>
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
    </div>

    <div class="card">
      <div class="table-info">
        <div class="table-title">兑换审核列表</div>
        <div class="notice">当前 {{ filteredRedemptions.length }} 条记录</div>
      </div>

      <div class="redemption-filter-bar">
        <div class="redemption-filter">
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input
              v-model="draftFilters.keyword"
              size="small"
              placeholder="搜索孩子/奖励/备注"
              clearable
              style="width: 220px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">状态:</span>
            <el-select v-model="draftFilters.status" size="small" style="width: 140px;">
              <el-option label="全部" value="ALL" />
              <el-option label="待审核" value="PENDING" />
              <el-option label="已同意" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">开始:</span>
            <el-date-picker
              v-model="draftFilters.startDate"
              type="date"
              size="small"
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
              v-model="draftFilters.endDate"
              type="date"
              size="small"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              clearable
              :disabled-date="disableEndDate"
              style="width: 150px;"
            />
          </div>
        </div>
        <div class="redemption-filter-actions">
          <VeaButton class="btn-sm btn-soft info" @click="applyFilters">
            <span class="btn-icon"><el-icon><Search /></el-icon></span>
            查询
          </VeaButton>
          <VeaButton class="btn-sm btn-soft neutral" @click="resetFilters">
            <span class="btn-icon"><el-icon><Refresh /></el-icon></span>
            清空
          </VeaButton>
        </div>
      </div>

      <div class="row compact" style="max-width: 520px; margin-bottom: 12px; justify-content: flex-start;">
        <VeaButton class="btn-sm btn-soft success" v-if="can('parent.redemption.review')" @click="openBatchModal('approve')" :disabled="!selectedIds.length">
          <span class="btn-icon"><el-icon><Check /></el-icon></span>
          批量同意
        </VeaButton>
        <VeaButton class="btn-sm btn-soft danger" v-if="can('parent.redemption.review')" @click="openBatchModal('reject')" :disabled="!selectedIds.length">
          <span class="btn-icon"><el-icon><Close /></el-icon></span>
          批量拒绝
        </VeaButton>
        <div class="notice">已选 {{ selectedIds.length }} 项</div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>
                <input type="checkbox" :checked="allSelected" @change="toggleAll($event)" style="width: auto;" />
              </th>
              <th style="width: 60px;">序号</th>
              <th>孩子</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>申请时间</th>
              <th>审核备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in filteredRedemptions" :key="item.id">
              <td>
                <input type="checkbox" :disabled="item.status !== 'PENDING'" :value="item.id" v-model="selectedIds" style="width: auto;" />
              </td>
              <td>{{ index + 1 }}</td>
              <td>{{ item.childName }}</td>
              <td>{{ item.rewardName }}</td>
              <td>{{ item.pointsCost }}</td>
              <td>{{ statusLabel(item.status) }}</td>
              <td>{{ formatDateTime(item.redeemedAt || item.createdAt) }}</td>
              <td>{{ item.reviewNote || '-' }}</td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton class="btn-sm btn-soft success" v-if="item.status === 'PENDING' && can('parent.redemption.review')" @click="openSingleModal('approve', item.id)">
                    <span class="btn-icon"><el-icon><Check /></el-icon></span>
                    同意
                  </VeaButton>
                  <VeaButton class="btn-sm btn-soft danger" v-if="item.status === 'PENDING' && can('parent.redemption.review')" @click="openSingleModal('reject', item.id)">
                    <span class="btn-icon"><el-icon><Close /></el-icon></span>
                    拒绝
                  </VeaButton>
                  <span v-else class="notice">已处理</span>
                </div>
              </td>
            </tr>
            <tr v-if="!filteredRedemptions.length">
              <td colspan="9" class="notice">暂无兑换记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showReviewModal" class="drawer-backdrop" @click="closeReviewModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ reviewMode === 'approve' ? '同意兑换' : '拒绝兑换' }}</div>
          <VeaButton class="modal-close" @click="closeReviewModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice" style="margin-bottom: 10px;">已选 {{ reviewIds.length }} 项</div>
          <label>处理备注（可选）</label>
          <input v-model="reviewNote" placeholder="填写审核备注" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeReviewModal" text>取消</VeaButton>
          <VeaButton @click="confirmReview">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Check, Close, UserFilled, Calendar, Present, Checked, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'
import { formatDateTime } from '../utils'

const redemptions = ref([])
const draftFilters = ref({
  keyword: '',
  status: 'ALL',
  startDate: '',
  endDate: ''
})
const filters = ref({
  keyword: '',
  status: 'ALL',
  startDate: '',
  endDate: ''
})
const selectedIds = ref([])
const reviewNote = ref('')
const showReviewModal = ref(false)
const reviewMode = ref('approve')
const reviewIds = ref([])
const { stats, days, refresh, notifyUpdate } = useParentStats()

const disableStartDate = (date) => {
  if (!draftFilters.value.endDate) return false
  return date.getTime() > new Date(draftFilters.value.endDate).getTime()
}

const disableEndDate = (date) => {
  if (!draftFilters.value.startDate) return false
  return date.getTime() < new Date(draftFilters.value.startDate).getTime()
}

const filteredRedemptions = computed(() => {
  const keyword = filters.value.keyword.trim().toLowerCase()
  const { status, startDate, endDate } = filters.value

  return redemptions.value.filter((item) => {
    const matchKeyword =
      !keyword ||
      String(item.childName || '').toLowerCase().includes(keyword) ||
      String(item.rewardName || '').toLowerCase().includes(keyword) ||
      String(item.reviewNote || '').toLowerCase().includes(keyword)

    const matchStatus = status === 'ALL' || item.status === status
    const recordDate = String(item.redeemedAt || item.createdAt || '').slice(0, 10)
    const matchDate =
      (!startDate || (recordDate && recordDate >= startDate)) &&
      (!endDate || (recordDate && recordDate <= endDate))

    return matchKeyword && matchStatus && matchDate
  })
})

const allSelected = computed(() => {
  const pendingIds = filteredRedemptions.value.filter(item => item.status === 'PENDING').map(item => item.id)
  return pendingIds.length > 0 && pendingIds.every(id => selectedIds.value.includes(id))
})

const statusLabel = (status) => {
  if (status === 'PENDING') return '待审核'
  if (status === 'APPROVED') return '已同意'
  if (status === 'REJECTED') return '已拒绝'
  return status || '-'
}

const applyFilters = () => {
  filters.value = { ...draftFilters.value }
  selectedIds.value = selectedIds.value.filter((id) => filteredRedemptions.value.some((item) => item.id === id))
}

const resetFilters = () => {
  draftFilters.value = {
    keyword: '',
    status: 'ALL',
    startDate: '',
    endDate: ''
  }
  filters.value = {
    keyword: '',
    status: 'ALL',
    startDate: '',
    endDate: ''
  }
  selectedIds.value = selectedIds.value.filter((id) => redemptions.value.some((item) => item.id === id))
}

const loadAll = async () => {
  const { data } = await api.get('/parent/redemptions')
  redemptions.value = data
  selectedIds.value = selectedIds.value.filter((id) => redemptions.value.some((item) => item.id === id))
}

const approveRedemption = async (id, note) => {
  await api.post(`/parent/redemptions/${id}/approve`, { note })
  await loadAll()
  notifyUpdate()
}

const rejectRedemption = async (id, note) => {
  await api.post(`/parent/redemptions/${id}/reject`, { note })
  await loadAll()
  notifyUpdate()
}

const toggleAll = (event) => {
  if (event.target.checked) {
    selectedIds.value = filteredRedemptions.value.filter(item => item.status === 'PENDING').map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const approveBatch = async (ids, note) => {
  await api.post('/parent/redemptions/batch-approve', {
    ids,
    note
  })
  selectedIds.value = []
  await loadAll()
  notifyUpdate()
}

const rejectBatch = async (ids, note) => {
  await api.post('/parent/redemptions/batch-reject', {
    ids,
    note
  })
  selectedIds.value = []
  await loadAll()
  notifyUpdate()
}

const openSingleModal = (mode, id) => {
  reviewMode.value = mode
  reviewIds.value = [id]
  reviewNote.value = ''
  showReviewModal.value = true
}

const openBatchModal = (mode) => {
  reviewMode.value = mode
  reviewIds.value = selectedIds.value.slice()
  reviewNote.value = ''
  showReviewModal.value = true
}

const closeReviewModal = () => {
  showReviewModal.value = false
  reviewNote.value = ''
  reviewIds.value = []
}

const confirmReview = async () => {
  if (!reviewIds.value.length) return
  const promptText = reviewMode.value === 'approve' ? '确认同意所选兑换吗？' : '确认拒绝所选兑换吗？'
  try {
    await ElMessageBox.confirm(promptText, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const note = reviewNote.value
  if (reviewIds.value.length === 1) {
    if (reviewMode.value === 'approve') {
      await approveRedemption(reviewIds.value[0], note)
    } else {
      await rejectRedemption(reviewIds.value[0], note)
    }
  } else {
    if (reviewMode.value === 'approve') {
      await approveBatch(reviewIds.value, note)
    } else {
      await rejectBatch(reviewIds.value, note)
    }
  }
  ElMessage.success('操作成功')
  closeReviewModal()
}

onMounted(() => {
  refresh()
  loadAll()
})
</script>

<style scoped>
.table-info {
  display: grid;
  gap: 6px;
  margin-bottom: 12px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.redemption-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  flex-wrap: wrap;
}

.redemption-filter {
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
  white-space: nowrap;
}

.redemption-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.redemption-filter-bar :deep(.el-input__wrapper),
.redemption-filter-bar :deep(.el-select__wrapper),
.redemption-filter-bar :deep(.el-date-editor .el-input__wrapper) {
  min-height: 32px !important;
}

.redemption-filter-bar :deep(.el-input--small),
.redemption-filter-bar :deep(.el-select--small),
.redemption-filter-bar :deep(.el-date-editor.el-input--small) {
  --el-component-size-small: 32px;
}

@media (max-width: 960px) {
  .redemption-filter {
    width: 100%;
  }

  .redemption-filter-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>

