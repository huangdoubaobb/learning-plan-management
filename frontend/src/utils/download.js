export const downloadBlob = (data, filename) => {
  const blob = data instanceof Blob ? data : new Blob([data])
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()

  window.URL.revokeObjectURL(url)
}

export const downloadCsv = (rows, filename, options = {}) => {
  const { withBom = true } = options
  const csv = Array.isArray(rows) ? rows.join('\n') : String(rows || '')
  const payload = withBom ? `\uFEFF${csv}` : csv
  downloadBlob(new Blob([payload], { type: 'text/csv;charset=utf-8;' }), filename)
}
