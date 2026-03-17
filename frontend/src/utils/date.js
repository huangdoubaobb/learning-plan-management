// 日期处理工具函数

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期字符串
 */
export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:MM:SS 格式
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 获取相对时间描述
 * @param {Date} date - 日期对象
 * @returns {string} 相对时间描述
 */
export const getRelativeTime = (date) => {
  if (!date) return ''
  const now = new Date()
  const target = new Date(date)
  const diff = now - target
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (days > 0) {
    return `${days}天前`
  } else if (hours > 0) {
    return `${hours}小时前`
  } else if (minutes > 0) {
    return `${minutes}分钟前`
  } else {
    return '刚刚'
  }
}

/**
 * 获取指定天数前的日期
 * @param {number} days - 天数
 * @returns {Date} 计算后的日期对象
 */
export const getDaysAgo = (days) => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return date
}

/**
 * 获取日期范围
 * @param {number} days - 天数
 * @returns {Object} 包含开始和结束日期的对象
 */
export const getDateRange = (days) => {
  const end = new Date()
  const start = getDaysAgo(days)
  return {
    start: formatDate(start),
    end: formatDate(end)
  }
}
