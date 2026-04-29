import http from '../utils/http'

// 微信登录（传 code 让后端换 openId，或直接传 openId）
export const wxLogin = (code, openId, nickName, avatarUrl) => {
  const data = { code }
  if (openId) data.openId = openId
  if (nickName) data.nickName = nickName
  if (avatarUrl) data.avatarUrl = avatarUrl
  return http.post('/auth/wxLogin', data)
}

// 手机号密码注册（需验证码）
export const register = (phone, passWord, code) => {
  return http.post('/auth/register', { phone, passWord, code })
}

// 手机号密码登录
export const accountLogin = (phone, passWord) => {
  return http.post('/auth/login', { phone, passWord })
}

// 短信验证码登录（confirm=true 表示用户确认对未注册手机号进行注册）
export const smsLogin = (phone, code, confirm = false) => {
  return http.post('/auth/smsLogin', { phone, code, confirmRegister: confirm ? 'true' : '' })
}

// 发送短信验证码
export const sendSmsCode = (phone, type = 0) => {
  return http.post('/sms/send', { phone, type: String(type) })
}

// 绑定手机号
export const bindPhone = (phone, code) => {
  return http.post('/profile/bind-phone', { phone, code })
}

// 获取用户信息
export const getUserInfo = () => {
  return http.get('/profile/info')
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return http.put('/profile/update', data)
}

// 修改密码
export const updatePassword = (oldPassword, newPassword) => {
  return http.put('/profile/password', { oldPassword, newPassword })
}
