<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>近 7 天行为趋势</template>
          <el-table :data="trend" stripe>
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column prop="viewCount" label="详情浏览" width="100" />
            <el-table-column prop="clickCount" label="推荐点击" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>热门推荐位</template>
          <el-table :data="sourceSections" stripe>
            <el-table-column prop="sourceSection" label="区块编码" min-width="180" />
            <el-table-column prop="count" label="事件数" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>点击最多的内容</template>
          <el-table :data="topClickedItems" stripe>
            <el-table-column prop="itemTypeText" label="类型" width="80" />
            <el-table-column prop="itemName" label="名称" min-width="180" />
            <el-table-column prop="count" label="点击数" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>浏览最多的内容</template>
          <el-table :data="topViewedItems" stripe>
            <el-table-column prop="itemTypeText" label="类型" width="80" />
            <el-table-column prop="itemName" label="名称" min-width="180" />
            <el-table-column prop="count" label="浏览数" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px;">
      <template #header>最近行为明细</template>
      <div class="filter-row">
        <el-select v-model="query.itemType" clearable placeholder="内容类型" style="width: 140px;">
          <el-option label="课程" :value="1" />
          <el-option label="商品" :value="2" />
        </el-select>
        <el-select v-model="query.behaviorType" clearable placeholder="行为类型" style="width: 160px;">
          <el-option label="详情浏览" value="view_detail" />
          <el-option label="推荐点击" value="recommend_click" />
        </el-select>
        <el-input v-model="query.sourceSection" clearable placeholder="推荐区块编码" style="width: 200px;" />
        <el-button type="primary" @click="loadBehaviorList">筛选</el-button>
      </div>
      <el-table :data="behaviorList" stripe style="margin-top: 12px;">
        <el-table-column prop="createTime" label="时间" min-width="170" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="itemTypeText" label="类型" width="80" />
        <el-table-column prop="itemName" label="内容名称" min-width="180" />
        <el-table-column prop="behaviorTypeText" label="行为" width="100" />
        <el-table-column prop="sourcePage" label="页面" width="160" />
        <el-table-column prop="sourceSection" label="区块" width="160" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          :total="total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getRecommendBehaviorPage, getRecommendStatsSummary } from '../../api/recommend'

const summary = ref({})
const behaviorList = ref([])
const total = ref(0)
const query = ref({
  pageNum: 1,
  pageSize: 10,
  itemType: undefined,
  behaviorType: '',
  sourceSection: ''
})

const cards = computed(() => [
  { label: '总行为数', value: summary.value.totalBehaviors || 0 },
  { label: '详情浏览', value: summary.value.detailViews || 0 },
  { label: '推荐点击', value: summary.value.recommendClicks || 0 },
  { label: '活跃用户', value: summary.value.uniqueUsers || 0 },
  { label: '课程浏览', value: summary.value.courseViews || 0 },
  { label: '商品浏览', value: summary.value.prodViews || 0 },
  { label: '课程点击', value: summary.value.courseClicks || 0 },
  { label: '商品点击', value: summary.value.prodClicks || 0 }
])

const trend = computed(() => summary.value.trend || [])
const topClickedItems = computed(() => summary.value.topClickedItems || [])
const topViewedItems = computed(() => summary.value.topViewedItems || [])
const sourceSections = computed(() => summary.value.sourceSections || [])

const loadSummary = async () => {
  const res = await getRecommendStatsSummary()
  summary.value = res.data || {}
}

const loadBehaviorList = async () => {
  const params = { ...query.value }
  const res = await getRecommendBehaviorPage(params)
  behaviorList.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const onPageChange = (page) => {
  query.value.pageNum = page
  loadBehaviorList()
}

onMounted(async () => {
  await loadSummary()
  await loadBehaviorList()
})
</script>

<style scoped>
.stat-card { min-height: 120px; display: flex; flex-direction: column; justify-content: center; }
.stat-label { color: #888; font-size: 14px; }
.stat-value { font-size: 28px; font-weight: 700; margin-top: 10px; }
.filter-row { display: flex; gap: 12px; flex-wrap: wrap; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
