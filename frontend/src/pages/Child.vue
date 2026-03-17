<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>孩子学习任务</h2>
        <div class="notice">{{ me?.displayName || '-' }} · 当前积分 {{ me?.points ?? 0 }}</div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">任务总数</div>
        <div class="stat-value">{{ tasks.length }}</div>
        <div class="stat-foot">今日任务列表</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">待完成</div>
        <div class="stat-value">{{ pendingTasks }}</div>
        <div class="stat-foot">还未完成</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">可兑换奖励</div>
        <div class="stat-value">{{ rewards.length }}</div>
        <div class="stat-foot">奖励库存</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">当前积分</div>
        <div class="stat-value">{{ me?.points ?? 0 }}</div>
        <div class="stat-foot">累计积分</div>
      </div>
    </div>

    <div class="card">
      <h3>今日任务</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>任务</th>
              <th>积分</th>
              <th>状态</th>
              <th>打卡时间</th>
              <th>打卡备注</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in tasks" :key="task.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ task.title }}</td>
              <td>{{ task.points }}</td>
              <td>{{ task.status }}</td>
              <td>{{ formatDateTime(task.completedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
              <td>
                <div v-if="task.status === 'PENDING'">
                  <VeaButton class="btn-sm success" @click="openCompleteModal(task)">
                    <span class="btn-icon"><el-icon><CircleCheck /></el-icon></span>
                    完成
                  </VeaButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h3>可兑换奖励</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>所需积分</th>
              <th>库存</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in rewards" :key="reward.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ reward.name }}</td>
              <td>{{ reward.pointsCost }}</td>
              <td>{{ reward.stock }}</td>
              <td>
                <VeaButton class="btn-sm info" @click="redeem(reward.id)">
                  <span class="btn-icon"><el-icon><ShoppingCart /></el-icon></span>
                  兑换
                </VeaButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="notice" style="margin-top: 8px;">兑换后需要家长审核</div>
    </div>

    <div class="card">
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
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in redemptions" :key="record.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ record.rewardName }}</td>
              <td>{{ record.pointsCost }}</td>
              <td>{{ record.status }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showCompleteModal" class="drawer-backdrop" @click="closeCompleteModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">完成任务</div>
          <VeaButton class="modal-close" @click="closeCompleteModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice" style="margin-bottom: 10px;">{{ activeTask?.title || '' }}</div>
          <label>打卡备注</label>
          <input v-model="completionNote" placeholder="写点打卡备注" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeCompleteModal" text>取消</VeaButton>
          <VeaButton @click="confirmComplete">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { CircleCheck, ShoppingCart, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'
const me = ref(null)
const tasks = ref([])
const rewards = ref([])
const redemptions = ref([])
const noteMap = reactive({})
const showCompleteModal = ref(false)
const activeTask = ref(null)
const completionNote = ref('')
const pendingTasks = computed(() => tasks.value.filter(t => t.status === 'PENDING').length)

const loadAll = async () => {
  const [meRes, taskRes, rewardRes, redemptionRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks'),
    api.get('/child/rewards'),
    api.get('/child/redemptions')
  ])
  me.value = meRes.data
  tasks.value = taskRes.data
  rewards.value = rewardRes.data
  redemptions.value = redemptionRes.data
}

const completeTask = async (id, note) => {
  await api.post(`/child/tasks/${id}/complete`, {
    note: note || ''
  })
  if (id in noteMap) noteMap[id] = ''
  await loadAll()
}

const openCompleteModal = (task) => {
  activeTask.value = task
  completionNote.value = noteMap[task.id] || ''
  showCompleteModal.value = true
}

const closeCompleteModal = () => {
  showCompleteModal.value = false
  activeTask.value = null
  completionNote.value = ''
}

const confirmComplete = async () => {
  if (!activeTask.value) return
  try {
    await ElMessageBox.confirm('确认保存吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await completeTask(activeTask.value.id, completionNote.value)
  ElMessage.success('保存成功')
  closeCompleteModal()
}

const redeem = async (id) => {
  await api.post(`/child/rewards/${id}/redeem`)
  ElMessage.success('兑换成功')
  await loadAll()
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(loadAll)
</script>
