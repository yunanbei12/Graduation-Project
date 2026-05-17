// 后端API基础地址
const BASE_URL = 'http://localhost:8186'

// 将相对路径图片URL转为完整URL
function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  return BASE_URL + url
}

// 统一课程封面策略：
// 团课优先展示地点图，其次课程图；私教课直接展示课程图。
function getCourseCoverImage(course, fallback = '/static/logo.png') {
  if (!course) return fallback
  const isGroupCourse = Number(course.courseType || course.type) === 2
  const image = isGroupCourse
    ? (course.locationImage || course.image || course.pic)
    : (course.pic || course.image)
  return getImageUrl(image) || fallback
}

export default {
  BASE_URL,
  getImageUrl,
  getCourseCoverImage
}
