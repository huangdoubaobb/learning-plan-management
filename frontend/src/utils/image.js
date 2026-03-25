export const resolveImageUrl = (api, url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = api.defaults.baseURL.replace(/\/api$/, '')
  return `${base}${url.startsWith('/') ? '' : '/'}${url}`
}
