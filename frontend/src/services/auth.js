import api from './api'

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  registerParent: (data) => api.post('/auth/register-parent', data),
  getProfile: () => api.get('/auth/me')
}
