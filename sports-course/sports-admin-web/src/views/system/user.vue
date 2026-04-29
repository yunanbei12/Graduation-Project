<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="title">管理员管理</span>
        <el-button type="primary" @click="handleAdd">新增管理员</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickName" label="昵称" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑管理员' : '新增管理员'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingId">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" placeholder="选择角色" clearable style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSysUserList, addSysUser, updateSysUser, deleteSysUser, resetPassword, getAllRoles } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ username: '', password: '', nickName: '', roleId: null, status: 1 })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSysUserList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = await getAllRoles()
  roleList.value = res.data
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { username: '', password: '', nickName: '', roleId: null, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { ...row, password: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (editingId.value) {
    await updateSysUser({ ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await addSysUser(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该管理员？', '提示')
  await deleteSysUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm('确认重置密码为 123456？', '提示')
  await resetPassword({ id: row.id, password: 'e10adc3949ba59abbe56e057f20f883e' })
  ElMessage.success('密码已重置')
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 16px; font-weight: 600; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
