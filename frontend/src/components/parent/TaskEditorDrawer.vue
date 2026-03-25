<template>
  <div v-if="visible" class="drawer-backdrop" @click="$emit('close')">
    <div class="drawer-panel" @click.stop>
      <div class="drawer-header">
        <div class="drawer-title">{{ formMode === 'create' ? '新增任务' : '修改任务' }}</div>
        <VeaButton class="modal-close" @click="$emit('close')" text>关闭</VeaButton>
      </div>
      <div class="drawer-body">
        <div class="row">
          <div>
            <label>选择孩子</label>
            <select :value="taskForm.childId" @change="updateField('childId', normalizeValue($event.target.value))">
              <option disabled value="">请选择</option>
              <option v-for="child in children" :key="child.id" :value="child.id">{{ child.displayName }}</option>
            </select>
          </div>
          <div>
            <label>任务标题</label>
            <input :value="taskForm.title" placeholder="例如：完成数学作业" @input="updateField('title', $event.target.value)" />
          </div>
        </div>
        <div class="row" style="margin-top: 10px;">
          <div>
            <label>积分</label>
            <input
              :value="taskForm.points"
              type="number"
              @input="updateField('points', Number($event.target.value || 0))"
            />
          </div>
        </div>
        <label style="margin-top: 10px;">描述</label>
        <textarea :value="taskForm.description" rows="3" @input="updateField('description', $event.target.value)"></textarea>
        <div class="upload-block">
          <label>任务图片</label>
          <div class="upload-row">
            <input
              ref="fileInput"
              type="file"
              class="hidden-input"
              accept="image/*"
              multiple
              @change="$emit('image-change', $event)"
            />
            <VeaButton class="btn-sm btn-soft info" @click="pickImages">
              <span class="btn-icon"><el-icon><Upload /></el-icon></span>
              选择图片
            </VeaButton>
            <span class="notice" v-if="taskImageFilesLength">已选择 {{ taskImageFilesLength }} 张</span>
          </div>
          <div class="notice" v-if="formMode === 'edit' && existingTaskImages.length">
            已有图片，上传新图将追加到现有图片中
          </div>
          <div class="image-thumbs" v-if="existingTaskImages.length">
            <img
              v-for="(img, idx) in existingTaskImages"
              :key="`${img}-${idx}`"
              :src="resolveImageUrl(img)"
              class="thumb"
              @click="$emit('preview-images', existingTaskImages, idx)"
            />
          </div>
          <div class="image-thumbs" v-if="taskImagePreviews.length">
            <div class="thumb-wrap" v-for="(img, idx) in taskImagePreviews" :key="img.url">
              <img :src="img.url" class="thumb" />
              <button type="button" class="thumb-remove" @click="$emit('remove-task-image', idx)">x</button>
            </div>
          </div>
        </div>
      </div>
      <div class="drawer-footer">
        <VeaButton class="secondary" @click="$emit('close')" text>取消</VeaButton>
        <VeaButton @click="$emit('save')">
          <span class="btn-icon"><el-icon><Check /></el-icon></span>
          保存
        </VeaButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Check, Upload } from '@element-plus/icons-vue'
import VeaButton from '../VeaButton.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  formMode: { type: String, default: 'create' },
  taskForm: { type: Object, required: true },
  children: { type: Array, default: () => [] },
  existingTaskImages: { type: Array, default: () => [] },
  taskImageFilesLength: { type: Number, default: 0 },
  taskImagePreviews: { type: Array, default: () => [] },
  resolveImageUrl: { type: Function, required: true }
})

const emit = defineEmits(['close', 'save', 'update:taskForm', 'preview-images', 'image-change', 'remove-task-image'])
const fileInput = ref(null)

const normalizeValue = (value) => {
  if (value === '') return ''
  const num = Number(value)
  return Number.isNaN(num) ? value : num
}

const updateField = (key, value) => {
  emit('update:taskForm', {
    ...props.taskForm,
    [key]: value
  })
}

const pickImages = () => {
  if (fileInput.value) {
    fileInput.value.value = ''
    fileInput.value.click()
  }
}
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

.hidden-input {
  display: none;
}

.image-thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.thumb-wrap {
  position: relative;
  display: inline-flex;
}

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid rgba(28, 27, 34, 0.12);
  background: #fff;
  cursor: pointer;
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
</style>
