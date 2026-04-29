<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-select v-model="query.locationId" placeholder="筛选地点" clearable style="width: 220px" @change="handleQueryLocationChange">
            <el-option v-for="loc in locationList" :key="loc.id" :label="loc.name" :value="loc.id" />
          </el-select>
          <el-select
            v-model="query.courseId"
            placeholder="筛选团课"
            clearable
            style="width: 220px"
            :disabled="!!query.locationId && !queryCourseList.length"
            @change="handleQueryCourseChange"
          >
            <el-option v-for="c in queryCourseList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </div>
        <div class="toolbar-actions">
          <el-button @click="$router.push('/schedule/board')">教练排课看板</el-button>
          <el-button type="primary" @click="handleAdd">新增排课</el-button>
        </div>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="课程" min-width="140">
          <template #default="{ row }">
            {{ getCourseInfo(row.courseId)?.name || row.courseId }}
          </template>
        </el-table-column>
        <el-table-column label="排课日期" width="120">
          <template #default="{ row }">
            {{ row.scheduleDate || (row.startTime ? row.startTime.slice(0, 10) : '-') }}
          </template>
        </el-table-column>
        <el-table-column label="时间段" width="140">
          <template #default="{ row }">
            <span v-if="getCourseInfo(row.courseId)?.startHour">
              {{ getCourseInfo(row.courseId).startHour }} - {{ getCourseInfo(row.courseId).endHour }}
            </span>
            <span v-else-if="row.startTime">
              {{ row.startTime.slice(11, 16) }} - {{ row.endTime?.slice(11, 16) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="地点" width="140">
          <template #default="{ row }">
            {{ getCourseInfo(row.courseId)?.location || row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalSeats" label="总座位" width="80" />
        <el-table-column label="已报/成团" width="110">
          <template #default="{ row }">
            {{ row.enrolledSeats || 0 }} / {{ getCourseInfo(row.courseId)?.minGroupSize || 1 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type" size="small">{{ statusTag(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" v-if="row.status === 0 || row.status === 1" @click="handleCheckGroup(row)">成团判断</el-button>
            <el-button link type="warning" v-if="row.status === 0 || row.status === 1" @click="handleSettle(row)">结课</el-button>
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

    <!-- 新增/编辑排课弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑排课' : '新增排课'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上课地点" prop="locationId">
          <el-select v-model="form.locationId" placeholder="选择上课地点" style="width: 100%" @change="handleFormLocationChange">
            <el-option v-for="loc in locationList" :key="loc.id" :label="loc.name" :value="loc.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="团课课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            :placeholder="form.locationId ? '选择团课' : '请先选择上课地点'"
            style="width: 100%"
            :disabled="!form.locationId"
            @change="onCourseChange"
          >
            <el-option v-for="c in dialogCourseList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <!-- 选择课程后展示模板时间地点 -->
        <el-form-item label="课程时间" v-if="selectedCourse">
          <span style="color: #606266">
            {{ selectedCourse.startHour }} - {{ selectedCourse.endHour }}
            &nbsp;&nbsp;|&nbsp;&nbsp;
            {{ selectedCourse.location }}
          </span>
        </el-form-item>
        <el-form-item label="成团人数" v-if="selectedCourse">
          <span style="color: #606266">
            {{ selectedCourse.minGroupSize || 1 }} 人成团
          </span>
        </el-form-item>
        <el-form-item v-if="editingId" label="排课日期" prop="scheduleDate">
          <el-date-picker
            v-model="form.scheduleDate"
            type="date"
            placeholder="选择具体上课日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-else label="排课日期" prop="scheduleDates">
          <el-date-picker
            v-model="form.scheduleDates"
            type="dates"
            placeholder="可多选多个排课日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disablePastDate"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总座位" prop="totalSeats">
          <el-input-number v-model="form.totalSeats" :min="minSeats" style="width: 100%" />
          <div v-if="selectedCourse" class="form-tip">总座位数不能少于成团人数 {{ minSeats }} 人</div>
        </el-form-item>
        <el-form-item label="授课教练" prop="coachId">
          <el-select v-model="form.coachId" placeholder="选择本次排课教练" style="width: 100%">
            <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 结课弹窗 -->
    <el-dialog v-model="settleVisible" title="团课结课" width="600px">
      <div class="settle-tip">  请勾选未到场的学员（承认为出勤，不勾就是缺勤）</div>
      <el-table :data="attendeeList" v-loading="attendeeLoading" @selection-change="handleAttendeeSelect">
        <el-table-column type="selection" width="50" />
        <el-table-column label="头像" width="70">
          <template #default="{ row }">
            <el-avatar :src="row.avatarUrl" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="nickName" label="昵称" />
      </el-table>
      <template #footer>
        <el-button @click="settleVisible = false">取消</el-button>
        <el-button type="primary" :loading="settling" @click="confirmSettle">确认结课</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { getScheduleList, addScheduleBatch, updateSchedule, deleteSchedule, getCourseList, getCourseLocationList, getScheduleAttendees, groupSettle, checkGroupStatus } from '../../api/course'
import { getCoachList } from '../../api/coach'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const courseList = ref([])
const locationList = ref([])
const coachList = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const selectedCourse = ref(null)

// 结课相关
const settleVisible = ref(false)
const settling = ref(false)
const attendeeLoading = ref(false)
const attendeeList = ref([])
const selectedAbsent = ref([]) // 勾选的是出勤的，unselected 就是缺勤
const currentSettleScheduleId = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, locationId: null, courseId: null })
const form = reactive({ locationId: null, courseId: null, scheduleDate: '', scheduleDates: [], totalSeats: 20, coachId: null })
const validateTotalSeats = (_, value, callback) => {
  const limit = minSeats.value
  if (value == null || value <= 0) {
    callback(new Error('请输入正确的总座位数'))
    return
  }
  if (value < limit) {
    callback(new Error(`总座位数不能少于成团人数 ${limit} 人`))
    return
  }
  callback()
}
const rules = {
  locationId: [{ required: true, message: '请选择上课地点', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排课日期', trigger: 'change' }],
  scheduleDates: [{ required: true, message: '请至少选择一个排课日期', trigger: 'change' }],
  totalSeats: [{ validator: validateTotalSeats, trigger: 'change' }],
  coachId: [{ required: true, message: '请选择授课教练', trigger: 'change' }]
}

const queryCourseList = computed(() => {
  if (!query.locationId) {
    return courseList.value
  }
  return courseList.value.filter(item => item.locationId === query.locationId)
})

const dialogCourseList = computed(() => {
  if (!form.locationId) {
    return []
  }
  return courseList.value.filter(item => item.locationId === form.locationId)
})

const minSeats = computed(() => {
  return selectedCourse.value?.minGroupSize || 1
})

const statusTag = (status) => {
  const map = { 0: { label: '未开始', type: 'info' }, 1: { label: '进行中', type: 'success' }, 2: { label: '已结课', type: '' }, 3: { label: '已取消', type: 'danger' } }
  return map[status] || { label: '未知', type: 'info' }
}

const disablePastDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getScheduleList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCourses = async () => {
  // 只加载团课（type=2）
  const res = await getCourseList({ pageNum: 1, pageSize: 100, type: 2 })
  courseList.value = res.data.records
}

const loadLocations = async () => {
  const res = await getCourseLocationList({ status: 1 })
  locationList.value = res.data || []
}

const loadCoaches = async () => {
  const res = await getCoachList({ pageNum: 1, pageSize: 100, status: 1 })
  coachList.value = res.data.records
}

const handleAdd = () => {
  editingId.value = null
  selectedCourse.value = null
  Object.assign(form, { locationId: query.locationId || null, courseId: null, scheduleDate: '', scheduleDates: [], totalSeats: 20, coachId: null })
  dialogVisible.value = true
}

const onCourseChange = (courseId) => {
  const course = courseList.value.find(c => c.id === courseId)
  selectedCourse.value = course || null
  form.locationId = course?.locationId || form.locationId
  if (form.totalSeats < minSeats.value) {
    form.totalSeats = minSeats.value
  }
}

const handleQueryLocationChange = () => {
  query.pageNum = 1
  query.courseId = null
  loadData()
}

const handleQueryCourseChange = () => {
  query.pageNum = 1
  loadData()
}

const handleFormLocationChange = () => {
  if (selectedCourse.value?.locationId !== form.locationId) {
    form.courseId = null
    selectedCourse.value = null
  }
}

const handleEdit = (row) => {
  const course = courseList.value.find(c => c.id === row.courseId) || null
  editingId.value = row.id
  selectedCourse.value = course
  Object.assign(form, {
    locationId: course?.locationId || null,
    courseId: row.courseId,
    scheduleDate: row.scheduleDate || (row.startTime ? row.startTime.slice(0, 10) : ''),
    scheduleDates: [],
    totalSeats: Math.max(row.totalSeats || 0, course?.minGroupSize || 1),
    coachId: row.coachId
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (editingId.value) {
    const submitData = { ...form, id: editingId.value }
    delete submitData.locationId
    delete submitData.scheduleDates
    await updateSchedule(submitData)
  } else {
    const submitData = { ...form }
    submitData.scheduleDates = [...new Set((submitData.scheduleDates || []).filter(Boolean))]
    delete submitData.locationId
    delete submitData.scheduleDate
    delete submitData.id
    await addScheduleBatch(submitData)
  }
  ElMessage.success(editingId.value ? '操作成功' : `已新增 ${form.scheduleDates.length} 条排课`)
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该排课？', '提示')
  await deleteSchedule(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 成团判断
const handleCheckGroup = async (row) => {
  try {
    await ElMessageBox.confirm(`确认对「${row.location || 'ID:' + row.id}」排课执行成团判断？不成团将自动取消排课并退款`, '成团判断')
    const res = await checkGroupStatus(row.id)
    ElMessage({ type: res.data.grouped ? 'success' : 'warning', message: res.data.message, duration: 4000 })
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

// 结课操作
const handleSettle = async (row) => {
  currentSettleScheduleId.value = row.id
  attendeeList.value = []
  selectedAbsent.value = []
  settleVisible.value = true
  attendeeLoading.value = true
  try {
    const res = await getScheduleAttendees(row.id)
    attendeeList.value = res.data || []
  } catch (e) {
    ElMessage.error('加载学员列表失败')
  } finally {
    attendeeLoading.value = false
  }
}

// table selection变化（勾选的是出勤）
const handleAttendeeSelect = (selection) => {
  selectedAbsent.value = selection // 勾选的为出勤
}

const confirmSettle = async () => {
  settling.value = true
  try {
    // selectedAbsent 是勾选的出勤用户，缺勤 = 所有人 - 出勤人
    const allIds = attendeeList.value.map(u => u.userId)
    const presentIds = selectedAbsent.value.map(u => u.userId)
    const absentIds = allIds.filter(id => !presentIds.includes(id))
    await groupSettle(currentSettleScheduleId.value, absentIds)
    ElMessage.success('结课成功')
    settleVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('结课失败')
  } finally {
    settling.value = false
  }
}

// 根据courseId获取课程信息
const getCourseInfo = (courseId) => {
  return courseList.value.find(c => c.id === courseId)
}

onMounted(() => {
  loadData()
  loadCourses()
  loadLocations()
  loadCoaches()
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.toolbar-left { display: flex; gap: 12px; }
.toolbar-actions { display: flex; gap: 12px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.settle-tip { color: #909399; font-size: 13px; margin-bottom: 12px; }
.form-tip { font-size: 12px; color: #909399; line-height: 1.4; margin-top: 6px; }
</style>
