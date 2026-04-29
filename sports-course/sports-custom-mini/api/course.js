import http from '../utils/http'

// 课程列表
export const getCourseList = (params) => {
  return http.get('/course/list', params)
}

// 课程详情
export const getCourseDetail = (id) => {
  return http.get(`/course/detail/${id}`)
}

// 课程分类列表
export const getCourseCategoryList = () => {
  return http.get('/course/category/list')
}

// 团课地点列表
export const getCourseLocationList = (categoryId) => {
  const params = {}
  if (categoryId) params.categoryId = categoryId
  return http.get('/course/location/list', params)
}

// 团课地点对象列表
export const getCourseLocationOptions = (categoryId) => {
  const params = {}
  if (categoryId) params.categoryId = categoryId
  return http.get('/course/location/options', params)
}

// 团课排课列表
export const getScheduleList = (courseId) => {
  return http.get('/course/schedule/list', { courseId, pageSize: 50 })
}

// 排课详情
export const getScheduleDetail = (scheduleId) => {
  return http.get(`/course/schedule/detail/${scheduleId}`)
}

// 首页近期团课排课（近7天可预约）
export const getUpcomingSchedules = (limit = 4) => {
  return http.get('/course/schedule/upcoming', { limit })
}

// 我的课包列表
export const getMyPackages = () => {
  return http.get('/user/package/list')
}

// 课包详情
export const getPackageDetail = (id) => {
  return http.get(`/user/package/detail/${id}`)
}

// 课包上课记录
export const getPackageCheckins = (packageId) => {
  return http.get(`/user/package/checkin/${packageId}`)
}

// 团课上课记录
export const getGroupCheckins = () => {
  return http.get('/user/checkin/group')
}
