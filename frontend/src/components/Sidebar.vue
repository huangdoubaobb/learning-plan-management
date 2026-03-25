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

    <el-sub-menu v-for="group in menuTree" :key="group.id" :index="group.id">
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
  Avatar,
  Calendar,
  Checked,
  DataAnalysis,
  HomeFilled,
  List,
  Lock,
  Medal,
  Menu,
  Present,
  Setting,
  Tickets,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import api from '../api'
import { getToken } from '../utils/authStorage'

defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()
const menuRoutes = ref([])

const activePath = computed(() => route.path)

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
  'child.reward.list': '/child/rewards',
  'child.points.view': '/child/points'
}

const titleMap = {
  '/home': '首页',
  '/admin/roles': '角色管理',
  '/admin/permissions': '权限列表',
  '/admin/users': '用户管理',
  '/admin/logs': '日志管理',
  '/parent/children': '孩子管理',
  '/parent/tasks': '任务列表',
  '/parent/task-stats': '报表统计',
  '/parent/rewards': '奖励管理',
  '/parent/redemptions': '兑换审核',
  '/parent/reports': '报表统计',
  '/child/tasks': '任务列表',
  '/child/rewards': '兑换奖励',
  '/child/points': '积分明细'
}

const normalizeMenuPath = (path) => {
  if (path === '/parent/task-stats') return '/parent/reports'
  return path || '/'
}

const resolvePath = (routeNode) => {
  const permission = routeNode?.meta?.permissions?.[0]
  if (permission && permissionPathMap[permission]) return permissionPathMap[permission]
  return normalizeMenuPath(routeNode?.path)
}

const normalizeTitle = (routeNode) => {
  const path = resolvePath(routeNode)
  return titleMap[path] || routeNode?.meta?.title || routeNode?.name || routeNode?.path || ''
}

const toMenuNode = (routeNode) => ({
  id: routeNode.name || routeNode.path,
  label: normalizeTitle(routeNode),
  icon: routeNode?.meta?.icon ? (iconMap[routeNode.meta.icon] || iconMap.menu) : iconMap.menu,
  children: Array.isArray(routeNode.children)
    ? Array.from(
      new Map(
        routeNode.children
          .filter(child => !child.hidden)
          .map(child => {
            const index = resolvePath(child)
            return [index, {
              id: child.name || child.path,
              index,
              label: normalizeTitle(child),
              icon: child?.meta?.icon ? (iconMap[child.meta.icon] || iconMap.menu) : iconMap.menu
            }]
          })
      ).values()
    )
    : []
})

const menuTree = computed(() => {
  return menuRoutes.value
    .filter(routeNode => !routeNode.hidden)
    .map(toMenuNode)
    .filter(node => node.children.length > 0)
})

const loadMenus = async () => {
  const token = getToken()
  if (!token) return
  try {
    const { data } = await api.get('/system/menu/getRouters')
    menuRoutes.value = Array.isArray(data) ? data : []
  } catch {
    menuRoutes.value = []
  }
}

onMounted(loadMenus)
</script>
