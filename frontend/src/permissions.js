export const getPermissions = () => {
  const raw = localStorage.getItem('permissions')
  if (!raw) return []
  try {
    const list = JSON.parse(raw)
    return Array.isArray(list) ? list : []
  } catch (err) {
    return []
  }
}

export const setPermissions = (permissions) => {
  const list = Array.isArray(permissions) ? permissions : []
  localStorage.setItem('permissions', JSON.stringify(list))
}

export const can = (code) => {
  if (!code) return true
  const list = getPermissions()
  return list.includes(code)
}
