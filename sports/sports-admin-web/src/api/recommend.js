import http from '../utils/http'

export const getRecommendStatsSummary = () => http.get('/admin/recommend/stats/summary')

export const getRecommendBehaviorPage = (params) => http.get('/admin/recommend/behavior/list', { params })
