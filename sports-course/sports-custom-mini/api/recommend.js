import http from '../utils/http'

export const getHomeRecommend = (params) => {
  return http.get('/recommend/home', params)
}

export const getCourseRecommend = (params) => {
  return http.get('/recommend/course', params)
}

export const getRelatedCourseRecommend = (courseId, params) => {
  return http.get(`/recommend/course/related/${courseId}`, params)
}
