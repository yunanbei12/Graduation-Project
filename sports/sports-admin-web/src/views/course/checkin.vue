<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card shadow="never" class="op-card">
          <template #header>
            <span class="card-title">私教课销课</span>
          </template>

          <div class="step-block">
            <div class="step-label">① 选择学员</div>
            <el-input
              v-model="keyword"
              placeholder="输入手机号或昵称搜索学员"
              clearable
              @input="searchUser"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <div class="user-list" v-if="userResults.length > 0">
              <div
                v-for="u in userResults"
                :key="u.id"
                class="user-item"
                :class="{ active: selectedUser && selectedUser.id === u.id }"
                @click="pickUser(u)"
              >
                <el-avatar :src="u.avatarUrl" :size="32" />
                <div class="user-info">
                  <span class="user-phone-highlight">{{ u.phone || '未绑定手机' }}</span>
                  <span class="user-name-sub">{{ u.nickName || '未命名' }}</span>
                </div>
                <el-icon v-if="selectedUser && selectedUser.id === u.id" color="#67c23a"><Check /></el-icon>
              </div>
            </div>
            <div v-if="selectedUser" class="selected-user-tag">
              <el-tag type="success">已选：{{ selectedUser.nickName || selectedUser.phone || ('学员#' + selectedUser.id) }}</el-tag>
            </div>
          </div>

          <div class="step-block" v-if="selectedUser">
            <div class="step-label">② 选择课程</div>
            <div v-if="privateCourseOptions.length === 0" class="empty-tip">该学员暂无可用私教课包</div>
            <div
              v-for="course in privateCourseOptions"
              :key="course.courseId"
              class="pkg-item"
              :class="{ active: selectedCourse && selectedCourse.courseId === course.courseId }"
              @click="pickCourse(course)"
            >
              <div class="pkg-main">
                <span class="pkg-name">{{ course.courseName }}</span>
                <el-tag size="small" type="warning">剩余 {{ course.remainingLessons }} 节</el-tag>
              </div>
              <div class="pkg-sub">
                最早到期 {{ formatDate(course.earliestExpireTime) }}
                <span v-if="course.defaultCoachName"> · 默认教练 {{ course.defaultCoachName }}</span>
              </div>
            </div>
          </div>

          <div class="step-block" v-if="selectedCourse">
            <div class="step-label">③ 选择教练</div>
            <el-select v-model="selectedCoachId" placeholder="请选择教练" style="width: 100%">
              <el-option
                v-for="coach in coachOptions"
                :key="coach.id"
                :label="coach.name"
                :value="coach.id"
              />
            </el-select>
          </div>

          <div class="step-block" v-if="selectedCourse">
            <div class="step-label">④ 选择上课时间</div>
            <el-date-picker
              v-model="checkinTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择上课时间"
              style="width: 100%"
            />
          </div>

          <div class="step-block" v-if="selectedCourse">
            <div class="step-label">⑤ 销课备注</div>
            <el-input
              v-model="remark"
              type="textarea"
              :rows="3"
              placeholder="上课内容、地点等备注（可选）"
            />
          </div>

          <el-button
            type="primary"
            style="width: 100%; margin-top: 16px;"
            :disabled="!canSubmit"
            :loading="submitting"
            @click="handleSubmit"
          >
            确认销课
          </el-button>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="header-row">
              <span class="card-title">消课记录</span>
              <el-button size="small" @click="loadCheckins">刷新</el-button>
            </div>
          </template>

          <el-table :data="checkinList" v-loading="listLoading" stripe size="small">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column label="学员" width="120">
              <template #default="{ row }">
                <span>{{ userMap[row.userId] || ('ID:' + row.userId) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="课程" width="130">
              <template #default="{ row }">
                <span>{{ courseNameMap[row.courseId] || ('ID:' + row.courseId) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="教练" width="110">
              <template #default="{ row }">
                <span>{{ coachMap[row.coachId] || (row.coachId ? ('ID:' + row.coachId) : '-') }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="checkinTime" label="上课时间" min-width="150">
              <template #default="{ row }">
                {{ formatDateTime(row.checkinTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="coachAmount" label="教练金额" width="100">
              <template #default="{ row }">
                ¥{{ row.coachAmount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '出勤' : '缺勤' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="listQuery.pageNum"
              v-model:page-size="listQuery.pageSize"
              :total="listTotal"
              layout="total, prev, pager, next"
              @current-change="loadCheckins"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { Search, Check } from '@element-plus/icons-vue'
import { privateCheckin, getCheckinList, getPackageList } from '../../api/checkin'
import { getMemberList } from '../../api/system'
import { getCourseList } from '../../api/course'
import { getCoachList } from '../../api/coach'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const userResults = ref([])
const selectedUser = ref(null)
const packages = ref([])
const selectedCourse = ref(null)
const selectedCoachId = ref(null)
const checkinTime = ref('')
const remark = ref('')
const submitting = ref(false)
let searchTimer = null

const checkinList = ref([])
const listLoading = ref(false)
const listTotal = ref(0)
const listQuery = reactive({ pageNum: 1, pageSize: 10 })

const userMap = ref({})
const courseNameMap = ref({})
const coachMap = ref({})
const coachOptions = ref([])

const privateCourseOptions = computed(() => {
  const grouped = new Map()
  packages.value.forEach(pkg => {
    const totalLessons = Number(pkg.totalLessons || 0)
    const usedLessons = Number(pkg.usedLessons || 0)
    const remainingLessons = Math.max(totalLessons - usedLessons, 0)
    if (pkg.status !== 1 || remainingLessons <= 0) return

    const courseName = courseNameMap.value[pkg.courseId] || `课程#${pkg.courseId}`
    const defaultCoachId = courseDefaultCoachMap.value[pkg.courseId] || null
    const defaultCoachName = defaultCoachId ? coachMap.value[defaultCoachId] : ''
    const current = grouped.get(pkg.courseId) || {
      courseId: pkg.courseId,
      courseName,
      remainingLessons: 0,
      earliestExpireTime: pkg.expireTime,
      defaultCoachId,
      defaultCoachName
    }

    current.remainingLessons += remainingLessons
    if (!current.earliestExpireTime || (pkg.expireTime && pkg.expireTime < current.earliestExpireTime)) {
      current.earliestExpireTime = pkg.expireTime
    }
    if (!current.defaultCoachId && defaultCoachId) {
      current.defaultCoachId = defaultCoachId
      current.defaultCoachName = defaultCoachName
    }

    grouped.set(pkg.courseId, current)
  })
  return Array.from(grouped.values())
})

const courseDefaultCoachMap = computed(() => {
  const map = {}
  courseList.value.forEach(course => {
    map[course.id] = course.coachId || null
  })
  return map
})

const courseList = ref([])

const canSubmit = computed(() => {
  return !!(selectedUser.value && selectedCourse.value && selectedCoachId.value && checkinTime.value)
})

const searchUser = () => {
  clearTimeout(searchTimer)
  if (!keyword.value) {
    userResults.value = []
    return
  }
  searchTimer = setTimeout(async () => {
    try {
      const res = await getMemberList({ keyword: keyword.value, pageNum: 1, pageSize: 10 })
      userResults.value = res.data?.records || []
    } catch (e) {
      userResults.value = []
    }
  }, 400)
}

const pickUser = async (user) => {
  selectedUser.value = user
  selectedCourse.value = null
  selectedCoachId.value = null
  remark.value = ''
  checkinTime.value = getDefaultCheckinTime()
  userResults.value = []
  keyword.value = user.nickName || user.phone || ''
  try {
    const res = await getPackageList({ userId: user.id, pageNum: 1, pageSize: 100 })
    packages.value = res.data?.records || []
  } catch (e) {
    packages.value = []
  }
}

const pickCourse = (courseOption) => {
  selectedCourse.value = courseOption
  selectedCoachId.value = courseOption.defaultCoachId || null
  if (!checkinTime.value) {
    checkinTime.value = getDefaultCheckinTime()
  }
}

const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完整选择学员、课程、教练和上课时间')
    return
  }
  submitting.value = true
  try {
    await privateCheckin({
      userId: selectedUser.value.id,
      courseId: selectedCourse.value.courseId,
      coachId: selectedCoachId.value,
      checkinTime: checkinTime.value,
      remark: remark.value
    })
    ElMessage.success('销课成功')
    selectedUser.value = null
    selectedCourse.value = null
    selectedCoachId.value = null
    packages.value = []
    remark.value = ''
    keyword.value = ''
    checkinTime.value = ''
    loadCheckins()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '销课失败')
  } finally {
    submitting.value = false
  }
}

const loadCheckins = async () => {
  listLoading.value = true
  try {
    const res = await getCheckinList({ ...listQuery, checkinType: 1 })
    checkinList.value = res.data?.records || []
    listTotal.value = res.data?.total || 0
    await ensureMetaMaps()
  } finally {
    listLoading.value = false
  }
}

const ensureMetaMaps = async () => {
  const [memberRes, courseRes, coachRes] = await Promise.allSettled([
    getMemberList({ pageNum: 1, pageSize: 500 }),
    getCourseList({ pageNum: 1, pageSize: 500, type: 1 }),
    getCoachList({ pageNum: 1, pageSize: 500, status: 1 })
  ])

  if (memberRes.status === 'fulfilled') {
    const members = memberRes.value.data?.records || []
    members.forEach(user => {
      userMap.value[user.id] = user.nickName || user.phone || `用户${user.id}`
    })
  }

  if (courseRes.status === 'fulfilled') {
    courseList.value = courseRes.value.data?.records || []
    courseList.value.forEach(course => {
      courseNameMap.value[course.id] = course.name
    })
  }

  if (coachRes.status === 'fulfilled') {
    const coaches = coachRes.value.data?.records || []
    coachOptions.value = coaches
    coaches.forEach(coach => {
      coachMap.value[coach.id] = coach.name
    })
  }
}

const pad = (value) => String(value).padStart(2, '0')

const getDefaultCheckinTime = () => {
  const now = new Date()
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:00`
}

const formatDate = (dt) => {
  if (!dt) return '-'
  if (typeof dt === 'string') return dt.slice(0, 10)
  return new Date(dt).toISOString().slice(0, 10)
}

const formatDateTime = (dt) => {
  if (!dt) return '-'
  if (typeof dt === 'string') return dt.replace('T', ' ').slice(0, 16)
  return new Date(dt).toLocaleString()
}

onMounted(async () => {
  await ensureMetaMaps()
  await loadCheckins()
})
</script>

<style scoped>
.page-container { padding: 0; }
.card-title { font-size: 15px; font-weight: 600; }
.op-card { min-height: 560px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }

.step-block { margin-bottom: 20px; }
.step-label { font-size: 13px; color: #606266; font-weight: 600; margin-bottom: 8px; }

.user-list { border: 1px solid #e4e7ed; border-radius: 6px; margin-top: 8px; max-height: 220px; overflow-y: auto; }
.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s;
}
.user-item:last-child { border-bottom: none; }
.user-item:hover { background: #f5f7fa; }
.user-item.active { background: #f0f9eb; }
.user-info { flex: 1; display: flex; flex-direction: column; }
.user-phone-highlight { font-size: 14px; font-weight: 600; color: #303133; }
.user-name-sub { font-size: 12px; color: #909399; margin-top: 2px; }
.selected-user-tag { margin-top: 8px; }

.pkg-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.pkg-item:hover { border-color: #409eff; }
.pkg-item.active { border-color: #67c23a; background: #f0f9eb; }
.pkg-main { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.pkg-name { font-size: 13px; font-weight: 600; color: #303133; }
.pkg-sub { font-size: 12px; color: #909399; }
.empty-tip { color: #c0c4cc; font-size: 13px; text-align: center; padding: 20px 0; }
.pagination { display: flex; justify-content: flex-end; margin-top: 12px; }
</style>
