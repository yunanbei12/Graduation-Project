<template>
  <div class="page-wrap">
    <section class="toolbar-card">
      <div class="toolbar-copy">
        <h3>教练管理</h3>
        <p>支持教练新增、编辑与工作量查看，方便你可视化验收后台基础数据模块。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增教练</el-button>
    </section>

    <section class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="教练名 / 手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="loadCoaches">查询</el-button>
          <el-button text @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </section>

    <section class="table-card">
      <el-table :data="tableData.list" v-loading="loading" stripe>
        <el-table-column prop="coachName" label="教练姓名" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="项目" min-width="180">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="item in row.sportItems" :key="item" type="success">{{ item }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="课时费" width="110" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? "启用" : "禁用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button text @click="showWorkload(row)">工作量</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增教练' : '编辑教练'" width="680px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <div class="dialog-grid">
          <el-form-item label="教练姓名" prop="coachName">
            <el-input v-model="form.coachName" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="form.gender">
              <el-option label="未知" :value="0" />
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="课时费">
            <el-input-number v-model="form.hourlyRate" :min="0" :precision="2" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="授课项目">
            <el-select v-model="form.sportItems" multiple allow-create filterable default-first-option>
              <el-option v-for="item in form.sportItems" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="简介">
          <el-input v-model="form.introduction" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="workloadVisible" title="教练工作量" width="500px">
      <div class="workload-grid" v-if="workload">
        <article class="workload-card">
          <span>总排期</span>
          <strong>{{ workload.totalSchedules }}</strong>
        </article>
        <article class="workload-card">
          <span>待上课</span>
          <strong>{{ workload.upcomingSchedules }}</strong>
        </article>
        <article class="workload-card">
          <span>已完成</span>
          <strong>{{ workload.completedSchedules }}</strong>
        </article>
        <article class="workload-card">
          <span>已取消</span>
          <strong>{{ workload.cancelledSchedules }}</strong>
        </article>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import { createCoach, fetchCoaches, fetchCoachWorkload, updateCoach } from "@/api/admin";
import type { CoachItem, CoachPayload, CoachWorkload, PageResponse } from "@/types/admin";

type DialogMode = "create" | "edit";

const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const workloadVisible = ref(false);
const workload = ref<CoachWorkload | null>(null);
const dialogMode = ref<DialogMode>("create");
const editingId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const filters = reactive({
  keyword: "",
  status: undefined as number | undefined
});

const pagination = reactive({
  pageNo: 1,
  pageSize: 10
});

const tableData = reactive<PageResponse<CoachItem>>({
  list: [],
  pageNo: 1,
  pageSize: 10,
  total: 0
});

const form = reactive<CoachPayload>({
  coachName: "",
  phone: "",
  gender: 1,
  sportItems: [],
  introduction: "",
  hourlyRate: 0,
  status: 1
});

const rules: FormRules = {
  coachName: [{ required: true, message: "请输入教练姓名", trigger: "blur" }]
};

async function loadCoaches() {
  loading.value = true;
  try {
    const result = await fetchCoaches({
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      status: filters.status
    });
    Object.assign(tableData, result);
  } finally {
    loading.value = false;
  }
}

function resetFilters() {
  filters.keyword = "";
  filters.status = undefined;
  pagination.pageNo = 1;
  loadCoaches();
}

function handlePageChange(page: number) {
  pagination.pageNo = page;
  loadCoaches();
}

function openCreateDialog() {
  dialogMode.value = "create";
  editingId.value = null;
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: CoachItem) {
  dialogMode.value = "edit";
  editingId.value = row.coachId;
  Object.assign(form, {
    coachName: row.coachName,
    phone: row.phone || "",
    idCardNo: row.idCardNo || "",
    gender: row.gender,
    sportItems: row.sportItems || [],
    introduction: row.introduction || "",
    hourlyRate: row.hourlyRate,
    availableTimes: row.availableTimes || [],
    status: row.status
  });
  dialogVisible.value = true;
}

function resetForm() {
  Object.assign(form, {
    coachName: "",
    phone: "",
    idCardNo: "",
    gender: 1,
    sportItems: [],
    introduction: "",
    hourlyRate: 0,
    availableTimes: [],
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
      await createCoach(form);
      ElMessage.success("教练创建成功");
    } else if (editingId.value) {
      await updateCoach(editingId.value, form);
      ElMessage.success("教练更新成功");
    }
    dialogVisible.value = false;
    loadCoaches();
  } finally {
    saving.value = false;
  }
}

async function showWorkload(row: CoachItem) {
  workload.value = await fetchCoachWorkload(row.coachId);
  workloadVisible.value = true;
}

onMounted(loadCoaches);
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

.dialog-grid,
.workload-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.workload-card {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(135deg, #f2faf7, #eef4ff);
}

.workload-card span {
  color: #64808d;
  font-size: 13px;
}

.workload-card strong {
  display: block;
  margin-top: 10px;
  color: #173042;
  font-size: 28px;
}

@media (max-width: 768px) {
  .toolbar-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .dialog-grid,
  .workload-grid {
    grid-template-columns: 1fr;
  }
}
</style>
