import http from '../utils/http'

// 登录
export const login = (data) => http.post('/auth/login', data)

// 获取当前用户信息
export const getInfo = () => http.get('/auth/info')

// 退出登录
export const logout = () => http.post('/auth/logout')

// 修改密码
export const updatePassword = (data) => http.put('/auth/password', data)
