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
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.01em;
  border: 1px solid rgba(148, 163, 184, 0.18);
  box-shadow: none;
  transition: background-color 0.16s ease, border-color 0.16s ease, color 0.16s ease;
}

.vea-btn:hover:not(.is-disabled) {
  box-shadow: none;
}

.vea-btn.is-disabled {
  opacity: 0.56;
  box-shadow: none;
}

.vea-btn.btn-sm {
  height: 32px;
  padding: 6px 11px;
}

.vea-btn.btn-soft {
  box-shadow: none;
}

.vea-btn.btn-soft.success {
  color: #37694c;
  background: #f3faf5;
  border-color: rgba(55, 105, 76, 0.12);
}

.vea-btn.btn-soft.success:hover:not(.is-disabled) {
  background: #edf7f0;
  border-color: rgba(55, 105, 76, 0.18);
}

.vea-btn.btn-soft.info {
  color: #3e628c;
  background: #f2f7ff;
  border-color: rgba(62, 98, 140, 0.12);
}

.vea-btn.btn-soft.info:hover:not(.is-disabled) {
  background: #ebf3ff;
  border-color: rgba(62, 98, 140, 0.18);
}

.vea-btn.btn-soft.neutral,
.vea-btn.secondary:not(.is-text):not(.is-link) {
  color: #4b5565;
  background: #fafbfd;
  border-color: rgba(75, 85, 101, 0.12);
}

.vea-btn.btn-soft.neutral:hover:not(.is-disabled),
.vea-btn.secondary:not(.is-text):not(.is-link):hover:not(.is-disabled) {
  background: #f4f7fa;
  border-color: rgba(75, 85, 101, 0.18);
}

.vea-btn.btn-soft.danger {
  color: #8b5656;
  background: #fff5f5;
  border-color: rgba(139, 86, 86, 0.12);
}

.vea-btn.btn-soft.danger:hover:not(.is-disabled) {
  background: #ffefef;
  border-color: rgba(139, 86, 86, 0.18);
}

.vea-btn.is-text,
.vea-btn.is-link {
  padding-left: 8px;
  padding-right: 8px;
  border-color: transparent;
  box-shadow: none;
}

.vea-btn.is-text:hover:not(.is-disabled),
.vea-btn.is-link:hover:not(.is-disabled) {
  box-shadow: none;
}

.vea-btn.modal-close {
  border-radius: 8px;
}

.vea-btn-icon {
  margin-right: 6px;
}
</style>
