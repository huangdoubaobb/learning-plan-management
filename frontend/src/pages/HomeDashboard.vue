<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>首页</h2>
        <div class="notice">全局统计与趋势概览</div>
      </div>
      <div v-if="isParent" class="row" style="max-width: 520px;">
        <select v-model="selectedChildId" @change="reloadParent">
          <option value="">选择孩子</option>
          <option v-for="child in children" :key="child.id" :value="child.id">{{ child.displayName }}</option>
        </select>
        <select v-model.number="trendDays" @change="reloadParent">
          <option :value="7">近 7 天</option>
          <option :value="14">近 14 天</option>
          <option :value="30">近 30 天</option>
        </select>
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
        <div class="notice">请选择孩子查看趋势与分布</div>
      </div>

      <div v-else>
        <div class="dashboard-grid">
          <div class="card">
            <h3>完成率</h3>
            <div class="notice">任务完成情况</div>
            <div class="chart-modern">
              <VueUiDonut class="dataui-donut" :dataset="completionDonutDataset" :config="donutConfig" />
              <div class="pie-info">
                <div class="pie-value">{{ completionPercent }}%</div>
                <div class="pie-label">完成率</div>
                <div class="pie-sub">已完成 {{ summary.tasksCompleted }} / {{ summary.tasksTotal }}</div>
              </div>
            </div>
          </div>
          <div class="card">
            <h3>积分消耗</h3>
            <div class="notice">积分获得 vs 消耗</div>
            <div class="chart-modern">
              <VueUiDonut class="dataui-donut" :dataset="pointsDonutDataset" :config="donutConfig" />
              <div class="pie-info">
                <div class="pie-value">{{ pointsSpendPercent }}%</div>
                <div class="pie-label">消耗占比</div>
                <div class="pie-sub">获得 {{ summary.pointsEarned }} / 消耗 {{ summary.pointsSpent }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <h3>任务趋势</h3>
          <div class="notice">按天统计任务创建与完成</div>
          <div v-if="taskTrendLabels.length" class="chart-modern-full">
            <VueUiXy class="dataui-xy" :dataset="taskTrendDataset" :config="taskTrendConfig" />
          </div>
          <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
        </div>

        <div class="card">
          <h3>积分趋势</h3>
          <div class="notice">按天统计获得与消耗</div>
          <div v-if="pointsTrendLabels.length" class="chart-modern-full">
            <VueUiXy class="dataui-xy" :dataset="pointsTrendDataset" :config="pointsTrendConfig" />
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
          <div class="notice">启用 vs 禁用</div>
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
          <div class="notice">按角色统计账号</div>
          <div v-if="roleLabels.length" class="chart-modern-full">
            <VueUiXy class="dataui-xy" :dataset="roleDataset" :config="roleConfig" />
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
            <div class="vea-stat-title">任务数量</div>
            <div class="vea-stat-value">{{ childTasks.length }}</div>
            <div class="vea-stat-foot">今日任务 + 历史</div>
          </div>
        </div>
        <div class="vea-stat">
          <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
          <div class="vea-stat-content">
            <div class="vea-stat-title">兑换次数</div>
            <div class="vea-stat-value">{{ childRedemptions.length }}</div>
            <div class="vea-stat-foot">累计兑换</div>
          </div>
        </div>
      </div>
      <div class="card">
        <h3>积分趋势</h3>
        <div class="notice">按天统计获得与消耗</div>
        <div v-if="childPointsLabels.length" class="chart-modern-full">
          <VueUiXy class="dataui-xy" :dataset="childPointsDataset" :config="childPointsConfig" />
        </div>
        <div v-else class="notice" style="margin-top: 12px;">暂无数据</div>
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
import { VueUiDonut, VueUiXy } from 'vue-data-ui'
import api from '../api'
import { donutConfig, makeXyConfig, makeMultiColorSeries } from '../composables/useDataUiCharts'

const role = ref(localStorage.getItem('role') || '')
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

const completionPercent = computed(() => {
  if (!summary.value.tasksTotal) return 0
  return Math.round((summary.value.tasksCompleted / summary.value.tasksTotal) * 100)
})

const pointsSpendPercent = computed(() => {
  const total = summary.value.pointsEarned + summary.value.pointsSpent
  if (!total) return 0
  return Math.round((summary.value.pointsSpent / total) * 100)
})

const completionDonutDataset = computed(() => {
  const completed = summary.value.tasksCompleted || 0
  const total = summary.value.tasksTotal || 0
  const pending = Math.max(total - completed, 0)
  return [
    { name: '已完成', values: [completed], color: '#22c55e' },
    { name: '未完成', values: [pending], color: '#e2e8f0' }
  ]
})

const pointsDonutDataset = computed(() => ([
  { name: '获得', values: [summary.value.pointsEarned || 0], color: '#3b82f6' },
  { name: '消耗', values: [summary.value.pointsSpent || 0], color: '#f59e0b' }
]))

const taskTrendLabels = computed(() => (taskTrend.value.points || []).map(r => String(r.date || '').slice(5)))
const taskTrendDataset = computed(() => {
  const rows = taskTrend.value.points || []
  const totals = rows.map(r => r.tasksTotal || 0)
  const completed = rows.map(r => r.tasksCompleted || 0)
  return [
    ...makeMultiColorSeries(taskTrendLabels.value, totals, '总-', ['#60a5fa', '#3b82f6', '#2563eb', '#93c5fd']),
    ...makeMultiColorSeries(taskTrendLabels.value, completed, '完-', ['#22c55e', '#16a34a', '#4ade80', '#86efac'])
  ]
})
const taskTrendConfig = computed(() => makeXyConfig(taskTrendLabels.value))

const pointsTrendLabels = computed(() => (pointsTrend.value.points || []).map(r => String(r.date || '').slice(5)))
const pointsTrendDataset = computed(() => {
  const rows = pointsTrend.value.points || []
  const earned = rows.map(r => r.pointsEarned || 0)
  const spent = rows.map(r => r.pointsSpent || 0)
  return [
    ...makeMultiColorSeries(pointsTrendLabels.value, earned, '得-', ['#60a5fa', '#3b82f6', '#2563eb', '#93c5fd']),
    ...makeMultiColorSeries(pointsTrendLabels.value, spent, '耗-', ['#f59e0b', '#f97316', '#fb923c', '#fbbf24'])
  ]
})
const pointsTrendConfig = computed(() => makeXyConfig(pointsTrendLabels.value))

const enabledCount = computed(() => adminUsers.value.filter(u => u.enabled).length)
const disabledCount = computed(() => adminUsers.value.filter(u => !u.enabled).length)
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
  adminUsers.value.forEach(u => {
    counts[u.role] = (counts[u.role] || 0) + 1
  })
  return Object.keys(counts)
})
const roleDataset = computed(() => {
  const counts = {}
  adminUsers.value.forEach(u => {
    counts[u.role] = (counts[u.role] || 0) + 1
  })
  const values = roleLabels.value.map(role => counts[role] || 0)
  return makeMultiColorSeries(roleLabels.value, values, '')
})
const roleConfig = computed(() => makeXyConfig(roleLabels.value))

const childPointsLabels = computed(() => {
  const map = new Map()
  childTasks.value.filter(t => t.status === 'COMPLETED').forEach(t => {
    const date = String(t.completedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.earned += t.points || 0
    map.set(date, row)
  })
  childRedemptions.value.forEach(r => {
    const date = String(r.reviewedAt || r.redeemedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.spent += r.pointsCost || 0
    map.set(date, row)
  })
  return Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date)).map(r => r.date.slice(5))
})
const childPointsDataset = computed(() => {
  const map = new Map()
  childTasks.value.filter(t => t.status === 'COMPLETED').forEach(t => {
    const date = String(t.completedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.earned += t.points || 0
    map.set(date, row)
  })
  childRedemptions.value.forEach(r => {
    const date = String(r.reviewedAt || r.redeemedAt || '').slice(0, 10)
    if (!date) return
    const row = map.get(date) || { date, earned: 0, spent: 0 }
    row.spent += r.pointsCost || 0
    map.set(date, row)
  })
  const rows = Array.from(map.values()).sort((a, b) => a.date.localeCompare(b.date))
  const earned = rows.map(r => r.earned || 0)
  const spent = rows.map(r => r.spent || 0)
  return [
    ...makeMultiColorSeries(childPointsLabels.value, earned, '得-', ['#60a5fa', '#3b82f6', '#2563eb', '#93c5fd']),
    ...makeMultiColorSeries(childPointsLabels.value, spent, '耗-', ['#f59e0b', '#f97316', '#fb923c', '#fbbf24'])
  ]
})
const childPointsConfig = computed(() => makeXyConfig(childPointsLabels.value))

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
  parentStats.value = statsRes.data
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
.chart-modern {
  display: flex;
  align-items: center;
  gap: 16px;
}

.dataui-donut {
  width: 170px;
  height: 170px;
}

.chart-modern-full {
  width: 100%;
  min-height: 200px;
}

.dataui-xy {
  width: 100%;
  height: 200px;
}
</style>
