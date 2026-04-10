import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import AdminLayout from "@/layouts/AdminLayout.vue";
import DashboardView from "@/views/DashboardView.vue";
import LoginView from "@/views/LoginView.vue";
import CourseManageView from "@/views/CourseManageView.vue";
import CoachManageView from "@/views/CoachManageView.vue";
import ScheduleManageView from "@/views/ScheduleManageView.vue";
import { useAuthStore } from "@/stores/auth";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "login",
    component: LoginView
  },
  {
    path: "/",
    component: AdminLayout,
    redirect: "/dashboard",
    children: [
      {
        path: "/dashboard",
        name: "dashboard",
        component: DashboardView
      },
      {
        path: "/courses",
        name: "courses",
        component: CourseManageView
      },
      {
        path: "/coaches",
        name: "coaches",
        component: CoachManageView
      },
      {
        path: "/schedules",
        name: "schedules",
        component: ScheduleManageView
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.path === "/login") {
    if (authStore.isAuthenticated) {
      return "/dashboard";
    }
    return true;
  }

  if (!authStore.isAuthenticated) {
    return "/login";
  }

  return true;
});

export default router;
