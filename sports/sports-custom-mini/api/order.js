import http from '../utils/http'

// 创建课程订单
export const createCourseOrder = (data) => {
  return http.post('/order/course', data)
}

// 创建商品订单
export const createProdOrder = (data) => {
  return http.post('/order/prod', data)
}

// 订单列表
export const getOrderList = (params) => {
  return http.get('/order/list', params)
}

// 订单详情
export const getOrderDetail = (id) => {
  return http.get(`/order/detail/${id}`)
}

// 订单项列表
export const getOrderItems = (orderId) => {
  return http.get(`/order/items/${orderId}`)
}

// 退款预估
export const getRefundPreview = (id) => {
  return http.get(`/order/refund-preview/${id}`)
}

// 取消订单
export const cancelOrder = (id) => {
  return http.put(`/order/cancel/${id}`)
}

// 申请退款
export const refundOrder = (id, refundReason) => {
  return http.put(`/order/refund/${id}`, { refundReason })
}

// 模拟支付
export const payOrder = (id) => {
  return http.put(`/order/pay/${id}`)
}

// 确认收货
export const confirmReceive = (id) => {
  return http.put(`/order/confirm/${id}`)
}
