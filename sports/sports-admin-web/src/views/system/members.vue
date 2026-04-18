<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="title">小程序用户管理</span>
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/手机号"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
          @clear="loadData"
        >
          <template #suffix>
            <el-icon style="cursor:pointer" @click="loadData"><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar
              :src="row.avatarUrl || ''"
              :size="36"
              style="background:#f0f0f0"
            >{{ (row.nickName || '?').charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="nickName" label="昵称/用户名" min-width="130" />
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="注册方式" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.registerType === 0" type="success" size="small">微信注册</el-tag>
            <el-tag v-else-if="row.registerType === 1" type="primary" size="small">手机号注册</el-tag>
            <el-tag v-else-if="row.registerType === 2" type="warning" size="small">验证码登录</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMemberList, updateMemberStatus } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const keyword = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMemberList({ ...query, keyword: keyword.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${actionText}该用户？`, '提示')
  await updateMemberStatus({ id: row.id, status: newStatus })
  ElMessage.success(`已${actionText}`)
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
