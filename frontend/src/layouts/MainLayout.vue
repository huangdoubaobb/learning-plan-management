<template>
  <el-container class="layout-root vea-layout">
    <el-aside :width="asideWidth" class="layout-aside vea-aside">
      <div class="brand vea-logo">
        <div class="brand-mark logo-mark">LP</div>
        <div class="brand-text logo-text" v-show="!collapsed">
          <div class="brand-title">学习计划</div>
          <div class="brand-sub">管理系统</div>
        </div>
      </div>
      <Sidebar :collapsed="collapsed" />
    </el-aside>
    <el-container>
      <el-header class="layout-header vea-header">
        <div class="header-left vea-header-left">
          <VeaButton class="icon-btn" @click="toggleCollapse" circle>
            <el-icon><component :is="collapsed ? Expand : Fold" /></el-icon>
          </VeaButton>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbItems" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right vea-header-right">
          <el-tag size="small" effect="dark" type="success">{{ roleLabel }}</el-tag>
          <VeaButton class="theme-toggle" text @click="toggleTheme">切换背景</VeaButton>
          <el-dropdown @command="handleUserCommand">
            <span class="user-chip">
              {{ displayName || roleLabel }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">修改账户信息</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <div class="tags-wrap">
        <TagsView />
      </div>
      <el-main class="layout-main vea-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>

  <el-drawer
    v-model="showProfile"
    class="profile-drawer"
    direction="rtl"
    size="420px"
    :with-header="false"
  >
    <div class="drawer-header">
      <div class="drawer-title">修改账户信息</div>
      <VeaButton text @click="closeProfile">关闭</VeaButton>
    </div>
    <div class="drawer-body">
      <el-form label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="profileForm.displayName" placeholder="请输入显示名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="profileForm.password" type="password" placeholder="不修改请留空" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="profileForm.passwordConfirm" type="password" placeholder="再次输入新密码" show-password />
        </el-form-item>
        <el-form-item label="最近登录">
          <el-input :value="formatDateTime(profileForm.lastLoginAt)" disabled />
        </el-form-item>
      </el-form>
    </div>
    <div class="drawer-footer">
      <VeaButton @click="closeProfile">取消</VeaButton>
      <VeaButton type="primary" :icon="Check" @click="saveProfile">保存</VeaButton>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Check, Expand, Fold } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sidebar from '../components/Sidebar.vue'
import TagsView from '../components/TagsView.vue'
import VeaButton from '../components/VeaButton.vue'
import api from '../api'
import { formatDateTime } from '../utils'
import {
  clearAuthState,
  getDisplayName,
  getRole,
  getTheme,
  setDisplayName,
  setTheme
} from '../utils/authStorage'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const displayName = ref(getDisplayName())
const showProfile = ref(false)
const profileForm = ref({
  username: '',
  displayName: '',
  phone: '',
  password: '',
  passwordConfirm: '',
  lastLoginAt: ''
})
const theme = ref(getTheme())
const themeList = ['', 'mist', 'ocean', 'sand', 'dusk']

const roleLabel = computed(() => {
  const role = getRole()
  if (role === 'ADMIN') return '管理员'
  if (role === 'PARENT') return '家长'
  if (role === 'CHILD') return '孩子'
  return '访客'
})

const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  if (!matched.length) return [{ title: '控制台', path: route.path }]
  return matched.map(item => ({ title: item.meta.title, path: item.path }))
})

const asideWidth = computed(() => (collapsed.value ? '64px' : '210px'))

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const applyTheme = (value) => {
  const targets = [document.documentElement, document.body, document.getElementById('app')].filter(Boolean)
  if (value) {
    targets.forEach((el) => el.setAttribute('data-theme', value))
  } else {
    targets.forEach((el) => el.removeAttribute('data-theme'))
  }
  setTheme(value)
}

const toggleTheme = () => {
  const idx = themeList.indexOf(theme.value)
  const next = themeList[(idx + 1) % themeList.length]
  theme.value = next
  applyTheme(theme.value)
  const labelMap = {
    '': '默认背景',
    mist: '雾白',
    ocean: '海风',
    sand: '暖砂',
    dusk: '暮蓝'
  }
  ElMessage.success(`已切换为${labelMap[next] || '默认背景'}`)
}

const handleUserCommand = (command) => {
  if (command === 'profile') {
    openProfile()
    return
  }
  if (command === 'logout') {
    logout()
  }
}

const openProfile = async () => {
  showProfile.value = true
  try {
    const { data } = await api.get('/auth/me')
    profileForm.value.username = data.username || ''
    profileForm.value.displayName = data.displayName || ''
    profileForm.value.phone = data.phone || ''
    profileForm.value.lastLoginAt = data.lastLoginAt || ''
    profileForm.value.password = ''
    profileForm.value.passwordConfirm = ''
  } catch {
    profileForm.value.username = ''
  }
}

const closeProfile = () => {
  showProfile.value = false
}

const saveProfile = async () => {
  if (!profileForm.value.displayName.trim()) {
    ElMessage.warning('请填写显示名')
    return
  }
  if (profileForm.value.password && profileForm.value.password !== profileForm.value.passwordConfirm) {
    ElMessage.warning('两次密码不一致')
    return
  }
  try {
    await ElMessageBox.confirm('确认保存吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.put('/auth/profile', {
    displayName: profileForm.value.displayName,
    phone: profileForm.value.phone,
    password: profileForm.value.password
  })
  displayName.value = profileForm.value.displayName
  setDisplayName(profileForm.value.displayName)
  ElMessage.success('保存成功')
  closeProfile()
}

const logout = () => {
  clearAuthState()
  router.push('/login')
}

onMounted(() => {
  applyTheme(theme.value)
})
</script>
