<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>首页</h2>
        <div class="notice">查看全局统计、任务完成情况和积分变化</div>
      </div>
    </div>

    <div v-if="isParent">
      <div class="vea-stat-grid">
        <div class="vea-stat">
          <div class="vea-stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">孩子数量</div>
            <div class="vea-stat-value">{{ parentStats.children }}</div>
            <div class="vea-stat-foot">当前家庭成员</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">任务总数</div>
            <div class="vea-stat-value">{{ parentStats.tasks }}</div>
            <div class="vea-stat-foot">近 30 天新增 {{ parentStats.tasksInRange }}</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">奖励数量</div>
            <div class="vea-stat-value">{{ parentStats.rewards }}</div>
            <div class="vea-stat-foot">可兑换奖励</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-red"><el-icon><Checked /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">待审核兑换</div>
            <div class="vea-stat-value">{{ parentStats.pending }}</div>
            <div class="vea-stat-foot">近 30 天兑换 {{ parentStats.redemptionsInRange }}</div>
          </div>
        </div>
      </div>

      <div v-if="!selectedChildId" class="card">
        <div class="notice">请选择孩子后查看完成率、积分和趋势图表</div>
      </div>

      <div v-else>
        <div class="card">
          <div class="table-info">
            <div class="table-title">趋势图筛选</div>
            <div class="notice">选择孩子和时间范围后，下方图表会同步刷新</div>
          </div>
          <div class="dashboard-filter-bar">
            <div class="dashboard-filter">
              <div class="filter-item">
                <span class="filter-label">孩子:</span>
                <el-select v-model="selectedChildId" size="small" placeholder="请选择孩子" style="width: 180px;" @change="reloadParent">
                  <el-option v-for="child in children" :key="child.id" :label="child.displayName" :value="String(child.id)" />
                </el-select>
              </div>
              <div class="filter-item">
                <span class="filter-label">时间:</span>
                <el-select v-model="trendDays" size="small" style="width: 140px;" @change="reloadParent">
                  <el-option :value="7" label="近 7 天" />
                  <el-option :value="14" label="近 14 天" />
                  <el-option :value="30" label="近 30 天" />
                </el-select>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <h3>任务趋势</h3>
          <div class="notice">按天统计任务总数和已完成数</div>
          <div v-if="taskTrendLabels.length" class="chart-summary-strip">
            <div class="chart-summary-pill">
              <span>完成峰值</span>
              <strong>{{ taskCompletionPeak }}</strong>
            </div>
            <div class="chart-summary-pill">
              <span>统计窗口</span>
              <strong>近 {{ trendDays }} 天</strong>
            </div>
          </div>
          <div v-if="taskTrendLabels.length" class="chart-modern-full chart-modern-home">
            <VueUiXy class="dataui-xy dataui-xy-home" :dataset="taskTrendDataset" :config="taskTrendConfig" />
          </div>
          <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
        </div>

        <div class="card">
          <h3>积分趋势</h3>
          <div class="notice">按天统计积分获得和消费</div>
          <div v-if="pointsTrendLabels.length" class="chart-summary-strip">
            <div class="chart-summary-pill">
              <span>净变化</span>
              <strong :class="{ 'is-positive': pointsNetChange >= 0, 'is-negative': pointsNetChange < 0 }">
                {{ pointsNetChange >= 0 ? '+' : '' }}{{ pointsNetChange }}
              </strong>
            </div>
            <div class="chart-summary-pill">
              <span>当前积分</span>
              <strong>{{ summary.pointsBalance }}</strong>
            </div>
          </div>
          <div v-if="pointsTrendLabels.length" class="chart-modern-full chart-modern-home">
            <VueUiXy class="dataui-xy dataui-xy-home" :dataset="pointsTrendDataset" :config="pointsTrendConfig" />
          </div>
          <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
        </div>
      </div>
    </div>

    <div v-else-if="isAdmin">
      <div class="vea-stat-grid">
        <div class="vea-stat">
          <div class="vea-stat-icon bg-blue"><el-icon><User /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">用户数量</div>
            <div class="vea-stat-value">{{ adminUsers.length }}</div>
            <div class="vea-stat-foot">系统账号总数</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-purple"><el-icon><Avatar /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">角色数量</div>
            <div class="vea-stat-value">{{ adminRoles.length }}</div>
            <div class="vea-stat-foot">权限角色</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-orange"><el-icon><Lock /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">权限数量</div>
            <div class="vea-stat-value">{{ adminPermissions.length }}</div>
            <div class="vea-stat-foot">可分配权限</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-green"><el-icon><Checked /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">启用账号</div>
            <div class="vea-stat-value">{{ enabledCount }}</div>
            <div class="vea-stat-foot">禁用 {{ disabledCount }}</div>
          </div>
        </div>
      </div>
      <div class="dashboard-grid">
        <div class="card">
          <h3>账号启用占比</h3>
          <div class="notice">启用与禁用分布</div>
          <div class="chart-modern">
            <VueUiDonut class="dataui-donut" :dataset="adminDonutDataset" :config="donutConfig" />
            <div class="pie-info">
              <div class="pie-value">{{ adminEnabledPercent }}%</div>
              <div class="pie-label">启用</div>
              <div class="pie-sub">启用 {{ enabledCount }} / 禁用 {{ disabledCount }}</div>
            </div>
          </div>
        </div>
        <div class="card">
          <h3>角色分布</h3>
          <div class="notice">按角色统计账号数量</div>
          <div v-if="roleLabels.length" class="chart-modern-full chart-modern-home">
            <VueUiXy class="dataui-xy dataui-xy-home" :dataset="roleDataset" :config="roleConfig" />
          </div>
          <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
        </div>
      </div>
    </div>

    <div v-else>
      <div class="vea-stat-grid">
        <div class="vea-stat">
          <div class="vea-stat-icon bg-blue"><el-icon><Medal /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">当前积分</div>
            <div class="vea-stat-value">{{ childMe?.points ?? 0 }}</div>
            <div class="vea-stat-foot">积分余额</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">待完成任务</div>
            <div class="vea-stat-value">{{ childPendingCount }}</div>
            <div class="vea-stat-foot">前往任务列表处理</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">待审核兑换</div>
            <div class="vea-stat-value">{{ childPendingRedemptions }}</div>
            <div class="vea-stat-foot">累计兑换 {{ childRedemptions.length }}</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-purple"><el-icon><Checked /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">已完成任务</div>
            <div class="vea-stat-value">{{ childCompletedCount }}</div>
            <div class="vea-stat-foot">历史完成记录</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="table-info">
          <div class="table-title">首页概览</div>
          <div class="notice">详细任务、奖励兑换和积分趋势请进入对应功能页查看</div>
        </div>
        <div class="chart-summary-strip">
          <div class="chart-summary-pill">
            <span>任务总数</span>
            <strong>{{ childTasks.length }}</strong>
          </div>
          <div class="chart-summary-pill">
            <span>兑换总数</span>
            <strong>{{ childRedemptions.length }}</strong>
          </div>
          <div class="chart-summary-pill">
            <span>净积分变化</span>
            <strong :class="{ 'is-positive': childNetPoints >= 0, 'is-negative': childNetPoints < 0 }">
              {{ childNetPoints >= 0 ? '+' : '' }}{{ childNetPoints }}
            </strong>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  UserFilled,
  Calendar,
  Present,
  Checked,
  User,
  Avatar,
  Lock,
  Medal
} from '@element-plus/icons-vue'
import { VueUiXy } from 'vue-data-ui'
import api from '../api'
import { makeBarSeries, makeXyConfig } from '../composables/useDataUiCharts'
import { normalizeParentStats } from '../composables/useParentStats'
import { getRole } from '../utils/authStorage'

const role = ref(getRole())
const isParent = computed(() => role.value === 'PARENT')
const isAdmin = computed(() => role.value === 'ADMIN')

const children = ref([])
const selectedChildId = ref('')
const trendDays = ref(14)
const parentStats = ref({
  children: 0,
  tasks: 0,
  rewards: 0,
  pending: 0,
  tasksInRange: 0,
  redemptionsInRange: 0
})
const summary = ref({
  tasksTotal: 0,
  tasksCompleted: 0,
  pointsBalance: 0,
  pointsEarned: 0,
  pointsSpent: 0,
  rewardsRedeemed: 0
})
const taskTrend = ref({ points: [] })
const pointsTrend = ref({ points: [] })

const adminUsers = ref([])
const adminRoles = ref([])
const adminPermissions = ref([])

const childMe = ref(null)
const childTasks = ref([])
const childRedemptions = ref([])

const childPendingCount = computed(() => childTasks.value.filter(task => task.status === 'PENDING' && !task.submittedAt).length)
const childCompletedCount = computed(() => childTasks.value.filter(task => task.status === 'COMPLETED' || !!task.submittedAt).length)
const childPendingRedemptions = computed(() => childRedemptions.value.filter(item => item.status === 'PENDING').length)

const taskCompletionPeak = computed(() => {
  const rows = taskTrend.value.points || []
  if (!rows.length) return '-'
  const row = [...rows].sort((a, b) => (b.tasksCompleted || 0) - (a.tasksCompleted || 0))[0]
  return `${String(row.date || '').slice(5)} ${row.tasksCompleted || 0}`
})

const pointsNetChange = computed(() => {
  const rows = pointsTrend.value.points || []
  return rows.reduce((sum, row) => sum + (row.pointsEarned || 0) - (row.pointsSpent || 0), 0)
})

const taskTrendLabels = computed(() => (taskTrend.value.points || []).map(row => String(row.date || '').slice(5)))
const taskTrendDataset = computed(() => {
  const rows = taskTrend.value.points || []
  const totals = rows.map(row => row.tasksTotal || 0)
  const completed = rows.map(row => row.tasksCompleted || 0)
  return [
    ...makeBarSeries('任务总数', totals, '#93c5fd'),
    ...makeBarSeries('已完成', completed, '#16a34a')
  ]
})
const taskTrendConfig = computed(() => makeXyConfig(taskTrendLabels.value, {
  chart: {
    height: 296,
    legend: {
      show: true
    }
  }
}))

const pointsTrendLabels = computed(() => (pointsTrend.value.points || []).map(row => String(row.date || '').slice(5)))
const pointsTrendDataset = computed(() => {
  const rows = pointsTrend.value.points || []
  const earned = rows.map(row => row.pointsEarned || 0)
  const spent = rows.map(row => row.pointsSpent || 0)
  return [
    ...makeBarSeries('获得', earned, '#60a5fa'),
    ...makeBarSeries('消费', spent, '#f97316')
  ]
})
const pointsTrendConfig = computed(() => makeXyConfig(pointsTrendLabels.value, {
  chart: {
    height: 296,
    legend: {
      show: true
    }
  }
}))

const enabledCount = computed(() => adminUsers.value.filter(user => user.enabled).length)
const disabledCount = computed(() => adminUsers.value.filter(user => !user.enabled).length)
const adminEnabledPercent = computed(() => {
  const total = adminUsers.value.length
  if (!total) return 0
  return Math.round((enabledCount.value / total) * 100)
})
const adminDonutDataset = computed(() => ([
  { name: '启用', values: [enabledCount.value], color: '#3b82f6' },
  { name: '禁用', values: [disabledCount.value], color: '#e2e8f0' }
]))

const roleLabels = computed(() => {
  const counts = {}
  adminUsers.value.forEach(user => {
    counts[user.role] = (counts[user.role] || 0) + 1
  })
  return Object.keys(counts)
})
const roleDataset = computed(() => {
  const counts = {}
  adminUsers.value.forEach(user => {
    counts[user.role] = (counts[user.role] || 0) + 1
  })
  const values = roleLabels.value.map(roleName => counts[roleName] || 0)
  return makeBarSeries('账号数', values, '#3b82f6')
})
const roleConfig = computed(() => makeXyConfig(roleLabels.value, {
  chart: {
    height: 296
  }
}))

const childPointRows = computed(() => {
  const map = new Map()
  childTasks.value.filter(task => task.status === 'COMPLETED').forEach(task => {
    const date = String(task.completedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.earned += task.points || 0
    map.set(date, row)
  })
  childRedemptions.value.forEach(record => {
    const date = String(record.reviewedAt || record.redeemedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.spent += record.pointsCost || 0
    map.set(date, row)
  })
  return Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date))
})
const childNetPoints = computed(() => childPointRows.value.reduce((sum, row) => sum + (row.earned || 0) - (row.spent || 0), 0))
const childPointsLabels = computed(() => childPointRows.value.map(row => row.date.slice(5)))
const childPointsDataset = computed(() => {
  const earned = childPointRows.value.map(row => row.earned || 0)
  const spent = childPointRows.value.map(row => row.spent || 0)
  return [
    ...makeBarSeries('获得', earned, '#60a5fa'),
    ...makeBarSeries('消费', spent, '#f97316')
  ]
})
const childPointsConfig = computed(() => makeXyConfig(childPointsLabels.value, {
  chart: {
    height: 296,
    legend: {
      show: true
    }
  }
}))

const reloadParent = async () => {
  if (!selectedChildId.value) return
  const [summaryRes, trendRes, pointsRes] = await Promise.all([
    api.get(`/parent/report/summary?childId=${selectedChildId.value}`),
    api.get(`/parent/report/trend?childId=${selectedChildId.value}&days=${trendDays.value}`),
    api.get(`/parent/report/points-trend?childId=${selectedChildId.value}&days=${trendDays.value}`)
  ])
  summary.value = summaryRes.data
  taskTrend.value = trendRes.data
  pointsTrend.value = pointsRes.data
}

const loadParent = async () => {
  const [childrenRes, statsRes] = await Promise.all([
    api.get('/parent/children'),
    api.get('/parent/stats?days=30')
  ])
  children.value = childrenRes.data
  parentStats.value = normalizeParentStats(statsRes.data)
  if (!selectedChildId.value && children.value.length) {
    selectedChildId.value = String(children.value[0].id)
  }
  await reloadParent()
}

const loadAdmin = async () => {
  const [usersRes, rolesRes, permsRes] = await Promise.all([
    api.get('/admin/users'),
    api.get('/admin/roles'),
    api.get('/admin/permissions')
  ])
  adminUsers.value = usersRes.data
  adminRoles.value = rolesRes.data
  adminPermissions.value = permsRes.data
}

const loadChild = async () => {
  const [meRes, tasksRes, redemptionsRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks'),
    api.get('/child/redemptions')
  ])
  childMe.value = meRes.data
  childTasks.value = tasksRes.data
  childRedemptions.value = redemptionsRes.data
}

onMounted(async () => {
  if (isParent.value) {
    await loadParent()
  } else if (isAdmin.value) {
    await loadAdmin()
  } else {
    await loadChild()
  }
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

.dashboard-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 16px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.dashboard-filter {
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

.dashboard-filter-bar :deep(.el-input__wrapper),
.dashboard-filter-bar :deep(.el-select__wrapper) {
  min-height: 32px !important;
}

.dashboard-filter-bar :deep(.el-input--small),
.dashboard-filter-bar :deep(.el-select--small) {
  --el-component-size-small: 32px;
}

.chart-modern-home {
  min-height: 296px;
}

.dataui-xy-home {
  min-height: 296px;
  height: 296px;
}

@media (max-width: 768px) {
  .dashboard-filter {
    width: 100%;
  }

  .chart-modern-home {
    min-height: 260px;
  }

  .dataui-xy-home {
    min-height: 260px;
    height: 260px;
  }
}
</style>
