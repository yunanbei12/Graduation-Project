<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="title">角色管理</span>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="200" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleAssignMenu(row)">分配权限</el-button>
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

    <!-- 新增/编辑角色 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑角色' : '新增角色'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
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

    <!-- 分配权限 -->
    <el-dialog v-model="menuDialogVisible" title="分配权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuList"
        show-checkbox
        node-key="id"
        :props="{ label: 'name', children: 'children' }"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenu">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSysRoleList, addSysRole, updateSysRole, deleteSysRole, getRoleMenuIds, assignRoleMenus, getSysMenuList } from '../../api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const menuDialogVisible = ref(false)
const menuList = ref([])
const currentRoleId = ref(null)
const menuTreeRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({ name: '', remark: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }] }

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSysRoleList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadMenus = async () => {
  const res = await getSysMenuList()
  // 构建树形结构
  const menus = res.data
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
  menuList.value = tree
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { name: '', remark: '', status: 1 })
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
    await updateSysRole({ ...form, id: editingId.value })
  } else {
    const submitData = { ...form }
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await addSysRole(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该角色？', '提示')
  await deleteSysRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  await loadMenus()
  const res = await getRoleMenuIds(row.id)
  menuDialogVisible.value = true
  // 等待 DOM 更新后设置选中节点
  await new Promise(resolve => setTimeout(resolve, 0))
  // 只设置叶子节点的checked
  const leafIds = getLeafIds(res.data, menuList.value)
  menuTreeRef.value?.setCheckedKeys(leafIds)
}

const getLeafIds = (ids, tree) => {
  const leafIds = []
  const findLeaf = (nodes) => {
    nodes.forEach(n => {
      if (n.children && n.children.length > 0) {
        findLeaf(n.children)
      } else {
        if (ids.includes(n.id)) leafIds.push(n.id)
      }
    })
  }
  findLeaf(tree)
  return leafIds
}

const handleSaveMenu = async () => {
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]
  await assignRoleMenus(currentRoleId.value, allKeys)
  ElMessage.success('权限分配成功')
  menuDialogVisible.value = false
}

onMounted(() => loadData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 16px; font-weight: 600; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
