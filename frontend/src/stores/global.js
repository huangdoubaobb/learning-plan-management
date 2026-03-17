import { defineStore } from 'pinia'

export const useGlobalStore = defineStore('global', {
  state: () => ({
    loading: false,
    error: null,
    sidebarCollapsed: false,
    currentChildId: ''
  }),
  
  actions: {
    setLoading(loading) {
      this.loading = loading
    },
    
    setError(error) {
      this.error = error
    },
    
    clearError() {
      this.error = null
    },
    
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    
    setCurrentChildId(childId) {
      this.currentChildId = childId
    }
  }
})
