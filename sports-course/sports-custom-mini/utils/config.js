// 后端API基础地址
const BASE_URL = 'http://localhost:8186'

// 将相对路径图片URL转为完整URL
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return BASE_URL + url
}

export default {
  BASE_URL,
  getImageUrl
}
