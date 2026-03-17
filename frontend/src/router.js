import { createRouter, createWebHistory } from 'vue-router'

const Login = () => import('./pages/Login.vue')
const HomeDashboard = () => import('./pages/HomeDashboard.vue')
const AdminRoles = () => import('./pages/AdminRoles.vue')
const AdminPermissions = () => import('./pages/AdminPermissions.vue')
const AdminUsers = () => import('./pages/AdminUsers.vue')
const AdminLogs = () => import('./pages/AdminLogs.vue')
const ParentChildren = () => import('./pages/ParentChildren.vue')
const ParentTaskList = () => import('./pages/ParentTaskList.vue')
const ParentTaskStats = () => import('./pages/ParentTaskStats.vue')
const ParentRewardList = () => import('./pages/ParentRewardList.vue')
const ParentRedemptions = () => import('./pages/ParentRedemptions.vue')
const ParentReports = () => import('./pages/ParentReports.vue')
const ChildTasks = () => import('./pages/ChildTasks.vue')
const ChildRewards = () => import('./pages/ChildRewards.vue')
const ChildPoints = () => import('./pages/ChildPoints.vue')
const MainLayout = () => import('./layouts/MainLayout.vue')

const routes = [
  { path: '/login', component: Login, meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: 'home', component: HomeDashboard, meta: { title: '首页' } },
      { path: 'admin', redirect: '/admin/roles' },
      { path: 'admin/roles', component: AdminRoles, meta: { role: 'ADMIN', title: '角色管理' } },
      { path: 'admin/permissions', component: AdminPermissions, meta: { role: 'ADMIN', title: '权限列表' } },
      { path: 'admin/users', component: AdminUsers, meta: { role: 'ADMIN', title: '用户管理' } },
      { path: 'admin/logs', component: AdminLogs, meta: { role: 'ADMIN', title: '日志管理' } },
      { path: 'parent', redirect: '/parent/children' },
      { path: 'parent/children', component: ParentChildren, meta: { role: 'PARENT', title: '孩子管理' } },
      { path: 'parent/tasks', component: ParentTaskList, meta: { role: 'PARENT', title: '任务列表' } },
      { path: 'parent/task-stats', component: ParentTaskStats, meta: { role: 'PARENT', title: '任务统计' } },
      { path: 'parent/rewards', component: ParentRewardList, meta: { role: 'PARENT', title: '奖励管理' } },
      { path: 'parent/redemptions', component: ParentRedemptions, meta: { role: 'PARENT', title: '兑换审核' } },
      { path: 'parent/reports', component: ParentReports, meta: { role: 'PARENT', title: '报表统计' } },
      { path: 'child', redirect: '/child/tasks' },
      { path: 'child/tasks', component: ChildTasks, meta: { role: 'CHILD', title: '今日任务' } },
      { path: 'child/rewards', component: ChildRewards, meta: { role: 'CHILD', title: '兑换记录' } },
      { path: 'child/points', component: ChildPoints, meta: { role: 'CHILD', title: '积分明细' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta?.public) return true
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token) return '/login'
  if (to.path === '/') return '/home'
  if (to.meta?.role && role !== 'ADMIN' && to.meta.role !== role) return '/login'
  return true
})

router.afterEach((to) => {
  const baseTitle = '学习计划管理系统'
  const pageTitle = to.meta?.title ? `${to.meta.title} - ${baseTitle}` : baseTitle
  document.title = pageTitle
  // Clean up stray overlays when switching roles/logging in
  document.querySelectorAll('.drawer-backdrop, .el-overlay, .el-overlay-message-box').forEach((el) => el.remove())
  document.body.classList.remove('el-popup-parent--hidden')
  document.body.style.removeProperty('overflow')
})

export default router
