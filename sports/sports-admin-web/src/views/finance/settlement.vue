<template>
  <div class="page-container">
    <!-- 发起结算 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <template #header>
        <span>发起教练结算</span>
      </template>
      <el-form :model="settleForm" inline>
        <el-form-item label="教练ID">
          <el-input-number v-model="settleForm.coachId" :min="1" style="width: 120px" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="settleForm.periodStart" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="settleForm.periodEnd" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSettle" :loading="settling">发起结算</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待结算汇总 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <template #header>
        <span>待结算消课记录</span>
      </template>
      <el-table :data="unsettledList" v-loading="unsettledLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="coachId" label="教练ID" width="100" />
        <el-table-column prop="checkinType" label="类型" width="80">
          <template #default="{ row }">{{ row.checkinType === 1 ? '私教' : '团课' }}</template>
        </el-table-column>
        <el-table-column prop="lessonPrice" label="课时费" width="100">
          <template #default="{ row }">¥{{ row.lessonPrice || 0 }}</template>
        </el-table-column>
        <el-table-column prop="coachRatio" label="分成比例" width="100">
          <template #default="{ row }">{{ row.coachRatio ? (row.coachRatio * 100) + '%' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="coachAmount" label="教练金额" width="100">
          <template #default="{ row }">¥{{ row.coachAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="checkinTime" label="上课时间" min-width="160" />
      </el-table>
    </el-card>

    <!-- 结算记录 -->
    <el-card shadow="never">
      <template #header>
        <span>结算记录</span>
      </template>
      <el-table :data="settlementList" v-loading="settlementLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="coachId" label="教练ID" width="100" />
        <el-table-column prop="totalLessons" label="课时数" width="80" />
        <el-table-column prop="totalAmount" label="结算金额" width="120">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="periodStart" label="周期开始" width="120" />
        <el-table-column prop="periodEnd" label="周期结束" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">{{ row.status === 0 ? '待确认' : '已结算' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="primary" @click="handleConfirm(row)">确认结算</el-button>
            <span v-else class="text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="settlementTotal"
          layout="total, prev, pager, next"
          @current-change="loadSettlements"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import http from '../../utils/http'
import { ElMessage } from 'element-plus'

const settling = ref(false)
const unsettledLoading = ref(false)
const settlementLoading = ref(false)
const unsettledList = ref([])
const settlementList = ref([])
const settlementTotal = ref(0)

const settleForm = reactive({
  coachId: null,
  periodStart: '',
  periodEnd: ''
})

const query = reactive({ pageNum: 1, pageSize: 10 })

const loadUnsettled = async () => {
  unsettledLoading.value = true
  try {
    const res = await http.get('/finance/unsettled/summary')
    unsettledList.value = res.data || []
  } finally {
    unsettledLoading.value = false
  }
}

const loadSettlements = async () => {
  settlementLoading.value = true
  try {
    const res = await http.get('/finance/settlement/list', { params: query })
    settlementList.value = res.data.records || []
    settlementTotal.value = res.data.total || 0
  } finally {
    settlementLoading.value = false
  }
}

const handleSettle = async () => {
  if (!settleForm.coachId || !settleForm.periodStart || !settleForm.periodEnd) {
    ElMessage.warning('请填写完整结算信息')
    return
  }
  settling.value = true
  try {
    await http.post('/finance/settle', settleForm)
    ElMessage.success('结算发起成功')
    loadUnsettled()
    loadSettlements()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '结算失败')
  } finally {
    settling.value = false
  }
}

const handleConfirm = async (row) => {
  try {
    await http.put(`/finance/settle/confirm/${row.id}`)
    ElMessage.success('已确认结算')
    loadSettlements()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadUnsettled()
  loadSettlements()
})
</script>

<style scoped>
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.text-muted { color: #999; font-size: 12px; }
</style>
