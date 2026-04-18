/**
 * 登录校验工具
 */

// 需要登录的页面列表
const AUTH_PAGES = [
  'pages/cart/cart',
  'pages/profile/my-packages',
  'pages/profile/package-detail',
  'pages/profile/my-coupons',
  'pages/profile/my-checkins',
  'pages/profile/edit-profile',
  'pages/profile/bind-phone',
  'pages/order/confirm-product',
  'pages/order/confirm-private',
  'pages/order/confirm-group',
  'pages/order/my-orders-course',
  'pages/order/my-orders-product',
  'pages/order/order-detail-product',
  'pages/order/refund'
]

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  return !!uni.getStorageSync('token')
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  const info = uni.getStorageSync('userInfo')
  return info ? JSON.parse(info) : null
}

/**
 * 需要登录校验的页面
 */
export function needAuth(pagePath) {
  // 去掉开头的 /
  const path = pagePath.replace(/^\//, '')
  return AUTH_PAGES.some(p => path.startsWith(p))
}

/**
 * 检查登录状态，未登录则跳转登录页
 * @param {string} fromPage 来源页面路径（可选，用于登录后返回）
 * @returns {boolean} 是否已登录
 */
export function checkLogin(fromPage) {
  if (isLoggedIn()) {
    return true
  }
  
  // 未登录，跳转到登录页
  const currentPage = fromPage || getCurrentPagePath()
  uni.navigateTo({
    url: `/pages/accountLogin/accountLogin?redirect=${encodeURIComponent(currentPage)}`
  })
  return false
}

/**
 * 获取当前页面路径（含参数）
 */
export function getCurrentPagePath() {
  const pages = getCurrentPages()
  if (pages.length === 0) return ''
  const current = pages[pages.length - 1]
  const path = '/' + current.route
  // 如果有参数，拼接参数
  if (current.options && Object.keys(current.options).length > 0) {
    const query = Object.entries(current.options)
      .map(([key, val]) => `${key}=${val}`)
      .join('&')
    return `${path}?${query}`
  }
  return path
}

/**
 * 登录成功后跳转回原页面
 */
export function navigateBackAfterLogin(redirectPath) {
  if (redirectPath) {
    const path = decodeURIComponent(redirectPath)
    // 判断是否是 tabBar 页面
    const tabBarPages = ['pages/index/index', 'pages/course/course', 'pages/mall/mall', 'pages/profile/profile']
    const isTabBar = tabBarPages.some(p => path.startsWith(p))
    
    if (isTabBar) {
      uni.switchTab({ url: path.split('?')[0] })
    } else {
      uni.redirectTo({ url: path })
    }
  } else {
    // 没有来源页面，跳转到个人中心
    uni.switchTab({ url: '/pages/profile/profile' })
  }
}

export default {
  isLoggedIn,
  getUserInfo,
  needAuth,
  checkLogin,
  getCurrentPagePath,
  navigateBackAfterLogin
}
