import api from './api'

export const adminService = {
  // 用户管理
  getUsers: () => api.get('/admin/users'),
  createUser: (data) => api.post('/admin/users', data),
  updateUser: (id, data) => api.put(`/admin/users/${id}`, data),
  
  // 角色管理
  getRoles: () => api.get('/admin/roles'),
  createRole: (data) => api.post('/admin/roles', data),
  updateRole: (id, data) => api.put(`/admin/roles/${id}`, data),
  
  // 权限管理
  getPermissions: () => api.get('/admin/permissions'),
  
  // 菜单管理
  getMenus: () => api.get('/admin/menus'),
  updateRoleMenus: (roleId, data) => api.put(`/admin/roles/${roleId}/menus`, data)
}
