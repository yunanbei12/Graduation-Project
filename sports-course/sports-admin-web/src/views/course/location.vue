<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="title">上课地点管理</span>
        <el-button type="primary" @click="handleAdd">新增地点</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="地点图片" width="110">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage.startsWith('http') ? row.coverImage : row.coverImage"
              fit="cover"
              style="width: 64px; height: 64px; border-radius: 10px;"
            />
            <span v-else class="placeholder">未上传</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="地点名称" min-width="160" />
        <el-table-column prop="address" label="详细地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地点' : '新增地点'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="地点名称" prop="name">
          <el-input v-model="form.name" placeholder="如：集美大学光前体育馆" />
        </el-form-item>
        <el-form-item label="地点主图">
          <ImageUpload v-model="form.coverImage" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.address" placeholder="如：集美区银江路185号" />
        </el-form-item>
        <el-form-item label="地点介绍">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" style="width: 100%" />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '../../components/ImageUpload.vue'
import {
  getCourseLocationList,
  addCourseLocation,
  updateCourseLocation,
  deleteCourseLocation
} from '../../api/course'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  coverImage: '',
  address: '',
  description: '',
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入地点名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCourseLocationList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { name: '', coverImage: '', address: '', description: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name || '',
    coverImage: row.coverImage || '',
    address: row.address || '',
    description: row.description || '',
    sort: row.sort || 0,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (editingId.value) {
    await updateCourseLocation({ ...form, id: editingId.value })
  } else {
    await addCourseLocation({ ...form })
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该地点？已关联课程的地点不建议直接删除。', '提示')
  await deleteCourseLocation(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 16px; font-weight: 600; }
.placeholder { font-size: 12px; color: #909399; }
</style>
