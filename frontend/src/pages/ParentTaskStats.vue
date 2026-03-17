<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>任务完成统计</h2>
        <div class="notice">查看任务完成情况与趋势</div>
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

    <div class="stats-grid" v-if="summary">
      <div class="stat-card">
        <div class="stat-title">任务总数</div>
        <div class="stat-value">{{ summary.tasksTotal }}</div>
        <div class="stat-foot">统计区间</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">已完成</div>
        <div class="stat-value">{{ summary.tasksCompleted }}</div>
        <div class="stat-foot">完成率 {{ summaryRate }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">当前积分</div>
        <div class="stat-value">{{ summary.pointsBalance }}</div>
        <div class="stat-foot">积分余额</div>
      </div>
    </div>

    <div class="card">
      <h3>完成率趋势（近{{ days }}天）</h3>
      <div class="notice">用于观察任务完成情况走势</div>
      <div v-if="trendLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="trendDataset" :config="trendConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无趋势数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import { useParentStats } from '../composables/useParentStats'
import { makeXyConfig, makeMultiColorSeries } from '../composables/useDataUiCharts'

const children = ref([])
const childId = ref('')
const summary = ref(null)
const trend = ref([])
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
  const [summaryRes, trendRes] = await Promise.all([
    api.get(`/parent/report/summary?childId=${childId.value}`),
    api.get(`/parent/report/trend?childId=${childId.value}&days=${days.value}`)
  ])
  summary.value = summaryRes.data
  trend.value = trendRes.data.points || []
  refresh()
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

onMounted(() => {
  refresh()
  loadChildren()
})
</script>
