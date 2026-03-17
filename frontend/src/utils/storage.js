// 安全存储工具函数

/**
 * 存储数据到localStorage
 * @param {string} key - 存储键名
 * @param {any} value - 存储值
 */
export const setStorage = (key, value) => {
  try {
    const serializedValue = JSON.stringify(value)
    localStorage.setItem(key, serializedValue)
  } catch (error) {
    console.error('Error saving to localStorage:', error)
  }
}

/**
 * 从localStorage读取数据
 * @param {string} key - 存储键名
 * @param {any} defaultValue - 默认值
 * @returns {any} 存储的值或默认值
 */
export const getStorage = (key, defaultValue = null) => {
  try {
    const serializedValue = localStorage.getItem(key)
    if (serializedValue === null) {
      return defaultValue
    }
    return JSON.parse(serializedValue)
  } catch (error) {
    console.error('Error reading from localStorage:', error)
    return defaultValue
  }
}

/**
 * 从localStorage删除数据
 * @param {string} key - 存储键名
 */
export const removeStorage = (key) => {
  try {
    localStorage.removeItem(key)
  } catch (error) {
    console.error('Error removing from localStorage:', error)
  }
}

/**
 * 存储敏感数据到sessionStorage
 * @param {string} key - 存储键名
 * @param {any} value - 存储值
 */
export const setSessionStorage = (key, value) => {
  try {
    const serializedValue = JSON.stringify(value)
    sessionStorage.setItem(key, serializedValue)
  } catch (error) {
    console.error('Error saving to sessionStorage:', error)
  }
}

/**
 * 从sessionStorage读取敏感数据
 * @param {string} key - 存储键名
 * @param {any} defaultValue - 默认值
 * @returns {any} 存储的值或默认值
 */
export const getSessionStorage = (key, defaultValue = null) => {
  try {
    const serializedValue = sessionStorage.getItem(key)
    if (serializedValue === null) {
      return defaultValue
    }
    return JSON.parse(serializedValue)
  } catch (error) {
    console.error('Error reading from sessionStorage:', error)
    return defaultValue
  }
}

/**
 * 从sessionStorage删除敏感数据
 * @param {string} key - 存储键名
 */
export const removeSessionStorage = (key) => {
  try {
    sessionStorage.removeItem(key)
  } catch (error) {
    console.error('Error removing from sessionStorage:', error)
  }
}

/**
 * 清除所有存储数据
 */
export const clearStorage = () => {
  try {
    localStorage.clear()
    sessionStorage.clear()
  } catch (error) {
    console.error('Error clearing storage:', error)
  }
}
