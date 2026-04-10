<template>
  <div class="login-page">
    <section class="hero-panel">
      <p class="eyebrow">Week 2 Admin Delivery</p>
      <h1>体育教培机构管理系统</h1>
      <p class="hero-text">
        先把后台登录、课程、教练、排期跑通，再逐步补齐订单、拼团、退款和财务模块。
      </p>

      <div class="hero-grid">
        <article v-for="item in highlights" :key="item.title" class="hero-card">
          <strong>{{ item.title }}</strong>
          <span>{{ item.desc }}</span>
        </article>
      </div>
    </section>

    <section class="form-panel">
      <div class="form-head">
        <p>后台登录</p>
        <h2>进入可视化验收后台</h2>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button class="login-btn" type="primary" size="large" :loading="submitting" @click="handleLogin">
          登录后台
        </el-button>
      </el-form>

      <div class="tips">
        <span>默认账号：admin</span>
        <span>默认密码：123456</span>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { fetchAdminProfile, login } from "@/api/admin";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const submitting = ref(false);

const form = reactive({
  username: "admin",
  password: "123456"
});

const rules: FormRules<typeof form> = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
};

const highlights = [
  { title: "课程管理", desc: "支持新增、编辑、上下架课程" },
  { title: "教练管理", desc: "支持教练信息维护与工作量查看" },
  { title: "排期管理", desc: "支持排课、查看、取消排期" }
];

async function handleLogin() {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }

  submitting.value = true;
  try {
    const loginResult = await login(form);
    authStore.setToken(loginResult.token);
    const profile = await fetchAdminProfile();
    authStore.setProfile(profile);
    ElMessage.success("登录成功");
    router.push("/dashboard");
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 28px;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
  background:
    radial-gradient(circle at top, rgba(241, 196, 15, 0.25), transparent 25%),
    linear-gradient(135deg, #09203f 0%, #537895 52%, #f5f1e8 52%, #fbfaf6 100%);
}

.hero-panel,
.form-panel {
  border-radius: 32px;
  padding: 40px;
  backdrop-filter: blur(18px);
}

.hero-panel {
  color: #fff;
  background: rgba(8, 19, 29, 0.72);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.eyebrow {
  margin: 0 0 14px;
  color: #f5d56b;
  font-size: 13px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-panel h1 {
  margin: 0;
  font-size: 48px;
  line-height: 1.1;
}

.hero-text {
  max-width: 560px;
  margin: 18px 0 0;
  color: #cdd8df;
  line-height: 1.8;
  font-size: 16px;
}

.hero-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 32px;
}

.hero-card {
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero-card strong {
  font-size: 18px;
}

.hero-card span {
  color: #c1d4df;
  line-height: 1.7;
  font-size: 14px;
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 28px 70px rgba(16, 37, 53, 0.12);
}

.form-head p {
  margin: 0;
  color: #1aa18d;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  font-size: 12px;
}

.form-head h2 {
  margin: 12px 0 28px;
  color: #173042;
  font-size: 34px;
}

.login-btn {
  width: 100%;
  margin-top: 12px;
  height: 48px;
  border: none;
  background: linear-gradient(135deg, #1aa18d, #2f73ec);
}

.tips {
  margin-top: 22px;
  display: flex;
  justify-content: space-between;
  color: #67808f;
  font-size: 13px;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .hero-grid {
    grid-template-columns: 1fr;
  }

  .hero-panel,
  .form-panel {
    padding: 28px;
  }

  .hero-panel h1 {
    font-size: 36px;
  }
}
</style>
