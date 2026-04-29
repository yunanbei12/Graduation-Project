<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in cards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>热门咨询意图</template>
          <el-table :data="topIntents" stripe>
            <el-table-column prop="intent" label="意图编码" min-width="180" />
            <el-table-column label="次数" width="100">
              <template #default="{ row }">{{ row.count }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>运营建议</template>
          <div class="tips">
            <div class="tip-item">如果待人工会话持续升高，优先补充知识库中的退款、订单和账号场景。</div>
            <div class="tip-item">若通用问答占比过高，说明快捷入口和知识标签仍可继续细化。</div>
            <div class="tip-item">把高频无帮助反馈的问题沉淀为规则或标准问答，命中率会提升更明显。</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAiStatsSummary } from '../../api/ai'

const summary = ref({})

const cards = computed(() => [
  { label: '总会话数', value: summary.value.totalSessions || 0 },
  { label: '已解决会话', value: summary.value.resolvedSessions || 0 },
  { label: '转人工会话', value: summary.value.handoverSessions || 0 },
  { label: '知识库条数', value: summary.value.knowledgeCount || 0 },
  { label: '正向反馈', value: summary.value.positiveFeedback || 0 },
  { label: '负向反馈', value: summary.value.negativeFeedback || 0 }
])

const topIntents = computed(() => summary.value.topIntents || [])

const loadData = async () => {
  const res = await getAiStatsSummary()
  summary.value = res.data || {}
}

onMounted(loadData)
</script>

<style scoped>
.stat-card { min-height: 120px; display: flex; flex-direction: column; justify-content: center; }
.stat-label { color: #888; font-size: 14px; }
.stat-value { font-size: 30px; font-weight: 700; margin-top: 10px; }
.tips { display: flex; flex-direction: column; gap: 12px; }
.tip-item { padding: 12px 14px; border-radius: 12px; background: #f7f8fa; color: #555; line-height: 1.7; }
</style>
