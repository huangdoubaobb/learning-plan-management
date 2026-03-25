const KEYS = {
  token: 'token',
  role: 'role',
  permissions: 'permissions',
  displayName: 'displayName',
  theme: 'theme'
}

const read = (key, fallback = '') => {
  try {
    const value = localStorage.getItem(key)
    return value ?? fallback
  } catch {
    return fallback
  }
}

const write = (key, value) => {
  try {
    if (value === null || value === undefined) {
      localStorage.removeItem(key)
      return
    }
    localStorage.setItem(key, String(value))
  } catch {
    // Ignore storage write failures in the browser sandbox.
  }
}

export const getToken = () => read(KEYS.token)
export const getRole = () => read(KEYS.role)
export const getDisplayName = () => read(KEYS.displayName)
export const getTheme = () => read(KEYS.theme)

export const setAuthSession = ({ token = '', role = '', displayName = '' } = {}) => {
  write(KEYS.token, token)
  write(KEYS.role, role)
  write(KEYS.displayName, displayName)
}

export const setDisplayName = (value) => {
  write(KEYS.displayName, value || '')
}

export const setTheme = (value) => {
  write(KEYS.theme, value || '')
}

export const getPermissions = () => {
  try {
    const raw = localStorage.getItem(KEYS.permissions)
    if (!raw) return []
    const list = JSON.parse(raw)
    return Array.isArray(list) ? list : []
  } catch {
    return []
  }
}

export const setPermissions = (permissions) => {
  try {
    const list = Array.isArray(permissions) ? permissions : []
    localStorage.setItem(KEYS.permissions, JSON.stringify(list))
  } catch {
    // Ignore storage write failures in the browser sandbox.
  }
}

export const clearAuthState = () => {
  try {
    localStorage.removeItem(KEYS.token)
    localStorage.removeItem(KEYS.role)
    localStorage.removeItem(KEYS.permissions)
    localStorage.removeItem(KEYS.displayName)
  } catch {
    // Ignore storage write failures in the browser sandbox.
  }
}
