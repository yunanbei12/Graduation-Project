import config from './config'

const BASE_URL = config.BASE_URL

// 获取本地存储的token
function getToken() {
  return uni.getStorageSync('token') || ''
}

// 通用请求方法
function request(options) {
  // 记录请求时是否已有token
  options._hadToken = !!getToken()
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': getToken()
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data
          if (data.code === 200) {
            resolve(data.data)
          } else if (data.code === 401) {
            // token过期，清除登录信息
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            // 仅在已有token时提示（避免未登录状态频繁弹toast）
            const hadToken = !!options._hadToken
            if (hadToken) {
              uni.showToast({ title: '请先登录', icon: 'none' })
              setTimeout(() => {
                uni.reLaunch({ url: '/pages/profile/profile' })
              }, 1500)
            }
            reject(data)
          } else {
            // 不在这里显示toast，让业务代码自行处理
            reject(data)
          }
        } else if (res.statusCode === 401) {
          // HTTP 401（未携带token或token无效）
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          // 未登录时静默处理，不弹toast
          reject(res)
        } else if (res.statusCode === 500) {
          uni.showToast({ title: '服务器异常', icon: 'none' })
          reject(res)
        } else {
          uni.showToast({ title: '网络错误', icon: 'none' })
          reject(res)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

// GET请求
function get(url, data) {
  return request({ url, method: 'GET', data })
}

// POST请求
function post(url, data) {
  return request({ url, method: 'POST', data })
}

// PUT请求
function put(url, data) {
  return request({ url, method: 'PUT', data })
}

// DELETE请求
function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

export default {
  request,
  get,
  post,
  put,
  del
}
