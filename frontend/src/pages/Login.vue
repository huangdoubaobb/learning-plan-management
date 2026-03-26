<template>
  <div class="login-page">
    <section class="login-visual">
      <div class="login-brand">
        <div class="login-brand-icon">
          <span />
        </div>
        <strong>学习计划管理</strong>
      </div>

      <div class="login-copy">
        <div class="login-badge">家庭协作任务系统</div>
        <h1>让家庭任务管理更直观</h1>
        <p>任务、积分、奖励，一个页面完成协作。</p>
      </div>

      <div class="login-characters">
        <AnimatedCharacters
          :is-typing="isTyping"
          :has-secret="hasSecret"
          :secret-visible="showSecret"
        />
      </div>

      <div class="login-meta">
        <span>任务追踪</span>
        <span>积分激励</span>
        <span>奖励兑换</span>
      </div>

      <div class="deco-grid" />
      <div class="deco-circle deco-circle-1" />
      <div class="deco-circle deco-circle-2" />
    </section>

    <section class="login-panel">
      <div class="form-shell">
        <div class="mobile-brand">
          <div class="login-brand-icon">
            <span />
          </div>
          <strong>学习计划管理</strong>
        </div>

        <div class="mode-switch">
          <button
            type="button"
            class="mode-chip"
            :class="{ active: mode === 'login' }"
            @click="switchMode('login')"
          >
            登录
          </button>
          <button
            type="button"
            class="mode-chip"
            :class="{ active: mode === 'register' }"
            @click="switchMode('register')"
          >
            家长注册
          </button>
        </div>

        <div class="form-head">
          <h2>{{ mode === 'login' ? '欢迎回来' : '创建家长账号' }}</h2>
          <p>{{ mode === 'login' ? '请输入账号和密码进入系统' : '填写基本信息后即可开始创建家庭任务计划' }}</p>
        </div>

        <form v-if="mode === 'login'" class="form-stack" @submit.prevent="handleLogin">
          <div class="field">
            <label for="login-username">账号或手机号</label>
            <input
              id="login-username"
              v-model="loginForm.username"
              placeholder="请输入账号或手机号"
              autocomplete="username"
              @focus="setTyping(true)"
              @blur="setTyping(false)"
            />
          </div>

          <div class="field">
            <label for="login-password">密码</label>
            <div class="password-wrap">
              <input
                id="login-password"
                v-model="loginForm.password"
                :type="showLoginPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
                @focus="setTyping(true)"
                @blur="setTyping(false)"
              />
              <button type="button" class="eye-btn" @click="showLoginPassword = !showLoginPassword">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
                  <circle cx="12" cy="12" r="3.2" />
                  <path v-if="!showLoginPassword" d="M4 4l16 16" />
                </svg>
              </button>
            </div>
          </div>

          <div v-if="error" class="error-banner">{{ error }}</div>

          <button type="submit" class="submit-btn">登录</button>
        </form>

        <form v-else class="form-stack" @submit.prevent="handleRegister">
          <div class="field">
            <label for="register-username">账号</label>
            <input
              id="register-username"
              v-model="registerForm.username"
              placeholder="设置家长账号"
              autocomplete="username"
              @focus="setTyping(true)"
              @blur="setTyping(false)"
            />
          </div>

          <div class="field">
            <label for="register-password">密码</label>
            <div class="password-wrap">
              <input
                id="register-password"
                v-model="registerForm.password"
                :type="showRegisterPassword ? 'text' : 'password'"
                placeholder="设置登录密码"
                autocomplete="new-password"
                @focus="setTyping(true)"
                @blur="setTyping(false)"
              />
              <button type="button" class="eye-btn" @click="showRegisterPassword = !showRegisterPassword">
                <svg viewBox="0 0 24 24" aria-hidden="true">
                  <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
                  <circle cx="12" cy="12" r="3.2" />
                  <path v-if="!showRegisterPassword" d="M4 4l16 16" />
                </svg>
              </button>
            </div>
          </div>

          <div class="field-grid">
            <div class="field">
              <label for="register-phone">手机号</label>
              <input
                id="register-phone"
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                autocomplete="tel"
                @focus="setTyping(true)"
                @blur="setTyping(false)"
              />
            </div>

            <div class="field">
              <label for="register-display-name">显示名称</label>
              <input
                id="register-display-name"
                v-model="registerForm.displayName"
                placeholder="例如：李妈妈"
                @focus="setTyping(true)"
                @blur="setTyping(false)"
              />
            </div>
          </div>

          <div v-if="error" class="error-banner">{{ error }}</div>

          <button type="submit" class="submit-btn">注册并进入</button>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import AnimatedCharacters from '../components/login/AnimatedCharacters.vue'
import { setPermissions } from '../permissions'
import { setAuthSession } from '../utils/authStorage'

const router = useRouter()
const error = ref('')
const mode = ref('login')
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const focusedCount = ref(0)

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

const isTyping = computed(() => focusedCount.value > 0)
const hasSecret = computed(() => mode.value === 'login' ? !!loginForm.value.password : !!registerForm.value.password)
const showSecret = computed(() => mode.value === 'login' ? showLoginPassword.value : showRegisterPassword.value)

const normalizeErrorMessage = (raw, fallback) => {
  if (!raw) return fallback
  const msg = typeof raw === 'string' ? raw : (raw.message || raw.error || '')
  if (!msg) return fallback
  if (msg.includes('閿') || /鑴|鑺|蹇|姘|鐩|鑾|鍐|甯/.test(msg)) return fallback
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

const setTyping = (active) => {
  if (active) focusedCount.value += 1
  else focusedCount.value = Math.max(0, focusedCount.value - 1)
}

const switchMode = (nextMode) => {
  mode.value = nextMode
  error.value = ''
  focusedCount.value = 0
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
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(440px, 1.08fr) minmax(380px, 0.92fr);
  background: #ffffff;
}

.login-visual {
  position: relative;
  overflow: hidden;
  padding: 40px 48px 32px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
  background: linear-gradient(135deg, #5340ff 0%, #6c3ff5 48%, #7f5cff 100%);
}

.login-brand,
.mobile-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 2;
}

.login-brand strong,
.mobile-brand strong {
  font-size: 18px;
  font-weight: 700;
}

.login-brand-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
}

.login-brand-icon span {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(135deg, #ffffff 0%, rgba(255, 255, 255, 0.45) 100%);
  transform: rotate(45deg);
}

.login-copy {
  position: relative;
  z-index: 2;
  max-width: 520px;
  display: grid;
  gap: 18px;
  margin-top: 32px;
}

.login-badge {
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.login-copy h1 {
  margin: 0;
  font-size: clamp(34px, 4vw, 56px);
  line-height: 1.02;
  letter-spacing: -0.03em;
  color: #ffffff;
}

.login-copy p {
  margin: 0;
  max-width: 460px;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.82);
  font-size: 15px;
}

.login-characters {
  position: relative;
  z-index: 2;
  min-height: 360px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.login-meta {
  position: relative;
  z-index: 2;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.login-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px 28px;
  background:
    radial-gradient(circle at top right, rgba(108, 63, 245, 0.08), transparent 32%),
    linear-gradient(180deg, #ffffff 0%, #faf8ff 100%);
}

.form-shell {
  width: 100%;
  max-width: 460px;
  padding: 36px;
  border-radius: 28px;
  border: 1px solid rgba(110, 84, 255, 0.08);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(12px);
  box-shadow: 0 30px 70px rgba(67, 44, 163, 0.12);
}

.mobile-brand {
  display: none;
  margin-bottom: 26px;
  color: #171717;
}

.mode-switch {
  display: inline-grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 6px;
  width: 100%;
  border-radius: 999px;
  background: #f3efff;
  margin-bottom: 24px;
}

.mode-chip {
  height: 42px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #6b7280;
  font-size: 14px;
  font-weight: 700;
  box-shadow: none;
}

.mode-chip.active {
  background: linear-gradient(135deg, #5a46ff 0%, #7b61ff 100%);
  color: #ffffff;
  box-shadow: 0 12px 20px rgba(93, 69, 255, 0.24);
}

.form-head {
  margin-bottom: 26px;
}

.form-head h2 {
  margin: 0 0 8px;
  font-size: 34px;
  line-height: 1.05;
  letter-spacing: -0.03em;
  color: #171717;
}

.form-head p {
  margin: 0;
  color: #71717a;
  line-height: 1.6;
  font-size: 14px;
}

.form-stack {
  display: grid;
  gap: 18px;
}

.field {
  display: grid;
  gap: 8px;
}

.field label {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #27272a;
}

.field input {
  height: 52px;
  border-radius: 16px;
  border: 1px solid #e4e4e7;
  background: rgba(255, 255, 255, 0.92);
  padding: 0 16px;
  font-size: 14px;
  color: #18181b;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.field input:focus {
  border-color: #6c3ff5;
  box-shadow: 0 0 0 4px rgba(108, 63, 245, 0.14);
  transform: translateY(-1px);
}

.password-wrap {
  position: relative;
}

.password-wrap input {
  width: 100%;
  padding-right: 56px;
}

.eye-btn {
  position: absolute;
  top: 50%;
  right: 12px;
  transform: translateY(-50%);
  width: 36px !important;
  min-width: 36px;
  height: 36px;
  padding: 0;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 999px;
  background: rgba(109, 95, 255, 0.08);
  color: #7c3aed;
  box-shadow: none;
}

.eye-btn:hover {
  background: rgba(109, 95, 255, 0.14);
}

.eye-btn svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.7;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.submit-btn {
  height: 54px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, #5a46ff 0%, #7b61ff 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  box-shadow: 0 18px 28px rgba(93, 69, 255, 0.24);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 22px 34px rgba(93, 69, 255, 0.28);
}

.error-banner {
  padding: 12px 14px;
  border-radius: 14px;
  border: 1px solid rgba(220, 38, 38, 0.12);
  background: rgba(254, 242, 242, 0.96);
  color: #b91c1c;
  font-size: 13px;
  line-height: 1.5;
}

.deco-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.06) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(52px);
}

.deco-circle-1 {
  top: 18%;
  right: -80px;
  width: 280px;
  height: 280px;
  background: rgba(255, 255, 255, 0.14);
}

.deco-circle-2 {
  bottom: -40px;
  left: -60px;
  width: 320px;
  height: 320px;
  background: rgba(255, 255, 255, 0.08);
}

@media (max-width: 1024px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    display: none;
  }

  .mobile-brand {
    display: flex;
  }

  .login-panel {
    min-height: 100vh;
    padding: 20px;
  }
}

@media (max-width: 640px) {
  .form-shell {
    padding: 24px 18px;
    border-radius: 22px;
  }

  .field-grid {
    grid-template-columns: 1fr;
  }

  .form-head h2 {
    font-size: 28px;
  }
}
</style>
