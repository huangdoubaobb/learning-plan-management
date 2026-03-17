// 表单验证工具函数

/**
 * 验证是否为空
 * @param {string} value - 输入值
 * @returns {boolean} 是否为空
 */
export const isEmpty = (value) => {
  return value === null || value === undefined || value.trim() === ''
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {boolean} 是否有效
 */
export const isValidPhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 验证邮箱
 * @param {string} email - 邮箱
 * @returns {boolean} 是否有效
 */
export const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

/**
 * 验证密码强度
 * @param {string} password - 密码
 * @returns {Object} 包含验证结果和强度等级的对象
 */
export const validatePassword = (password) => {
  let strength = 0
  let message = ''

  if (password.length >= 8) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^A-Za-z0-9]/.test(password)) strength++

  switch (strength) {
    case 0:
    case 1:
      message = '密码强度：弱'
      break
    case 2:
    case 3:
      message = '密码强度：中等'
      break
    case 4:
    case 5:
      message = '密码强度：强'
      break
  }

  return {
    valid: password.length >= 8,
    strength,
    message
  }
}

/**
 * 验证身份证号
 * @param {string} idCard - 身份证号
 * @returns {boolean} 是否有效
 */
export const isValidIdCard = (idCard) => {
  const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/
  return idCardRegex.test(idCard)
}

/**
 * 验证日期范围
 * @param {string} startDate - 开始日期
 * @param {string} endDate - 结束日期
 * @returns {boolean} 是否有效
 */
export const isValidDateRange = (startDate, endDate) => {
  const start = new Date(startDate)
  const end = new Date(endDate)
  return start <= end
}
