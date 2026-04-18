import http from '../utils/http'

// 课程列表
export const getCourseList = (params) => http.get('/course/list', { params })

// 课程详情
export const getCourseDetail = (id) => http.get(`/course/detail/${id}`)

// 新增课程
export const addCourse = (data) => http.post('/course', data)

// 修改课程
export const updateCourse = (data) => http.put('/course', data)

// 删除课程
export const deleteCourse = (id) => http.delete(`/course/${id}`)

// 课程分类列表
export const getCourseCategoryList = () => http.get('/course/category/list')

// 新增课程分类
export const addCourseCategory = (data) => http.post('/course/category', data)

// 修改课程分类
export const updateCourseCategory = (data) => http.put('/course/category', data)

// 删除课程分类
export const deleteCourseCategory = (id) => http.delete(`/course/category/${id}`)

// 排课列表
export const getScheduleList = (params) => http.get('/schedule/list', { params })

// 新增排课
export const addSchedule = (data) => http.post('/schedule', data)

// 修改排课
export const updateSchedule = (data) => http.put('/schedule', data)

// 删除排课
export const deleteSchedule = (id) => http.delete(`/schedule/${id}`)

// 获取排课报名用户列表
export const getScheduleAttendees = (scheduleId) => http.get(`/checkin/group/attendees/${scheduleId}`)

// 团课批量结课
export const groupSettle = (scheduleId, absentUserIds) => http.post(`/checkin/group/settle/${scheduleId}`, absentUserIds)

// 成团判断
export const checkGroupStatus = (scheduleId) => http.post(`/schedule/check-group/${scheduleId}`)
