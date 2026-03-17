<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>积分明细</h2>
        <div class="notice">任务完成与兑换记录汇总</div>
      </div>
      <div class="row" style="max-width: 520px;">
        <input v-model="startDate" placeholder="开始日期 YYYY-MM-DD" />
        <input v-model="endDate" placeholder="结束日期 YYYY-MM-DD" />
        <select v-model="typeFilter">
          <option value="ALL">全部类型</option>
          <option value="TASK">任务获得</option>
          <option value="REDEEM">兑换消费</option>
        </select>
        <VeaButton class="btn-sm btn-soft info" @click="exportPoints">
          <span class="btn-icon"><el-icon><Download /></el-icon></span>
          导出 Excel
        </VeaButton>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">累计获得</div>
        <div class="stat-value">{{ earnedTotal }}</div>
        <div class="stat-foot">任务完成积分</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">累计消费</div>
        <div class="stat-value">{{ spentTotal }}</div>
        <div class="stat-foot">兑换消耗积分</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">当前积分</div>
        <div class="stat-value">{{ me?.points ?? 0 }}</div>
        <div class="stat-foot">积分余额</div>
      </div>
    </div>

    <div class="card">
      <h3>积分趋势</h3>
      <div class="notice">按天统计获得与消费</div>
      <div v-if="trendLabels.length" class="chart-modern-full">
        <VueUiXy class="dataui-xy" :dataset="trendDataset" :config="trendConfig" />
      </div>
      <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
    </div>

    <div class="card" v-if="showTasks">
      <h3>任务完成记录</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>任务</th>
              <th>积分</th>
              <th>完成时间</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in filteredCompletedTasks" :key="task.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ task.title }}</td>
              <td>+{{ task.points }}</td>
              <td>{{ formatDateTime(task.completedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card" v-if="showRedemptions">
      <h3>兑换记录</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
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
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ record.rewardName }}</td>
              <td>-{{ record.pointsCost }}</td>
              <td>{{ record.status }}</td>
              <td>{{ record.reviewNote || '-' }}</td>
              <td>{{ formatDateTime(record.reviewedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'
import { makeXyConfig, makeMultiColorSeries } from '../composables/useDataUiCharts'

const me = ref(null)
const tasks = ref([])
const redemptions = ref([])
const startDate = ref('')
const endDate = ref('')
const typeFilter = ref('ALL')

const completedTasks = computed(() => tasks.value.filter(t => t.status === 'COMPLETED'))

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

const filteredCompletedTasks = computed(() => completedTasks.value.filter(t => inRange(t.completedAt)))
const filteredRedemptions = computed(() => redemptions.value.filter(r => inRange(extractRecordDate(r))))

const showTasks = computed(() => typeFilter.value === 'ALL' || typeFilter.value === 'TASK')
const showRedemptions = computed(() => typeFilter.value === 'ALL' || typeFilter.value === 'REDEEM')

const earnedTotal = computed(() => {
  if (!showTasks.value) return 0
  return filteredCompletedTasks.value.reduce((sum, t) => sum + (t.points || 0), 0)
})
const spentTotal = computed(() => {
  if (!showRedemptions.value) return 0
  return filteredRedemptions.value.reduce((sum, r) => sum + (r.pointsCost || 0), 0)
})

const trendRows = computed(() => {
  const map = new Map()
  filteredCompletedTasks.value.forEach(t => {
    const date = String(t.completedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.earned += t.points || 0
    map.set(date, row)
  })
  filteredRedemptions.value.forEach(r => {
    const date = String(extractRecordDate(r) || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.spent += r.pointsCost || 0
    map.set(date, row)
  })
  return Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date))
})

const trendLabels = computed(() => trendRows.value.map(r => r.date.slice(5)))
const trendDataset = computed(() => {
  const earned = trendRows.value.map(r => r.earned || 0)
  const spent = trendRows.value.map(r => r.spent || 0)
  return [
    ...makeMultiColorSeries(trendLabels.value, earned, '得-', ['#60a5fa', '#3b82f6', '#2563eb', '#93c5fd']),
    ...makeMultiColorSeries(trendLabels.value, spent, '耗-', ['#f59e0b', '#f97316', '#fb923c', '#fbbf24'])
  ]
})
const trendConfig = computed(() => makeXyConfig(trendLabels.value))

const loadAll = async () => {
  const [meRes, taskRes, redemptionRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks'),
    api.get('/child/redemptions')
  ])
  me.value = meRes.data
  tasks.value = taskRes.data
  redemptions.value = redemptionRes.data
}

const exportPoints = async () => {
  const params = new URLSearchParams()
  if (startDate.value) params.append('startDate', startDate.value)
  if (endDate.value) params.append('endDate', endDate.value)
  const res = await api.get(`/child/points/export?${params.toString()}`, { responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([res.data]))
  const link = document.createElement('a')
  link.href = url
  link.download = `points-${me.value?.username || 'child'}.xlsx`
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadAll)
</script>
