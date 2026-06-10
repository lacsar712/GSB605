export async function downloadProtectedFile(file, token) {
  if (!file?.url) {
    throw new Error('文件地址无效')
  }

  const response = await fetch(file.url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })

  if (!response.ok) {
    throw new Error('文件下载失败')
  }

  const blob = await response.blob()
  const blobUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = file.name || 'download'
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.setTimeout(() => window.URL.revokeObjectURL(blobUrl), 1000)
}
