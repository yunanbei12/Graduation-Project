import http from '../utils/http'

export const trackDetailView = (data) => {
  return http.post('/behavior/view', data)
}

export const trackRecommendClick = (data) => {
  return http.post('/behavior/recommend-click', data)
}
