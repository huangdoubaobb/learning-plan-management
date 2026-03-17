<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>奖励管理</h2>
        <div class="notice">创建奖励并维护库存</div>
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
        <div class="stat-title">奖励数量</div>
        <div class="stat-value">{{ stats.rewards }}</div>
        <div class="stat-foot">可兑换奖励</div>
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
        <div class="stat-title">待审核兑换</div>
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-foot">近 {{ days }} 天兑换 {{ stats.redemptionsInRange }}</div>
      </div>
    </div>

    <div class="card">
      <div class="row compact" style="justify-content: space-between; align-items: center;">
        <h3 style="margin: 0;">奖励列表</h3>
        <VeaButton v-if="can('parent.reward.create')" class="btn-sm btn-soft success" @click="openCreateModal">
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
              <th>图片</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(reward, index) in pagedRewards" :key="reward.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ reward.name }}</td>
              <td>{{ reward.pointsCost }}</td>
              <td>{{ reward.stock }}</td>
              <td>
                <div class="image-thumbs" v-if="reward.images && reward.images.length">
                  <img
                    v-for="(img, idx) in reward.images"
                    :key="img + idx"
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
                    明细
                  </VeaButton>
                  <VeaButton v-if="can('parent.reward.create')" class="btn-sm btn-soft info" @click="openEditModal(reward)">
                    <span class="btn-icon"><el-icon><Edit /></el-icon></span>
                    修改
                  </VeaButton>
                  <VeaButton v-if="can('parent.reward.create')" class="btn-sm btn-soft danger" @click="removeReward(reward)">
                    <span class="btn-icon"><el-icon><Delete /></el-icon></span>
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="notice" v-if="!rewards.length">暂无奖励</div>
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

    <div v-if="showRewardModal" class="drawer-backdrop" @click="closeRewardModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">{{ formMode === 'create' ? '新增奖励' : '修改奖励' }}</div>
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
          <div class="upload-block">
            <label>奖励图片</label>
            <div class="upload-row">
              <input
                ref="rewardImageInput"
                type="file"
                class="hidden-input"
                accept="image/*"
                @change="handleRewardImageChange"
              />
              <VeaButton class="btn-sm btn-soft info" @click="triggerRewardImagePick">
                <span class="btn-icon"><el-icon><Upload /></el-icon></span>
                选择图片
              </VeaButton>
              <span class="notice" v-if="rewardImageFile">已选择 1 张</span>
            </div>
            <div class="notice" v-if="formMode === 'edit' && existingRewardImages.length">
              已有图片（新上传将替换）
            </div>
            <div class="image-thumbs" v-if="existingRewardImages.length">
              <img
                v-for="(img, idx) in existingRewardImages"
                :key="img + idx"
                :src="resolveImageUrl(img)"
                class="thumb"
                @click="openImagePreview(existingRewardImages, idx)"
              />
            </div>
            <div class="image-thumbs" v-if="rewardImagePreview">
              <div class="thumb-wrap">
                <img :src="rewardImagePreview.url" class="thumb" />
                <button type="button" class="thumb-remove" @click="removeRewardImage">×</button>
              </div>
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeRewardModal" text>取消</VeaButton>
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
          <div class="drawer-title">奖励明细</div>
          <VeaButton class="modal-close" @click="closeDetailDrawer" text>关闭</VeaButton>
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
              <div class="image-thumbs" v-if="detailReward?.images && detailReward.images.length">
                <img
                  v-for="(img, idx) in detailReward.images"
                  :key="img + idx"
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
          <VeaButton class="secondary" @click="closeDetailDrawer" text>关闭</VeaButton>
        </div>
      </div>
    </div>

    <div v-if="showImagePreview" class="modal-backdrop" @click="closeImagePreview">
      <div class="modal image-preview-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">图片预览</div>
          <VeaButton class="modal-close" @click="closeImagePreview" text>关闭</VeaButton>
        </div>
        <div class="modal-body">
          <img v-if="previewImages.length" :src="previewImages[previewIndex]" class="preview-image" />
        </div>
        <div class="modal-footer">
          <VeaButton class="secondary" :disabled="previewIndex <= 0" @click="prevImage" text>上一张</VeaButton>
          <div class="notice">第 {{ previewIndex + 1 }} / {{ previewImages.length }} 张</div>
          <VeaButton class="secondary" :disabled="previewIndex >= previewImages.length - 1" @click="nextImage" text>下一张</VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Plus, Check, Upload, Edit, Delete, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import { useParentStats } from '../composables/useParentStats'
import VeaButton from '../components/VeaButton.vue'

const rewards = ref([])
const rewardForm = ref({ id: null, name: '', description: '', pointsCost: 0, stock: 0 })
const { stats, days, refresh, notifyUpdate } = useParentStats()
const showRewardModal = ref(false)
const formMode = ref('create')
const pageSize = ref(10)
const currentPage = ref(1)
const showDetailDrawer = ref(false)
const detailReward = ref(null)
const showImagePreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)

const rewardImageInput = ref(null)
const rewardImageFile = ref(null)
const rewardImagePreview = ref(null)
const existingRewardImages = ref([])

const loadAll = async () => {
  const { data } = await api.get('/parent/rewards')
  rewards.value = data
  clampPage()
}

const openCreateModal = () => {
  formMode.value = 'create'
  rewardForm.value = { id: null, name: '', description: '', pointsCost: 0, stock: 0 }
  existingRewardImages.value = []
  resetRewardImageState()
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
  resetRewardImageState()
  showRewardModal.value = true
}

const closeRewardModal = () => {
  showRewardModal.value = false
  rewardForm.value = { id: null, name: '', description: '', pointsCost: 0, stock: 0 }
  existingRewardImages.value = []
  resetRewardImageState()
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
    name: rewardForm.value.name,
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
  if (rewardId && rewardImageFile.value) {
    await uploadRewardImages(rewardId)
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
    if (data) detailReward.value = data
  } catch {
    detailReward.value = reward
  }
}

const closeDetailDrawer = () => {
  showDetailDrawer.value = false
  detailReward.value = null
}

const totalPages = computed(() => Math.max(1, Math.ceil(rewards.value.length / pageSize.value)))
const pagedRewards = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return rewards.value.slice(start, start + pageSize.value)
})

const clampPage = () => {
  const maxPage = Math.max(1, Math.ceil(rewards.value.length / pageSize.value))
  if (currentPage.value > maxPage) currentPage.value = maxPage
}

watch([rewards, pageSize], () => {
  clampPage()
})

const resolveImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = api.defaults.baseURL.replace(/\/api$/, '')
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`
}

const openImagePreview = (images, index = 0) => {
  const list = Array.isArray(images) ? images.map(resolveImageUrl) : []
  previewImages.value = list
  previewIndex.value = Math.max(0, Math.min(index, list.length - 1))
  showImagePreview.value = true
}

const closeImagePreview = () => {
  showImagePreview.value = false
  previewImages.value = []
  previewIndex.value = 0
}

const prevImage = () => {
  if (previewIndex.value > 0) previewIndex.value -= 1
}

const nextImage = () => {
  if (previewIndex.value < previewImages.value.length - 1) previewIndex.value += 1
}

const triggerRewardImagePick = () => {
  if (rewardImageInput.value) {
    rewardImageInput.value.value = ''
    rewardImageInput.value.click()
  }
}

const resetRewardImageState = () => {
  if (rewardImagePreview.value?.url) {
    URL.revokeObjectURL(rewardImagePreview.value.url)
  }
  rewardImagePreview.value = null
  rewardImageFile.value = null
  if (rewardImageInput.value) {
    rewardImageInput.value.value = ''
  }
}

const handleRewardImageChange = (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  if (!file.type || !file.type.startsWith('image/')) {
    ElMessage.warning('仅支持图片文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }
  if (rewardImagePreview.value?.url) {
    URL.revokeObjectURL(rewardImagePreview.value.url)
  }
  rewardImageFile.value = file
  rewardImagePreview.value = { url: URL.createObjectURL(file), name: file.name }
}

const removeRewardImage = () => {
  if (rewardImagePreview.value?.url) {
    URL.revokeObjectURL(rewardImagePreview.value.url)
  }
  rewardImagePreview.value = null
  rewardImageFile.value = null
}

const uploadRewardImages = async (rewardId) => {
  if (!rewardImageFile.value) return
  const formData = new FormData()
  formData.append('file', rewardImageFile.value)
  await api.post(`/parent/rewards/${rewardId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

onMounted(loadAll)
</script>

<style scoped>
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
  gap: 6px;
}

.image-thumbs .thumb {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid rgba(28, 27, 34, 0.12);
  cursor: pointer;
}

.image-preview-modal {
  max-width: 720px;
}

.preview-image {
  width: 100%;
  max-height: 520px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid rgba(28, 27, 34, 0.08);
  background: #fff;
}
</style>
