import http from '../utils/http'

// 教练列表
export const getCoachList = (params) => {
  return http.get('/coach/list', params)
}

// 教练详情
export const getCoachDetail = (id) => {
  return http.get(`/coach/detail/${id}`)
}
