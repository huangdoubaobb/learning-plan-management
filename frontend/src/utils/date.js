export const formatDateTime = (value, length = 16) => {
  if (!value) return '-'
  const text = String(value).replace('T', ' ')
  return text.slice(0, length)
}

export const formatDateTimeSeconds = (value) => formatDateTime(value, 19)
