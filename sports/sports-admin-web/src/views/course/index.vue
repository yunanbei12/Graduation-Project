<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.type" placeholder="课程类型" clearable style="width: 140px" @change="loadData">
            <el-option label="私教课" :value="1" />
            <el-option label="团课" :value="2" />
          </el-select>
          <el-select v-model="query.categoryId" placeholder="课程分类" clearable style="width: 140px" @change="loadData">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">新增课程</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : 'success'">{{ row.type === 1 ? '私教课' : '团课' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="课程类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">私教课</el-radio>
            <el-radio :value="2">团课</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>

        <!-- 私教课字段 -->
        <template v-if="form.type === 1">
          <el-form-item label="课时数" prop="lessonCount">
            <el-input-number v-model="form.lessonCount" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="有效期(天)">
            <el-input-number v-model="form.validityDays" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="特色标签">
            <div class="feature-editor">
              <div class="feature-tags" v-if="featureList.length">
                <el-tag
                  v-for="(feat, idx) in featureList"
                  :key="idx"
                  closable
                  @close="removeFeature(idx)"
                  class="feature-tag"
                >
                  {{ feat.icon }} {{ feat.text }}
                </el-tag>
              </div>
              <div class="feature-add">
                <el-input v-model="newFeature.icon" placeholder="图标" style="width: 80px; margin-right: 8px" />
                <el-input v-model="newFeature.text" placeholder="标签文字" style="width: 150px; margin-right: 8px" />
                <el-button type="primary" size="small" @click="addFeature">添加</el-button>
              </div>
              <div class="feature-presets">
                <span class="preset-label">快捷添加：</span>
                <el-button size="small" v-for="preset in featurePresets" :key="preset.text" @click="addPresetFeature(preset)">
                  {{ preset.icon }} {{ preset.text }}
                </el-button>
              </div>
            </div>
          </el-form-item>
          <el-form-item label="是否上门">
            <el-switch v-model="form.isDoorService" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </template>

        <!-- 团课字段 -->
        <template v-if="form.type === 2">
          <el-form-item label="成团人数">
            <el-input-number v-model="form.minGroupSize" :min="1" style="width: 100%" />
          </el-form-item>
          <el-form-item label="开课时间" required>
            <div style="display: flex; gap: 8px; align-items: center; width: 100%">
              <el-time-select
                v-model="form.startHour"
                start="06:00" step="00:30" end="22:30"
                placeholder="开始时间"
                style="flex: 1"
              />
              <span style="color: #909399">-</span>
              <el-time-select
                v-model="form.endHour"
                start="06:00" step="00:30" end="23:00"
                placeholder="结束时间"
                :min-time="form.startHour"
                style="flex: 1"
              />
            </div>
          </el-form-item>
          <el-form-item label="上课地点" required>
            <el-input v-model="form.location" placeholder="如：一楼瑜伽馆A室" />
          </el-form-item>
          <el-form-item label="是否上门">
            <el-switch v-model="form.isDoorService" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </template>

        <el-form-item label="授课教练">
          <el-select v-model="form.coachId" placeholder="选择教练" clearable style="width: 100%">
            <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练分成(%)">
          <el-input-number v-model="form.settleRatioPercent" :min="0" :max="100" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课程图片">
          <ImageUpload v-model="form.pic" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="课程详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { getCourseList, addCourse, updateCourse, deleteCourse, getCourseCategoryList } from '../../api/course'
import { getCoachList } from '../../api/coach'
import ImageUpload from '../../components/ImageUpload.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categoryList = ref([])
const coachList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, type: null, categoryId: null })

const form = reactive({
  name: '', type: 1, categoryId: null, price: 0, originalPrice: 0,
  lessonCount: 1, validityDays: 90, features: '', isDoorService: 0,
  minGroupSize: 5, startHour: '', endHour: '', location: '',
  coachId: null, settleRatioPercent: 50,
  pic: '', description: '', detail: '', status: 1
})

// 特色标签可视化编辑
const featureList = ref([])
const newFeature = reactive({ icon: '', text: '' })
const featurePresets = [
  { icon: '⏱', text: '60min/课' },
  { icon: '🎯', text: '一对一指导' },
  { icon: '📍', text: '可上门授课' },
  { icon: '🏆', text: '专业认证教练' },
  { icon: '📅', text: '灵活预约' },
  { icon: '💪', text: '定制训练计划' }
]

const addFeature = () => {
  if (newFeature.icon && newFeature.text) {
    featureList.value.push({ icon: newFeature.icon, text: newFeature.text })
    newFeature.icon = ''
    newFeature.text = ''
  }
}

const addPresetFeature = (preset) => {
  // 避免重复添加
  if (!featureList.value.some(f => f.text === preset.text)) {
    featureList.value.push({ ...preset })
  }
}

const removeFeature = (idx) => {
  featureList.value.splice(idx, 1)
}

const parseFeatures = (features) => {
  if (!features) return []
  try {
    return typeof features === 'string' ? JSON.parse(features) : features
  } catch {
    return []
  }
}

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCourseList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCategory = async () => {
  const res = await getCourseCategoryList()
  categoryList.value = res.data
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { name: '', type: 1, categoryId: null, price: 0, originalPrice: 0, lessonCount: 1, validityDays: 90, features: '', isDoorService: 0, minGroupSize: 5, startHour: '', endHour: '', location: '', coachId: null, settleRatioPercent: 50, pic: '', description: '', detail: '', status: 1 })
  featureList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  const settleRatioPercent = row.settleRatio ? Math.round(row.settleRatio * 100) : 50
  Object.assign(form, { ...row, settleRatioPercent })
  featureList.value = parseFeatures(row.features)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const submitData = { ...form }
  // 转换分成比例为小数
  submitData.settleRatio = (form.settleRatioPercent || 0) / 100
  // 将特色标签数组转为JSON字符串
  submitData.features = featureList.value.length > 0 ? JSON.stringify(featureList.value) : null
  delete submitData.settleRatioPercent
  if (editingId.value) {
    await updateCourse({ ...submitData, id: editingId.value })
  } else {
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await addCourse(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该课程？', '提示')
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const loadCoach = async () => {
  const res = await getCoachList({ pageNum: 1, pageSize: 100 })
  coachList.value = res.data.records
}

onMounted(() => {
  loadData()
  loadCategory()
  loadCoach()
})
</script>

<style scoped>
.page-container { padding: 0; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.feature-editor { width: 100%; }
.feature-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.feature-tag { font-size: 13px; }
.feature-add { display: flex; align-items: center; margin-bottom: 8px; }
.feature-presets { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.preset-label { font-size: 12px; color: #909399; margin-right: 4px; }
</style>
