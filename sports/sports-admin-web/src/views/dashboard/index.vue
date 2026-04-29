<template>
  <div class="dashboard">
    <!-- 核心指标卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
              <div class="stat-sub" v-if="item.sub">{{ item.sub }}</div>
            </div>
            <el-icon class="stat-icon" :style="{ color: item.color }">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <!-- 近7天订单趋势 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span>近 7 天订单趋势</span>
          </template>
          <div class="chart-wrap">
            <div class="bar-chart">
              <div
                v-for="item in dailyOrders"
                :key="item.date"
                class="bar-item"
              >
                <div class="bar-value">{{ item.count }}</div>
                <div
                  class="bar"
                  :style="{ height: barHeight(item.count) + 'px' }"
                ></div>
                <div class="bar-label">{{ item.date }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 + 待办 -->
      <el-col :span="10">
        <el-card shadow="hover" style="margin-bottom: 20px;">
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/course')">管理课程</el-button>
            <el-button type="success" @click="$router.push('/coach')">管理教练</el-button>
            <el-button type="info" @click="$router.push('/order')">查看订单</el-button>
          </div>
        </el-card>

        <el-card shadow="hover">
          <template #header>待办事项</template>
          <div class="todo-list">
            <div class="todo-item" @click="$router.push('/order')">
              <span class="todo-label">待处理退款</span>
              <el-badge :value="stats.pendingRefunds" :max="99" type="danger" />
            </div>
            <div class="todo-item" @click="$router.push('/finance')">
              <span class="todo-label">待结算消课</span>
              <el-badge :value="stats.unsettledCheckins" :max="999" type="warning" />
            </div>
            <div class="todo-item">
              <span class="todo-label">本月消课次数</span>
              <span class="todo-value">{{ stats.monthCheckins }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import http from '../../utils/http'

const stats = ref({
  totalUsers: '-',
  totalCourses: '-',
  totalCoaches: '-',
  todayOrders: '-',
  todayIncome: '-',
  pendingRefunds: 0,
  monthCheckins: '-',
  unsettledCheckins: 0
})

const dailyOrders = ref([])

const statCards = computed(() => [
  {
    title: '注册用户',
    value: stats.value.totalUsers,
    icon: 'User',
    color: '#409eff'
  },
  {
    title: '上架课程',
    value: stats.value.totalCourses,
    icon: 'Reading',
    color: '#67c23a'
  },
  {
    title: '今日订单',
    value: stats.value.todayOrders,
    icon: 'List',
    color: '#e6a23c'
  },
  {
    title: '今日收入',
    value: stats.value.todayIncome !== undefined && stats.value.todayIncome !== '-'
      ? '¥' + Number(stats.value.todayIncome).toFixed(2)
      : '-',
    icon: 'Money',
    color: '#f56c6c',
    sub: stats.value.totalCoaches !== undefined && stats.value.totalCoaches !== '-'
      ? `在职教练 ${stats.value.totalCoaches} 人`
      : ''
  }
])

const maxDailyCount = computed(() => {
  const counts = dailyOrders.value.map(d => d.count)
  return Math.max(...counts, 1)
})

function barHeight(count) {
  return Math.max(4, Math.round((count / maxDailyCount.value) * 120))
}

async function loadStats() {
  try {
    const res = await http.get('/dashboard/stats')
    stats.value = res.data
    dailyOrders.value = res.data.dailyOrders || []
  } catch (e) {
    console.error('加载仪表盘数据失败', e)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stat-card { cursor: default; }
.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-title { font-size: 13px; color: #999; margin-bottom: 6px; }
.stat-value { font-size: 30px; font-weight: 700; }
.stat-sub { font-size: 12px; color: #bbb; margin-top: 4px; }
.stat-icon { font-size: 44px; opacity: 0.15; }

/* 柱状图 */
.chart-wrap { padding: 8px 0 0; }
.bar-chart {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  height: 160px;
  padding: 0 8px;
}
.bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.bar-value { font-size: 12px; color: #666; }
.bar {
  width: 100%;
  background: linear-gradient(180deg, #409eff, #79bbff);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  transition: height 0.3s;
}
.bar-label { font-size: 11px; color: #999; }

/* 快捷操作 */
.quick-actions { display: flex; gap: 10px; flex-wrap: wrap; }

/* 待办 */
.todo-list { display: flex; flex-direction: column; gap: 16px; }
.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 4px 0;
}
.todo-item:hover .todo-label { color: #409eff; }
.todo-label { font-size: 14px; color: #333; }
.todo-value { font-size: 14px; color: #666; font-weight: 600; }
</style>
