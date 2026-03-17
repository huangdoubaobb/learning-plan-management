<template>
  <el-button
    class="vea-btn"
    :type="type"
    :size="size"
    :plain="plain"
    :text="text"
    :link="link"
    :disabled="disabled"
    :loading="loading"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <el-icon v-if="icon" class="vea-btn-icon">
      <component :is="icon" />
    </el-icon>
    <slot />
  </el-button>
</template>

<script setup>
defineOptions({ inheritAttrs: false })

const props = defineProps({
  type: { type: String, default: 'default' },
  size: { type: String, default: 'small' },
  plain: { type: Boolean, default: false },
  text: { type: Boolean, default: false },
  link: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  icon: { type: [Object, Function, String], default: null }
})

defineEmits(['click'])
</script>

<style scoped>
.vea-btn {
  height: 32px;
  padding: 6px 12px;
}

.vea-btn-icon {
  margin-right: 6px;
}
</style>
