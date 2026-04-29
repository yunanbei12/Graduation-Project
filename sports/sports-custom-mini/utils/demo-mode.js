export const DEMO_MODE = {
  hideCommerce: true
}

const hiddenPagePrefixes = [
  '/pages/mall/',
  '/pages/cart/',
  '/pages/order/confirm-product',
  '/pages/order/success-product',
  '/pages/order/order-detail-product',
  '/pages/order/my-orders-product',
  '/pages/address/'
]

export const TAB_BAR_PAGES = ['/pages/index/index', '/pages/course/course', '/pages/profile/profile']

export function isCommercePage(pagePath = '') {
  const normalized = pagePath.startsWith('/') ? pagePath : `/${pagePath}`
  return hiddenPagePrefixes.some(prefix => normalized === prefix || normalized.startsWith(prefix))
}

export function canAccessPage(pagePath = '') {
  if (!DEMO_MODE.hideCommerce) return true
  return !isCommercePage(pagePath)
}

export function filterRecommendActions(actions = []) {
  if (!DEMO_MODE.hideCommerce) return actions
  return actions.filter(action => !isCommercePage(action.route || ''))
}

export function filterRecommendCards(cards = []) {
  if (!DEMO_MODE.hideCommerce) return cards
  return cards.filter(card => !isCommercePage(card.route || ''))
}
