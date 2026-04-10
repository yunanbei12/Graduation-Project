import { computed, ref } from "vue";
import { defineStore } from "pinia";
import type { AdminProfile } from "@/types/admin";

export const useAuthStore = defineStore("auth", () => {
  const token = ref(localStorage.getItem("admin_token") || "");
  const profile = ref<AdminProfile | null>(null);

  const isAuthenticated = computed(() => Boolean(token.value));

  function setToken(value: string) {
    token.value = value;
    localStorage.setItem("admin_token", value);
  }

  function setProfile(value: AdminProfile | null) {
    profile.value = value;
  }

  function clearSession() {
    token.value = "";
    profile.value = null;
    localStorage.removeItem("admin_token");
  }

  return {
    token,
    profile,
    isAuthenticated,
    setToken,
    setProfile,
    clearSession
  };
});
