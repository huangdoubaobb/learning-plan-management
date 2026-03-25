<template>
  <div class="container">
    <div class="header auth-header">
      <div>
        <h2>学习计划管理系统</h2>
      </div>
    </div>

    <div class="auth-shell auth-blue" :style="bgStyle">
      <div class="auth-card auth-merged">
        <div class="auth-side">
          <div class="auth-hero">
            <div class="auth-emblem">
              <svg class="auth-sun" viewBox="0 0 64 64" aria-hidden="true">
                <g>
                  <circle class="sun-core" cx="32" cy="32" r="12" />
                  <g class="sun-rays">
                    <line x1="32" y1="6" x2="32" y2="16" />
                    <line x1="32" y1="48" x2="32" y2="58" />
                    <line x1="6" y1="32" x2="16" y2="32" />
                    <line x1="48" y1="32" x2="58" y2="32" />
                    <line x1="12" y1="12" x2="19" y2="19" />
                    <line x1="45" y1="45" x2="52" y2="52" />
                    <line x1="12" y1="52" x2="19" y2="45" />
                    <line x1="45" y1="19" x2="52" y2="12" />
                  </g>
                </g>
              </svg>
            </div>
            <div>
              <div class="auth-badge">学习计划管理系统</div>
              <h3 class="auth-title">更清晰的家庭任务协作</h3>
            </div>
          </div>
          <p class="auth-desc">
            家长创建任务与奖励，孩子完成任务后获得积分并进行兑换。
            让每天的学习目标变得可视、可达、可追踪。
          </p>
          <div class="auth-form">
            <div v-if="mode === 'login'">
              <h3 class="auth-form-title">登录</h3>
              <div class="notice error" v-if="error">{{ error }}</div>
              <div class="auth-fields">
                <div>
                  <label>账号或手机号</label>
                  <input v-model="loginForm.username" placeholder="请输入账号或手机号" />
                </div>
                <div>
                  <label>密码</label>
                  <div class="auth-input-wrap">
                    <input v-model="loginForm.password" :type="showLoginPassword ? 'text' : 'password'" placeholder="请输入密码" />
                    <VeaButton class="auth-eye" text native-type="button" @click="showLoginPassword = !showLoginPassword">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
                        <circle cx="12" cy="12" r="3.2" />
                        <path v-if="!showLoginPassword" d="M4 4l16 16" />
                      </svg>
                    </VeaButton>
                  </div>
                </div>
              </div>
              <div style="margin-top: 16px;">
                <VeaButton class="auth-primary" type="primary" @click="handleLogin">登录</VeaButton>
              </div>
              <div class="notice auth-switch">
                还没有账号？
                <VeaButton class="secondary" style="max-width: 160px;" text @click="mode = 'register'">去注册</VeaButton>
              </div>
            </div>

            <div v-else>
              <h3 class="auth-form-title">家长注册</h3>
              <div class="notice error" v-if="error">{{ error }}</div>
              <div class="auth-fields">
                <div>
                  <label>账号</label>
                  <input v-model="registerForm.username" placeholder="家长账号" />
                </div>
                <div>
                  <label>密码</label>
                  <div class="auth-input-wrap">
                    <input v-model="registerForm.password" :type="showRegisterPassword ? 'text' : 'password'" placeholder="设置密码" />
                    <VeaButton class="auth-eye" text native-type="button" @click="showRegisterPassword = !showRegisterPassword">
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
                        <circle cx="12" cy="12" r="3.2" />
                        <path v-if="!showRegisterPassword" d="M4 4l16 16" />
                      </svg>
                    </VeaButton>
                  </div>
                </div>
                <div>
                  <label>手机号</label>
                  <input v-model="registerForm.phone" placeholder="请输入手机号" />
                </div>
                <div>
                  <label>显示名称</label>
                  <input v-model="registerForm.displayName" placeholder="例如：李妈妈" />
                </div>
              </div>
              <div style="margin-top: 16px;">
                <VeaButton class="auth-primary" type="primary" @click="handleRegister">注册并进入</VeaButton>
              </div>
              <div class="notice auth-switch">
                已有账号？
                <VeaButton class="secondary" style="max-width: 160px;" text @click="mode = 'login'">去登录</VeaButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import { setPermissions } from '../permissions'
import VeaButton from '../components/VeaButton.vue'
import { setAuthSession } from '../utils/authStorage'

const router = useRouter()
const error = ref('')
const mode = ref('login')
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const bgSources = Object.values(import.meta.glob('../assets/login/*.{png,jpg,jpeg,webp,avif}', { eager: true, import: 'default' }))

const bgStyle = computed(() => {
  if (!bgSources.length) return {}
  const shuffled = [...bgSources].sort(() => Math.random() - 0.5)
  const pick = (i) => `url("${shuffled[i % shuffled.length]}")`
  return {
    '--auth-bg-1': pick(0),
    '--auth-bg-2': pick(1),
    '--auth-bg-3': pick(2)
  }
})

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  phone: '',
  displayName: ''
})

const normalizeErrorMessage = (raw, fallback) => {
  if (!raw) return fallback
  const msg = typeof raw === 'string' ? raw : (raw.message || raw.error || '')
  if (!msg) return fallback
  if (msg.includes('锟') || /脙.|芒.|忙.|氓.|盲.|莽.|冒.|帽./.test(msg)) return fallback
  return msg
}

const loadPermissions = async () => {
  try {
    const { data } = await api.get('/auth/me')
    setPermissions(data.permissions || [])
  } catch {
    setPermissions([])
  }
}

const redirectByRole = (role) => {
  if (role === 'ADMIN') router.push('/admin/roles')
  else if (role === 'PARENT') router.push('/parent/children')
  else router.push('/child/tasks')
}

const handleLogin = async () => {
  error.value = ''
  try {
    const { data } = await api.post('/auth/login', loginForm.value)
    setAuthSession(data)
    await loadPermissions()
    redirectByRole(data.role)
  } catch (err) {
    error.value = normalizeErrorMessage(err?.response?.data, '登录失败')
  }
}

const handleRegister = async () => {
  error.value = ''
  try {
    const { data } = await api.post('/auth/register-parent', registerForm.value)
    setAuthSession(data)
    await loadPermissions()
    redirectByRole(data.role)
  } catch (err) {
    error.value = normalizeErrorMessage(err?.response?.data, '注册失败')
  }
}
</script>

<style scoped>
.auth-shell {
  max-width: 1120px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: start;
  position: relative;
  min-height: 70vh !important;
}

.auth-blue {
  --primary: #5b8ff0;
  --primary-strong: #3a6fd6;
  --accent: #7dd3fc;
  --accent-strong: #38bdf8;
  --btn-primary-start: #5b8ff0;
  --btn-primary-end: #86b6ff;
  --btn-primary-hover-start: #3a6fd6;
  --btn-primary-hover-end: #5b8ff0;
  --badge-bg: rgba(91, 143, 240, 0.14);
  --badge-text: #3a6fd6;
  --chip-bg: rgba(91, 143, 240, 0.08);
  --chip-border: rgba(91, 143, 240, 0.18);
  --bar-track: rgba(91, 143, 240, 0.08);
  --bar-fill: linear-gradient(180deg, #3a6fd6 0%, #9bbcf6 100%);
  --table-hover: rgba(91, 143, 240, 0.06);
}

.auth-header {
  justify-content: center;
  text-align: center;
}

.auth-header h2 {
  font-size: 34px;
  letter-spacing: 0.6px;
}

.auth-shell::before {
  content: "";
  position: fixed;
  inset: 0;
  z-index: -1;
  border-radius: 0;
  background-size: cover;
  background-position: center;
  animation: authCarousel 18s ease-in-out infinite;
  filter: blur(0.5px) saturate(1.05);
  pointer-events: none;
}

.auth-merged {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  align-items: start;
}

.auth-side {
  background:
    radial-gradient(320px 220px at 10% 0%, rgba(91, 143, 240, 0.28), transparent 60%),
    radial-gradient(260px 200px at 90% 20%, rgba(125, 211, 252, 0.25), transparent 60%),
    linear-gradient(145deg, rgba(91, 143, 240, 0.12), rgba(255, 255, 255, 0.95));
  border-radius: 16px;
  padding: 14px;
  border: 1px solid rgba(28, 27, 34, 0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  overflow: hidden;
}

.auth-side::after {
  content: "";
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(91, 143, 240, 0.18), transparent 65%);
  right: -70px;
  bottom: -70px;
  opacity: 0.8;
  pointer-events: none;
}

.auth-hero {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.auth-emblem {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #fff;
  background:
    radial-gradient(22px 22px at 70% 20%, rgba(255, 255, 255, 0.6), transparent 60%),
    linear-gradient(135deg, #2f6f64 0%, #76b5a9 100%);
  box-shadow: 0 12px 20px rgba(79, 143, 130, 0.25);
}

.auth-sun {
  width: 36px;
  height: 36px;
  fill: none;
  stroke: #ffffff;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
  filter: drop-shadow(0 6px 10px rgba(0, 0, 0, 0.2));
}

.auth-sun .sun-core {
  fill: rgba(255, 255, 255, 0.95);
  stroke: none;
}

.auth-sun .sun-rays {
  opacity: 0.9;
  transform-origin: 32px 32px;
  animation: sunPulse 3.6s ease-in-out infinite;
}

.auth-badge {
  display: inline-flex;
  align-self: flex-start;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(91, 143, 240, 0.14);
  color: var(--primary-strong);
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.auth-hero .auth-badge {
  align-self: center;
}

.auth-title {
  margin: 0;
  font-size: 22px;
  text-align: center;
}

.auth-desc {
  margin: 0;
  color: var(--muted);
  line-height: 1.4;
  font-size: 12px;
  text-align: center;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.auth-card {
  background: var(--card);
  border-radius: var(--radius);
  padding: 14px 16px;
  border: 1px solid rgba(28, 27, 34, 0.08);
  box-shadow: var(--shadow);
  position: relative;
  overflow: hidden;
  max-width: 760px;
  margin: 0 auto;
}

.auth-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(200px 140px at 100% 0%, rgba(91, 143, 240, 0.08), transparent 60%);
  pointer-events: none;
}

.auth-form {
  padding: 0;
}

.auth-card h3.auth-form-title {
  text-align: center;
  font-size: 22px;
  color: #1e3a8a !important;
}

.auth-fields {
  margin-top: 8px;
  display: grid;
  gap: 8px;
}

.auth-form label {
  margin-bottom: 4px;
  font-size: 12px;
}

.auth-form input {
  padding: 8px 10px;
  height: 36px;
}

.auth-input-wrap {
  position: relative;
}

.auth-input-wrap input {
  padding-right: 72px;
}

:deep(.auth-eye) {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: auto;
  padding: 6px 8px;
  border-radius: 999px;
  background: rgba(28, 27, 34, 0.08);
  color: var(--text);
  border: 1px solid rgba(28, 27, 34, 0.12);
  box-shadow: none;
  font-size: 12px;
  display: grid;
  place-items: center;
}

:deep(.auth-eye:hover) {
  background: rgba(28, 27, 34, 0.14);
}

:deep(.auth-eye svg) {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: var(--text);
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
}

:deep(.auth-primary) {
  height: 38px;
  width: 100%;
  font-size: 14px;
  letter-spacing: 0.02em;
}

.auth-switch {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

:deep(.auth-switch .el-button) {
  max-width: 120px !important;
  padding: 6px 10px;
  font-size: 12px;
  box-shadow: none;
}

@keyframes panelFloat {
  from {
    transform: translateY(6px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes sunPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}

@keyframes authCarousel {
  0% {
    background-image:
      linear-gradient(120deg, rgba(242, 246, 255, 0.7) 0%, rgba(238, 246, 255, 0.7) 100%),
      var(--auth-bg-1, none);
  }
  33% {
    background-image:
      linear-gradient(120deg, rgba(241, 245, 255, 0.7) 0%, rgba(234, 244, 255, 0.7) 100%),
      var(--auth-bg-2, none);
  }
  66% {
    background-image:
      linear-gradient(120deg, rgba(243, 246, 255, 0.7) 0%, rgba(238, 244, 255, 0.7) 100%),
      var(--auth-bg-3, none);
  }
  100% {
    background-image:
      linear-gradient(120deg, rgba(242, 246, 255, 0.7) 0%, rgba(238, 246, 255, 0.7) 100%),
      var(--auth-bg-1, none);
  }
}

.auth-panel,
.auth-card {
  animation: panelFloat 0.45s ease both;
}

@media (max-width: 900px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-merged {
    grid-template-columns: 1fr;
  }
}
</style>
