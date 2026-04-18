<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-show="!isCollapse" class="logo-text">KINETIC</span>
        <span v-show="isCollapse" class="logo-text-short">K</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        background-color="#1d1e2c"
        text-color="#a0a4b8"
        active-text-color="#409eff"
      >
        <!-- 仪表盘（所有人可见） -->
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <!-- 动态菜单 -->
        <template v-for="menu in userStore.menus" :key="menu.id">
          <!-- 目录类型：有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="String(menu.id)">
            <template #title>
              <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item 
              v-for="child in menu.children" 
              :key="child.id" 
              :index="child.path"
            >
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 菜单类型：无子菜单 -->
          <el-menu-item v-else-if="menu.path" :index="menu.path">
            <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><Avatar /></el-icon>
              {{ userStore.userInfo?.nickName || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { 
  Odometer, Avatar, Expand, Fold,
  Reading, User, Goods, List, Present, Money, ChatDotRound, Setting,
  Document, Calendar, Ticket, Wallet, ChatLineSquare, DataLine, Grid
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

// 图标映射
const iconMap = {
  'education': Reading,
  'user': User,
  'shopping': Goods,
  'document': List,
  'ticket': Present,
  'money': Money,
  'chat': ChatDotRound,
  'setting': Setting,
  'list': Document,
  'calendar': Calendar,
  'category': Grid,
  'chart': DataLine,
  'wallet': Wallet,
  'tree': Grid,
  'peoples': User
}

const getIcon = (iconName) => {
  return iconMap[iconName] || Document
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await userStore.logoutAction()
    router.push('/login')
  }
}

onMounted(async () => {
  try {
    if (!userStore.userInfo) {
      await userStore.getInfoAction()
    }
    if (userStore.menus.length === 0) {
      await userStore.getMenusAction()
    }
  } catch (e) {
    // token 失效，跳转登录
    router.push('/login')
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.aside {
  background-color: #1d1e2c;
  overflow-x: hidden;
  transition: width 0.3s;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #409eff;
  letter-spacing: 2px;
}
.logo-text-short {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #333;
}
.main {
  background: #f5f5f5;
  padding: 20px;
}
.el-menu {
  border-right: none;
}
</style>
