<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>孩子管理</h2>
        <div class="notice">创建孩子账号并查看积分情况</div>
      </div>
    </div>

    <div class="row" style="max-width: 220px; margin-bottom: 12px;">
      <select v-model.number="days" @change="refresh">
        <option :value="7">近 7 天</option>
        <option :value="14">近 14 天</option>
        <option :value="30">近 30 天</option>
        <option :value="60">近 60 天</option>
      </select>
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
      <div class="row compact" style="justify-content: space-between; align-items: center;">
        <h3 style="margin: 0;">孩子列表</h3>
        <VeaButton v-if="can('parent.child.create')" class="btn-sm btn-soft success" @click="openChildModal">
          <span class="btn-icon"><el-icon><Plus /></el-icon></span>
          新增
        </VeaButton>
      </div>
      <div class="table-wrap" style="margin-top: 12px;">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>名称</th>
              <th>账号</th>
              <th>积分</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(child, index) in pagedChildren" :key="child.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ child.displayName }}</td>
              <td>{{ child.username }}</td>
              <td>{{ child.points }}</td>
            </tr>
          </tbody>
        </table>
        <div class="notice" v-if="!children.length">暂无孩子</div>
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
          <div class="drawer-title">新增孩子</div>
          <VeaButton class="modal-close" @click="closeChildModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <label>账号</label>
          <input v-model="childForm.username" placeholder="孩子账号" />
          <label style="margin-top: 10px;">密码</label>
          <input v-model="childForm.password" type="password" placeholder="孩子密码" />
          <label style="margin-top: 10px;">显示名</label>
          <input v-model="childForm.displayName" placeholder="例如：小明" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeChildModal" text>取消</VeaButton>
          <VeaButton v-if="can('parent.child.create')" @click="createChild">
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
import { Plus, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'

const children = ref([])
const childForm = ref({ username: '', password: '', displayName: '' })
const { stats, days, refresh, notifyUpdate } = useParentStats()
const showChildModal = ref(false)
const pageSize = ref(10)
const currentPage = ref(1)

const loadAll = async () => {
  const { data } = await api.get('/parent/children')
  children.value = data
  clampPage()
}

const createChild = async () => {
  if (!childForm.value.username.trim() || !childForm.value.password.trim() || !childForm.value.displayName.trim()) {
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
  await api.post('/parent/children', childForm.value)
  ElMessage.success('新增成功')
  childForm.value = { username: '', password: '', displayName: '' }
  await loadAll()
  notifyUpdate()
  closeChildModal()
}

const openChildModal = () => {
  childForm.value = { username: '', password: '', displayName: '' }
  showChildModal.value = true
}

const closeChildModal = () => {
  showChildModal.value = false
  childForm.value = { username: '', password: '', displayName: '' }
}

const totalPages = computed(() => Math.max(1, Math.ceil(children.value.length / pageSize.value)))
const pagedChildren = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return children.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(children.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

watch([children, pageSize], () => {
  clampPage()
})

onMounted(loadAll)
</script>
