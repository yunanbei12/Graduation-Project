<template>
  <div class="dashboard-page">
    <section class="hero">
      <div>
        <p class="hero-kicker">验收模式</p>
        <h3>第 2 周后台管理闭环已进入可视化阶段</h3>
        <p class="hero-desc">
          你现在可以不再手工敲接口，直接在左侧菜单依次体验课程、教练、排期管理，并把页面现象反馈给我。
        </p>
      </div>
      <div class="hero-badge">Week 2</div>
    </section>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.title" class="stat-card">
        <span>{{ item.title }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.desc }}</p>
      </article>
    </section>

    <section class="roadmap">
      <div class="section-head">
        <h4>当前开发进度</h4>
        <p>后续我会继续按开发周期文档往前推进，你主要负责页面验收与反馈。</p>
      </div>

      <div class="roadmap-grid">
        <article v-for="item in roadmap" :key="item.title" class="roadmap-card">
          <span class="step">{{ item.step }}</span>
          <h5>{{ item.title }}</h5>
          <p>{{ item.desc }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const metrics = [
  { title: "后台认证", value: "已接通", desc: "登录、获取当前用户、退出登录已可用" },
  { title: "课程管理", value: "可验收", desc: "支持新增、编辑、上下架与分页查询" },
  { title: "教练管理", value: "可验收", desc: "支持教练维护与基础工作量统计" },
  { title: "排期管理", value: "可验收", desc: "支持排期创建、分页查看、取消" }
];

const roadmap = [
  { step: "01", title: "后台管理基础", desc: "先把课程、教练、排期这些最小后台闭环跑通。" },
  { step: "02", title: "小程序基础页面", desc: "随后转入小程序登录、课程列表、课程详情。" },
  { step: "03", title: "交易核心链路", desc: "再继续订单、支付、拼团、退款与销课逻辑。" }
];
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero {
  padding: 28px 30px;
  border-radius: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #0f766e, #2f73ec 72%, #f4b942 140%);
  color: #fff;
}

.hero-kicker {
  margin: 0;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 0.16em;
  font-size: 12px;
  text-transform: uppercase;
}

.hero h3 {
  margin: 10px 0 8px;
  font-size: 30px;
}

.hero-desc {
  margin: 0;
  max-width: 680px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.86);
}

.hero-badge {
  min-width: 110px;
  padding: 20px 18px;
  border-radius: 22px;
  text-align: center;
  background: rgba(255, 255, 255, 0.16);
  font-size: 24px;
  font-weight: 700;
}

.stat-grid,
.roadmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 18px;
}

.stat-card,
.roadmap-card {
  padding: 22px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 44px rgba(17, 44, 36, 0.08);
}

.stat-card span,
.step {
  color: #1aa18d;
  font-size: 13px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.stat-card strong {
  display: block;
  margin-top: 12px;
  color: #173042;
  font-size: 28px;
}

.stat-card p,
.roadmap-card p {
  margin: 12px 0 0;
  color: #617885;
  line-height: 1.7;
}

.roadmap {
  padding: 26px;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.62);
}

.section-head h4 {
  margin: 0;
  color: #173042;
  font-size: 22px;
}

.section-head p {
  margin: 10px 0 22px;
  color: #617885;
}

.roadmap-card h5 {
  margin: 12px 0 0;
  color: #173042;
  font-size: 20px;
}
</style>
