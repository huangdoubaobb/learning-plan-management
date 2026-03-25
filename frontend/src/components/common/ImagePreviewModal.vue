<template>
  <div v-if="visible" class="modal-backdrop" @click="$emit('close')">
    <div class="modal image-preview-modal" @click.stop>
      <div class="modal-header">
        <div class="modal-title">图片预览</div>
        <VeaButton class="modal-close" @click="$emit('close')" text>关闭</VeaButton>
      </div>
      <div class="modal-body">
        <div
          v-if="images.length"
          ref="imageStage"
          class="preview-image-stage"
          :class="{
            'is-draggable': isDraggable,
            'is-dragging': isDragging
          }"
          @pointermove="handlePointerMove"
          @pointerup="stopDrag"
          @pointercancel="stopDrag"
        >
          <img
            :src="images[currentIndex]"
            ref="previewImage"
            class="preview-image"
            draggable="false"
            :style="{ transform: `translate(${offsetX}px, ${offsetY}px) scale(${zoomLevel})` }"
            @wheel.prevent="handleWheelZoom"
            @load="handleImageLoad"
            @dragstart.prevent
            @dblclick.prevent="handleDoubleClick"
            @pointerdown="startDrag"
          />
        </div>
      </div>
      <div class="modal-footer">
        <div class="preview-toolbar">
          <VeaButton class="secondary" :disabled="!canZoomOut" @click="zoomOut" text>缩小</VeaButton>
          <div class="notice">{{ zoomPercent }}%</div>
          <VeaButton class="secondary" :disabled="!canZoomIn" @click="zoomIn" text>放大</VeaButton>
          <VeaButton class="secondary" :disabled="zoomLevel === 1" @click="resetZoom" text>重置</VeaButton>
        </div>
        <VeaButton class="secondary" :disabled="currentIndex <= 0" @click="$emit('prev')" text>上一张</VeaButton>
        <div class="notice">第 {{ currentIndex + 1 }} / {{ images.length }} 张</div>
        <VeaButton class="secondary" :disabled="currentIndex >= images.length - 1" @click="$emit('next')" text>下一张</VeaButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import VeaButton from '../VeaButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  images: {
    type: Array,
    default: () => []
  },
  currentIndex: {
    type: Number,
    default: 0
  }
})

defineEmits(['close', 'prev', 'next'])

const MIN_ZOOM = 0.5
const MAX_ZOOM = 3
const ZOOM_STEP = 0.25
const DOUBLE_CLICK_ZOOM = 2

const zoomLevel = ref(1)
const imageStage = ref(null)
const previewImage = ref(null)
const offsetX = ref(0)
const offsetY = ref(0)
const isDragging = ref(false)
const pointerId = ref(null)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragOriginX = ref(0)
const dragOriginY = ref(0)
const baseImageWidth = ref(0)
const baseImageHeight = ref(0)

const clampZoom = (value) => Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, value))
const isDraggable = computed(() => zoomLevel.value > 1)

const getStageSize = () => {
  if (!imageStage.value) {
    return { width: 0, height: 0 }
  }

  return {
    width: imageStage.value.clientWidth,
    height: imageStage.value.clientHeight
  }
}

const updateBaseImageSize = () => {
  const stage = imageStage.value
  const image = previewImage.value
  if (!stage || !image?.naturalWidth || !image?.naturalHeight) {
    return
  }

  const stageWidth = stage.clientWidth
  const stageHeight = stage.clientHeight
  if (!stageWidth || !stageHeight) {
    return
  }

  const widthRatio = stageWidth / image.naturalWidth
  const heightRatio = stageHeight / image.naturalHeight
  const containRatio = Math.min(widthRatio, heightRatio, 1)

  baseImageWidth.value = image.naturalWidth * containRatio
  baseImageHeight.value = image.naturalHeight * containRatio
}

const clampOffset = (nextX, nextY) => {
  const { width: stageWidth, height: stageHeight } = getStageSize()
  const overflowX = Math.max(0, (baseImageWidth.value * zoomLevel.value - stageWidth) / 2)
  const overflowY = Math.max(0, (baseImageHeight.value * zoomLevel.value - stageHeight) / 2)

  return {
    x: Math.min(overflowX, Math.max(-overflowX, nextX)),
    y: Math.min(overflowY, Math.max(-overflowY, nextY))
  }
}

const applyZoom = (value, anchorX = 0, anchorY = 0) => {
  const nextZoom = clampZoom(Number(value.toFixed(2)))
  const prevZoom = zoomLevel.value

  if (nextZoom === prevZoom) {
    return
  }

  zoomLevel.value = nextZoom

  if (prevZoom <= 0) {
    return
  }

  const nextOffset = clampOffset(
    anchorX - ((anchorX - offsetX.value) / prevZoom) * nextZoom,
    anchorY - ((anchorY - offsetY.value) / prevZoom) * nextZoom
  )
  offsetX.value = nextOffset.x
  offsetY.value = nextOffset.y
}

const zoomIn = () => {
  applyZoom(zoomLevel.value + ZOOM_STEP)
}

const zoomOut = () => {
  applyZoom(zoomLevel.value - ZOOM_STEP)
}

const resetZoom = () => {
  zoomLevel.value = 1
  offsetX.value = 0
  offsetY.value = 0
}

const handleImageLoad = () => {
  updateBaseImageSize()
  const nextOffset = clampOffset(offsetX.value, offsetY.value)
  offsetX.value = nextOffset.x
  offsetY.value = nextOffset.y
}

const startDrag = (event) => {
  if (!isDraggable.value || event.button !== 0) {
    return
  }

  isDragging.value = true
  pointerId.value = event.pointerId
  dragStartX.value = event.clientX
  dragStartY.value = event.clientY
  dragOriginX.value = offsetX.value
  dragOriginY.value = offsetY.value
  imageStage.value?.setPointerCapture?.(event.pointerId)
}

const handlePointerMove = (event) => {
  if (!isDragging.value || pointerId.value !== event.pointerId) {
    return
  }

  const nextOffset = clampOffset(
    dragOriginX.value + event.clientX - dragStartX.value,
    dragOriginY.value + event.clientY - dragStartY.value
  )
  offsetX.value = nextOffset.x
  offsetY.value = nextOffset.y
}

const stopDrag = (event) => {
  if (!isDragging.value || (event && pointerId.value !== event.pointerId)) {
    return
  }

  if (pointerId.value !== null) {
    imageStage.value?.releasePointerCapture?.(pointerId.value)
  }

  isDragging.value = false
  pointerId.value = null
}

const handleWheelZoom = (event) => {
  const stage = imageStage.value
  if (!stage) {
    return
  }

  const rect = stage.getBoundingClientRect()
  const anchorX = event.clientX - rect.left - rect.width / 2
  const anchorY = event.clientY - rect.top - rect.height / 2

  if (event.deltaY < 0) {
    applyZoom(zoomLevel.value + ZOOM_STEP, anchorX, anchorY)
    return
  }

  applyZoom(zoomLevel.value - ZOOM_STEP, anchorX, anchorY)
}

const handleDoubleClick = (event) => {
  const stage = imageStage.value
  if (!stage) {
    return
  }

  const rect = stage.getBoundingClientRect()
  const anchorX = event.clientX - rect.left - rect.width / 2
  const anchorY = event.clientY - rect.top - rect.height / 2

  if (zoomLevel.value > 1.05) {
    resetZoom()
    return
  }

  applyZoom(DOUBLE_CLICK_ZOOM, anchorX, anchorY)
}

const zoomPercent = computed(() => Math.round(zoomLevel.value * 100))
const canZoomIn = computed(() => zoomLevel.value < MAX_ZOOM)
const canZoomOut = computed(() => zoomLevel.value > MIN_ZOOM)

watch(zoomLevel, (value) => {
  if (value <= 1) {
    stopDrag()
    offsetX.value = 0
    offsetY.value = 0
    return
  }

  const nextOffset = clampOffset(offsetX.value, offsetY.value)
  offsetX.value = nextOffset.x
  offsetY.value = nextOffset.y
})

watch(
  () => [props.visible, props.currentIndex, props.images.length],
  ([visible]) => {
    if (visible) {
      resetZoom()
    }
  }
)
</script>

<style scoped>
.image-preview-modal {
  width: min(960px, 92vw);
  max-width: 960px;
}

.modal-body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  max-height: calc(100vh - 180px);
  overflow: auto;
  background: #fff;
}

.preview-image-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100%;
  padding: 8px;
  overflow: hidden;
  touch-action: none;
}

.preview-image {
  display: block;
  max-width: 100%;
  max-height: calc(100vh - 220px);
  width: auto;
  height: auto;
  object-fit: contain;
  transform-origin: center center;
  transition: transform 0.2s ease;
  border-radius: 8px;
  border: 1px solid rgba(28, 27, 34, 0.08);
  background: #fff;
  cursor: default;
  user-select: none;
  -webkit-user-drag: none;
}

.preview-toolbar {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.preview-image-stage.is-draggable .preview-image {
  cursor: grab;
}

.preview-image-stage.is-dragging .preview-image {
  cursor: grabbing;
  transition: none;
}
</style>
