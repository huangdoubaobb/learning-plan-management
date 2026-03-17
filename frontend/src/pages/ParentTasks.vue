<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>任务与奖励</h2>
        <div class="notice">创建任务、奖励，并处理兑换审核</div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">任务数量</div>
        <div class="stat-value">{{ tasks.length }}</div>
        <div class="stat-foot">当前任务总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">奖励数量</div>
        <div class="stat-value">{{ rewards.length }}</div>
        <div class="stat-foot">可兑换奖励</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">待审核兑换</div>
        <div class="stat-value">{{ pendingCount }}</div>
        <div class="stat-foot">需要处理</div>
      </div>
    </div>

    <div class="card">
      <div class="row compact" style="justify-content: space-between; align-items: center;">
        <div>
          <h3 style="margin: 0;">创建每日任务</h3>
          <div class="notice">提示：重复任务会在孩子完成后自动生成下一次任务。</div>
        </div>
        <VeaButton class="btn-sm btn-soft success" @click="openTaskModal">
          <span class="btn-icon"><el-icon><Plus /></el-icon></span>
          新增
        </VeaButton>
      </div>
    </div>

    <div class="card">
      <h3>任务列表</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>孩子</th>
              <th>任务</th>
              <th>积分</th>
              <th>状态</th>
              <th>打卡时间</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in tasks" :key="task.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ childName(task.childId) }}</td>
              <td>{{ task.title }}</td>
              <td>{{ task.points }}</td>
              <td>{{ task.status }}</td>
              <td>{{ formatDateTime(task.completedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="row compact" style="justify-content: space-between; align-items: center;">
        <h3 style="margin: 0;">奖励列表</h3>
        <VeaButton class="btn-sm btn-soft success" @click="openRewardModal">
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
              <th>积分</th>
              <th>库存</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in rewards" :key="reward.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ reward.name }}</td>
              <td>{{ reward.pointsCost }}</td>
              <td>{{ reward.stock }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h3>兑换审核</h3>
      <div class="notice">孩子兑换后会进入审核列表</div>
      <div class="table-wrap" style="margin-top: 12px;">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>孩子</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in redemptions" :key="item.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ item.childName }}</td>
              <td>{{ item.rewardName }}</td>
              <td>{{ item.pointsCost }}</td>
              <td>{{ item.status }}</td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton class="btn-sm success" v-if="item.status === 'PENDING'" @click="approveRedemption(item.id)">
                    <span class="btn-icon"><el-icon><Check /></el-icon></span>
                    同意
                  </VeaButton>
                  <VeaButton class="btn-sm danger" v-if="item.status === 'PENDING'" @click="rejectRedemption(item.id)">
                    <span class="btn-icon"><el-icon><Close /></el-icon></span>
                    拒绝
                  </VeaButton>
                  <span v-else class="notice">已处理</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showTaskModal" class="drawer-backdrop" @click="closeTaskModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">创建任务</div>
          <VeaButton class="modal-close" @click="closeTaskModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="row">
            <div>
              <label>选择孩子</label>
              <select v-model="taskForm.childId">
                <option disabled value="">请选择</option>
                <option v-for="child in children" :key="child.id" :value="child.id">{{ child.displayName }}</option>
              </select>
            </div>
            <div>
              <label>任务标题</label>
              <input v-model="taskForm.title" placeholder="例如：完成数学作业" />
            </div>
          </div>
          <div class="row" style="margin-top: 10px;">
            <div>
              <label>积分</label>
              <input v-model.number="taskForm.points" type="number" />
            </div>
          </div>
          <label style="margin-top: 10px;">描述</label>
          <textarea v-model="taskForm.description" rows="3"></textarea>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeTaskModal" text>取消</VeaButton>
          <VeaButton @click="createTask">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>

    <div v-if="showRewardModal" class="drawer-backdrop" @click="closeRewardModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">创建奖励</div>
          <VeaButton class="modal-close" @click="closeRewardModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <label>奖励名称</label>
          <input v-model="rewardForm.name" placeholder="例如：周末电影票" />
          <label style="margin-top: 10px;">描述</label>
          <input v-model="rewardForm.description" placeholder="可选" />
          <div class="row" style="margin-top: 10px;">
            <div>
              <label>所需积分</label>
              <input v-model.number="rewardForm.pointsCost" type="number" />
            </div>
            <div>
              <label>库存</label>
              <input v-model.number="rewardForm.stock" type="number" />
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeRewardModal" text>取消</VeaButton>
          <VeaButton @click="createReward">
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
import { Plus, Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'

const children = ref([])
const tasks = ref([])
const rewards = ref([])
const redemptions = ref([])

const taskForm = ref({ childId: '', title: '', description: '', points: 0 })
const rewardForm = ref({ name: '', description: '', pointsCost: 0, stock: 0 })
const showTaskModal = ref(false)
const showRewardModal = ref(false)

const pendingCount = computed(() => redemptions.value.filter(item => item.status === 'PENDING').length)

const loadAll = async () => {
  const [childRes, taskRes, rewardRes, redemptionRes] = await Promise.all([
    api.get('/parent/children'),
    api.get('/parent/tasks'),
    api.get('/parent/rewards'),
    api.get('/parent/redemptions')
  ])
  children.value = childRes.data
  tasks.value = taskRes.data
  rewards.value = rewardRes.data
  redemptions.value = redemptionRes.data

  if (children.value.length && !taskForm.value.childId) {
    taskForm.value.childId = children.value[0].id
  }
}

const createTask = async () => {
  if (!taskForm.value.childId || !taskForm.value.title.trim()) {
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
  await api.post('/parent/tasks', taskForm.value)
  ElMessage.success('新增成功')
  taskForm.value.title = ''
  taskForm.value.description = ''
  await loadAll()
  closeTaskModal()
}

const createReward = async () => {
  if (!rewardForm.value.name.trim()) {
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
  await api.post('/parent/rewards', rewardForm.value)
  ElMessage.success('新增成功')
  rewardForm.value = { name: '', description: '', pointsCost: 0, stock: 0 }
  await loadAll()
  closeRewardModal()
}

const openTaskModal = () => {
  showTaskModal.value = true
}

const closeTaskModal = () => {
  showTaskModal.value = false
  taskForm.value.title = ''
  taskForm.value.description = ''
}

const openRewardModal = () => {
  showRewardModal.value = true
}

const closeRewardModal = () => {
  showRewardModal.value = false
  rewardForm.value = { name: '', description: '', pointsCost: 0, stock: 0 }
}

const approveRedemption = async (id) => {
  await api.post(`/parent/redemptions/${id}/approve`)
  ElMessage.success('操作成功')
  await loadAll()
}

const rejectRedemption = async (id) => {
  await api.post(`/parent/redemptions/${id}/reject`)
  ElMessage.success('操作成功')
  await loadAll()
}

const childName = (id) => {
  return children.value.find(c => c.id === id)?.displayName || '-'
}
\r\nconst formatDateTime = (value) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(loadAll)
</script>
