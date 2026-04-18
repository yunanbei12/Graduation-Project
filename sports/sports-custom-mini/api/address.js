import http from '../utils/http'

// 获取地址列表
export const getAddressList = () => {
  return http.get('/address/list')
}

// 获取地址详情
export const getAddressDetail = (id) => {
  return http.get(`/address/detail/${id}`)
}

// 获取默认地址
export const getDefaultAddress = () => {
  return http.get('/address/default')
}

// 新增地址
export const addAddress = (data) => {
  return http.post('/address', data)
}

// 修改地址
export const updateAddress = (data) => {
  return http.put('/address', data)
}

// 删除地址
export const deleteAddress = (id) => {
  return http.delete(`/address/${id}`)
}

// 设置默认地址
export const setDefaultAddress = (id) => {
  return http.put(`/address/default/${id}`)
}
