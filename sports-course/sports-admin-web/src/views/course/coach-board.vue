<template>
  <div class="board-page">
    <el-card shadow="never" class="filter-card">
      <div class="filter-header">
        <div>
          <div class="page-title">教练排课看板</div>
          <div class="page-subtitle">只展示团课排期，帮助运营按周查看教练安排</div>
        </div>
        <div class="header-actions">
          <el-button @click="jumpWeek(-1)">上一周</el-button>
          <el-button @click="resetToCurrentWeek">本周</el-button>
          <el-button @click="jumpWeek(1)">下一周</el-button>
          <el-button type="primary" @click="goSchedulePage">前往排课管理</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-date-picker
          v-model="weekRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          style="width: 280px"
          @change="handleRangeChange"
        />
        <el-select v-model="filters.coachId" clearable placeholder="全部教练" style="width: 180px">
          <el-option v-for="coach in coachList" :key="coach.id" :label="coach.name" :value="coach.id" />
        </el-select>
        <el-select v-model="filters.location" clearable placeholder="全部地点" style="width: 220px">
          <el-option v-for="item in locationOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 160px">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="loadBoard">查询</el-button>
      </div>

      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">本周排课</div>
          <div class="stat-value">{{ summary.totalSchedules }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">涉及教练</div>
          <div class="stat-value">{{ summary.totalCoaches }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">总座位数</div>
          <div class="stat-value">{{ summary.totalSeats }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已报名人数</div>
          <div class="stat-value">{{ summary.totalEnrolled }}</div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="board-card">
      <div class="board-scroll" v-loading="loading">
        <div class="board-grid" :style="{ gridTemplateColumns: gridTemplateColumns }" v-if="boardRows.length">
          <div class="grid-head coach-head">教练</div>
          <div v-for="day in weekDays" :key="day.date" class="grid-head day-head">
            <div class="day-title">{{ day.weekLabel }}</div>
            <div class="day-date">{{ day.dateLabel }}</div>
          </div>

          <template v-for="row in boardRows" :key="row.coachId">
            <div class="coach-cell">
              <div class="coach-name">{{ row.coachName }}</div>
              <div class="coach-meta">
                <span>{{ row.scheduleCount }} 节团课</span>
                <span>{{ row.totalEnrolled }} 人已报</span>
              </div>
            </div>

            <div
              v-for="day in weekDays"
              :key="`${row.coachId}-${day.date}`"
              class="day-cell"
              :class="{ 'is-today': day.isToday }"
            >
              <template v-if="getSchedulesByDay(row, day.date).length">
                <div
                  v-for="item in getSchedulesByDay(row, day.date)"
                  :key="item.scheduleId"
                  class="schedule-card"
                  :class="statusClass(item.status)"
                  @click="openDetail(item)"
                >
                  <div class="schedule-time">{{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</div>
                  <div class="schedule-name">{{ item.courseName || '未命名团课' }}</div>
                  <div class="schedule-meta">{{ item.location || '地点待定' }}</div>
                  <div class="schedule-footer">
                    <span>{{ item.enrolledSeats || 0 }}/{{ item.totalSeats || 0 }}</span>
                    <el-tag size="small" :type="statusTag(item.status).type">{{ statusTag(item.status).label }}</el-tag>
                  </div>
                </div>
              </template>
              <div v-else class="empty-slot">暂无排课</div>
            </div>
          </template>
        </div>

        <el-empty v-else description="当前筛选条件下暂无团课排期" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="排课详情" width="460px">
      <template v-if="activeSchedule">
        <div class="detail-list">
          <div class="detail-row">
            <span class="detail-label">团课名称</span>
            <span class="detail-value">{{ activeSchedule.courseName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">授课教练</span>
            <span class="detail-value">{{ activeSchedule.coachName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上课日期</span>
            <span class="detail-value">{{ formatDate(activeSchedule.scheduleDate) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">时间段</span>
            <span class="detail-value">{{ formatTime(activeSchedule.startTime) }} - {{ formatTime(activeSchedule.endTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">上课地点</span>
            <span class="detail-value">{{ activeSchedule.location || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">报名情况</span>
            <span class="detail-value">{{ activeSchedule.enrolledSeats || 0 }}/{{ activeSchedule.totalSeats || 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">排课状态</span>
            <span class="detail-value">
              <el-tag size="small" :type="statusTag(activeSchedule.status).type">{{ statusTag(activeSchedule.status).label }}</el-tag>
            </span>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="goSchedulePage">前往排课管理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCoachList } from '../../api/coach'
import { getCourseList, getScheduleBoard } from '../../api/course'

const router = useRouter()
const loading = ref(false)
const detailVisible = ref(false)
const activeSchedule = ref(null)
const coachList = ref([])
const courseList = ref([])
const scheduleList = ref([])
const filters = reactive({
  coachId: null,
  location: '',
  status: null
})

const statusOptions = [
  { label: '未开始', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结课', value: 2 },
  { label: '已取消', value: 3 }
]

const statusTag = (status) => {
  const map = {
    0: { label: '未开始', type: 'info' },
    1: { label: '进行中', type: 'success' },
    2: { label: '已结课', type: '' },
    3: { label: '已取消', type: 'danger' }
  }
  return map[status] || { label: '未知', type: 'info' }
}

const statusClass = (status) => {
  const map = {
    0: 'status-pending',
    1: 'status-running',
    2: 'status-finished',
    3: 'status-cancelled'
  }
  return map[status] || 'status-pending'
}

const formatDateKey = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const parseDateString = (value) => {
  if (!value) return new Date()
  const [year, month, day] = String(value).split('-').map(Number)
  return new Date(year, (month || 1) - 1, day || 1)
}

const getWeekStart = (base = new Date()) => {
  const current = new Date(base)
  const day = current.getDay() || 7
  current.setHours(0, 0, 0, 0)
  current.setDate(current.getDate() - day + 1)
  return current
}

const addDays = (date, days) => {
  const current = new Date(date)
  current.setDate(current.getDate() + days)
  return current
}

const currentWeekStart = ref(getWeekStart())
const weekRange = ref([formatDateKey(currentWeekStart.value), formatDateKey(addDays(currentWeekStart.value, 6))])

const weekDays = computed(() => {
  const labels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return Array.from({ length: 7 }).map((_, index) => {
    const current = addDays(currentWeekStart.value, index)
    const key = formatDateKey(current)
    return {
      date: key,
      weekLabel: labels[index],
      dateLabel: `${current.getMonth() + 1}/${current.getDate()}`,
      isToday: key === formatDateKey(new Date())
    }
  })
})

const summary = computed(() => ({
  totalSchedules: scheduleList.value.length,
  totalCoaches: new Set(scheduleList.value.map(item => item.coachId).filter(Boolean)).size,
  totalSeats: scheduleList.value.reduce((sum, item) => sum + Number(item.totalSeats || 0), 0),
  totalEnrolled: scheduleList.value.reduce((sum, item) => sum + Number(item.enrolledSeats || 0), 0)
}))

const locationOptions = computed(() => {
  const set = new Set()
  courseList.value.forEach(item => {
    if (item.location) {
      set.add(item.location)
    }
  })
  scheduleList.value.forEach(item => {
    if (item.location) {
      set.add(item.location)
    }
  })
  return Array.from(set)
})

const boardRows = computed(() => {
  const grouped = new Map()
  scheduleList.value.forEach(item => {
    if (!item.coachId) {
      return
    }
    const current = grouped.get(item.coachId) || {
      coachId: item.coachId,
      coachName: item.coachName || `教练#${item.coachId}`,
      schedules: [],
      scheduleCount: 0,
      totalEnrolled: 0
    }
    current.schedules.push(item)
    current.scheduleCount += 1
    current.totalEnrolled += Number(item.enrolledSeats || 0)
    grouped.set(item.coachId, current)
  })

  if (filters.coachId) {
    const coach = coachList.value.find(item => item.id === filters.coachId)
    const existed = grouped.get(filters.coachId)
    if (existed) {
      return [existed]
    }
    if (coach) {
      return [{
        coachId: coach.id,
        coachName: coach.name,
        schedules: [],
        scheduleCount: 0,
        totalEnrolled: 0
      }]
    }
  }

  return Array.from(grouped.values())
    .sort((a, b) => a.coachName.localeCompare(b.coachName, 'zh-Hans-CN'))
})

const gridTemplateColumns = computed(() => `220px repeat(${weekDays.value.length}, minmax(180px, 1fr))`)

const getSchedulesByDay = (row, date) => {
  return row.schedules
    .filter(item => normalizeDate(item.scheduleDate) === date)
    .sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
}

const normalizeDate = (value) => {
  if (!value) return ''
  return String(value).slice(0, 10)
}

const formatDate = (value) => {
  return normalizeDate(value) || '-'
}

const formatTime = (value) => {
  if (!value) return '--:--'
  const text = String(value)
  if (text.includes('T')) {
    return text.slice(11, 16)
  }
  if (text.includes(' ')) {
    return text.slice(11, 16)
  }
  return text.slice(0, 5)
}

const openDetail = (item) => {
  activeSchedule.value = item
  detailVisible.value = true
}

const goSchedulePage = () => {
  router.push('/schedule')
}

const syncWeekRange = () => {
  weekRange.value = [
    formatDateKey(currentWeekStart.value),
    formatDateKey(addDays(currentWeekStart.value, 6))
  ]
}

const resetToCurrentWeek = () => {
  currentWeekStart.value = getWeekStart()
  syncWeekRange()
  loadBoard()
}

const jumpWeek = (offset) => {
  currentWeekStart.value = addDays(currentWeekStart.value, offset * 7)
  syncWeekRange()
  loadBoard()
}

const handleRangeChange = (value) => {
  if (!value || value.length !== 2) return
  currentWeekStart.value = parseDateString(value[0])
  syncWeekRange()
  loadBoard()
}

const loadBaseData = async () => {
  const [coachRes, courseRes] = await Promise.all([
    getCoachList({ pageNum: 1, pageSize: 200, status: 1 }),
    getCourseList({ pageNum: 1, pageSize: 500, type: 2 })
  ])
  coachList.value = coachRes.data.records || []
  courseList.value = courseRes.data.records || []
}

const loadBoard = async () => {
  loading.value = true
  try {
    const res = await getScheduleBoard({
      startDate: weekRange.value[0],
      endDate: weekRange.value[1],
      coachId: filters.coachId,
      location: filters.location,
      status: filters.status
    })
    scheduleList.value = res.data || []
  } catch (e) {
    console.error('加载教练排课看板失败', e)
    ElMessage.error(e?.msg || e?.message || '加载教练排课看板失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadBaseData()
    await loadBoard()
  } catch (e) {
    console.error('初始化教练排课看板失败', e)
    ElMessage.error('初始化失败，请刷新重试')
  }
})
</script>

<style scoped>
.board-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.board-card {
  border-radius: 16px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 6px;
}

.page-subtitle {
  font-size: 13px;
  color: #6b7280;
}

.header-actions,
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
}

.stat-card {
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fafc, #eef2ff);
  border: 1px solid #e5e7eb;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.board-scroll {
  overflow-x: auto;
}

.board-grid {
  display: grid;
  min-width: 1280px;
  gap: 0;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  overflow: hidden;
}

.grid-head {
  padding: 14px 12px;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  color: #374151;
}

.coach-head {
  display: flex;
  align-items: center;
}

.day-head {
  text-align: center;
}

.day-title {
  font-size: 14px;
  font-weight: 700;
}

.day-date {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
}

.coach-cell {
  padding: 16px 14px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.coach-name {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.coach-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.day-cell {
  min-height: 150px;
  padding: 10px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
}

.day-cell.is-today {
  background: #fffaf0;
}

.schedule-card {
  border-radius: 12px;
  padding: 10px 12px;
  margin-bottom: 8px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.schedule-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}

.status-pending {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.status-running {
  background: #ecfdf5;
  border-color: #86efac;
}

.status-finished {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.status-cancelled {
  background: #fef2f2;
  border-color: #fca5a5;
}

.schedule-time {
  font-size: 12px;
  color: #2563eb;
  font-weight: 700;
  margin-bottom: 6px;
}

.schedule-name {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 6px;
  line-height: 1.4;
}

.schedule-meta {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 10px;
}

.schedule-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #4b5563;
}

.empty-slot {
  color: #c0c4cc;
  font-size: 12px;
  line-height: 1.6;
  padding-top: 4px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f3f4f6;
}

.detail-label {
  color: #6b7280;
}

.detail-value {
  color: #111827;
  font-weight: 600;
  text-align: right;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .filter-header {
    flex-direction: column;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }
}
</style>
