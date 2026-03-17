<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>日志管理</h2>
        <div class="notice">查看系统操作失败日志</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Tickets /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">日志数量</div>
          <div class="vea-stat-value">{{ logs.length }}</div>
          <div class="vea-stat-foot">当前查询结果</div>
        </div>
      </div>
    </div>

    <div class="card vea-panel">
      <div class="vea-panel-header">
        <div class="vea-panel-title">日志列表</div>
      </div>
      <div class="vea-filter-bar">
        <div class="vea-toolbar">
          <div class="vea-toolbar-left">
            <span class="filter-label">关键词：</span>
            <el-input v-model="filters.keyword" placeholder="用户/路径/错误" clearable style="width: 220px;" />
            <span class="filter-label">状态：</span>
            <el-select v-model="filters.status" clearable placeholder="全部" style="width: 140px;">
              <el-option label="400" :value="400" />
              <el-option label="500" :value="500" />
            </el-select>
            <span class="filter-label">日期：</span>
            <el-date-picker
              v-model="filters.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 260px;"
            />
          </div>
          <div class="vea-toolbar-right">
            <VeaButton type="primary" :icon="Search" @click="loadAll">查询</VeaButton>
            <VeaButton @click="resetFilters">重置</VeaButton>
          </div>
        </div>
      </div>
      <div class="table-wrap">
        <table class="table vea-table">
          <thead>
            <tr>
              <th style="width: 170px;">时间</th>
              <th style="width: 120px;">用户</th>
              <th style="width: 120px;">角色</th>
              <th style="width: 80px;">方法</th>
              <th>路径</th>
              <th style="width: 90px;">状态</th>
              <th>错误信息</th>
              <th style="width: 120px;">IP</th>
              <th style="width: 90px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pagedLogs" :key="row.id">
              <td>{{ formatTime(row.createdAt) }}</td>
              <td>{{ row.username || '-' }}</td>
              <td>{{ row.roleCodes || '-' }}</td>
              <td>{{ row.method || '-' }}</td>
              <td class="mono">{{ row.path || '-' }}</td>
              <td>
                <el-tag size="small" :type="statusType(row.statusCode)">
                  {{ row.statusCode ?? '-' }}
                </el-tag>
              </td>
              <td>
                <el-tooltip v-if="row.errorMessage" :content="row.errorMessage" placement="top">
                  <span class="ellipsis">{{ row.errorMessage }}</span>
                </el-tooltip>
                <span v-else>-</span>
              </td>
              <td>{{ row.ip || '-' }}</td>
              <td>
                <VeaButton size="small" type="primary" plain @click="openDetail(row)">详情</VeaButton>
              </td>
            </tr>
            <tr v-if="!pagedLogs.length">
              <td colspan="9" class="notice">暂无日志</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="vea-pagination">
        <VeaButton :icon="Refresh" @click="loadAll">刷新</VeaButton>
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="filteredLogs.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50]"
          @size-change="pageSize = $event"
          @current-change="currentPage = $event"
        />
      </div>
    </div>

    <el-drawer
      v-model="showDetail"
      class="log-drawer"
      direction="rtl"
      size="460px"
      :with-header="false"
    >
      <div class="log-drawer-header">
        <div class="log-drawer-title">日志详情</div>
        <VeaButton class="log-drawer-close" text @click="closeDetail">关闭</VeaButton>
      </div>
      <div class="log-drawer-body" v-if="selectedLog">
        <div class="log-detail-grid">
          <div class="log-detail-item">
            <div class="label">ID</div>
            <div class="value">{{ selectedLog.id ?? '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">时间</div>
            <div class="value">{{ formatTime(selectedLog.createdAt) }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">用户ID</div>
            <div class="value">{{ selectedLog.userId ?? '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">用户名</div>
            <div class="value">{{ selectedLog.username || '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">角色</div>
            <div class="value">{{ selectedLog.roleCodes || '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">方法</div>
            <div class="value">{{ selectedLog.method || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">路径</div>
            <div class="value mono">{{ selectedLog.path || '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">状态码</div>
            <div class="value">{{ selectedLog.statusCode ?? '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">IP</div>
            <div class="value">{{ selectedLog.ip || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">错误信息</div>
            <div class="value">{{ selectedLog.errorMessage || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">请求参数</div>
            <div class="value">{{ selectedLog.queryParams || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">请求体</div>
            <div class="value">{{ selectedLog.requestBody || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">响应体</div>
            <div class="value">{{ selectedLog.responseBody || '-' }}</div>
          </div>
          <div class="log-detail-item span-2">
            <div class="label">User Agent</div>
            <div class="value">{{ selectedLog.userAgent || '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="label">更新时间</div>
            <div class="value">{{ formatTime(selectedLog.updatedAt) }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Search, Refresh, Tickets } from '@element-plus/icons-vue'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'

const logs = ref([])
const filters = ref({
  keyword: '',
  status: '',
  dateRange: []
})
const pageSize = ref(10)
const currentPage = ref(1)
const showDetail = ref(false)
const selectedLog = ref(null)

const loadAll = async () => {
  const params = {}
  const [startDate, endDate] = filters.value.dateRange || []
  if (startDate) params.startDate = startDate
  if (endDate) params.endDate = endDate
  const { data } = await api.get('/admin/logs', { params })
  logs.value = Array.isArray(data) ? data : []
  clampPage()
}

const filteredLogs = computed(() => {
  let list = logs.value
  if (filters.value.status) {
    list = list.filter(row => Number(row.statusCode) === Number(filters.value.status))
  }
  if (filters.value.keyword) {
    const k = filters.value.keyword.toLowerCase()
    list = list.filter(row => {
      const username = String(row.username || '').toLowerCase()
      const path = String(row.path || '').toLowerCase()
      const error = String(row.errorMessage || '').toLowerCase()
      return username.includes(k) || path.includes(k) || error.includes(k)
    })
  }
  return list
})

const pagedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredLogs.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(filteredLogs.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

watch([logs, pageSize], () => {
  clampPage()
})

const resetFilters = () => {
  filters.value.keyword = ''
  filters.value.status = ''
  filters.value.dateRange = []
  loadAll()
}

const formatTime = (value) => {
  if (!value) return '-'
  const str = String(value)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  return str
}

const statusType = (status) => {
  const code = Number(status)
  if (code >= 500) return 'danger'
  if (code >= 400) return 'warning'
  return 'success'
}

const openDetail = (row) => {
  selectedLog.value = row
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
}

onMounted(loadAll)
</script>

<style scoped>
.ellipsis {
  display: inline-block;
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
}

.log-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.log-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.log-drawer-close {
  padding: 0;
}

.log-drawer-body {
  padding: 16px;
}

.log-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.log-detail-item {
  border: 1px solid rgba(28, 27, 34, 0.08);
  border-radius: 10px;
  padding: 10px 12px;
  background: #fff;
  display: grid;
  gap: 6px;
}

.log-detail-item .label {
  font-size: 12px;
  color: #606266;
}

.log-detail-item .value {
  font-size: 13px;
  color: #1f2328;
  word-break: break-word;
}

.log-detail-item.span-2 {
  grid-column: span 2;
}
</style>
