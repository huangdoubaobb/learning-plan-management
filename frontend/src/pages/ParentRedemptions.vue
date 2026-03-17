<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>兑换审核</h2>
        <div class="notice">处理孩子的兑换申请</div>
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
        <div class="stat-title">待审核兑换</div>
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
      </div>
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
    </div>

    <div class="card">
      <div class="row" style="max-width: 520px; margin-bottom: 12px;">
        <VeaButton class="btn-sm success" v-if="can('parent.redemption.review')" @click="openBatchModal('approve')" :disabled="!selectedIds.length">
          <span class="btn-icon"><el-icon><Check /></el-icon></span>
          批量同意
        </VeaButton>
        <VeaButton class="btn-sm danger" v-if="can('parent.redemption.review')" @click="openBatchModal('reject')" :disabled="!selectedIds.length">
          <span class="btn-icon"><el-icon><Close /></el-icon></span>
          批量拒绝
        </VeaButton>
        <div class="notice">已选 {{ selectedIds.length }} 项</div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>
                <input type="checkbox" :checked="allSelected" @change="toggleAll($event)" style="width: auto;" />
              </th>
              <th style="width: 60px;">序号</th>
              <th>孩子</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>审核备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in redemptions" :key="item.id">
              <td>
                <input type="checkbox" :disabled="item.status !== 'PENDING'" :value="item.id" v-model="selectedIds" style="width: auto;" />
              </td>
              <td>{{ index + 1 }}</td>
              <td>{{ item.childName }}</td>
              <td>{{ item.rewardName }}</td>
              <td>{{ item.pointsCost }}</td>
              <td>{{ item.status }}</td>
              <td>{{ item.reviewNote || '-' }}</td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton class="btn-sm success" v-if="item.status === 'PENDING' && can('parent.redemption.review')" @click="openSingleModal('approve', item.id)">
                    <span class="btn-icon"><el-icon><Check /></el-icon></span>
                    同意
                  </VeaButton>
                  <VeaButton class="btn-sm danger" v-if="item.status === 'PENDING' && can('parent.redemption.review')" @click="openSingleModal('reject', item.id)">
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

    <div v-if="showReviewModal" class="drawer-backdrop" @click="closeReviewModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ reviewMode === 'approve' ? '同意兑换' : '拒绝兑换' }}</div>
          <VeaButton class="modal-close" @click="closeReviewModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice" style="margin-bottom: 10px;">已选 {{ reviewIds.length }} 项</div>
          <label>处理备注（可选）</label>
          <input v-model="reviewNote" placeholder="填写审核备注" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeReviewModal" text>取消</VeaButton>
          <VeaButton @click="confirmReview">
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
import { Check, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'

const redemptions = ref([])
const selectedIds = ref([])
const reviewNote = ref('')
const showReviewModal = ref(false)
const reviewMode = ref('approve')
const reviewIds = ref([])
const { stats, days, refresh, notifyUpdate } = useParentStats()
const allSelected = computed(() => {
  const pendingIds = redemptions.value.filter(item => item.status === 'PENDING').map(item => item.id)
  return pendingIds.length > 0 && pendingIds.every(id => selectedIds.value.includes(id))
})

const loadAll = async () => {
  const { data } = await api.get('/parent/redemptions')
  redemptions.value = data
}

const approveRedemption = async (id, note) => {
  await api.post(`/parent/redemptions/${id}/approve`, { note })
  await loadAll()
  notifyUpdate()
}

const rejectRedemption = async (id, note) => {
  await api.post(`/parent/redemptions/${id}/reject`, { note })
  await loadAll()
  notifyUpdate()
}

const toggleAll = (event) => {
  if (event.target.checked) {
    selectedIds.value = redemptions.value.filter(item => item.status === 'PENDING').map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const approveBatch = async (ids, note) => {
  await api.post('/parent/redemptions/batch-approve', {
    ids,
    note
  })
  selectedIds.value = []
  await loadAll()
  notifyUpdate()
}

const rejectBatch = async (ids, note) => {
  await api.post('/parent/redemptions/batch-reject', {
    ids,
    note
  })
  selectedIds.value = []
  await loadAll()
  notifyUpdate()
}

const openSingleModal = (mode, id) => {
  reviewMode.value = mode
  reviewIds.value = [id]
  reviewNote.value = ''
  showReviewModal.value = true
}

const openBatchModal = (mode) => {
  reviewMode.value = mode
  reviewIds.value = selectedIds.value.slice()
  reviewNote.value = ''
  showReviewModal.value = true
}

const closeReviewModal = () => {
  showReviewModal.value = false
  reviewNote.value = ''
  reviewIds.value = []
}

const confirmReview = async () => {
  if (!reviewIds.value.length) return
  const promptText = reviewMode.value === 'approve' ? '确认同意所选兑换吗？' : '确认拒绝所选兑换吗？'
  try {
    await ElMessageBox.confirm(promptText, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  const note = reviewNote.value
  if (reviewIds.value.length === 1) {
    if (reviewMode.value === 'approve') {
      await approveRedemption(reviewIds.value[0], note)
    } else {
      await rejectRedemption(reviewIds.value[0], note)
    }
  } else {
    if (reviewMode.value === 'approve') {
      await approveBatch(reviewIds.value, note)
    } else {
      await rejectBatch(reviewIds.value, note)
    }
  }
  ElMessage.success('操作成功')
  closeReviewModal()
}

onMounted(() => {
  refresh()
  loadAll()
})
</script>
