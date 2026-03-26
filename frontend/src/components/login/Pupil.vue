<template>
  <div
    ref="pupilRef"
    class="pupil"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      backgroundColor: pupilColor,
      transform: `translate(${pos.x}px, ${pos.y}px)`
    }"
  />
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = defineProps({
  size: { type: Number, default: 12 },
  maxDistance: { type: Number, default: 5 },
  pupilColor: { type: String, default: 'black' },
  forceLookX: { type: Number, default: undefined },
  forceLookY: { type: Number, default: undefined }
})

const mouseX = ref(0)
const mouseY = ref(0)
const pupilRef = ref()

const onMove = (event) => {
  mouseX.value = event.clientX
  mouseY.value = event.clientY
}

onMounted(() => window.addEventListener('mousemove', onMove))
onUnmounted(() => window.removeEventListener('mousemove', onMove))

const pos = computed(() => {
  if (!pupilRef.value) return { x: 0, y: 0 }
  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    return { x: props.forceLookX, y: props.forceLookY }
  }
  const rect = pupilRef.value.getBoundingClientRect()
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
.pupil {
  border-radius: 999px;
  transition: transform 0.1s ease-out;
}
</style>
