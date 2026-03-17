import api from './api'

export const parentService = {
  // 孩子管理
  getChildren: () => api.get('/parent/children'),
  
  // 任务管理
  createTask: (data) => api.post('/parent/tasks', data),
  getTasks: () => api.get('/parent/tasks'),
  getTaskStats: () => api.get('/parent/task-stats'),
  
  // 奖励管理
  getRewards: () => api.get('/parent/rewards'),
  createReward: (data) => api.post('/parent/rewards', data),
  
  // 兑换管理
  getRedemptions: () => api.get('/parent/redemptions'),
  reviewRedemption: (id, data) => api.put(`/parent/redemptions/${id}/review`, data),
  
  // 报表统计
  getStats: (days) => api.get(`/parent/stats?days=${days}`),
  getReportSummary: (childId) => api.get(`/parent/report/summary?childId=${childId}`),
  getReportTrend: (childId, days) => api.get(`/parent/report/trend?childId=${childId}&days=${days}`),
  getPointsTrend: (childId, days) => api.get(`/parent/report/points-trend?childId=${childId}&days=${days}`)
}
