import http from '../utils/http'

// 订单列表
export const getOrderList = (params) => http.get('/order/list', { params })

// 订单详情
export const getOrderDetail = (id) => http.get(`/order/detail/${id}`)

// 更新订单状态
export const updateOrderStatus = (data) => http.put('/order/status', data)
