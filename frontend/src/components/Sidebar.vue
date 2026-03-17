<template>
  <el-menu
    class="sidebar-menu vea-menu"
    :default-active="activePath"
    :router="true"
    :collapse="collapsed"
  >
    <el-menu-item index="/home">
      <el-icon><HomeFilled /></el-icon>
      <span>首页</span>
    </el-menu-item>

    <el-sub-menu v-for="group in menuTree" :key="group.id" :index="group.index">
      <template #title>
        <el-icon v-if="group.icon"><component :is="group.icon" /></el-icon>
        <span>{{ group.label }}</span>
      </template>
      <el-menu-item v-for="child in group.children" :key="child.id" :index="child.index">
        <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
        <span>{{ child.label }}</span>
      </el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  HomeFilled,
  Setting,
  UserFilled,
  Avatar,
  User,
  Lock,
  List,
  Calendar,
  DataAnalysis,
  Present,
  Tickets,
  Checked,
  Medal,
  Menu
} from '@element-plus/icons-vue'
import api from '../api'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const activePath = computed(() => route.path)

const menuRoutes = ref([])

const iconMap = {
  setting: Setting,
  role: User,
  user: List,
  lock: Lock,
  parent: UserFilled,
  child: Avatar,
  task: Calendar,
  chart: DataAnalysis,
  gift: Present,
  check: Checked,
  ticket: Tickets,
  points: Medal,
  menu: Menu
}

const permissionPathMap = {
  'admin.role.list': '/admin/roles',
  'admin.permission.list': '/admin/permissions',
  'admin.user.list': '/admin/users',
  'admin.log.list': '/admin/logs',
  'parent.child.list': '/parent/children',
  'parent.task.list': '/parent/tasks',
  'parent.reward.list': '/parent/rewards',
  'parent.redemption.list': '/parent/redemptions',
  'parent.report.view': '/parent/reports',
  'child.task.list': '/child/tasks',
  'child.reward.list': '/child/rewards'
}

const resolvePath = (routeNode) => {
  const permission = routeNode?.meta?.permissions?.[0]
  if (permission && permissionPathMap[permission]) return permissionPathMap[permission]
  return routeNode?.path || '/'
}

const normalizeTitle = (routeNode) => {
  const path = resolvePath(routeNode)
  const title = routeNode?.meta?.title || routeNode?.name || routeNode?.path || ''
  if (path === '/admin/roles' || routeNode?.meta?.permissions?.includes('admin.role.list')) {
    return '角色管理'
  }
  return title
}

const toMenuNode = (routeNode) => ({
  id: routeNode.name || routeNode.path,
  index: resolvePath(routeNode),
  label: normalizeTitle(routeNode),
  icon: routeNode?.meta?.icon ? (iconMap[routeNode.meta.icon] || iconMap.menu) : iconMap.menu,
  children: Array.isArray(routeNode.children)
    ? routeNode.children.filter(child => !child.hidden).map(child => ({
      id: child.name || child.path,
      index: resolvePath(child),
      label: normalizeTitle(child),
      icon: child?.meta?.icon ? (iconMap[child.meta.icon] || iconMap.menu) : iconMap.menu
    }))
    : []
})

const menuTree = computed(() => {
  return menuRoutes.value
    .filter(routeNode => !routeNode.hidden)
    .map(toMenuNode)
    .filter(node => node.children.length > 0)
})

const loadMenus = async () => {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const { data } = await api.get('/system/menu/getRouters')
    menuRoutes.value = Array.isArray(data) ? data : []
  } catch (error) {
    menuRoutes.value = []
  }
}

onMounted(loadMenus)
</script>
