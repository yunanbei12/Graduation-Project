<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="query.name" placeholder="姓名" clearable style="width: 120px" @keyup.enter="loadData" />
          <el-input v-model="query.phone" placeholder="电话" clearable style="width: 130px" @keyup.enter="loadData" />
          <el-input v-model="query.skills" placeholder="擅长项目" clearable style="width: 120px" @keyup.enter="loadData" />
          <el-select v-model="query.status" placeholder="在职/离职" clearable style="width: 110px" @change="loadData">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
          <el-button type="primary" @click="loadData">查询</el-button>
        </div>
        <el-button type="primary" @click="handleAdd">新增教练</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="englishName" label="英文名" width="120" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :src="row.avatar" :size="36" />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="years" label="教龄(年)" width="100" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="skills" label="擅长" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '在职' : '离职' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑教练' : '新增教练'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="英文名">
          <el-input v-model="form.englishName" />
        </el-form-item>
        <el-form-item label="头像">
          <ImageUpload v-model="form.avatar" />
          <div class="form-tip">用于教练列表卡片展示</div>
        </el-form-item>
        <el-form-item label="详情图片">
          <ImageUpload v-model="form.pic" />
          <div class="form-tip">用于教练详情页大图展示</div>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="方便后台联系教练" />
        </el-form-item>
        <el-form-item label="教龄(年)">
          <el-input-number v-model="form.years" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="擅长项目">
          <div class="skill-editor">
            <div class="skill-tags" v-if="skillList.length">
              <el-tag
                v-for="(skill, idx) in skillList"
                :key="idx"
                closable
                @close="removeSkill(idx)"
                class="skill-tag"
                type="success"
              >
                {{ skill }}
              </el-tag>
            </div>
            <div class="skill-add">
              <el-input v-model="newSkill" placeholder="输入项目名称" style="width: 200px; margin-right: 8px" @keyup.enter="addSkill" />
              <el-button type="primary" size="small" @click="addSkill">添加</el-button>
            </div>
            <div class="skill-presets" v-if="categoryPresets.length">
              <span class="preset-label">快捷添加：</span>
              <el-button size="small" v-for="cat in categoryPresets" :key="cat.id" @click="addPresetSkill(cat.name)">
                {{ cat.name }}
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="认证资质">
          <div class="cert-editor">
            <div class="cert-tags" v-if="certList.length">
              <el-tag
                v-for="(cert, idx) in certList"
                :key="idx"
                closable
                @close="removeCert(idx)"
                class="cert-tag"
                type="warning"
              >
                {{ cert }}
              </el-tag>
            </div>
            <div class="cert-add">
              <el-input v-model="newCert" placeholder="输入认证名称" style="width: 200px; margin-right: 8px" @keyup.enter="addCert" />
              <el-button type="primary" size="small" @click="addCert">添加</el-button>
            </div>
            <div class="cert-presets">
              <span class="preset-label">快捷添加：</span>
              <el-button size="small" v-for="preset in certPresets" :key="preset" @click="addPresetCert(preset)">
                {{ preset }}
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
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
import { getCoachList, addCoach, updateCoach, deleteCoach } from '../../api/coach'
import { getCourseCategoryList } from '../../api/course'
import ImageUpload from '../../components/ImageUpload.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, name: '', phone: '', skills: '', status: null })
const form = reactive({ name: '', englishName: '', avatar: '', pic: '', phone: '', years: 0, rating: 5.0, skills: '', certs: '', bio: '', status: 1 })
const rules = { name: [{ required: true, message: '请输入姓名', trigger: 'blur' }] }

// 擅长项目可视化编辑
const skillList = ref([])
const newSkill = ref('')
const categoryPresets = ref([])

const addSkill = () => {
  const skill = newSkill.value.trim()
  if (skill && !skillList.value.includes(skill)) {
    skillList.value.push(skill)
    newSkill.value = ''
  }
}

const addPresetSkill = (skill) => {
  // 自动匹配emoji
  const emoji = getSkillEmoji(skill)
  const skillWithEmoji = emoji ? `${emoji} ${skill}` : skill
  if (!skillList.value.includes(skillWithEmoji) && !skillList.value.includes(skill)) {
    skillList.value.push(skillWithEmoji)
  }
}

// 根据项目名称匹配emoji
const getSkillEmoji = (skill) => {
  const emojiMap = {
    '篮球': '🏀',
    '足球': '⚽',
    '羽毛球': '🏸',
    '网球': '🎾',
    '乒乓球': '🏓',
    '游泳': '🏊',
    '健身': '💪',
    '瑜伽': '🧘',
    '跑步': '🏃',
    '骑行': '🚴',
    '跆拳道': '🥋',
    '拳击': '🥊',
    '高尔夫': '⛳',
    '排球': '🏐',
    '武术': '🥋',
    '舞蹈': '💃',
    '体适能': '💪',
    '体能训练': '💪',
    '减脂': '🔥',
    '增肌': '💪',
    '康复': '🏥',
    '拉伸': '🧘',
    '普拉提': '🧘',
    '格斗': '🥊',
    '散打': '🥊',
    '柔道': '🥋',
    '太极': '🥋',
    '攀岩': '🧗',
    '滑雪': '⛷',
    '轮滑': '🛼',
    '滑板': '🛹'
  }
  // 精确匹配
  if (emojiMap[skill]) return emojiMap[skill]
  // 模糊匹配
  for (const key in emojiMap) {
    if (skill.includes(key) || key.includes(skill)) {
      return emojiMap[key]
    }
  }
  return '⚡' // 默认图标
}

const removeSkill = (idx) => {
  skillList.value.splice(idx, 1)
}

const parseSkills = (skills) => {
  if (!skills) return []
  return skills.split(',').map(s => s.trim()).filter(s => s)
}

const loadCategoryPresets = async () => {
  try {
    const res = await getCourseCategoryList()
    categoryPresets.value = res.data || []
  } catch (e) {
    console.error('加载课程分类失败', e)
  }
}

// 认证资质可视化编辑
const certList = ref([])
const newCert = ref('')
const certPresets = [
  '📜 国家职业资格认证',
  '🇺🇸 ACE美国运动委员会认证',
  '🏆 NASM美国国家运动医学会认证',
  '🏅 ACSM美国运动医学会认证',
  '💪 NSCA美国体能协会认证',
  '🌏 AASFP亚洲体适能专业学院认证',
  '🥇 国家一级运动员',
  '🥈 国家二级运动员',
  '❤️ CPR急救证书'
]

const addCert = () => {
  const cert = newCert.value.trim()
  if (cert && !certList.value.includes(cert)) {
    certList.value.push(cert)
    newCert.value = ''
  }
}

const addPresetCert = (preset) => {
  if (!certList.value.includes(preset)) {
    certList.value.push(preset)
  }
}

const removeCert = (idx) => {
  certList.value.splice(idx, 1)
}

const parseCerts = (certs) => {
  if (!certs) return []
  return certs.split(',').map(c => c.trim()).filter(c => c)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCoachList(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingId.value = null
  Object.assign(form, { name: '', englishName: '', avatar: '', pic: '', phone: '', years: 0, rating: 5.0, skills: '', certs: '', bio: '', status: 1 })
  certList.value = []
  skillList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingId.value = row.id
  Object.assign(form, row)
  certList.value = parseCerts(row.certs)
  skillList.value = parseSkills(row.skills)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  const submitData = { ...form }
  // 将认证列表转为逗号分隔字符串
  submitData.certs = certList.value.length > 0 ? certList.value.join(',') : null
  // 将擅长项目转为逗号分隔字符串
  submitData.skills = skillList.value.length > 0 ? skillList.value.join(',') : null
  if (editingId.value) {
    await updateCoach({ ...submitData, id: editingId.value })
  } else {
    delete submitData.id  // 新增时删除id，让数据库自动生成
    await addCoach(submitData)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确认删除该教练？', '提示')
  await deleteCoach(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadData()
  loadCategoryPresets()
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px; }
.toolbar-left { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.title { font-size: 16px; font-weight: 600; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.cert-editor { width: 100%; }
.cert-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.cert-tag { font-size: 13px; }
.cert-add { display: flex; align-items: center; margin-bottom: 8px; }
.cert-presets { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
.preset-label { font-size: 12px; color: #909399; margin-right: 4px; }
.skill-editor { width: 100%; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.skill-tag { font-size: 13px; }
.skill-add { display: flex; align-items: center; margin-bottom: 8px; }
.skill-presets { display: flex; flex-wrap: wrap; align-items: center; gap: 6px; }
</style>
