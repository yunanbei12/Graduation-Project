<template>
  <div class="page-wrap">
    <section class="toolbar-card">
      <div class="toolbar-copy">
        <h3>课程管理</h3>
        <p>这一页用于验收课程的新增、编辑、上下架与分页查询。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增课程</el-button>
    </section>

    <section class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="课程名 / 编码 / 运动类型" clearable />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="filters.courseType" placeholder="全部" clearable style="width: 140px">
            <el-option label="私教" :value="1" />
            <el-option label="团课" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="loadCourses">查询</el-button>
          <el-button text @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-card">
      <el-table :data="tableData.list" v-loading="loading" stripe>
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="courseCode" label="课程编码" min-width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.courseType === 1 ? 'warning' : 'success'">
              {{ row.courseType === 1 ? "私教" : "团课" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sportType" label="运动项目" width="120" />
        <el-table-column prop="price" label="售价" width="110" />
        <el-table-column prop="lessonCount" label="课时数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? "上架" : "下架" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button text @click="toggleStatus(row)">
              {{ row.status === 1 ? "下架" : "上架" }}
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增课程' : '编辑课程'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="108px">
        <div class="dialog-grid">
          <el-form-item label="课程名称" prop="courseName">
            <el-input v-model="form.courseName" />
          </el-form-item>
          <el-form-item label="课程编码" prop="courseCode">
            <el-input v-model="form.courseCode" />
          </el-form-item>
          <el-form-item label="课程类型" prop="courseType">
            <el-select v-model="form.courseType">
              <el-option label="私教" :value="1" />
              <el-option label="团课" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="运动项目" prop="sportType">
            <el-input v-model="form.sportType" />
          </el-form-item>
          <el-form-item label="售价" prop="price">
            <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="原价">
            <el-input-number v-model="form.originalPrice" :min="0" :precision="2" :step="10" />
          </el-form-item>
          <el-form-item label="课时数">
            <el-input-number v-model="form.lessonCount" :min="0" />
          </el-form-item>
          <el-form-item label="有效期(天)">
            <el-input-number v-model="form.validityDays" :min="0" />
          </el-form-item>
          <el-form-item label="上课地点">
            <el-input v-model="form.fixedLocation" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="上架" :value="1" />
              <el-option label="下架" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="服务区域">
            <el-select v-model="form.serviceAreas" multiple allow-create filterable default-first-option>
              <el-option v-for="item in form.serviceAreas" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
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
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { createCourse, fetchCourses, updateCourse, updateCourseStatus } from "@/api/admin";
import type { CourseItem, CoursePayload, PageResponse } from "@/types/admin";

type DialogMode = "create" | "edit";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref<DialogMode>("create");
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const filters = reactive({
  keyword: "",
  courseType: undefined as number | undefined,
  status: undefined as number | undefined
});

const pagination = reactive({
  pageNo: 1,
  pageSize: 10
});

const tableData = reactive<PageResponse<CourseItem>>({
  list: [],
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const form = reactive<CoursePayload>({
  courseName: "",
  courseCode: "",
  courseType: 2,
  sportType: "",
  price: 0,
  originalPrice: null,
  lessonCount: null,
  validityDays: null,
  isDoorToDoor: 0,
  serviceAreas: [],
  fixedLocation: "",
  description: "",
  status: 1
});

const rules: FormRules = {
  courseName: [{ required: true, message: "请输入课程名称", trigger: "blur" }],
  courseType: [{ required: true, message: "请选择课程类型", trigger: "change" }],
  sportType: [{ required: true, message: "请输入运动项目", trigger: "blur" }],
  price: [{ required: true, message: "请输入售价", trigger: "change" }]
};

async function loadCourses() {
  loading.value = true;
  try {
    const result = await fetchCourses({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      courseType: filters.courseType,
      status: filters.status
    });
    Object.assign(tableData, result);
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  filters.keyword = "";
  filters.courseType = undefined;
  filters.status = undefined;
  pagination.pageNo = 1;
  loadCourses();
}

function handlePageChange(page: number) {
  pagination.pageNo = page;
  loadCourses();
}

function openCreateDialog() {
  dialogMode.value = "create";
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: CourseItem) {
  dialogMode.value = "edit";
  editingId.value = row.courseId;
  Object.assign(form, {
    courseName: row.courseName,
    courseCode: row.courseCode || "",
    courseType: row.courseType,
    sportType: row.sportType,
    coverUrl: row.coverUrl || "",
    description: row.description || "",
    detailImages: row.detailImages || [],
    price: row.price,
    originalPrice: row.originalPrice,
    lessonCount: row.lessonCount,
    validityDays: row.validityDays,
    isDoorToDoor: row.isDoorToDoor,
    serviceAreas: row.serviceAreas || [],
    fixedScheduleDesc: row.fixedScheduleDesc || "",
    fixedLocation: row.fixedLocation || "",
    maxParticipants: row.maxParticipants,
    groupSuccessCount: row.groupSuccessCount,
    operationWeight: row.operationWeight,
    status: row.status
  });
  dialogVisible.value = true;
}

function resetForm() {
  Object.assign(form, {
    courseName: "",
    courseCode: "",
    courseType: 2,
    sportType: "",
    coverUrl: "",
    description: "",
    detailImages: [],
    price: 0,
    originalPrice: null,
    lessonCount: null,
    validityDays: null,
    isDoorToDoor: 0,
    serviceAreas: [],
    fixedScheduleDesc: "",
    fixedLocation: "",
    maxParticipants: null,
    groupSuccessCount: null,
    operationWeight: 1,
    status: 1
  });
  formRef.value?.clearValidate();
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
    if (dialogMode.value === "create") {
      await createCourse(form);
      ElMessage.success("课程创建成功");
    } else if (editingId.value) {
      await updateCourse(editingId.value, form);
      ElMessage.success("课程更新成功");
    }
    dialogVisible.value = false;
    loadCourses();
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row: CourseItem) {
  const nextStatus = row.status === 1 ? 0 : 1;
  await updateCourseStatus(row.courseId, nextStatus);
  ElMessage.success(nextStatus === 1 ? "课程已上架" : "课程已下架");
  loadCourses();
}

onMounted(loadCourses);
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

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 4px 18px;
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
