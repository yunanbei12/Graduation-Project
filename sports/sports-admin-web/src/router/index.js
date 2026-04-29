import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('../views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading' }
      },
      {
        path: 'course/category',
        name: 'CourseCategory',
        component: () => import('../views/course/category.vue'),
        meta: { title: '课程分类', icon: 'Menu' }
      },
      {
        path: 'course/location',
        name: 'CourseLocation',
        component: () => import('../views/course/location.vue'),
        meta: { title: '上课地点', icon: 'Grid' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('../views/course/schedule.vue'),
        meta: { title: '排课管理', icon: 'Calendar' }
      },
      {
        path: 'schedule/board',
        name: 'ScheduleBoard',
        component: () => import('../views/course/coach-board.vue'),
        meta: { title: '教练排课看板', icon: 'Calendar' }
      },
      {
        path: 'course/checkin',
        name: 'Checkin',
        component: () => import('../views/course/checkin.vue'),
        meta: { title: '销课管理', icon: 'EditPen' }
      },
      {
        path: 'coach',
        name: 'Coach',
        component: () => import('../views/coach/index.vue'),
        meta: { title: '教练管理', icon: 'User' }
      },
      {
        path: 'prod',
        name: 'Prod',
        component: () => import('../views/prod/index.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'prod/category',
        name: 'ProdCategory',
        component: () => import('../views/prod/category.vue'),
        meta: { title: '商品分类', icon: 'Grid' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('../views/order/index.vue'),
        meta: { title: '课程订单', icon: 'List' }
      },
      {
        path: 'order/product',
        name: 'OrderProduct',
        component: () => import('../views/order/product.vue'),
        meta: { title: '商品订单', icon: 'ShoppingCart' }
      },
      {
        path: 'marketing/banner',
        name: 'MarketingBanner',
        component: () => import('../views/marketing/banner.vue'),
        meta: { title: '轮播图管理', icon: 'Picture' }
      },
      {
        path: 'marketing/coupon',
        name: 'MarketingCoupon',
        component: () => import('../views/marketing/coupon.vue'),
        meta: { title: '优惠券管理', icon: 'Ticket' }
      },
      {
        path: 'finance/income',
        name: 'FinanceIncome',
        component: () => import('../views/finance/income.vue'),
        meta: { title: '收入统计', icon: 'Money' }
      },
      {
        path: 'finance/settlement',
        name: 'FinanceSettlement',
        component: () => import('../views/finance/settlement.vue'),
        meta: { title: '教练结算', icon: 'Wallet' }
      },
      {
        path: 'ai/session',
        name: 'AiSession',
        component: () => import('../views/ai/session.vue'),
        meta: { title: 'AI会话管理', icon: 'ChatLineRound' }
      },
      {
        path: 'ai/knowledge',
        name: 'AiKnowledge',
        component: () => import('../views/ai/knowledge.vue'),
        meta: { title: 'AI知识库', icon: 'Document' }
      },
      {
        path: 'ai/stats',
        name: 'AiStats',
        component: () => import('../views/ai/stats.vue'),
        meta: { title: 'AI客服统计', icon: 'DataAnalysis' }
      },
      {
        path: 'recommend/stats',
        name: 'RecommendStats',
        component: () => import('../views/recommend/stats.vue'),
        meta: { title: '推荐统计', icon: 'TrendCharts' }
      },
      {
        path: 'system/user',
        name: 'SysUser',
        component: () => import('../views/system/user.vue'),
        meta: { title: '管理员管理', icon: 'Avatar' }
      },
      {
        path: 'system/role',
        name: 'SysRole',
        component: () => import('../views/system/role.vue'),
        meta: { title: '角色管理', icon: 'Stamp' }
      },
      {
        path: 'system/menu',
        name: 'SysMenu',
        component: () => import('../views/system/menu.vue'),
        meta: { title: '菜单管理', icon: 'Expand' }
      },
      {
        path: 'system/members',
        name: 'Members',
        component: () => import('../views/system/members.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from) => {
  document.title = to.meta.title ? `${to.meta.title} - KINETIC` : 'KINETIC'
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
})

export default router
