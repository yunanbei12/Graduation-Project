<template>
  <div class="page-wrap">
    <section class="toolbar-card">
      <div class="toolbar-copy">
        <h3>排期管理</h3>
        <p>这里用于验收课程与教练的排课联动，支持创建、查看与取消排期。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增排期</el-button>
    </section>

    <section class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="课程">
          <el-select v-model="filters.courseId" clearable placeholder="全部课程" style="width: 180px">
            <el-option v-for="item in courseOptions" :key="item.courseId" :label="item.courseName" :value="item.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练">
          <el-select v-model="filters.coachId" clearable placeholder="全部教练" style="width: 180px">
            <el-option v-for="item in coachOptions" :key="item.coachId" :label="item.coachName" :value="item.coachId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部状态" style="width: 140px">
            <el-option label="待开始" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="loadSchedules">查询</el-button>
          <el-button text @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-card">
      <el-table :data="tableData.list" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程" min-width="180" />
        <el-table-column prop="coachName" label="教练" min-width="140" />
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="160" />
        <el-table-column label="人数" width="130">
          <template #default="{ row }">
            {{ row.enrolledCount }}/{{ row.capacity }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button text type="danger" :disabled="row.status === 2" @click="handleCancel(row)">
              取消排期
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="pagination.pageNo"
          :page-size="pagination.pageSize"
          :total="tableData.total"
          @current-change="handlePageChange"
        />
      </div>
    </section>

    <el-dialog v-model="dialogVisible" title="新增排期" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="dialog-grid">
          <el-form-item label="课程" prop="courseId">
            <el-select v-model="form.courseId" placeholder="请选择课程">
              <el-option v-for="item in courseOptions" :key="item.courseId" :label="item.courseName" :value="item.courseId" />
            </el-select>
          </el-form-item>
          <el-form-item label="教练" prop="coachId">
            <el-select v-model="form.coachId" placeholder="请选择教练">
              <el-option v-for="item in coachOptions" :key="item.coachId" :label="item.coachName" :value="item.coachId" />
            </el-select>
          </el-form-item>
          <el-form-item label="排课日期" prop="scheduleDate">
            <el-date-picker v-model="form.scheduleDate" type="date" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="上课地点" prop="location">
            <el-input v-model="form.location" />
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" />
          </el-form-item>
          <el-form-item label="容量" prop="capacity">
            <el-input-number v-model="form.capacity" :min="1" />
          </el-form-item>
          <el-form-item label="最小成团" prop="minGroupCount">
            <el-input-number v-model="form.minGroupCount" :min="1" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import { cancelSchedule, createSchedule, fetchCoaches, fetchCourses, fetchSchedules } from "@/api/admin";
import type { CoachItem, CourseItem, PageResponse, ScheduleItem, SchedulePayload } from "@/types/admin";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const formRef = ref<FormInstance>();
const courseOptions = ref<CourseItem[]>([]);
const coachOptions = ref<CoachItem[]>([]);

const filters = reactive({
  courseId: undefined as number | undefined,
  coachId: undefined as number | undefined,
  status: undefined as number | undefined
});

const pagination = reactive({
  pageNo: 1,
  pageSize: 10
});

const tableData = reactive<PageResponse<ScheduleItem>>({
  list: [],
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const form = reactive<SchedulePayload>({
  courseId: 0,
  coachId: 0,
  scheduleDate: "",
  startTime: "",
  endTime: "",
  location: "",
  capacity: 20,
  minGroupCount: 6
});

const rules: FormRules = {
  courseId: [{ required: true, message: "请选择课程", trigger: "change" }],
  coachId: [{ required: true, message: "请选择教练", trigger: "change" }],
  scheduleDate: [{ required: true, message: "请选择日期", trigger: "change" }],
  startTime: [{ required: true, message: "请选择开始时间", trigger: "change" }],
  endTime: [{ required: true, message: "请选择结束时间", trigger: "change" }],
  location: [{ required: true, message: "请输入地点", trigger: "blur" }],
  capacity: [{ required: true, message: "请输入容量", trigger: "change" }],
  minGroupCount: [{ required: true, message: "请输入最小成团人数", trigger: "change" }]
};

async function loadOptions() {
  const [courseResult, coachResult] = await Promise.all([
    fetchCourses({ pageNo: 1, pageSize: 100 }),
    fetchCoaches({ pageNo: 1, pageSize: 100 })
  ]);
  courseOptions.value = courseResult.list;
  coachOptions.value = coachResult.list;
}

async function loadSchedules() {
  loading.value = true;
  try {
    const result = await fetchSchedules({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      courseId: filters.courseId,
      coachId: filters.coachId,
      status: filters.status
    });
    Object.assign(tableData, result);
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  filters.courseId = undefined;
  filters.coachId = undefined;
  filters.status = undefined;
  pagination.pageNo = 1;
  loadSchedules();
}

function handlePageChange(page: number) {
  pagination.pageNo = page;
  loadSchedules();
}

function openCreateDialog() {
  Object.assign(form, {
    courseId: 0,
    coachId: 0,
    scheduleDate: "",
    startTime: "",
    endTime: "",
    location: "",
    capacity: 20,
    minGroupCount: 6
  });
  formRef.value?.clearValidate();
  dialogVisible.value = true;
}

async function submitForm() {
  if (!formRef.value) {
    return;
  }
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) {
    return;
  }

  saving.value = true;
  try {
    await createSchedule(form);
    ElMessage.success("排期创建成功");
    dialogVisible.value = false;
    loadSchedules();
  } finally {
    saving.value = false;
  }
}

async function handleCancel(row: ScheduleItem) {
  try {
    await ElMessageBox.confirm(`确认取消课程「${row.courseName ?? ""}」的这条排期吗？`, "取消排期", {
      type: "warning"
    });
  } catch {
    return;
  }

  await cancelSchedule(row.scheduleId, "后台手动取消");
  ElMessage.success("排期已取消");
  loadSchedules();
}

function statusText(status: number) {
  if (status === 1) {
    return "已完成";
  }
  if (status === 2) {
    return "已取消";
  }
  return "待开始";
}

function statusTagType(status: number) {
  if (status === 1) {
    return "success";
  }
  if (status === 2) {
    return "danger";
  }
  return "warning";
}

onMounted(async () => {
  await loadOptions();
  await loadSchedules();
});
</script>

<style scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.toolbar-card,
.filter-card,
.table-card {
  padding: 22px 24px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 44px rgba(17, 44, 36, 0.08);
}

.toolbar-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-copy h3 {
  margin: 0;
  color: #173042;
  font-size: 24px;
}

.toolbar-copy p {
  margin: 10px 0 0;
  color: #617885;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 18px;
}

@media (max-width: 768px) {
  .toolbar-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dialog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
