<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>报表统计</h2>
        <div class="notice">查看孩子任务完成情况和积分变化</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">孩子数量</div>
          <div class="vea-stat-value">{{ stats.children }}</div>
          <div class="vea-stat-foot">当前绑定的孩子账号</div>
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
          <div class="vea-stat-foot">当前可兑换奖励总数</div>
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

    <div class="card">
      <div class="table-info">
        <div class="table-title">图表筛选</div>
        <div class="notice">先筛选孩子和时间范围，再查看下方报表概览与趋势图</div>
      </div>
      <div class="report-filter-bar">
        <div class="report-filter">
          <div class="filter-item">
            <span class="filter-label">孩子:</span>
            <el-select v-model="childId" size="small" placeholder="请选择孩子" style="width: 180px;" @change="handleSelect">
              <el-option v-for="child in children" :key="child.id" :label="child.displayName" :value="child.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">时间:</span>
            <el-select v-model="days" size="small" style="width: 140px;" @change="handleSelect">
              <el-option :value="7" label="近 7 天" />
              <el-option :value="14" label="近 14 天" />
              <el-option :value="30" label="近 30 天" />
              <el-option :value="60" label="近 60 天" />
            </el-select>
          </div>
        </div>
        <div class="report-filter-actions">
          <VeaButton
            v-if="can('parent.report.export')"
            class="btn-sm btn-soft info"
            :disabled="!childId"
            @click="downloadReport"
          >
            <span class="btn-icon"><el-icon><Download /></el-icon></span>
            导出 Excel
          </VeaButton>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>报表概览</h3>
      <div class="notice">选择孩子后查看当前区间的完成情况和积分统计</div>

      <el-table
        v-if="summary"
        :data="summaryTableRows"
        stripe
        border
        class="summary-el-table"
      >
        <el-table-column prop="tasksTotal" label="任务总数" min-width="110" align="center" />
        <el-table-column prop="tasksCompleted" label="已完成" min-width="110" align="center" />
        <el-table-column prop="completionRate" label="完成率" min-width="110" align="center" />
        <el-table-column prop="pointsBalance" label="当前积分" min-width="110" align="center" />
        <el-table-column prop="pointsEarned" label="积分获得" min-width="110" align="center" />
        <el-table-column prop="pointsSpent" label="积分消费" min-width="110" align="center" />
        <el-table-column prop="rewardsRedeemed" label="已兑换奖励" min-width="120" align="center" />
      </el-table>
      <div v-else class="notice" style="margin-top: 12px;">请选择孩子查看报表</div>
    </div>

    <div class="card">
      <h3>完成率趋势（近 {{ days }} 天）</h3>
      <div class="notice">用于观察任务完成率变化</div>
      <div v-if="trendLabels.length" class="chart-summary-strip">
        <div class="chart-summary-pill">
          <span>峰值</span>
          <strong>{{ trendPeak }}</strong>
        </div>
        <div class="chart-summary-pill">
          <span>区间完成率</span>
          <strong>{{ summaryRate }}</strong>
        </div>
      </div>
      <div v-if="trendLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="trendDataset" :config="trendConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无趋势数据</div>
    </div>

    <div class="card">
      <h3>积分趋势（近 {{ days }} 天）</h3>
      <div class="notice">显示每日积分获得与消费</div>
      <div v-if="pointsLabels.length" class="chart-summary-strip">
        <div class="chart-summary-pill">
          <span>净变化</span>
          <strong :class="{ 'is-positive': pointsNet >= 0, 'is-negative': pointsNet < 0 }">
            {{ pointsNet >= 0 ? '+' : '' }}{{ pointsNet }}
          </strong>
        </div>
        <div class="chart-summary-pill">
          <span>当前积分</span>
          <strong>{{ summary?.pointsBalance ?? 0 }}</strong>
        </div>
      </div>
      <div v-if="pointsLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="pointsDataset" :config="pointsConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无积分数据</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Download, UserFilled, Calendar, Present, Checked } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'
import { makeBarSeries, makeXyConfig } from '../composables/useDataUiCharts'
import { downloadBlob } from '../utils'

const children = ref([])
const childId = ref('')
const summary = ref(null)
const trend = ref([])
const pointsTrend = ref([])
const { stats, days, refresh } = useParentStats()

const summaryRate = computed(() => {
  if (!summary.value || summary.value.tasksTotal === 0) return '0%'
  const rate = summary.value.tasksCompleted / summary.value.tasksTotal
  return `${Math.round(rate * 100)}%`
})

const summaryTableRows = computed(() => {
  if (!summary.value) return []
  return [{
    tasksTotal: summary.value.tasksTotal ?? 0,
    tasksCompleted: summary.value.tasksCompleted ?? 0,
    completionRate: summaryRate.value,
    pointsBalance: summary.value.pointsBalance ?? 0,
    pointsEarned: summary.value.pointsEarned ?? 0,
    pointsSpent: summary.value.pointsSpent ?? 0,
    rewardsRedeemed: summary.value.rewardsRedeemed ?? 0
  }]
})

const trendPeak = computed(() => {
  if (!trend.value.length) return '-'
  const row = [...trend.value].sort((a, b) => (b.completionRate || 0) - (a.completionRate || 0))[0]
  return `${String(row.date || '').slice(5)} ${Math.round((row.completionRate || 0) * 100)}%`
})

const pointsNet = computed(() => {
  const earned = pointsTrend.value.reduce((sum, row) => sum + (row.pointsEarned || 0), 0)
  const spent = pointsTrend.value.reduce((sum, row) => sum + (row.pointsSpent || 0), 0)
  return earned - spent
})

const loadChildren = async () => {
  const { data } = await api.get('/parent/children')
  children.value = Array.isArray(data) ? data : []
  if (children.value.length && !childId.value) {
    childId.value = children.value[0].id
    await handleSelect()
  }
}

const handleSelect = async () => {
  if (!childId.value) return
  const [summaryRes, trendRes, pointsRes] = await Promise.all([
    api.get(`/parent/report/summary?childId=${childId.value}`),
    api.get(`/parent/report/trend?childId=${childId.value}&days=${days.value}`),
    api.get(`/parent/report/points-trend?childId=${childId.value}&days=${days.value}`)
  ])
  summary.value = summaryRes.data
  trend.value = trendRes.data.points || []
  pointsTrend.value = pointsRes.data.points || []
  refresh()
}

const downloadReport = async () => {
  if (!childId.value) return
  const res = await api.get(`/parent/report/export?childId=${childId.value}&days=${days.value}`, { responseType: 'blob' })
  downloadBlob(res.data, `report-${childId.value}.xlsx`)
  ElMessage.success('导出成功')
}

const trendLabels = computed(() => trend.value.map(row => String(row.date || '').slice(5)))
const trendDataset = computed(() => {
  const values = trend.value.map(row => Math.round((row.completionRate || 0) * 100))
  return makeBarSeries('完成率', values, '#2563eb')
})
const trendConfig = computed(() => makeXyConfig(trendLabels.value, {
  chart: {
    grid: {
      labels: {
        yAxis: {
          formatter: ({ value }) => `${value}%`,
          scaleMax: 100,
          scaleMin: 0
        }
      }
    }
  }
}))

const pointsLabels = computed(() => pointsTrend.value.map(row => String(row.date || '').slice(5)))
const pointsDataset = computed(() => {
  const earned = pointsTrend.value.map(row => row.pointsEarned || 0)
  const spent = pointsTrend.value.map(row => row.pointsSpent || 0)
  return [
    ...makeBarSeries('获得', earned, '#60a5fa'),
    ...makeBarSeries('消费', spent, '#f97316')
  ]
})
const pointsConfig = computed(() => makeXyConfig(pointsLabels.value, {
  chart: {
    legend: {
      show: true
    }
  }
}))

onMounted(() => {
  refresh()
  loadChildren()
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

.report-filter-bar {
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

.report-filter {
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

.report-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.report-filter-bar :deep(.el-input__wrapper),
.report-filter-bar :deep(.el-select__wrapper) {
  min-height: 32px !important;
}

.report-filter-bar :deep(.el-input--small),
.report-filter-bar :deep(.el-select--small) {
  --el-component-size-small: 32px;
}

.summary-el-table {
  margin-top: 12px;
}

:deep(.summary-el-table .el-table__cell) {
  padding: 10px 0;
}

:deep(.summary-el-table th.el-table__cell) {
  background: #f8fafc;
  color: #64748b;
  font-weight: 600;
}

:deep(.summary-el-table td.el-table__cell) {
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 960px) {
  .report-filter {
    width: 100%;
  }

  .report-filter-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>
