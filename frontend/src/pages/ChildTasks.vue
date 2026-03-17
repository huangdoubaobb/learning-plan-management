<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>今日任务</h2>
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
        <div class="stat-foot">还未完成的任务</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">当前积分</div>
        <div class="stat-value">{{ me?.points ?? 0 }}</div>
        <div class="stat-foot">累计获得积分</div>
      </div>
    </div>

    <div class="card">
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>任务</th>
              <th>积分</th>
              <th>状态</th>
              <th>完成时间</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(task, index) in tasks" :key="task.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ task.title }}</td>
              <td>{{ task.points }}</td>
              <td>{{ statusLabel(task) }}</td>
              <td>{{ formatDateTime(task.completedAt || task.submittedAt) }}</td>
              <td>{{ task.checkinNote || '-' }}</td>
              <td>
                <div v-if="task.status === 'PENDING' && !task.submittedAt && can('child.task.complete')">
                  <VeaButton class="btn-sm success" @click="openCompleteModal(task)">
                    <span class="btn-icon"><el-icon><CircleCheck /></el-icon></span>
                    提交
                  </VeaButton>
                </div>
              </td>
            </tr>
            <tr v-if="!tasks.length">
              <td colspan="8" class="notice">暂无任务</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showCompleteModal" class="drawer-backdrop" @click="closeCompleteModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">提交任务</div>
          <VeaButton class="modal-close" @click="closeCompleteModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <div class="notice" style="margin-bottom: 10px;">{{ activeTask?.title || '' }}</div>
          <label>备注</label>
          <input v-model="completionNote" placeholder="填写任务完成说明" />
          <label style="margin-top: 10px;">上传图片</label>
          <input type="file" accept="image/*" @change="handleImageChange" />
          <div class="image-thumbs" v-if="imagePreviews.length" style="margin-top: 8px;">
            <div class="thumb-wrap" v-for="(src, idx) in imagePreviews" :key="src + idx">
              <img :src="src" class="thumb" />
              <button class="thumb-remove" @click="removeImage(idx)">×</button>
            </div>
          </div>
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
import { CircleCheck, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import { can } from '../permissions'
import VeaButton from '../components/VeaButton.vue'

const me = ref(null)
const tasks = ref([])
const noteMap = reactive({})
const showCompleteModal = ref(false)
const activeTask = ref(null)
const completionNote = ref('')
const imageFile = ref(null)
const imagePreviews = ref([])
const pendingTasks = computed(() => tasks.value.filter(t => t.status === 'PENDING').length)

const loadAll = async () => {
  const [meRes, taskRes] = await Promise.all([
    api.get('/child/me'),
    api.get('/child/tasks')
  ])
  me.value = meRes.data
  tasks.value = taskRes.data
}

const submitTask = async (id, note) => {
  const formData = new FormData()
  formData.append('note', note || '')
  if (imageFile.value) {
    formData.append('file', imageFile.value)
  }
  await api.post(`/child/tasks/${id}/submit`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  if (id in noteMap) noteMap[id] = ''
  await loadAll()
}

const openCompleteModal = (task) => {
  activeTask.value = task
  completionNote.value = noteMap[task.id] || ''
  clearImages()
  showCompleteModal.value = true
}

const closeCompleteModal = () => {
  showCompleteModal.value = false
  activeTask.value = null
  completionNote.value = ''
  clearImages()
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
  await submitTask(activeTask.value.id, completionNote.value)
  ElMessage.success('已提交，等待审核')
  closeCompleteModal()
}

const handleImageChange = (event) => {
  clearImages()
  const file = event?.target?.files?.[0] || null
  if (file) {
    const allowed = ['image/jpeg', 'image/png']
    if (!allowed.includes(file.type)) {
      ElMessage.warning('只支持 JPG/PNG 图片')
      return
    }
    const name = file.name || ''
    const ext = name.slice(name.lastIndexOf('.') + 1).toLowerCase()
    if (!['jpg', 'jpeg', 'png'].includes(ext)) {
      ElMessage.warning('只支持 JPG/PNG 图片')
      return
    }
    if (file.size > 10 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过 10MB')
      return
    }
  }
  imageFile.value = file
  imagePreviews.value = file ? [URL.createObjectURL(file)] : []
}

const removeImage = (index) => {
  imageFile.value = null
  const [removed] = imagePreviews.value.splice(index, 1)
  if (removed) URL.revokeObjectURL(removed)
}

const clearImages = () => {
  imagePreviews.value.forEach(url => URL.revokeObjectURL(url))
  imageFile.value = null
  imagePreviews.value = []
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
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
.image-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
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
</style>
