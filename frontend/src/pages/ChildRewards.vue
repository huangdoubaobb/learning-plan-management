<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>奖励兑换</h2>
        <div class="notice">查看可兑换奖励和历史兑换记录</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">可兑换奖励</div>
          <div class="vea-stat-value">{{ rewards.length }}</div>
          <div class="vea-stat-foot">当前可兑换奖励数量</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><Tickets /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">兑换记录</div>
          <div class="vea-stat-value">{{ redemptions.length }}</div>
          <div class="vea-stat-foot">历史兑换次数</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待审核</div>
          <div class="vea-stat-value">{{ pendingRedemptions }}</div>
          <div class="vea-stat-foot">等待家长确认</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-purple"><el-icon><Trophy /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">当前积分</div>
          <div class="vea-stat-value">{{ currentPoints }}</div>
          <div class="vea-stat-foot">可用于兑换奖励</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="table-info">
        <div class="table-title">可兑换奖励</div>
        <div class="notice">当前 {{ filteredRewards.length }} 个奖励，兑换后需要家长审核确认</div>
      </div>
      <div class="reward-filter-bar">
        <div class="reward-filter">
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input
              v-model="draftKeyword"
              size="small"
              placeholder="搜索奖励名称"
              clearable
              style="width: 220px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">条件:</span>
            <el-select v-model="draftRewardFilter" size="small" style="width: 140px;">
              <el-option label="全部" value="ALL" />
              <el-option label="可兑换" value="AFFORDABLE" />
              <el-option label="积分不足" value="INSUFFICIENT" />
              <el-option label="库存不足" value="OUT_OF_STOCK" />
            </el-select>
          </div>
        </div>
        <div class="reward-filter-actions">
          <VeaButton class="btn-sm btn-soft info" @click="applyFilters">
            <span class="btn-icon"><el-icon><Search /></el-icon></span>
            查询
          </VeaButton>
          <VeaButton class="btn-sm btn-soft neutral" @click="resetFilters">
            <span class="btn-icon"><el-icon><Refresh /></el-icon></span>
            重置
          </VeaButton>
        </div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>所需积分</th>
              <th>库存</th>
              <th>图片</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in filteredRewards" :key="reward.id">
              <td>{{ index + 1 }}</td>
              <td>{{ reward.name }}</td>
              <td>{{ reward.pointsCost }}</td>
              <td>{{ reward.stock }}</td>
              <td>
                <div v-if="reward.images?.length" class="image-thumbs">
                  <img
                    v-for="(img, idx) in reward.images"
                    :key="`${img}-${idx}`"
                    :src="resolveImageUrl(img)"
                    class="thumb"
                    @click="openImagePreview(reward.images, idx)"
                  />
                </div>
                <span v-else class="notice">-</span>
              </td>
              <td>
                <VeaButton
                  v-if="can('child.reward.redeem')"
                  class="btn-sm btn-soft info"
                  :disabled="submittingRewardId === reward.id || reward.stock <= 0 || currentPoints < reward.pointsCost"
                  @click="redeem(reward)"
                >
                  {{ submittingRewardId === reward.id ? '提交中...' : '兑换' }}
                </VeaButton>
              </td>
            </tr>
            <tr v-if="!filteredRewards.length">
              <td colspan="6" class="notice">暂无可兑换奖励</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <div class="table-info">
        <div class="table-title">兑换记录</div>
        <div class="notice">当前 {{ redemptions.length }} 条记录</div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>奖励</th>
              <th>积分</th>
              <th>状态</th>
              <th>审核备注</th>
              <th>审核时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(record, index) in redemptions" :key="record.id">
              <td>{{ index + 1 }}</td>
              <td>{{ record.rewardName }}</td>
              <td>{{ record.pointsCost }}</td>
              <td>{{ redemptionStatusLabel(record.status) }}</td>
              <td>{{ record.reviewNote || '-' }}</td>
              <td>{{ formatDateTime(record.reviewedAt) }}</td>
            </tr>
            <tr v-if="!redemptions.length">
              <td colspan="6" class="notice">暂无兑换记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <ImagePreviewModal
      :visible="showImagePreview"
      :images="previewImages"
      :current-index="previewIndex"
      @close="closeImagePreview"
      @prev="prevImage"
      @next="nextImage"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Present, Tickets, Checked, Trophy, Search, Refresh } from '@element-plus/icons-vue'
import api from '../api'
import { can } from '../permissions'
import VeaButton from '../components/VeaButton.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import { useImagePreview } from '../composables/useImagePreview'
import { formatDateTime, resolveImageUrl as resolveApiImageUrl } from '../utils'

const rewards = ref([])
const redemptions = ref([])
const submittingRewardId = ref(null)
const currentPoints = ref(0)
const draftKeyword = ref('')
const draftRewardFilter = ref('ALL')
const keyword = ref('')
const rewardFilter = ref('ALL')

const pendingRedemptions = computed(() => redemptions.value.filter((item) => item.status === 'PENDING').length)
const resolveImageUrl = (url) => resolveApiImageUrl(api, url)

const filteredRewards = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return rewards.value.filter((reward) => {
    const affordable = currentPoints.value >= (reward.pointsCost || 0)
    const inStock = (reward.stock || 0) > 0
    const matchKeyword = !q || String(reward.name || '').toLowerCase().includes(q)
    let matchFilter = true
    if (rewardFilter.value === 'AFFORDABLE') matchFilter = affordable && inStock
    if (rewardFilter.value === 'INSUFFICIENT') matchFilter = !affordable && inStock
    if (rewardFilter.value === 'OUT_OF_STOCK') matchFilter = !inStock
    return matchKeyword && matchFilter
  })
})

const {
  showImagePreview,
  previewImages,
  previewIndex,
  openImagePreview,
  closeImagePreview,
  prevImage,
  nextImage
} = useImagePreview(resolveImageUrl)

const loadAll = async () => {
  const [rewardRes, redemptionRes, meRes] = await Promise.all([
    api.get('/child/rewards'),
    api.get('/child/redemptions'),
    api.get('/child/me')
  ])
  rewards.value = Array.isArray(rewardRes.data) ? rewardRes.data : []
  redemptions.value = Array.isArray(redemptionRes.data) ? redemptionRes.data : []
  currentPoints.value = meRes.data?.points ?? 0
}

const applyFilters = () => {
  keyword.value = draftKeyword.value
  rewardFilter.value = draftRewardFilter.value
}

const resetFilters = () => {
  draftKeyword.value = ''
  draftRewardFilter.value = 'ALL'
  keyword.value = ''
  rewardFilter.value = 'ALL'
}

const redeem = async (reward) => {
  try {
    await ElMessageBox.confirm(`确认兑换奖励“${reward.name}”吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  submittingRewardId.value = reward.id
  try {
    await api.post(`/child/rewards/${reward.id}/redeem`)
    ElMessage.success('兑换申请已提交')
    await loadAll()
  } finally {
    submittingRewardId.value = null
  }
}

const redemptionStatusLabel = (status) => {
  if (status === 'PENDING') return '待审核'
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  return status || '-'
}

onMounted(loadAll)
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

.reward-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  flex-wrap: wrap;
}

.reward-filter {
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
}

.filter-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.reward-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.reward-filter-bar :deep(.el-input__wrapper),
.reward-filter-bar :deep(.el-select__wrapper) {
  min-height: 32px !important;
}

.reward-filter-bar :deep(.el-input--small),
.reward-filter-bar :deep(.el-select--small) {
  --el-component-size-small: 32px;
}

.image-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.thumb {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid rgba(28, 27, 34, 0.12);
  cursor: pointer;
}

@media (max-width: 960px) {
  .reward-filter {
    width: 100%;
  }

  .reward-filter-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>
