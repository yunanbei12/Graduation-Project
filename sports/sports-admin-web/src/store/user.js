import { defineStore } from 'pinia'
import { getInfo, login, logout } from '../api/auth'
import { getUserMenus } from '../api/system'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null,
    menus: []  // 用户菜单
  }),

  actions: {
    async loginAction(loginData) {
      const res = await login(loginData)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      return res
    },

    async getInfoAction() {
      const res = await getInfo()
      this.userInfo = res.data
      return res
    },

    async getMenusAction() {
      const res = await getUserMenus()
      this.menus = res.data || []
      return res
    },

    async logoutAction() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = null
        this.menus = []
        localStorage.removeItem('token')
      }
    }
  }
})
