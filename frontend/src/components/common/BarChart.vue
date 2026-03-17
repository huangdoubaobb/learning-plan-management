<template>
  <div class="chart">
    <div class="bar" v-for="(item, index) in data" :key="index">
      <div class="bar-track">
        <div class="bar-fill" :style="{ height: `${item.valuePercent}%` }"></div>
      </div>
      <div class="bar-label">{{ item.value }}</div>
      <div class="bar-label">{{ item.label }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  data: {
    type: Array,
    required: true
  }
})
</script>
