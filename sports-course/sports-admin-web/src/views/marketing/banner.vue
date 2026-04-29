<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.position" placeholder="展示位置" clearable style="width: 140px" @change="loadData">
            <el-option label="首页" :value="1" />
            <el-option label="课程页" :value="2" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">新增轮播图</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="图片" width="150">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width: 120px; height: 60px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="position" label="展示位置" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.position === 1 ? '首页' : '课程页' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑轮播图' : '新增轮播图'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <ImageUpload v-model="form.imageUrl" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接（选填）" />
        </el-form-item>
        <el-form-item label="跳转类型">
          <el-radio-group v-model="form.linkType">
            <el-radio :value="1">页面</el-radio>
            <el-radio :value="2">小程序</el-radio>
            <el-radio :value="3">H5</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="展示位置" prop="position">
          <el-radio-group v-model="form.position">
            <el-radio :value="1">首页</el-radio>
            <el-radio :value="2">课程页</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
          <div class="form-tip">数字越小越靠前</div>
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
import http from '../../utils/http'
import ImageUpload from '../../components/ImageUpload.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, position: null, status: null })
const form = reactive({
  title: '', imageUrl: '', linkUrl: '', linkType: 1, position: 1, sort: 0, status: 1
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  position: [{ required: true, message: '请选择展示位置', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await http.get('/banner/list', { params: query })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { title: '', imageUrl: '', linkUrl: '', linkType: 1, position: 1, sort: 0, status: 1 })
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
    await http.put('/banner', { ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id
    await http.post('/banner', submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该轮播图？', '提示')
  await http.delete(`/banner/${row.id}`)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>
