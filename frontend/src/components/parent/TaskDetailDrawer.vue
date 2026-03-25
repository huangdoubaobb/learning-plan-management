<template>
  <div v-if="visible" class="drawer-backdrop" @click="$emit('close')">
    <div class="drawer-panel" @click.stop>
      <div class="drawer-header">
        <div class="drawer-title">任务详情</div>
        <VeaButton class="modal-close" @click="$emit('close')" text>关闭</VeaButton>
      </div>
      <div class="drawer-body">
        <div class="detail-row">
          <span class="detail-label">孩子</span>
          <span class="detail-value">{{ task ? childName(task.childId) : '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">任务标题</span>
          <span class="detail-value">{{ task?.title || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">描述</span>
          <span class="detail-value">{{ task?.description || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">开始时间</span>
          <span class="detail-value">{{ formatDateTime(task?.createdAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">积分</span>
          <span class="detail-value">{{ task?.points ?? '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态</span>
          <span class="detail-value">{{ statusLabel(task) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">完成时间</span>
          <span class="detail-value">{{ formatDateTime(task?.completedAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">打卡备注</span>
          <span class="detail-value">{{ task?.checkinNote || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">任务图片</span>
          <div class="detail-images">
            <div class="image-thumbs" v-if="taskImageList.length">
              <img
                v-for="(img, idx) in taskImageList"
                :key="`${img}-${idx}`"
                :src="resolveImageUrl(img)"
                class="thumb"
                @click="$emit('preview-images', taskImageList, idx)"
              />
            </div>
            <span v-else class="notice">-</span>
          </div>
        </div>
        <div class="detail-row">
          <span class="detail-label">完成图片</span>
          <div class="detail-images">
            <div class="image-thumbs" v-if="checkinImageList.length">
              <img
                v-for="(img, idx) in checkinImageList"
                :key="`${img}-${idx}`"
                :src="resolveImageUrl(img)"
                class="thumb"
                @click="$emit('preview-images', checkinImageList, idx)"
              />
            </div>
            <span v-else class="notice">-</span>
          </div>
        </div>
      </div>
      <div class="drawer-footer">
        <VeaButton class="secondary" @click="$emit('close')" text>关闭</VeaButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import VeaButton from '../VeaButton.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  task: { type: Object, default: null },
  childName: { type: Function, required: true },
  statusLabel: { type: Function, required: true },
  formatDateTime: { type: Function, required: true },
  resolveImageUrl: { type: Function, required: true }
})

defineEmits(['close', 'preview-images'])

const taskImageList = computed(() => {
  if (Array.isArray(props.task?.taskImages) && props.task.taskImages.length) return props.task.taskImages
  return Array.isArray(props.task?.images) ? props.task.images : []
})

const checkinImageList = computed(() => {
  if (Array.isArray(props.task?.checkinImages) && props.task.checkinImages.length) return props.task.checkinImages
  return []
})
</script>

<style scoped>
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

.thumb {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid rgba(28, 27, 34, 0.12);
  background: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.thumb:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(28, 27, 34, 0.12);
}
</style>
