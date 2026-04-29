export const DEMO_MODE = {
  hideCommerce: true
}

const hiddenPathPrefixes = [
  '/prod',
  '/order/product',
  '/pages/mall',
  '/pages/cart',
  '/pages/order/confirm-product',
  '/pages/order/order-detail-product',
  '/pages/order/my-orders-product',
  '/pages/address'
]
const hiddenKeywords = ['商城', '商品']

export function isCommercePath(path = '') {
  return hiddenPathPrefixes.some(prefix => path === prefix || path.startsWith(`${prefix}/`) || path.startsWith(`${prefix}?`))
}

export function isCommerceLabel(label = '') {
  return hiddenKeywords.some(keyword => label.includes(keyword))
}

export function isCommerceMenu(menu = {}) {
  return isCommercePath(menu.path || '') || isCommerceLabel(menu.name || '') || isCommerceLabel(menu.title || '')
}

export function filterCommerceMenus(menus = []) {
  if (!DEMO_MODE.hideCommerce) return menus
  return menus.reduce((result, menu) => {
    if (isCommerceMenu(menu)) {
      return result
    }
    const next = { ...menu }
    if (Array.isArray(menu.children) && menu.children.length > 0) {
      next.children = filterCommerceMenus(menu.children)
    }
    result.push(next)
    return result
  }, [])
}
