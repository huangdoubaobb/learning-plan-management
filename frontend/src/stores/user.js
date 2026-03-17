import { defineStore } from 'pinia'
import { authService } from '../services'
import { getStorage, setStorage, removeStorage, clearStorage } from '../utils/storage'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getStorage('token', ''),
    role: getStorage('role', ''),
    permissions: getStorage('permissions', []),
    userInfo: null,
    loading: false
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.role === 'ADMIN',
    isParent: (state) => state.role === 'PARENT',
    isChild: (state) => state.role === 'CHILD'
  },
  
  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const { data } = await authService.login(credentials)
        this.token = data.token
        this.role = data.role
        setStorage('token', data.token)
        setStorage('role', data.role)
        await this.loadProfile()
        return data
      } finally {
        this.loading = false
      }
    },
    
    async registerParent(data) {
      this.loading = true
      try {
        const { data: response } = await authService.registerParent(data)
        this.token = response.token
        this.role = response.role
        setStorage('token', response.token)
        setStorage('role', response.role)
        await this.loadProfile()
        return response
      } finally {
        this.loading = false
      }
    },
    
    async loadProfile() {
      try {
        const { data } = await authService.getProfile()
        this.userInfo = data
        this.permissions = data.permissions || []
        setStorage('permissions', data.permissions || [])
      } catch (error) {
        console.error('Failed to load profile:', error)
      }
    },
    
    logout() {
      this.token = ''
      this.role = ''
      this.permissions = []
      this.userInfo = null
      removeStorage('token')
      removeStorage('role')
      removeStorage('permissions')
    },
    
    hasPermission(code) {
      if (!code) return true
      return this.permissions.includes(code)
    }
  }
})
