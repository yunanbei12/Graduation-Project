<template>
  <div class="page-container">
    <el-row :gutter="16">
      <!-- 收入统计卡片 -->
      <el-col :span="12">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">课程订单总收入</div>
          <div class="stat-value">¥{{ courseIncome }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">课程累计收入</div>
          <div class="stat-value total">¥{{ totalIncome }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px;">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="loadData">
            <el-option label="已支付" :value="2" />
            <el-option label="待排课" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="退款中" :value="6" />
            <el-option label="已退款" :value="7" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="loadData"
          />
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNumber" label="订单号" width="200" />
        <el-table-column prop="orderType" label="类型" width="100">
          <template #default><el-tag type="primary">课程</el-tag></template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付金额" width="120">
          <template #default="{ row }">¥{{ row.actualAmount }}</template>
        </el-table-column>
        <el-table-column prop="couponAmount" label="优惠金额" width="120">
          <template #default="{ row }">¥{{ row.couponAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 4 ? 'success' : 'primary'">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" min-width="160" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import http from '../../utils/http'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref(null)
const courseIncome = ref('0.00')

// status 不限制，展示所有已支付订单（状态 2=已支付 3=待排课/待发货 4=已完成）
const query = reactive({ pageNum: 1, pageSize: 10, orderType: 1, status: null })

const totalIncome = computed(() => {
  return parseFloat(courseIncome.value).toFixed(2)
})

const statusMap = { 1: '待付款', 2: '已支付', 3: '待排课', 4: '已完成', 5: '已取消', 6: '退款中', 7: '已退款', 8: '退款驳回' }
const getStatusLabel = (status) => statusMap[status] || '未知'

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...query }
    const res = await http.get('/order/list', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadIncomeStats = async () => {
  try {
    // 课程收入 - 统计已完成的订单（status=4，私教课包全部消课后自动完成）
    const allCourseRes = await http.get('/order/list', { 
      params: { pageNum: 1, pageSize: 999, orderType: 1, status: 4 } 
    })
    const courseOrders = allCourseRes.data.records || []
    courseIncome.value = courseOrders.reduce((sum, o) => sum + parseFloat(o.actualAmount || 0), 0).toFixed(2)
  } catch (e) {
    console.error('加载收入统计失败', e)
  }
}

onMounted(() => {
  loadData()
  loadIncomeStats()
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 20px 0; }
.stat-label { font-size: 14px; color: #999; margin-bottom: 8px; }
.stat-value { font-size: 32px; font-weight: 700; color: #409eff; }
.stat-value.total { color: #e6a23c; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; align-items: center; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
