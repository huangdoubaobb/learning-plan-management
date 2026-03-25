<template>
  <div class="task-page">
    <section class="vea-stat-grid task-stats">
      <div class="vea-stat">
        <div class="vea-stat-icon bg-green"><el-icon><Calendar /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">任务总数</div>
          <div class="vea-stat-value">{{ stats.tasks }}</div>
          <div class="vea-stat-foot">筛选结果总数 {{ total }}</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-blue"><el-icon><UserFilled /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">孩子数量</div>
          <div class="vea-stat-value">{{ stats.children }}</div>
          <div class="vea-stat-foot">家庭账号数</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-orange"><el-icon><Clock /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">待完成</div>
          <div class="vea-stat-value">{{ pendingCount }}</div>
          <div class="vea-stat-foot">筛选结果</div>
        </div>
      </div>
      <div class="vea-stat">
        <div class="vea-stat-icon bg-red"><el-icon><Checked /></el-icon></div>
        <div class="vea-stat-content">
          <div class="vea-stat-title">已完成</div>
          <div class="vea-stat-value">{{ completedCount }}</div>
          <div class="vea-stat-foot">筛选结果</div>
        </div>
      </div>
    </section>

    <section class="card table-card">
      <div class="table-info">
        <div class="table-title">任务清单</div>
        <div class="notice">当前 {{ total }} 条任务</div>
      </div>
      <div class="permission-filter-bar">
        <div class="permission-filter">
          <div class="filter-item">
            <span class="filter-label">孩子:</span>
            <el-select v-model="childFilter" size="small" clearable placeholder="全部孩子" style="width: 160px;">
              <el-option v-for="child in children" :key="child.id" :label="child.displayName" :value="child.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">状态:</span>
            <el-select v-model="statusFilter" size="small" style="width: 140px;">
              <el-option label="待完成" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">关键词:</span>
            <el-input v-model="keyword" size="small" placeholder="搜索标题/描述/备注" clearable style="width: 220px;" />
          </div>
          <div class="filter-item">
            <span class="filter-label">开始:</span>
            <el-date-picker
              v-model="startDate"
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
              v-model="endDate"
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
        <div class="permission-filter-actions">
          <VeaButton class="btn-sm btn-soft info" @click="applyFilters">
            <span class="btn-icon"><el-icon><Search /></el-icon></span>
            查询
          </VeaButton>
          <VeaButton class="btn-sm btn-soft neutral" @click="resetFilters">
            <span class="btn-icon"><el-icon><Refresh /></el-icon></span>
            清空
          </VeaButton>
        </div>
      </div>
      <div class="table-head">
        <div class="table-actions left-actions">
          <VeaButton class="btn-sm btn-soft success" @click="openCreateTask">
            <span class="btn-icon"><el-icon><Plus /></el-icon></span>
            新增任务
          </VeaButton>
          <VeaButton class="btn-sm btn-soft info" @click="openImportModal">
            <span class="btn-icon"><el-icon><Upload /></el-icon></span>
            导入
          </VeaButton>
          <VeaButton class="btn-sm btn-soft neutral" @click="exportTasks">
            <span class="btn-icon"><el-icon><Download /></el-icon></span>
            导出
          </VeaButton>
          <input ref="importInput" type="file" class="hidden-input" accept=".xlsx,.xls" @change="handleImportChange" />
        </div>
      </div>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>孩子</th>
              <th>任务</th>
              <th>开始时间</th>
              <th>积分</th>
              <th>状态</th>
              <th>完成时间</th>
              <th>备注</th>
              <th>图片</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in tasks" :key="task.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 + (currentPage - 1) * pageSize }}</td>
              <td>{{ childName(task.childId) }}</td>
              <td>{{ task.title }}</td>
              <td>{{ formatDateTime(task.createdAt) }}</td>
              <td>{{ task.points }}</td>
              <td>{{ statusLabel(task) }}</td>
              <td>{{ formatDateTime(task.completedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
              <td>
                <div class="image-thumbs" v-if="task.images && task.images.length">
                  <img
                    v-for="(img, idx) in task.images"
                    :key="img + idx"
                    :src="resolveImageUrl(img)"
                    class="thumb"
                    @click="openImagePreview(task.images, idx)"
                  />
                </div>
                <span v-else class="notice">-</span>
              </td>
              <td>
                <div class="row action-buttons" style="gap: 8px;">
                  <VeaButton
                    v-if="task.status === 'PENDING' && task.submittedAt"
                    class="btn-sm btn-soft success"
                    @click="approveTask(task)"
                  >
                    <span class="btn-icon"><el-icon><Check /></el-icon></span>
                    通过
                  </VeaButton>
                  <VeaButton
                    v-if="task.status === 'PENDING' && task.submittedAt"
                    class="btn-sm btn-soft danger"
                    @click="rejectTask(task)"
                  >
                    <span class="btn-icon"><el-icon><Close /></el-icon></span>
                    拒绝
                  </VeaButton>
                  <VeaButton class="btn-sm btn-soft neutral" @click="openDetail(task)">
                    <span class="btn-icon"><el-icon><View /></el-icon></span>
                    明细
                  </VeaButton>
                  <VeaButton class="btn-sm btn-soft info" :disabled="task.status === 'COMPLETED'" @click="openEditTask(task)">
                    <span class="btn-icon"><el-icon><Edit /></el-icon></span>
                    修改
                  </VeaButton>
                  <VeaButton class="btn-sm btn-soft danger" :disabled="task.status === 'COMPLETED' || task.submittedAt" @click="removeTask(task)">
                    <span class="btn-icon"><el-icon><Delete /></el-icon></span>
                    删除
                  </VeaButton>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="notice" v-if="!tasks.length">暂无任务</div>
      </div>
      <div class="pagination-bar">
        <div class="page-size">
          <span class="notice">每页</span>
          <select v-model.number="pageSize">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <span class="notice">条</span>
        </div>
        <VeaButton class="secondary" :disabled="currentPage === 1" @click="currentPage--" text>上一页</VeaButton>
        <div class="notice">第 {{ currentPage }} / {{ totalPages }} 页</div>
        <VeaButton class="secondary" :disabled="currentPage >= totalPages" @click="currentPage++" text>下一页</VeaButton>
      </div>
    </section>

    <TaskEditorDrawer
      :visible="showTaskModal"
      :form-mode="formMode"
      :task-form="taskForm"
      :children="children"
      :existing-task-images="existingTaskImages"
      :task-image-files-length="taskImageFiles.length"
      :task-image-previews="taskImagePreviews"
      :resolve-image-url="resolveImageUrl"
      @close="closeTaskModal"
      @save="saveTask"
      @update:task-form="taskForm = $event"
      @preview-images="openImagePreview"
      @image-change="handleTaskImageChange"
      @remove-task-image="removeTaskImage"
    />

    <TaskDetailDrawer
      :visible="showDetailDrawer"
      :task="detailTask"
      :child-name="childName"
      :status-label="statusLabel"
      :format-date-time="formatDateTime"
      :resolve-image-url="resolveImageUrl"
      @close="closeDetailDrawer"
      @preview-images="openImagePreview"
    />

    <ImagePreviewModal
      :visible="showImagePreview"
      :images="previewImages"
      :current-index="previewIndex"
      @close="closeImagePreview"
      @prev="prevImage"
      @next="nextImage"
    />

    <div v-if="showImportModal" class="modal-backdrop" @click="closeImportModal">
      <div class="modal import-modal" @click.stop>
        <div class="modal-header">
          <div class="modal-title">导入任务</div>
          <VeaButton class="modal-close" @click="closeImportModal" text>关闭</VeaButton>
        </div>
        <div class="modal-body">
          <div class="notice">
            请先下载模板，按模板填写后再上传。支持 .xlsx / .xls
          </div>
          <div class="row" style="margin-top: 12px;">
            <VeaButton class="btn-sm btn-soft neutral" @click="downloadTemplate">
              <span class="btn-icon"><el-icon><Download /></el-icon></span>
              下载模板
            </VeaButton>
            <VeaButton class="btn-sm btn-soft info" @click="triggerImport">
              <span class="btn-icon"><el-icon><Upload /></el-icon></span>
              选择文件
            </VeaButton>
          </div>
          <div class="notice" v-if="importFileName" style="margin-top: 8px;">
            已选择：{{ importFileName }}
          </div>
        </div>
        <div class="modal-footer">
          <VeaButton class="secondary" @click="closeImportModal" text>取消</VeaButton>
          <VeaButton @click="submitImport">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            开始导入
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Search, Refresh, Check, Delete, Plus, Edit, Upload, Download, View, Close, UserFilled, Calendar, Clock, Checked } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { useImagePreview } from '../composables/useImagePreview'
import { useParentTaskFilters } from '../composables/useParentTaskFilters'
import { useParentStats } from '../composables/useParentStats'
import { useTaskImageUpload } from '../composables/useTaskImageUpload'
import VeaButton from '../components/VeaButton.vue'
import ImagePreviewModal from '../components/common/ImagePreviewModal.vue'
import TaskDetailDrawer from '../components/parent/TaskDetailDrawer.vue'
import TaskEditorDrawer from '../components/parent/TaskEditorDrawer.vue'
import { formatDateTime, resolveImageUrl as resolveApiImageUrl } from '../utils'
import { downloadBlob } from '../utils/download'

const children = ref([])
const { stats, refresh } = useParentStats()
const {
  tasks,
  total,
  pendingCount,
  completedCount,
  statusFilter,
  childFilter,
  keyword,
  startDate,
  endDate,
  pageSize,
  currentPage,
  totalPages,
  buildPageParams,
  loadPage,
  loadCounts,
  applyFilters,
  resetFilters
} = useParentTaskFilters()
const showTaskModal = ref(false)
const formMode = ref('create')
const importInput = ref(null)
const showImportModal = ref(false)
const importFile = ref(null)
const importFileName = ref('')
const showDetailDrawer = ref(false)
const detailTask = ref(null)
const taskForm = ref({
  id: null,
  childId: '',
  title: '',
  description: '',
  points: 0,
  createdAt: ''
})

const disableStartDate = (date) => {
  if (!endDate.value) return false
  return date.getTime() > new Date(endDate.value).getTime()
}

const disableEndDate = (date) => {
  if (!startDate.value) return false
  return date.getTime() < new Date(startDate.value).getTime()
}

const loadAll = async () => {
  const childRes = await api.get('/parent/children')
  children.value = childRes.data
  await loadCounts()
  await loadPage()
}

const childName = (id) => {
  return children.value.find(c => c.id === id)?.displayName || '-'
}

const statusLabel = (task) => {
  if (!task) return '-'
  if (task.status === 'COMPLETED') return '已完成'
  if (task.submittedAt) return '待审核'
  if (task.status === 'PENDING') return '待完成'
  return task.status
}

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
  taskImageFiles,
  taskImagePreviews,
  existingTaskImages,
  resetTaskImageState,
  handleTaskImageChange,
  removeTaskImage,
  uploadTaskImages,
  setExistingTaskImages,
  clearExistingTaskImages
} = useTaskImageUpload({
  api,
  message: ElMessage
})

onMounted(loadAll)

watch([statusFilter, childFilter, pageSize], async () => {
  currentPage.value = 1
  await loadCounts()
  await loadPage()
})

watch(currentPage, () => {
  loadPage()
})

const triggerImport = () => {
  if (importInput.value) {
    importInput.value.value = ''
    importInput.value.click()
  }
}

const handleImportChange = async (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return
  importFile.value = file
  importFileName.value = file.name
}

const exportTasks = async () => {
  const res = await api.get(`/parent/tasks/export?${buildPageParams().toString()}`, { responseType: 'blob' })
  downloadBlob(res.data, `tasks-${new Date().toISOString().slice(0, 10)}.xlsx`)
}

const openImportModal = () => {
  showImportModal.value = true
  importFile.value = null
  importFileName.value = ''
}

const closeImportModal = () => {
  showImportModal.value = false
}

const submitImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  const formData = new FormData()
  formData.append('file', importFile.value)
  await api.post('/parent/tasks/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  ElMessage.success('导入成功')
  showImportModal.value = false
  importFile.value = null
  importFileName.value = ''
  await loadCounts()
  await loadPage()
  refresh()
}

const downloadTemplate = async () => {
  const res = await api.get('/parent/tasks/import-template', { responseType: 'blob' })
  downloadBlob(res.data, 'task-import-template.xlsx')
}

const openCreateTask = () => {
  formMode.value = 'create'
  taskForm.value = {
    id: null,
    childId: children.value[0]?.id || '',
    title: '',
    description: '',
    points: 0,
    createdAt: ''
  }
  clearExistingTaskImages()
  resetTaskImageState()
  showTaskModal.value = true
}

const openEditTask = (task) => {
  formMode.value = 'edit'
  taskForm.value = {
    id: task.id,
    childId: task.childId,
    title: task.title || '',
    description: task.description || '',
    points: task.points ?? 0,
    createdAt: task.createdAt || ''
  }
  setExistingTaskImages(task.taskImages)
  resetTaskImageState()
  showTaskModal.value = true
}

const closeTaskModal = () => {
  showTaskModal.value = false
  clearExistingTaskImages()
  resetTaskImageState()
}

const saveTask = async () => {
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
  const payload = {
    childId: taskForm.value.childId,
    title: taskForm.value.title,
    description: taskForm.value.description,
    points: taskForm.value.points
  }
  let taskId = taskForm.value.id
  if (formMode.value === 'create') {
    const { data } = await api.post('/parent/tasks', payload)
    taskId = data?.id || taskId
    ElMessage.success('新增成功')
  } else {
    const { data } = await api.put(`/parent/tasks/${taskForm.value.id}`, payload)
    taskId = data?.id || taskId
    ElMessage.success('修改成功')
  }
  if (taskId && taskImageFiles.value.length) {
    await uploadTaskImages(taskId)
  }
  showTaskModal.value = false
  resetTaskImageState()
  clearExistingTaskImages()
  await loadCounts()
  await loadPage()
  refresh()
}

const removeTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定删除任务 ${task.title} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.delete(`/parent/tasks/${task.id}`)
  ElMessage.success('删除成功')
  await loadCounts()
  await loadPage()
  refresh()
}

const approveTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确认通过任务 ${task.title} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.post(`/parent/tasks/${task.id}/approve`)
  ElMessage.success('审核通过')
  await loadCounts()
  await loadPage()
  refresh()
}

const rejectTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确认拒绝任务 ${task.title} 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.post(`/parent/tasks/${task.id}/reject`)
  ElMessage.success('已拒绝')
  await loadCounts()
  await loadPage()
  refresh()
}

const openDetail = async (task) => {
  detailTask.value = task
  showDetailDrawer.value = true
  try {
    const { data } = await api.get(`/parent/tasks/${task.id}`)
    if (data) detailTask.value = data
  } catch {
    detailTask.value = task
  }
}

const closeDetailDrawer = () => {
  showDetailDrawer.value = false
  detailTask.value = null
}

</script>

<style scoped>
.task-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.table-card {
  display: grid;
  gap: 12px;
}

.import-modal {
  max-width: 520px;
}

.permission-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.permission-filter {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 12px;
  color: #606266;
}

.permission-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-info {
  display: grid;
  gap: 6px;
}

.table-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text);
}

.left-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hidden-input {
  display: none;
}

.pagination-bar {
  justify-content: flex-end;
  gap: 10px;
}

.page-size {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.page-size select {
  height: 32px;
}

.task-stats {
  margin-top: 0;
}

.drawer-body :deep(.el-input__wrapper),
.drawer-body :deep(.el-date-editor .el-input__wrapper) {
  height: 36px !important;
  box-shadow: none !important;
  border: 1px solid #c0c4cc !important;
  padding: 0 10px !important;
  align-items: center !important;
}

.drawer-body :deep(.el-date-editor) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

.drawer-body :deep(.el-input__inner),
.drawer-body :deep(.el-date-editor .el-input__inner) {
  height: 34px !important;
  line-height: 34px !important;
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
}

.drawer-body :deep(.el-input--small .el-input__wrapper),
.drawer-body :deep(.el-date-editor.el-input--small .el-input__wrapper) {
  height: 36px !important;
}

.drawer-body :deep(.el-input--small .el-input__inner),
.drawer-body :deep(.el-date-editor.el-input--small .el-input__inner) {
  height: 34px !important;
  line-height: 34px !important;
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
