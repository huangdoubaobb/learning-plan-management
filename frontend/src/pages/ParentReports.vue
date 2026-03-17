<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>报表统计</h2>
        <div class="notice">查看完成情况与积分趋势</div>
      </div>
      <div class="row" style="max-width: 520px;">
        <select v-model="childId" @change="handleSelect">
          <option disabled value="">请选择孩子</option>
          <option v-for="child in children" :key="child.id" :value="child.id">{{ child.displayName }}</option>
        </select>
        <select v-model.number="days" @change="handleSelect">
          <option :value="7">近 7 天</option>
          <option :value="14">近 14 天</option>
          <option :value="30">近 30 天</option>
          <option :value="60">近 60 天</option>
        </select>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">孩子数量</div>
        <div class="stat-value">{{ stats.children }}</div>
        <div class="stat-foot">家庭账号数</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">任务数量</div>
        <div class="stat-value">{{ stats.tasks }}</div>
        <div class="stat-foot">近 {{ days }} 天新增 {{ stats.tasksInRange }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">奖励数量</div>
        <div class="stat-value">{{ stats.rewards }}</div>
        <div class="stat-foot">可兑换奖励</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">待审核兑换</div>
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
      </div>
    </div>

    <div class="card">
      <div class="header" style="margin-bottom: 8px;">
        <div>
          <h3>报表概览</h3>
          <div class="notice">选择孩子后可查看完成情况与积分统计</div>
        </div>
        <VeaButton v-if="can('parent.report.export')" class="btn-sm btn-soft info" @click="downloadReport" :disabled="!childId">
          <span class="btn-icon"><el-icon><Download /></el-icon></span>
          导出 Excel
        </VeaButton>
      </div>
      <div style="margin-top: 12px;" v-if="summary">
        <div class="row" style="flex-wrap: wrap;">
          <div>
            <div class="badge">任务总数</div>
            <div>{{ summary.tasksTotal }}</div>
          </div>
          <div>
            <div class="badge">已完成</div>
            <div>{{ summary.tasksCompleted }}</div>
          </div>
          <div>
            <div class="badge">完成率</div>
            <div>{{ summaryRate }}</div>
          </div>
          <div>
            <div class="badge">当前积分</div>
            <div>{{ summary.pointsBalance }}</div>
          </div>
          <div>
            <div class="badge">积分获得</div>
            <div>{{ summary.pointsEarned }}</div>
          </div>
          <div>
            <div class="badge">积分消费</div>
            <div>{{ summary.pointsSpent }}</div>
          </div>
          <div>
            <div class="badge">已兑换奖励</div>
            <div>{{ summary.rewardsRedeemed }}</div>
          </div>
        </div>
      </div>
      <div v-else class="notice" style="margin-top: 12px;">请选择孩子</div>
    </div>

    <div class="card">
      <h3>完成率趋势（近{{ days }}天）</h3>
      <div class="notice">用于观察任务完成情况走势</div>
      <div v-if="trendLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="trendDataset" :config="trendConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无趋势数据</div>
    </div>

    <div class="card">
      <h3>积分趋势（近{{ days }}天）</h3>
      <div class="notice">显示每日积分获得与消费</div>
      <div v-if="pointsLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="pointsDataset" :config="pointsConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无积分数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'
import { makeXyConfig, makeMultiColorSeries } from '../composables/useDataUiCharts'

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

const loadChildren = async () => {
  const { data } = await api.get('/parent/children')
  children.value = data
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
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = url
  link.download = `report-${childId.value}.xlsx`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const trendLabels = computed(() => trend.value.map(row => String(row.date || '').slice(5)))
const trendDataset = computed(() => {
  const values = trend.value.map(row => Math.round((row.completionRate || 0) * 100))
  return makeMultiColorSeries(trendLabels.value, values, '')
})
const trendConfig = computed(() => makeXyConfig(trendLabels.value, {
  chart: {
    labels: {
      yAxis: {
        formatter: (value) => `${value}%`
      }
    }
  }
}))

const pointsLabels = computed(() => pointsTrend.value.map(row => String(row.date || '').slice(5)))
const pointsDataset = computed(() => {
  const earned = pointsTrend.value.map(row => row.pointsEarned || 0)
  const spent = pointsTrend.value.map(row => row.pointsSpent || 0)
  return [
    ...makeMultiColorSeries(pointsLabels.value, earned, '得-', ['#60a5fa', '#3b82f6', '#2563eb', '#93c5fd']),
    ...makeMultiColorSeries(pointsLabels.value, spent, '耗-', ['#f59e0b', '#f97316', '#fb923c', '#fbbf24'])
  ]
})
const pointsConfig = computed(() => makeXyConfig(pointsLabels.value))

onMounted(() => {
  refresh()
  loadChildren()
})
</script>
