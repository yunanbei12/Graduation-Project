<template>
  <div class="layout-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-kicker">SportEdu</span>
        <h1>机构管理后台</h1>
        <p>按任务周期逐步交付的可视化工作台</p>
      </div>

      <el-menu
        :default-active="route.path"
        class="menu"
        background-color="transparent"
        text-color="#ccdae3"
        active-text-color="#ffffff"
        @select="handleSelect"
      >
        <el-menu-item index="/dashboard">首页概览</el-menu-item>
        <el-menu-item index="/courses">课程管理</el-menu-item>
        <el-menu-item index="/coaches">教练管理</el-menu-item>
        <el-menu-item index="/schedules">排期管理</el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-card">
          <strong>{{ authStore.profile?.realName || "系统管理员" }}</strong>
          <span>{{ authStore.profile?.roles?.join(" / ") || "SUPER_ADMIN" }}</span>
        </div>
      </div>
    </aside>

    <div class="main-shell">
      <header class="topbar">
        <div>
          <p class="topbar-kicker">后台验收入口</p>
          <h2>{{ currentTitle }}</h2>
        </div>
        <div class="topbar-actions">
          <span class="welcome">{{ authStore.profile?.username || "admin" }}</span>
          <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="content-panel">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { logout } from "@/api/admin";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const titleMap: Record<string, string> = {
  "/dashboard": "首页概览",
  "/courses": "课程管理",
  "/coaches": "教练管理",
  "/schedules": "排期管理"
};

const currentTitle = computed(() => titleMap[route.path] ?? "后台管理");

function handleSelect(path: string) {
  router.push(path);
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm("确认退出当前后台账号吗？", "退出确认", {
      type: "warning"
    });
    await logout();
  } catch {
    return;
  }

  authStore.clearSession();
  ElMessage.success("已退出登录");
  router.push("/login");
}
</script>

<style scoped>
.layout-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  background:
    radial-gradient(circle at top left, rgba(26, 188, 156, 0.18), transparent 26%),
    linear-gradient(135deg, #08131d 0%, #122536 52%, #e8f0eb 52%, #f6f7f4 100%);
}

.sidebar {
  padding: 28px 22px;
  display: flex;
  flex-direction: column;
  background: rgba(7, 18, 28, 0.88);
  backdrop-filter: blur(18px);
}

.brand {
  padding: 10px 12px 24px;
  color: #f3f8fb;
}

.brand-kicker {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #86f2d0;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.brand h1 {
  margin: 16px 0 8px;
  font-size: 28px;
  line-height: 1.2;
}

.brand p {
  margin: 0;
  color: #8ea7b8;
  line-height: 1.7;
  font-size: 14px;
}

.menu {
  border-right: none;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 48px;
  margin-bottom: 8px;
  border-radius: 14px;
  font-size: 15px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #1aa18d, #2b6de8);
}

.sidebar-footer {
  padding: 12px;
}

.user-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06);
  color: #f3f8fb;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user-card span {
  color: #9ec0ce;
  font-size: 13px;
}

.main-shell {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.topbar {
  padding: 22px 28px;
  border-radius: 28px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 24px 60px rgba(12, 37, 29, 0.08);
  backdrop-filter: blur(18px);
}

.topbar-kicker {
  margin: 0;
  color: #1aa18d;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.topbar h2 {
  margin: 8px 0 0;
  color: #12303f;
  font-size: 28px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 14px;
}

.welcome {
  color: #46606e;
  font-size: 14px;
}

.content-panel {
  flex: 1;
}

@media (max-width: 960px) {
  .layout-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    padding-bottom: 0;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
