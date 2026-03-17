<template>
  <div class="container">
    <div class="header">
      <div>
        <h2>后台管理</h2>
        <div class="notice">角色与权限管理</div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">角色数量</div>
        <div class="stat-value">{{ roles.length }}</div>
        <div class="stat-foot">系统角色总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">权限数量</div>
        <div class="stat-value">{{ permissions.length }}</div>
        <div class="stat-foot">可分配权限</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">用户数量</div>
        <div class="stat-value">{{ users.length }}</div>
        <div class="stat-foot">当前用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">选中角色权限</div>
        <div class="stat-value">{{ selectedRole?.permissionIds?.length || 0 }}</div>
        <div class="stat-foot">当前角色权限数</div>
      </div>
    </div>

    <div class="card">
      <div class="row compact" style="justify-content: space-between; align-items: center;">
        <h3 style="margin: 0;">角色列表</h3>
        <VeaButton class="btn-sm btn-soft success" @click="openRoleModal">
          <span class="btn-icon"><el-icon><Plus /></el-icon></span>
          新增
        </VeaButton>
      </div>
      <div class="table-wrap" style="margin-top: 12px;">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>角色</th>
              <th>编码</th>
              <th>权限数量</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(role, index) in roles" :key="role.id" @click="selectRole(role)" style="cursor: pointer;">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ role.name }}</td>
              <td>{{ role.code }}</td>
              <td>{{ role.permissionIds.length }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="card">
      <h3>权限分配</h3>
      <div class="notice">选择角色后勾选权限并保存</div>
      <div v-if="!selectedRole">请选择角色</div>
      <div v-else>
        <div class="row" style="margin-top: 12px;">
          <div>
            <strong>{{ selectedRole.name }}</strong>
            <div class="notice">{{ selectedRole.code }}</div>
          </div>
          <div style="text-align: right;">
            <VeaButton class="btn-sm btn-soft info" @click="savePermissions">
              <span class="btn-icon"><el-icon><Check /></el-icon></span>
              保存权限
            </VeaButton>
          </div>
        </div>
        <div style="margin-top: 12px;" class="grid">
          <label v-for="permission in permissions" :key="permission.id">
            <input type="checkbox" :value="permission.id" v-model="selectedPermissionIds" style="width: auto; margin-right: 8px;" />
            {{ permission.name }} ({{ permission.code }})
          </label>
        </div>
      </div>
    </div>

    <div class="card">
      <h3>用户列表</h3>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th style="width: 44px;">选择</th>
              <th style="width: 60px;">序号</th>
              <th>用户名</th>
              <th>显示名</th>
              <th>角色</th>
              <th>积分</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(user, index) in users" :key="user.id">
              <td><input type="checkbox" style="width: auto;" /></td>
              <td>{{ index + 1 }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.displayName }}</td>
              <td>{{ user.role }}</td>
              <td>{{ user.points }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="showRoleModal" class="drawer-backdrop" @click="closeRoleModal">
      <div class="drawer-panel" @click.stop>
        <div class="drawer-header">
          <div class="drawer-title">新增角色</div>
          <VeaButton class="modal-close" @click="closeRoleModal" text>关闭</VeaButton>
        </div>
        <div class="drawer-body">
          <label>角色编码</label>
          <input v-model="newRole.code" placeholder="例如：EDITOR" />
          <label style="margin-top: 10px;">角色名称</label>
          <input v-model="newRole.name" placeholder="例如：内容管理员" />
        </div>
        <div class="drawer-footer">
          <VeaButton class="secondary" @click="closeRoleModal" text>取消</VeaButton>
          <VeaButton @click="createRole">
            <span class="btn-icon"><el-icon><Check /></el-icon></span>
            保存
          </VeaButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../api'
import VeaButton from '../components/VeaButton.vue'
const roles = ref([])
const permissions = ref([])
const users = ref([])
const newRole = ref({ code: '', name: '' })
const selectedRole = ref(null)
const selectedPermissionIds = ref([])
const showRoleModal = ref(false)

const loadAll = async () => {
  const [roleRes, permRes, userRes] = await Promise.all([
    api.get('/admin/roles'),
    api.get('/admin/permissions'),
    api.get('/admin/users')
  ])
  roles.value = roleRes.data
  permissions.value = permRes.data
  users.value = userRes.data
}

const selectRole = (role) => {
  selectedRole.value = role
  selectedPermissionIds.value = [...role.permissionIds]
}

const createRole = async () => {
  if (!newRole.value.code || !newRole.value.name) {
    ElMessage.warning('请填写必填项')
    return
  }
  try {
    await ElMessageBox.confirm('确认保存吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await api.post('/admin/roles', newRole.value)
  ElMessage.success('新增成功')
  newRole.value = { code: '', name: '' }
  await loadAll()
  closeRoleModal()
}

const savePermissions = async () => {
  if (!selectedRole.value) return
  await api.put(`/admin/roles/${selectedRole.value.id}/permissions`, {
    permissionIds: selectedPermissionIds.value
  })
  ElMessage.success('保存成功')
  await loadAll()
}

const openRoleModal = () => {
  newRole.value = { code: '', name: '' }
  showRoleModal.value = true
}

const closeRoleModal = () => {
  showRoleModal.value = false
}

onMounted(loadAll)
</script>
