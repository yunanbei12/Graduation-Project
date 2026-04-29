<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="title">菜单管理</span>
        <el-button type="primary" @click="handleAdd(0)">新增顶级菜单</el-button>
      </div>

      <el-table :data="menuTree" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }" stripe>
        <el-table-column prop="name" label="菜单名称" min-width="180" />
        <el-table-column prop="path" label="路径" min-width="150" />
        <el-table-column prop="component" label="组件" min-width="150" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.type === 0" size="small">目录</el-tag>
            <el-tag v-else-if="row.type === 1" type="success" size="small">菜单</el-tag>
            <el-tag v-else type="warning" size="small">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.type !== 2" link type="primary" @click="handleAdd(row.id)">新增子菜单</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑菜单' : '新增菜单'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="上级菜单">
          <el-input :value="form.parentId ? '子菜单' : '顶级菜单'" disabled />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="路径" v-if="form.type !== 2">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="组件" v-if="form.type === 1">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.type !== 2">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.type === 2">
          <el-input v-model="form.permission" />
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
import { ref, reactive, onMounted } from 'vue'
import { getSysMenuList, addSysMenu, updateSysMenu, deleteSysMenu } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import { filterCommerceMenus } from '../../config/demo-mode'

const loading = ref(false)
const menuTree = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({ parentId: 0, name: '', path: '', component: '', icon: '', type: 1, permission: '', sort: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSysMenuList()
    const menus = filterCommerceMenus(res.data || [])
    const map = {}
    const tree = []
    menus.forEach(m => {
      m.children = []
      map[m.id] = m
    })
    menus.forEach(m => {
      if (m.parentId && map[m.parentId]) {
        map[m.parentId].children.push(m)
      } else if (!m.parentId || m.parentId === 0) {
        tree.push(m)
      }
    })
    menuTree.value = tree
  } finally {
    loading.value = false
  }
}

const handleAdd = (parentId) => {
  editingId.value = null
  Object.assign(form, { parentId, name: '', path: '', component: '', icon: '', type: parentId ? 1 : 0, permission: '', sort: 0, status: 1 })
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
    await updateSysMenu({ ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await addSysMenu(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该菜单？', '提示')
  await deleteSysMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 16px; font-weight: 600; }
</style>
