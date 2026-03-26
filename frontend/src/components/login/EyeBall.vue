<template>
  <div
    ref="eyeRef"
    class="eyeball"
    :style="{
      width: `${size}px`,
      height: isBlinking ? '2px' : `${size}px`,
      backgroundColor: eyeColor
    }"
  >
    <div
      v-if="!isBlinking"
      class="pupil-inner"
      :style="{
        width: `${pupilSize}px`,
        height: `${pupilSize}px`,
        backgroundColor: pupilColor,
        transform: `translate(${pos.x}px, ${pos.y}px)`
      }"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = defineProps({
  size: { type: Number, default: 48 },
  pupilSize: { type: Number, default: 16 },
  maxDistance: { type: Number, default: 10 },
  eyeColor: { type: String, default: 'white' },
  pupilColor: { type: String, default: 'black' },
  isBlinking: { type: Boolean, default: false },
  forceLookX: { type: Number, default: undefined },
  forceLookY: { type: Number, default: undefined }
})

const mouseX = ref(0)
const mouseY = ref(0)
const eyeRef = ref()

const onMove = (event) => {
  mouseX.value = event.clientX
  mouseY.value = event.clientY
}

onMounted(() => window.addEventListener('mousemove', onMove))
onUnmounted(() => window.removeEventListener('mousemove', onMove))

const pos = computed(() => {
  if (!eyeRef.value) return { x: 0, y: 0 }
  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    return { x: props.forceLookX, y: props.forceLookY }
  }
  const rect = eyeRef.value.getBoundingClientRect()
  const dx = mouseX.value - (rect.left + rect.width / 2)
  const dy = mouseY.value - (rect.top + rect.height / 2)
  const distance = Math.min(Math.sqrt(dx ** 2 + dy ** 2), props.maxDistance)
  const angle = Math.atan2(dy, dx)
  return {
    x: Math.cos(angle) * distance,
    y: Math.sin(angle) * distance
  }
})
</script>

<style scoped>
.eyeball {
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 0.15s ease;
}

.pupil-inner {
  border-radius: 999px;
  transition: transform 0.1s ease-out;
}
</style>
