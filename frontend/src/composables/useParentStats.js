import { ref, onMounted, onBeforeUnmount } from 'vue'
import api from '../api'

export const normalizeParentStats = (data = {}) => ({
  children: data.childrenTotal || 0,
  tasks: data.tasksTotal || 0,
  rewards: data.rewardsTotal || 0,
  pending: data.pendingRedemptions || 0,
  tasksInRange: data.tasksInRange || 0,
  redemptionsInRange: data.redemptionsInRange || 0
})

export const useParentStats = () => {
  const stats = ref({
    children: 0,
    tasks: 0,
    rewards: 0,
    pending: 0,
    tasksInRange: 0,
    redemptionsInRange: 0
  })
  const days = ref(30)

  const load = async () => {
    const { data } = await api.get(`/parent/stats?days=${days.value}`)
    stats.value = normalizeParentStats(data)
  }

  const notifyUpdate = () => {
    window.dispatchEvent(new Event('parent-stats-updated'))
  }

  const onUpdate = () => {
    load()
  }

  onMounted(() => {
    load()
    window.addEventListener('parent-stats-updated', onUpdate)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('parent-stats-updated', onUpdate)
  })

  return { stats, days, refresh: load, notifyUpdate }
}
