import http from '../utils/http'

// 教练列表
export const getCoachList = (params) => http.get('/coach/list', { params })

// 教练详情
export const getCoachDetail = (id) => http.get(`/coach/detail/${id}`)

// 新增教练
export const addCoach = (data) => http.post('/coach', data)

// 修改教练
export const updateCoach = (data) => http.put('/coach', data)

// 删除教练
export const deleteCoach = (id) => http.delete(`/coach/${id}`)
