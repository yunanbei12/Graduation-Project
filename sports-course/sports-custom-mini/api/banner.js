import http from '../utils/http'

/**
 * 获取 Banner 列表
 * @param {number} position 位置 1=首页 2=课程页
 */
export const getBannerList = (position = 1) => {
  return http.get('/banner/list', { position })
}
