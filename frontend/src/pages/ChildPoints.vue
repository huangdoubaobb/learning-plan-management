<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>积分明细</h2>
        <div class="notice">查看任务积分和奖励兑换记录</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Top /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">累计获得</div>
          <div class="vea-stat-value">{{ earnedTotal }}</div>
          <div class="vea-stat-foot">已完成任务获得的积分</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Bottom /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">累计消费</div>
          <div class="vea-stat-value">{{ spentTotal }}</div>
          <div class="vea-stat-foot">兑换奖励消耗的积分</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><Coin /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">当前积分</div>
          <div class="vea-stat-value">{{ me?.points ?? 0 }}</div>
          <div class="vea-stat-foot">当前积分余额</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-purple"><el-icon><TrendCharts /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">净变化</div>
          <div class="vea-stat-value">{{ netPoints >= 0 ? '+' : '' }}{{ netPoints }}</div>
          <div class="vea-stat-foot">当前筛选区间内净增减</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="table-info">
        <div class="table-title">积分筛选</div>
        <div class="notice">按时间和类型查看积分趋势与明细记录</div>
      </div>
      <div class="points-filter-bar">
        <div class="points-filter">
          <div class="filter-item">
            <span class="filter-label">开始:</span>
            <el-date-picker
              v-model="startDate"
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
              v-model="endDate"
              type="date"
              size="small"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              clearable
              :disabled-date="disableEndDate"
              style="width: 150px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">类型:</span>
            <el-select v-model="typeFilter" size="small" style="width: 140px;">
              <el-option label="全部类型" value="ALL" />
              <el-option label="任务获得" value="TASK" />
              <el-option label="兑换消费" value="REDEEM" />
            </el-select>
          </div>
        </div>
        <div class="points-filter-actions">
          <VeaButton class="btn-sm btn-soft info" @click="exportPoints">
            <span class="btn-icon"><el-icon><Download /></el-icon></span>
            导出 Excel
          </VeaButton>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>积分趋势</h3>
      <div class="notice">按天统计积分获得与消费</div>
      <div v-if="trendLabels.length" class="chart-summary-strip">
        <div class="chart-summary-pill">
          <span>净变化</span>
          <strong :class="{ 'is-positive': netPoints >= 0, 'is-negative': netPoints < 0 }">
            {{ netPoints >= 0 ? '+' : '' }}{{ netPoints }}
          </strong>
        </div>
        <div class="chart-summary-pill">
          <span>当前积分</span>
          <strong>{{ me?.points ?? 0 }}</strong>
        </div>
      </div>
      <div v-if="trendLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="trendDataset" :config="trendConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
    </div>

    <div v-if="showTasks" class="card">
      <div class="table-info">
        <div class="table-title">任务完成记录</div>
        <div class="notice">当前 {{ filteredCompletedTasks.length }} 条记录</div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>任务</th>
              <th>积分</th>
              <th>完成时间</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in filteredCompletedTasks" :key="task.id">
              <td>{{ index + 1 }}</td>
              <td>{{ task.title }}</td>
              <td>+{{ task.points }}</td>
              <td>{{ formatDateTime(task.completedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
            </tr>
            <tr v-if="!filteredCompletedTasks.length">
              <td colspan="5" class="notice">暂无任务积分记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showRedemptions" class="card">
      <div class="table-info">
        <div class="table-title">兑换记录</div>
        <div class="notice">当前 {{ filteredRedemptions.length }} 条记录</div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>审核备注</th>
              <th>审核时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in filteredRedemptions" :key="record.id">
              <td>{{ index + 1 }}</td>
              <td>{{ record.rewardName }}</td>
              <td>-{{ record.pointsCost }}</td>
              <td>{{ redemptionStatusLabel(record.status) }}</td>
              <td>{{ record.reviewNote || '-' }}</td>
              <td>{{ formatDateTime(record.reviewedAt) }}</td>
            </tr>
            <tr v-if="!filteredRedemptions.length">
              <td colspan="6" class="notice">暂无兑换记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Download, Top, Bottom, Coin, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'
import { makeBarSeries, makeXyConfig } from '../composables/useDataUiCharts'
import { downloadBlob, formatDateTime } from '../utils'

const me = ref(null)
const tasks = ref([])
const redemptions = ref([])
const startDate = ref('')
const endDate = ref('')
const typeFilter = ref('ALL')

const completedTasks = computed(() => tasks.value.filter((task) => task.status === 'COMPLETED'))

const extractRecordDate = (record) => {
  return record.reviewedAt || record.redeemedAt || record.completedAt || record.createdAt || record.createdTime || record.created_at || record.updatedAt || ''
}

const inRange = (value) => {
  if (!value) return true
  const dateStr = String(value).slice(0, 10)
  if (startDate.value && dateStr < startDate.value) return false
  if (endDate.value && dateStr > endDate.value) return false
  return true
}

const filteredCompletedTasks = computed(() => completedTasks.value.filter((task) => inRange(task.completedAt)))
const filteredRedemptions = computed(() => redemptions.value.filter((record) => inRange(extractRecordDate(record))))

const showTasks = computed(() => typeFilter.value === 'ALL' || typeFilter.value === 'TASK')
const showRedemptions = computed(() => typeFilter.value === 'ALL' || typeFilter.value === 'REDEEM')

const earnedTotal = computed(() => {
  if (!showTasks.value) return 0
  return filteredCompletedTasks.value.reduce((sum, task) => sum + (task.points || 0), 0)
})

const spentTotal = computed(() => {
  if (!showRedemptions.value) return 0
  return filteredRedemptions.value.reduce((sum, record) => sum + (record.pointsCost || 0), 0)
})

const netPoints = computed(() => earnedTotal.value - spentTotal.value)

const trendRows = computed(() => {
  const map = new Map()

  filteredCompletedTasks.value.forEach((task) => {
    const date = String(task.completedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.earned += task.points || 0
    map.set(date, row)
  })

  filteredRedemptions.value.forEach((record) => {
    const date = String(extractRecordDate(record) || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.spent += record.pointsCost || 0
    map.set(date, row)
  })

  return Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date))
})

const trendLabels = computed(() => trendRows.value.map((row) => row.date.slice(5)))
const trendDataset = computed(() => {
  const earned = trendRows.value.map((row) => row.earned || 0)
  const spent = trendRows.value.map((row) => row.spent || 0)
  return [
    ...makeBarSeries('获得', earned, '#60a5fa'),
    ...makeBarSeries('消费', spent, '#f97316')
  ]
})

const trendConfig = computed(() => makeXyConfig(trendLabels.value, {
  chart: {
    legend: {
      show: true
    }
  }
}))

const disableStartDate = (date) => {
  if (!endDate.value) return false
  return date.getTime() > new Date(endDate.value).getTime()
}

const disableEndDate = (date) => {
  if (!startDate.value) return false
  return date.getTime() < new Date(startDate.value).getTime()
}

const loadAll = async () => {
  const [meRes, taskRes, redemptionRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks'),
    api.get('/child/redemptions')
  ])
  me.value = meRes.data
  tasks.value = Array.isArray(taskRes.data) ? taskRes.data : []
  redemptions.value = Array.isArray(redemptionRes.data) ? redemptionRes.data : []
}

const exportPoints = async () => {
  const params = new URLSearchParams()
  if (startDate.value) params.append('startDate', startDate.value)
  if (endDate.value) params.append('endDate', endDate.value)
  const query = params.toString()
  const url = query ? `/child/points/export?${query}` : '/child/points/export'
  const res = await api.get(url, { responseType: 'blob' })
  downloadBlob(res.data, `points-${me.value?.username || 'child'}.xlsx`)
  ElMessage.success('导出成功')
}

const redemptionStatusLabel = (status) => {
  if (status === 'PENDING') return '待审核'
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  return status || '-'
}

onMounted(loadAll)
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

.points-filter-bar {
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

.points-filter {
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
}

.filter-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.points-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.points-filter-bar :deep(.el-input__wrapper),
.points-filter-bar :deep(.el-select__wrapper),
.points-filter-bar :deep(.el-date-editor .el-input__wrapper) {
  min-height: 32px !important;
}

.points-filter-bar :deep(.el-input--small),
.points-filter-bar :deep(.el-select--small),
.points-filter-bar :deep(.el-date-editor.el-input--small) {
  --el-component-size-small: 32px;
}

@media (max-width: 960px) {
  .points-filter {
    width: 100%;
  }

  .points-filter-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>
