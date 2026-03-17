import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: 'http://127.0.0.1:8080/api'
})

const normalizeMessage = (raw, fallback) => {
  if (!raw) return fallback
  const msg = typeof raw === 'string' ? raw : (raw.message || raw.msg || raw.error || '')
  if (!msg) return fallback
  if (msg.includes('�') || /Ã.|â.|æ.|å.|ä.|ç.|ð.|ñ./.test(msg)) return fallback
  return msg
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const payload = response?.data
    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code === 0) {
        response.data = payload.data
        return response
      }
      ElMessage.error(normalizeMessage(payload, '请求失败'))
      return Promise.reject(payload)
    }
    return response
  },
  (error) => {
    const status = error?.response?.status
    const data = error?.response?.data
    let message = '请求失败，请稍后重试'
    if (typeof data === 'string' && data.trim()) {
      message = data
    } else if (data && typeof data === 'object') {
      message = data.msg || data.message || data.error || message
    } else if (error?.message) {
      message = error.message
    }
    ElMessage.error(normalizeMessage(message, '请求失败，请稍后重试'))
    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('permissions')
      localStorage.removeItem('displayName')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
