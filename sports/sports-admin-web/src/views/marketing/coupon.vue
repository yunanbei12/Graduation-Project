<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.type" placeholder="优惠券类型" clearable style="width: 140px" @change="loadData">
            <el-option label="满减" :value="1" />
            <el-option label="折扣" :value="2" />
            <el-option label="无门槛" :value="3" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">新增优惠券</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'danger' : row.type === 2 ? 'warning' : 'success'">
              {{ row.type === 1 ? '满减' : row.type === 2 ? '折扣' : '无门槛' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" width="110">
          <template #default="{ row }">
            <span v-if="row.type === 1">满{{ row.minAmount }}减{{ row.discount }}</span>
            <span v-else-if="row.type === 2">满{{ row.minAmount }}打{{ row.discountRatio * 10 }}折</span>
            <span v-else>立减{{ row.discount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发放方式" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.registerGift === 1" type="success" size="small" style="margin-right: 4px">注册赠送</el-tag>
            <el-tag v-if="row.activityTrigger === 1" type="warning" size="small">满{{ row.activityAmount }}发放</el-tag>
            <span v-if="row.registerGift !== 1 && row.activityTrigger !== 1">手动发放</span>
          </template>
        </el-table-column>
        <el-table-column label="发放/总量" width="90">
          <template #default="{ row }">{{ row.usedCount }}/{{ row.totalCount || '不限' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleSend(row)">发放</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑优惠券' : '新增优惠券'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">满减</el-radio>
            <el-radio :value="2">折扣</el-radio>
            <el-radio :value="3">无门槛</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type === 1 || form.type === 3" label="优惠金额">
          <el-input-number v-model="form.discount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.type === 2" label="折扣率">
          <el-input-number v-model="form.discountRatio" :min="0.01" :max="0.99" :precision="2" :step="0.05" style="width: 100%" />
          <div class="form-tip">如：0.8 表示8折</div>
        </el-form-item>
        <el-form-item v-if="form.type === 1 || form.type === 2" label="使用门槛">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="适用范围" prop="scope">
          <el-radio-group v-model="form.scope">
            <el-radio v-for="item in scopeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发放总量">
          <el-input-number v-model="form.totalCount" :min="0" style="width: 100%" />
          <div class="form-tip">0表示不限制</div>
        </el-form-item>
        <el-form-item label="生效时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="过期时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择时间" style="width: 100%" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="注册赠送">
          <el-switch v-model="form.registerGift" :active-value="1" :inactive-value="0" />
          <div class="form-tip">开启后，新用户注册自动发放</div>
        </el-form-item>
        <el-form-item label="活动发放">
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-switch v-model="form.activityTrigger" :active-value="1" :inactive-value="null" />
            <span v-if="form.activityTrigger === 1">消费满</span>
            <el-input-number v-if="form.activityTrigger === 1" v-model="form.activityAmount" :min="0.01" :precision="2" style="width: 120px" />
            <span v-if="form.activityTrigger === 1">元自动发放</span>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 发放弹窗 -->
    <el-dialog v-model="sendDialogVisible" title="发放优惠券" width="600px">
      <el-tabs v-model="sendTab">
        <el-tab-pane label="指定用户" name="user">
          <div class="send-section">
            <el-input v-model="userQuery" placeholder="输入用户昵称或手机号搜索" @keyup.enter="searchUsers" style="width: 300px; margin-bottom: 12px">
              <template #append>
                <el-button @click="searchUsers">搜索</el-button>
              </template>
            </el-input>
            <el-table :data="userList" v-loading="userLoading" max-height="300" @selection-change="handleUserSelect">
              <el-table-column type="selection" width="50" />
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="nickName" label="昵称" />
              <el-table-column prop="phone" label="手机号" />
            </el-table>
            <div style="margin-top: 12px">
              <el-button type="primary" @click="sendToSelectedUsers" :disabled="selectedUsers.length === 0">发放给选中用户 ({{ selectedUsers.length }})</el-button>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="批量发放" name="batch">
          <div class="send-section">
            <p class="send-info">当前优惠券：{{ sendCoupon?.name }}</p>
            <p class="send-info">已发放：{{ sendCoupon?.usedCount }} / {{ sendCoupon?.totalCount || '不限' }}</p>
            <div style="margin-top: 16px">
              <el-button type="primary" @click="sendToAllUsers">发放给所有用户</el-button>
              <el-button type="warning" @click="sendToNewUsers">发放给新用户（近30天注册）</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import http from '../../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DEMO_MODE } from '../../config/demo-mode'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

// 发放相关
const sendDialogVisible = ref(false)
const sendTab = ref('user')
const sendCoupon = ref(null)
const userQuery = ref('')
const userList = ref([])
const userLoading = ref(false)
const selectedUsers = ref([])

const query = reactive({ pageNum: 1, pageSize: 10, type: null })
const form = reactive({
  name: '', type: 1, discount: 10, minAmount: 100, discountRatio: 0.8,
  scope: 1, totalCount: 0, startTime: null, endTime: null, status: 1,
  registerGift: 0, activityTrigger: null, activityAmount: null
})
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  scope: [{ required: true, message: '请选择适用范围', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择生效时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择过期时间', trigger: 'change' }]
}

const scopeOptions = DEMO_MODE.hideCommerce
  ? [
      { value: 1, label: '通用优惠券' },
      { value: 2, label: '课程优惠券' }
    ]
  : [
      { value: 1, label: '全场' },
      { value: 2, label: '仅课程' },
      { value: 3, label: '仅商品' }
    ]

const loadData = async () => {
  loading.value = true
  try {
    const res = await http.get('/coupon/list', { params: query })
    const records = res.data.records || []
    tableData.value = DEMO_MODE.hideCommerce
      ? records.filter(item => item.scope !== 3)
      : records
    total.value = DEMO_MODE.hideCommerce ? tableData.value.length : res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, {
    name: '', type: 1, discount: 10, minAmount: 100, discountRatio: 0.8,
    scope: 1, totalCount: 0, startTime: null, endTime: null, status: 1,
    registerGift: 0, activityTrigger: null, activityAmount: null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (editingId.value) {
    await http.put('/coupon', { ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id
    await http.post('/coupon', submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该优惠券？', '提示')
  await http.delete(`/coupon/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

// 发放相关方法
const handleSend = (row) => {
  sendCoupon.value = row
  sendTab.value = 'user'
  userQuery.value = ''
  userList.value = []
  selectedUsers.value = []
  sendDialogVisible.value = true
}

const searchUsers = async () => {
  userLoading.value = true
  try {
    const res = await http.get('/coupon/send/users', { params: { keyword: userQuery.value, pageSize: 20 } })
    userList.value = res.data.records
  } finally {
    userLoading.value = false
  }
}

const handleUserSelect = (selection) => {
  selectedUsers.value = selection
}

const sendToSelectedUsers = async () => {
  if (selectedUsers.value.length === 0) return
  let success = 0
  for (const user of selectedUsers.value) {
    try {
      await http.post(`/coupon/send/${sendCoupon.value.id}/user/${user.id}`)
      success++
    } catch (e) {
      // 忽略单个失败
    }
  }
  ElMessage.success(`成功发放 ${success} 张优惠券`)
  sendDialogVisible.value = false
  loadData()
}

const sendToAllUsers = async () => {
  await ElMessageBox.confirm('确认发放给所有用户？', '提示')
  const res = await http.post(`/coupon/send/${sendCoupon.value.id}/all`)
  ElMessage.success(`成功发放 ${res.data.success} 张优惠券`)
  sendDialogVisible.value = false
  loadData()
}

const sendToNewUsers = async () => {
  await ElMessageBox.confirm('确认发放给近30天注册的新用户？', '提示')
  const res = await http.post(`/coupon/send/${sendCoupon.value.id}/new`)
  ElMessage.success(`成功发放 ${res.data.success} 张优惠券`)
  sendDialogVisible.value = false
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
.send-section { padding: 12px 0; }
.send-info { margin: 8px 0; color: #666; }
</style>
