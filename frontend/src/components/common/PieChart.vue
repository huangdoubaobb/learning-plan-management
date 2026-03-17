<template>
  <div class="pie-wrap">
    <div class="pie" :style="pieStyle"></div>
    <div class="pie-info">
      <div class="pie-value">{{ value }}%</div>
      <div class="pie-label">{{ label }}</div>
      <div class="pie-sub">{{ subText }}</div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: Number,
    required: true
  },
  label: {
    type: String,
    required: true
  },
  subText: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: 'var(--primary)'
  }
})

const pieStyle = computed(() => ({
  background: `conic-gradient(${props.color} ${props.value}%, rgba(0,0,0,0.08) 0)`
}))
</script>
