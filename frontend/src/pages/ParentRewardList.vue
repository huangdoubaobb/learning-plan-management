<template>
  <div class="container reward-page">
    <div class="header">
      <div>
        <h2>奖励管理</h2>
        <div class="notice">创建奖励并维护积分、库存和展示图片</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Present /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">奖励数量</div>
          <div class="vea-stat-value">{{ stats.rewards }}</div>
          <div class="vea-stat-foot">当前可管理的奖励总数</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">孩子数量</div>
          <div class="vea-stat-value">{{ stats.children }}</div>
          <div class="vea-stat-foot">当前绑定的孩子账号</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">任务数量</div>
          <div class="vea-stat-value">{{ stats.tasks }}</div>
          <div class="vea-stat-foot">近 {{ days }} 天新增 {{ stats.tasksInRange }}</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-red"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待审核兑换</div>
          <div class="vea-stat-value">{{ stats.pending }}</div>
          <div class="vea-stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="row compact reward-head">
        <h3 style="margin: 0;">奖励列表</h3>
      </div>

      <div class="reward-filter-bar">
        <div class="reward-filter">
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input
              v-model="draftFilters.keyword"
              size="small"
              placeholder="搜索名称/描述"
              clearable
              style="width: 220px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">库存:</span>
            <el-select v-model="draftFilters.stockStatus" size="small" style="width: 140px;">
              <el-option label="全部" value="ALL" />
              <el-option label="有库存" value="IN_STOCK" />
              <el-option label="无库存" value="OUT_OF_STOCK" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">开始:</span>
            <el-date-picker
              v-model="draftFilters.startDate"
              type="date"
              size="small"
              value-format="YYYY-MM-DD"
              placeholder="开始日期"
              clearable
              :disabled-date="disableStartDate"
              style="width: 150px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">结束:</span>
            <el-date-picker
              v-model="draftFilters.endDate"
              type="date"
              size="small"
              value-format="YYYY-MM-DD"
              placeholder="结束日期"
              clearable
              :disabled-date="disableEndDate"
              style="width: 150px;"
            />
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

      <div class="vea-table-toolbar">
        <VeaButton
          v-if="can('parent.reward.create')"
          class="btn-sm btn-soft success"
          @click="openCreateModal"
        >
          <span class="btn-icon"><el-icon><Plus /></el-icon></span>
          新增奖励
        </VeaButton>
      </div>

      <div class="table-wrap" style="margin-top: 12px;">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 60px;">序号</th>
              <th>名称</th>
              <th>所需积分</th>
              <th>库存</th>
              <th>图片</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in pagedRewards" :key="reward.id">
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
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
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton class="btn-sm btn-soft neutral" @click="openDetail(reward)">
                    <span class="btn-icon"><el-icon><View /></el-icon></span>
                    详情
                  </VeaButton>
                  <VeaButton
                    v-if="can('parent.reward.create')"
                    class="btn-sm btn-soft info"
                    @click="openEditModal(reward)"
                  >
                    <span class="btn-icon"><el-icon><Edit /></el-icon></span>
                    修改
                  </VeaButton>
                  <VeaButton
                    v-if="can('parent.reward.create')"
                    class="btn-sm btn-soft danger"
                    @click="removeReward(reward)"
                  >
                    <span class="btn-icon"><el-icon><Delete /></el-icon></span>
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
            <tr v-if="!pagedRewards.length">
              <td colspan="6" class="notice">暂无奖励</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
        <select v-model.number="pageSize">
          <option :value="10">每页 10 条</option>
          <option :value="20">每页 20 条</option>
          <option :value="50">每页 50 条</option>
        </select>
        <VeaButton class="secondary" :disabled="currentPage === 1" text @click="currentPage--">
          上一页
        </VeaButton>
        <div class="notice">第 {{ currentPage }} / {{ totalPages }} 页</div>
        <VeaButton class="secondary" :disabled="currentPage >= totalPages" text @click="currentPage++">
          下一页
        </VeaButton>
      </div>
    </div>

    <div v-if="showRewardModal" class="drawer-backdrop" @click="closeRewardModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ formMode === 'create' ? '新增奖励' : '修改奖励' }}</div>
          <VeaButton class="modal-close" text @click="closeRewardModal">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <label>奖励名称</label>
          <input v-model="rewardForm.name" placeholder="例如：周末电影票" />

          <label style="margin-top: 10px;">描述</label>
          <input v-model="rewardForm.description" placeholder="补充奖励说明" />

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

          <div class="upload-block">
            <label>奖励图片</label>
            <div class="upload-row">
              <input
                ref="imageInput"
                type="file"
                class="hidden-input"
                accept="image/*"
                @change="handleImageChange"
              />
              <VeaButton class="btn-sm btn-soft info" @click="triggerImagePick">
                <span class="btn-icon"><el-icon><Upload /></el-icon></span>
                选择图片
              </VeaButton>
              <span class="notice" v-if="imageFile">已选择 1 张</span>
            </div>

            <div v-if="formMode === 'edit' && existingRewardImages.length" class="notice">
              已有图片，上传新图后将替换展示内容
            </div>

            <div v-if="existingRewardImages.length" class="image-thumbs">
              <img
                v-for="(img, idx) in existingRewardImages"
                :key="`${img}-${idx}`"
                :src="resolveImageUrl(img)"
                class="thumb"
                @click="openImagePreview(existingRewardImages, idx)"
              />
            </div>

            <div v-if="imagePreview" class="image-thumbs">
              <div class="thumb-wrap">
                <img :src="imagePreview.url" class="thumb" />
                <button type="button" class="thumb-remove" @click="removeImage">x</button>
              </div>
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" text @click="closeRewardModal">取消</VeaButton>
          <VeaButton v-if="can('parent.reward.create')" @click="saveReward">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>

    <div v-if="showDetailDrawer" class="drawer-backdrop" @click="closeDetailDrawer">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">奖励详情</div>
          <VeaButton class="modal-close" text @click="closeDetailDrawer">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="detail-row">
            <span class="detail-label">名称</span>
            <span class="detail-value">{{ detailReward?.name || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">描述</span>
            <span class="detail-value">{{ detailReward?.description || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">积分</span>
            <span class="detail-value">{{ detailReward?.pointsCost ?? '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">库存</span>
            <span class="detail-value">{{ detailReward?.stock ?? '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">图片</span>
            <div class="detail-images">
              <div v-if="detailReward?.images?.length" class="image-thumbs">
                <img
                  v-for="(img, idx) in detailReward.images"
                  :key="`${img}-${idx}`"
                  :src="resolveImageUrl(img)"
                  class="thumb"
                  @click="openImagePreview(detailReward.images, idx)"
                />
              </div>
              <span v-else class="notice">-</span>
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" text @click="closeDetailDrawer">关闭</VeaButton>
        </div>
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
import { computed, onMounted, ref, watch } from 'vue'
import {
  Plus,
  Check,
  Upload,
  Edit,
  Delete,
  View,
  UserFilled,
  Calendar,
  Present,
  Checked,
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import VeaButton from '../components/VeaButton.vue'
import { useImagePreview } from '../composables/useImagePreview'
import { useParentStats } from '../composables/useParentStats'
import { useSingleImageUpload } from '../composables/useSingleImageUpload'
import { resolveImageUrl as resolveApiImageUrl } from '../utils'

const rewards = ref([])
const rewardForm = ref({ id: null, name: '', description: '', pointsCost: 0, stock: 0 })
const draftFilters = ref({ keyword: '', stockStatus: 'ALL', startDate: '', endDate: '' })
const filters = ref({ keyword: '', stockStatus: 'ALL', startDate: '', endDate: '' })
const { stats, days, refresh, notifyUpdate } = useParentStats()
const showRewardModal = ref(false)
const formMode = ref('create')
const pageSize = ref(10)
const currentPage = ref(1)
const showDetailDrawer = ref(false)
const detailReward = ref(null)
const existingRewardImages = ref([])

const loadAll = async () => {
  const { data } = await api.get('/parent/rewards')
  rewards.value = Array.isArray(data) ? data : []
  clampPage()
}

const disableStartDate = (date) => {
  if (!draftFilters.value.endDate) return false
  return date.getTime() > new Date(draftFilters.value.endDate).getTime()
}

const disableEndDate = (date) => {
  if (!draftFilters.value.startDate) return false
  return date.getTime() < new Date(draftFilters.value.startDate).getTime()
}

const filteredRewards = computed(() => {
  const keyword = filters.value.keyword.trim().toLowerCase()
  const { startDate, endDate } = filters.value
  return rewards.value.filter((reward) => {
    const name = String(reward.name || '').toLowerCase()
    const description = String(reward.description || '').toLowerCase()
    const matchKeyword = !keyword || name.includes(keyword) || description.includes(keyword)
    const matchStock =
      filters.value.stockStatus === 'ALL' ||
      (filters.value.stockStatus === 'IN_STOCK' && Number(reward.stock || 0) > 0) ||
      (filters.value.stockStatus === 'OUT_OF_STOCK' && Number(reward.stock || 0) <= 0)
    const createdAt = String(reward.createdAt || '')
    const createdDate = createdAt ? createdAt.slice(0, 10) : ''
    const matchDate =
      (!startDate || (createdDate && createdDate >= startDate)) &&
      (!endDate || (createdDate && createdDate <= endDate))
    return matchKeyword && matchStock && matchDate
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRewards.value.length / pageSize.value)))
const pagedRewards = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRewards.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(filteredRewards.value.length / pageSize.value))
  if (currentPage.value > maxPage) {
    currentPage.value = maxPage
  }
}

const applyFilters = () => {
  filters.value = { ...draftFilters.value }
  currentPage.value = 1
  clampPage()
}

const resetFilters = () => {
  draftFilters.value = { keyword: '', stockStatus: 'ALL', startDate: '', endDate: '' }
  filters.value = { keyword: '', stockStatus: 'ALL', startDate: '', endDate: '' }
  currentPage.value = 1
  clampPage()
}

watch([filteredRewards, pageSize], () => {
  clampPage()
})

const resolveImageUrl = (url) => resolveApiImageUrl(api, url)

const {
  showImagePreview,
  previewImages,
  previewIndex,
  openImagePreview,
  closeImagePreview,
  prevImage,
  nextImage
} = useImagePreview(resolveImageUrl)

const {
  imageInput,
  imageFile,
  imagePreview,
  resetImageState,
  triggerImagePick,
  handleImageChange,
  removeImage,
  uploadImage
} = useSingleImageUpload({
  api,
  message: ElMessage,
  uploadPath: (rewardId) => `/parent/rewards/${rewardId}/images`
})

const resetRewardForm = () => {
  rewardForm.value = { id: null, name: '', description: '', pointsCost: 0, stock: 0 }
  existingRewardImages.value = []
  resetImageState()
}

const openCreateModal = () => {
  formMode.value = 'create'
  resetRewardForm()
  showRewardModal.value = true
}

const openEditModal = (reward) => {
  formMode.value = 'edit'
  rewardForm.value = {
    id: reward.id,
    name: reward.name || '',
    description: reward.description || '',
    pointsCost: reward.pointsCost ?? 0,
    stock: reward.stock ?? 0
  }
  existingRewardImages.value = Array.isArray(reward.images) ? reward.images : []
  resetImageState()
  showRewardModal.value = true
}

const closeRewardModal = () => {
  showRewardModal.value = false
  resetRewardForm()
}

const saveReward = async () => {
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

  const payload = {
    name: rewardForm.value.name.trim(),
    description: rewardForm.value.description,
    pointsCost: rewardForm.value.pointsCost,
    stock: rewardForm.value.stock
  }

  let rewardId = rewardForm.value.id
  if (formMode.value === 'create') {
    const { data } = await api.post('/parent/rewards', payload)
    rewardId = data?.id || rewardId
    ElMessage.success('新增成功')
  } else {
    const { data } = await api.put(`/parent/rewards/${rewardForm.value.id}`, payload)
    rewardId = data?.id || rewardId
    ElMessage.success('修改成功')
  }

  if (rewardId && imageFile.value) {
    await uploadImage(rewardId)
  }

  closeRewardModal()
  await loadAll()
  notifyUpdate()
}

const removeReward = async (reward) => {
  try {
    await ElMessageBox.confirm(`确定删除奖励 ${reward.name} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  await api.delete(`/parent/rewards/${reward.id}`)
  ElMessage.success('删除成功')
  await loadAll()
  notifyUpdate()
}

const openDetail = async (reward) => {
  detailReward.value = reward
  showDetailDrawer.value = true
  try {
    const { data } = await api.get(`/parent/rewards/${reward.id}`)
    if (data) {
      detailReward.value = data
    }
  } catch {
    detailReward.value = reward
  }
}

const closeDetailDrawer = () => {
  showDetailDrawer.value = false
  detailReward.value = null
}

onMounted(loadAll)
</script>

<style scoped>
.reward-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reward-head {
  justify-content: space-between;
  align-items: center;
}

.reward-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  margin-top: 12px;
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
  flex: 0 0 auto;
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

.upload-block {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hidden-input {
  display: none;
}

.thumb-wrap {
  position: relative;
  display: inline-flex;
}

.thumb-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  line-height: 18px;
  padding: 0;
}

.thumb-remove:hover {
  background: rgba(0, 0, 0, 0.75);
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed rgba(28, 27, 34, 0.08);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  width: 90px;
  flex: 0 0 90px;
  font-size: 12px;
  color: var(--muted);
}

.detail-value {
  flex: 1;
  color: var(--text);
  word-break: break-word;
}

.detail-images {
  flex: 1;
}

.image-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.image-thumbs .thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid rgba(28, 27, 34, 0.12);
  background: #fff;
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
