import http from '../utils/http'

// 商品列表
export const getProdList = (params) => {
  return http.get('/prod/list', params)
}

// 商品详情
export const getProdDetail = (id) => {
  return http.get(`/prod/detail/${id}`)
}

// 商品分类列表
export const getProdCategoryList = () => {
  return http.get('/prod/category/list')
}

// 商品SKU列表
export const getSkuList = (prodId) => {
  return http.get('/prod/sku/list', { prodId })
}
