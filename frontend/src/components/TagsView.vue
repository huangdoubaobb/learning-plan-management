<template>
  <div class="tags-view">
    <div
      v-for="tag in tags"
      :key="tag.path"
      class="tag-item"
      :class="{ active: tag.path === route.path }"
      @click="go(tag.path)"
    >
      <span>{{ tag.title }}</span>
      <el-icon v-if="tags.length > 1" class="tag-close" @click.stop="close(tag.path)">
        <Close />
      </el-icon>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const tags = reactive([])

const ensureTag = (r) => {
  if (!r.meta?.title || r.meta?.public) return
  if (tags.find(t => t.path === r.path)) return
  tags.push({ path: r.path, title: r.meta.title })
}

const go = (path) => {
  if (path !== route.path) router.push(path)
}

const close = (path) => {
  if (tags.length <= 1) return
  const idx = tags.findIndex(t => t.path === path)
  if (idx === -1) return
  const isActive = route.path === path
  tags.splice(idx, 1)
  if (isActive) {
    const next = tags[idx - 1] || tags[idx] || { path: '/login' }
    router.push(next.path)
  }
}

watch(
  () => route.path,
  () => ensureTag(route),
  { immediate: true }
)
</script>
