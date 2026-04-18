import http from '../utils/http'

// 商品列表
export const getProdList = (params) => http.get('/prod/list', { params })

// 商品详情
export const getProdDetail = (id) => http.get(`/prod/detail/${id}`)

// 新增商品
export const addProd = (data) => http.post('/prod', data)

// 修改商品
export const updateProd = (data) => http.put('/prod', data)

// 删除商品
export const deleteProd = (id) => http.delete(`/prod/${id}`)

// 商品分类列表
export const getProdCategoryList = () => http.get('/prod/category/list')

// 新增商品分类
export const addProdCategory = (data) => http.post('/prod/category', data)

// 修改商品分类
export const updateProdCategory = (data) => http.put('/prod/category', data)

// 删除商品分类
export const deleteProdCategory = (id) => http.delete(`/prod/category/${id}`)

// SKU列表
export const getSkuList = (prodId) => http.get('/sku/list', { params: { prodId } })

// 新增SKU
export const addSku = (data) => http.post('/sku', data)

// 修改SKU
export const updateSku = (data) => http.put('/sku', data)

// 删除SKU
export const deleteSku = (id) => http.delete(`/sku/${id}`)
