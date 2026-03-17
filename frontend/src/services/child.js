import api from './api'

export const childService = {
  // 个人信息
  getMe: () => api.get('/child/me'),
  
  // 任务管理
  getTasks: () => api.get('/child/tasks'),
  completeTask: (data) => api.post('/child/tasks/complete', data),
  
  // 兑换管理
  getRedemptions: () => api.get('/child/redemptions'),
  createRedemption: (data) => api.post('/child/redemptions', data),
  
  // 积分管理
  getPoints: () => api.get('/child/points')
}
