<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.keyword" placeholder="搜索标题/关键词/内容" clearable style="width: 240px" @keyup.enter="loadData" />
          <el-select v-model="query.category" placeholder="分类" clearable style="width: 160px" @change="loadData">
            <el-option label="faq" value="faq" />
            <el-option label="course" value="course" />
            <el-option label="coupon" value="coupon" />
            <el-option label="refund" value="refund" />
            <el-option label="account" value="account" />
          </el-select>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px" @change="loadData">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">新增知识</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="keywords" label="关键词" min-width="220" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="90" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑知识' : '新增知识'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="faq" value="faq" />
            <el-option label="course" value="course" />
            <el-option label="coupon" value="coupon" />
            <el-option label="refund" value="refund" />
            <el-option label="account" value="account" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="form.keywords" placeholder="多个关键词用英文逗号或中文逗号分隔" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="99" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
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
import { createAiKnowledge, deleteAiKnowledge, getAiKnowledges, updateAiKnowledge } from '../../api/ai'

const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', category: '', status: null })
const form = reactive({ title: '', category: 'faq', keywords: '', priority: 10, status: 1, content: '' })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getAiKnowledges(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, { title: '', category: 'faq', keywords: '', priority: 10, status: 1, content: '' })
}

const handleAdd = () => {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (editingId.value) {
    await updateAiKnowledge({ ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await createAiKnowledge(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除这条知识吗？', '提示')
  await deleteAiKnowledge(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 16px; }
.toolbar-left { display: flex; gap: 12px; flex-wrap: wrap; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
