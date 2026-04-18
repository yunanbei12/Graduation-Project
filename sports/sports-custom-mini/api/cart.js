import http from '../utils/http'

// 购物车列表
export const getCartList = () => {
  return http.get('/cart/list')
}

// 添加到购物车
export const addToCart = (data) => {
  return http.post('/cart/add', data)
}

// 更新购物车数量
export const updateCartQty = (data) => {
  return http.put('/cart/update', data)
}

// 删除购物车项
export const deleteCartItem = (id) => {
  return http.del(`/cart/${id}`)
}

// 清空购物车
export const clearCart = () => {
  return http.del('/cart/clear')
}

// 购物车数量
export const getCartCount = () => {
  return http.get('/cart/count')
}
