<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>任务列表</h2>
        <div class="notice">{{ me?.displayName || '-' }}，当前积分 {{ me?.points ?? 0 }}</div>
      </div>
    </div>

    <div class="vea-stat-grid">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">任务总数</div>
          <div class="vea-stat-value">{{ tasks.length }}</div>
          <div class="vea-stat-foot">当前可查看的全部任务</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Clock /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待完成</div>
          <div class="vea-stat-value">{{ pendingTasks }}</div>
          <div class="vea-stat-foot">尚未提交的任务数量</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">已完成</div>
          <div class="vea-stat-value">{{ completedTasks }}</div>
          <div class="vea-stat-foot">已完成或已提交审核</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-purple"><el-icon><Trophy /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">当前积分</div>
          <div class="vea-stat-value">{{ me?.points ?? 0 }}</div>
          <div class="vea-stat-foot">累计可用积分余额</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="table-info">
        <div class="table-title">任务清单</div>
        <div class="notice">当前 {{ filteredTasks.length }} 条任务</div>
      </div>
      <div class="task-filter-bar">
        <div class="task-filter">
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input
              v-model="draftKeyword"
              size="small"
              placeholder="搜索任务标题/备注"
              clearable
              style="width: 220px;"
            />
          </div>
          <div class="filter-item">
            <span class="filter-label">状态:</span>
            <el-select v-model="draftStatusFilter" size="small" style="width: 140px;">
              <el-option label="全部" value="ALL" />
              <el-option label="待完成" value="PENDING" />
              <el-option label="待审核" value="SUBMITTED" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </div>
        </div>
        <div class="task-filter-actions">
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
              <th>任务</th>
              <th>创建时间</th>
              <th>积分</th>
              <th>状态</th>
              <th>完成时间</th>
              <th>备注</th>
              <th>任务图片</th>
              <th>完成图片</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in filteredTasks" :key="task.id">
              <td>{{ index + 1 }}</td>
              <td>{{ task.title }}</td>
              <td>{{ formatDateTime(task.createdAt) }}</td>
              <td>{{ task.points }}</td>
              <td>{{ statusLabel(task) }}</td>
              <td>{{ formatDateTime(task.completedAt || task.submittedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
              <td>
                <div v-if="task.taskImages?.length" class="image-thumbs image-thumbs-inline">
                  <img
                    v-for="(img, imgIndex) in task.taskImages"
                    :key="`${img}-${imgIndex}`"
                    :src="resolveImageUrl(img)"
                    class="thumb thumb-sm"
                    @click="openImagePreview(task.taskImages, imgIndex)"
                  />
                </div>
                <span v-else class="notice">-</span>
              </td>
              <td>
                <div v-if="task.checkinImages?.length" class="image-thumbs image-thumbs-inline">
                  <img
                    v-for="(img, imgIndex) in task.checkinImages"
                    :key="`${img}-${imgIndex}`"
                    :src="resolveImageUrl(img)"
                    class="thumb thumb-sm"
                    @click="openImagePreview(task.checkinImages, imgIndex)"
                  />
                </div>
                <span v-else class="notice">-</span>
              </td>
              <td>
                <VeaButton
                  v-if="task.status === 'PENDING' && !task.submittedAt && can('child.task.complete')"
                  class="btn-sm btn-soft success"
                  @click="openCompleteModal(task)"
                >
                  <span class="btn-icon"><el-icon><CircleCheck /></el-icon></span>
                  提交
                </VeaButton>
                <span v-else class="notice">-</span>
              </td>
            </tr>
            <tr v-if="!filteredTasks.length">
              <td colspan="10" class="notice">暂无任务</td>
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

    <div v-if="showCompleteModal" class="drawer-backdrop" @click="closeCompleteModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">提交任务</div>
          <VeaButton class="modal-close" text @click="closeCompleteModal">关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice" style="margin-bottom: 10px;">{{ activeTask?.title || '' }}</div>

          <label>备注</label>
          <input v-model="completionNote" placeholder="填写任务完成说明" />

          <div class="upload-block">
            <label>上传图片</label>
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

            <div v-if="imagePreview" class="image-thumbs">
              <div class="thumb-wrap">
                <img :src="imagePreview.url" class="thumb" />
                <button type="button" class="thumb-remove" @click.stop="removeImage">x</button>
              </div>
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" text @click="closeCompleteModal">取消</VeaButton>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { CircleCheck, Check, Upload, Calendar, Clock, Checked, Trophy, Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import VeaButton from '../components/VeaButton.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import { useImagePreview } from '../composables/useImagePreview'
import { useSingleImageUpload } from '../composables/useSingleImageUpload'
import { formatDateTime, resolveImageUrl as resolveApiImageUrl } from '../utils'

const me = ref(null)
const tasks = ref([])
const noteMap = reactive({})
const showCompleteModal = ref(false)
const activeTask = ref(null)
const completionNote = ref('')
const draftKeyword = ref('')
const draftStatusFilter = ref('ALL')
const keyword = ref('')
const statusFilter = ref('ALL')

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
  removeImage
} = useSingleImageUpload({
  api,
  message: ElMessage,
  uploadPath: (taskId) => `/child/tasks/${taskId}/submit`
})

const pendingTasks = computed(() => tasks.value.filter(task => task.status === 'PENDING' && !task.submittedAt).length)
const completedTasks = computed(() => tasks.value.filter(task => task.status === 'COMPLETED' || !!task.submittedAt).length)

const filteredTasks = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  return tasks.value.filter((task) => {
    const taskStatus = task.status === 'COMPLETED'
      ? 'COMPLETED'
      : task.submittedAt
        ? 'SUBMITTED'
        : 'PENDING'
    const matchKeyword =
      !q ||
      String(task.title || '').toLowerCase().includes(q) ||
      String(task.checkinNote || '').toLowerCase().includes(q)
    const matchStatus = statusFilter.value === 'ALL' || taskStatus === statusFilter.value
    return matchKeyword && matchStatus
  })
})

const normalizeTaskImages = (task) => ({
  ...task,
  taskImages: Array.isArray(task.taskImages) ? task.taskImages : [],
  checkinImages: Array.isArray(task.checkinImages) ? task.checkinImages : [],
  images: Array.isArray(task.images) ? task.images : []
})

const loadAll = async () => {
  const [meRes, taskRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks')
  ])
  me.value = meRes.data
  tasks.value = Array.isArray(taskRes.data) ? taskRes.data.map(normalizeTaskImages) : []
}

const applyFilters = () => {
  keyword.value = draftKeyword.value
  statusFilter.value = draftStatusFilter.value
}

const resetFilters = () => {
  draftKeyword.value = ''
  draftStatusFilter.value = 'ALL'
  keyword.value = ''
  statusFilter.value = 'ALL'
}

const submitTask = async (taskId, note) => {
  const formData = new FormData()
  formData.append('note', note || '')
  if (imageFile.value) {
    formData.append('file', imageFile.value)
  }

  await api.post(`/child/tasks/${taskId}/submit`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

  if (taskId in noteMap) {
    noteMap[taskId] = ''
  }
  await loadAll()
}

const openCompleteModal = (task) => {
  activeTask.value = task
  completionNote.value = noteMap[task.id] || ''
  resetImageState()
  showCompleteModal.value = true
}

const closeCompleteModal = () => {
  showCompleteModal.value = false
  activeTask.value = null
  completionNote.value = ''
  resetImageState()
}

const confirmComplete = async () => {
  if (!activeTask.value) return

  try {
    await ElMessageBox.confirm('确认保存吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  await submitTask(activeTask.value.id, completionNote.value)
  ElMessage.success('已提交，等待审核')
  closeCompleteModal()
}

const statusLabel = (task) => {
  if (!task) return '-'
  if (task.status === 'COMPLETED') return '已完成'
  if (task.submittedAt) return '待审核'
  if (task.status === 'PENDING') return '待完成'
  return task.status
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

.task-filter-bar {
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

.task-filter {
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

.task-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.task-filter-bar :deep(.el-input__wrapper),
.task-filter-bar :deep(.el-select__wrapper) {
  min-height: 32px !important;
}

.task-filter-bar :deep(.el-input--small),
.task-filter-bar :deep(.el-select--small) {
  --el-component-size-small: 32px;
}

.hidden-input {
  display: none;
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

.image-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.image-thumbs-inline {
  margin-top: 0;
}

.thumb-wrap {
  position: relative;
  width: 54px;
  height: 54px;
}

.thumb {
  width: 54px;
  height: 54px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid rgba(28, 27, 34, 0.12);
}

.thumb-sm {
  width: 42px;
  height: 42px;
  border-radius: 6px;
  cursor: pointer;
}

.thumb-remove {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: none;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  line-height: 20px;
  cursor: pointer;
  padding: 0;
}

@media (max-width: 960px) {
  .task-filter {
    width: 100%;
  }

  .task-filter-actions {
    width: 100%;
    justify-content: flex-end;
    margin-left: 0;
  }
}
</style>
