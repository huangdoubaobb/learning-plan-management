import { computed, ref } from 'vue'
import api from '../api'

const appendIfPresent = (params, key, value) => {
  if (value !== null && value !== undefined && value !== '') {
    params.append(key, String(value))
  }
}

export const useParentTaskFilters = () => {
  const tasks = ref([])
  const total = ref(0)
  const pendingCount = ref(0)
  const completedCount = ref(0)

  const statusFilter = ref('PENDING')
  const childFilter = ref('')
  const keyword = ref('')
  const startDate = ref('')
  const endDate = ref('')
  const pageSize = ref(10)
  const currentPage = ref(1)

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

  const buildCommonParams = () => {
    const params = new URLSearchParams()
    appendIfPresent(params, 'childId', childFilter.value)
    appendIfPresent(params, 'q', keyword.value)
    appendIfPresent(params, 'startDate', startDate.value)
    appendIfPresent(params, 'endDate', endDate.value)
    return params
  }

  const buildPageParams = () => {
    const params = buildCommonParams()
    params.append('page', String(currentPage.value))
    params.append('size', String(pageSize.value))
    params.append('status', statusFilter.value)
    return params
  }

  const loadPage = async () => {
    const { data } = await api.get(`/parent/tasks/page?${buildPageParams().toString()}`)
    tasks.value = data.items || []
    total.value = data.total || 0

    const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
    if (currentPage.value > maxPage) {
      currentPage.value = maxPage
    }
  }

  const loadCounts = async () => {
    const { data } = await api.get(`/parent/tasks/counts?${buildCommonParams().toString()}`)
    pendingCount.value = data.pending || 0
    completedCount.value = data.completed || 0
  }

  const applyFilters = async () => {
    currentPage.value = 1
    await loadCounts()
    await loadPage()
  }

  const resetFilters = async () => {
    childFilter.value = ''
    keyword.value = ''
    startDate.value = ''
    endDate.value = ''
    statusFilter.value = 'PENDING'
    pageSize.value = 10
    await applyFilters()
  }

  return {
    tasks,
    total,
    pendingCount,
    completedCount,
    statusFilter,
    childFilter,
    keyword,
    startDate,
    endDate,
    pageSize,
    currentPage,
    totalPages,
    buildPageParams,
    loadPage,
    loadCounts,
    applyFilters,
    resetFilters
  }
}
