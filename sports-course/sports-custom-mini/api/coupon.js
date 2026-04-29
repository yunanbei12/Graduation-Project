import http from '../utils/http'

// 可领取的优惠券列表
export const getAvailableCoupons = () => {
  return http.get('/user/coupon/available')
}

// 领取优惠券
export const receiveCoupon = (couponId) => {
  return http.post(`/user/coupon/receive/${couponId}`)
}

// 我的优惠券列表
export const getMyCoupons = (status) => {
  return http.get('/user/coupon/list', { status })
}

// 下单时可用优惠券
export const getUsableCoupons = (orderType, amount) => {
  return http.get('/user/coupon/usable', { orderType, amount })
}
