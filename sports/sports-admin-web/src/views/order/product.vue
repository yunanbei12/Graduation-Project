<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.status" placeholder="订单状态" clearable style="width: 140px" @change="loadData">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNumber" label="订单号" width="200" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付" width="100">
          <template #default="{ row }">¥{{ row.actualAmount }}</template>
        </el-table-column>
        <el-table-column prop="couponAmount" label="优惠" width="100">
          <template #default="{ row }">¥{{ row.couponAmount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleStatus(cmd, row)">
              <el-button link type="warning">状态操作</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="2" v-if="row.status === 1">确认支付</el-dropdown-item>
                  <el-dropdown-item command="3" v-if="row.status === 2">发货</el-dropdown-item>
                  <el-dropdown-item command="4" v-if="row.status === 3">完成</el-dropdown-item>
                  <el-dropdown-item command="7" v-if="row.status === 6">同意退款</el-dropdown-item>
                  <el-dropdown-item command="8" v-if="row.status === 6">拒绝退款</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
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

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="订单号">{{ detailData.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="退款前状态">{{ detailData.beforeRefundStatus ? getStatusLabel(detailData.beforeRefundStatus) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">¥{{ detailData.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ detailData.actualAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ detailData.couponAmount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">¥{{ detailData.refundAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="关闭时间">{{ detailData.closeTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ detailData.finishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ detailData.refundReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOrderList, getOrderDetail, updateOrderStatus } from '../../api/order'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detailData = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, orderType: 2, status: null })

const statusOptions = [
  { label: '待付款', value: 1 },
  { label: '待发货', value: 2 },
  { label: '待收货', value: 3 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 },
  { label: '退款中', value: 6 },
  { label: '已退款', value: 7 }
]

const statusLabelMap = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款',
  8: '退款驳回(历史)'
}

const getStatusLabel = (status) => statusLabelMap[status] || '未知'
const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'primary', 4: 'success', 5: 'info', 6: 'warning', 7: 'success', 8: 'info' }
  return map[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOrderList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleDetail = async (row) => {
  try {
    const res = await getOrderDetail(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleStatus = async (cmd, row) => {
  await updateOrderStatus({ id: row.id, status: parseInt(cmd) })
  ElMessage.success('操作成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
