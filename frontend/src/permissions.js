import {
  getPermissions as getStoredPermissions,
  setPermissions as setStoredPermissions
} from './utils/authStorage'

export const getPermissions = () => getStoredPermissions()

export const setPermissions = (permissions) => {
  setStoredPermissions(permissions)
}

export const can = (code) => {
  if (!code) return true
  const list = getPermissions()
  return list.includes(code)
}
