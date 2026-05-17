<template>
  <div class="workbench-page">
    <div class="session-layout">
      <el-card shadow="never" class="session-sidebar">
        <template #header>
          <div class="sidebar-header">
            <div>
              <div class="sidebar-title">客服会话</div>
<!--              <div class="sidebar-sub">像 PC 微信一样查看和切换咨询窗口</div>-->
            </div>
            <el-button type="primary" link @click="refreshAll">刷新</el-button>
          </div>
        </template>

        <div class="sidebar-filters">
          <el-input
            v-model="query.keyword"
            placeholder="搜索用户/问题/回复"
            clearable
            @keyup.enter="loadSessions"
          />
          <el-select v-model="query.status" placeholder="会话状态" clearable @change="loadSessions">
            <el-option label="AI处理中" :value="0" />
            <el-option label="已解决" :value="1" />
            <el-option label="待人工" :value="2" />
            <el-option label="本轮已结束" :value="3" />
          </el-select>
          <el-select v-model="query.needHandover" placeholder="转人工" clearable @change="loadSessions">
            <el-option label="否" :value="0" />
            <el-option label="是" :value="1" />
          </el-select>
        </div>

        <div class="handover-strip" v-if="handoverData.length">
          <span class="handover-strip-label">待人工</span>
          <span class="handover-strip-value">{{ handoverData.length }}</span>
        </div>

        <div v-loading="loading" class="session-list">
          <div
            v-for="item in tableData"
            :key="item.conversationKey"
            :class="['session-item', { active: selectedConversationKey === item.conversationKey }]"
            @click="openDetail(item)"
          >
            <div class="session-avatar">
              <img v-if="item.userAvatarUrl" :src="item.userAvatarUrl" alt="avatar" />
              <span v-else>{{ getUserInitial(item.userNickName) }}</span>
            </div>
            <div class="session-content">
              <div class="session-row">
                <div class="session-name-wrap">
                  <span :class="['online-dot', { active: isUserOnline(item) }]"></span>
                  <div class="session-name">{{ item.userNickName || '游客' }}</div>
                  <el-badge v-if="(unreadMap[item.conversationKey] || 0) > 0" :value="formatUnread(unreadMap[item.conversationKey])" class="session-unread" />
                </div>
                <div class="session-time">{{ formatDateTime(item.lastMessageTime) }}</div>
              </div>
              <div class="session-row meta">
                <el-tag size="small" :type="statusType(item.status)" effect="plain">{{ statusText(item.status) }}</el-tag>
                <span class="session-phone">{{ item.userPhone || '-' }}</span>
              </div>
              <div class="session-rounds" v-if="item.sessionCount > 1">{{ item.sessionCount }} 轮咨询</div>
              <div class="session-preview">{{ formatPreview(item) }}</div>
            </div>
          </div>
          <el-empty v-if="!loading && !tableData.length" description="暂无会话" :image-size="80" />
        </div>

        <div class="sidebar-pagination">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            small
            layout="prev, pager, next"
            @current-change="loadSessions"
          />
        </div>
      </el-card>

      <el-card shadow="never" class="chat-panel">
        <template #header>
          <div v-if="currentSession" class="chat-header">
            <div class="chat-user">
              <div class="session-avatar large">
                <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" alt="avatar" />
                <span v-else>{{ getUserInitial(currentUser?.nickName) }}</span>
              </div>
              <div class="chat-user-meta">
                <div class="chat-user-name">{{ currentUser?.nickName || '游客' }}</div>
                <div class="chat-user-sub">
                  <span>{{ currentUser?.phone || '未绑定手机号' }}</span>
                  <span>·</span>
                  <span>{{ currentModeText }}</span>
                </div>
              </div>
            </div>
            <div class="chat-actions">
              <el-tag :type="statusType(currentSession.status)" effect="dark">{{ statusText(currentSession.status) }}</el-tag>
              <el-button v-if="currentSession.needHandover && currentSession.status === 2" @click="takeoverSession">接入人工</el-button>
              <el-button @click="markResolved(currentSession.id)" v-if="canResolveCurrentSession">标记解决</el-button>
              <el-button type="danger" plain @click="terminateCurrentSession" v-if="canTerminateCurrentSession">结束本轮咨询</el-button>
            </div>
          </div>
          <div v-else class="chat-placeholder-head">选择左侧会话后查看详情</div>
        </template>

        <div v-if="currentSession" class="chat-body">
          <div class="history-strip" v-if="sessionHistory.length">
            <span class="history-label">咨询轮次</span>
            <div class="history-list">
              <button
                v-for="item in sessionHistory"
                :key="item.sessionId"
                type="button"
                :class="['history-item', { active: item.sessionId === selectedSessionId }]"
                @click="switchSessionRound(item.sessionId)"
              >
                {{ formatHistoryLabel(item, sessionHistory.length - 1) }}
              </button>
            </div>
          </div>

          <div class="chat-messages" v-loading="detailLoading">
            <template v-for="item in currentMessages" :key="item.id">
              <div v-if="item.id === newMessageDividerId" class="new-message-divider">
                <span class="new-message-divider-text">以下为新消息</span>
              </div>
              <div
                :class="['message-line', item.role === 'user' ? 'from-user' : 'from-service']"
              >
                <div class="message-avatar">
                  <template v-if="item.role === 'user'">
                    <img v-if="currentUser?.avatarUrl" :src="currentUser.avatarUrl" alt="avatar" />
                    <span v-else>{{ getUserInitial(currentUser?.nickName) }}</span>
                  </template>
                  <template v-else>
                    <span class="service-bolt">⚡</span>
                  </template>
                </div>
                <div class="message-main">
                  <div class="message-head">
                    <span class="message-name">{{ item.role === 'user' ? (currentUser?.nickName || '用户') : getAssistantLabel(item) }}</span>
                    <span class="message-time">{{ formatDateTime(item.createTime) }}</span>
                  </div>
                  <div :class="['message-bubble', item.role === 'user' ? 'user' : 'service']">
                    <div class="message-text">{{ item.role === 'user' ? item.content : item.replyText }}</div>
                    <div class="message-extra" v-if="item.role !== 'user' && item.intent">意图：{{ formatIntent(item.intent) }}</div>
                    <div class="card-list" v-if="visibleCards(item.cards).length">
                      <div class="mini-card" v-for="card in visibleCards(item.cards)" :key="card.id + card.title">
                        <div class="mini-card-title">{{ card.title }}</div>
                        <div class="mini-card-sub">{{ card.subtitle }}</div>
                        <div class="mini-card-meta">{{ card.meta }}</div>
                      </div>
                    </div>
                    <div class="action-list" v-if="visibleActions(item.actions).length">
                      <el-tag v-for="action in visibleActions(item.actions)" :key="action.label + action.type" size="small">{{ action.label }}</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-if="!detailLoading && !currentMessages.length" description="暂无消息" :image-size="90" />
          </div>

          <div class="resize-handle" @mousedown="startResize">
            <span class="resize-line"></span>
          </div>

          <div class="composer-stack" :style="{ height: `${bottomPanelHeight}px` }">
            <div class="quick-replies">
              <span class="quick-reply-label">快捷回复</span>
              <div class="quick-reply-list">
                <el-tag
                  v-for="item in quickReplies"
                  :key="item"
                  class="quick-reply-item"
                  effect="plain"
                  @click="useQuickReply(item)"
                >
                  {{ item }}
                </el-tag>
              </div>
            </div>

            <div class="reply-panel">
              <el-input
                v-model="replyForm.replyText"
                type="textarea"
                :rows="4"
                placeholder="输入人工回复内容，用户在小程序端会实时看到"
              />
              <div class="reply-panel-actions">
                <div class="reply-options">
                  <el-checkbox v-model="replyForm.resolveAfterReply" @change="onResolveAfterReplyChange">回复后标记已解决</el-checkbox>
                  <el-checkbox v-model="replyForm.terminateAfterReply" @change="onTerminateAfterReplyChange">回复后结束本轮咨询</el-checkbox>
                </div>
                <div class="reply-buttons">
                  <el-button @click="clearReply">清空</el-button>
                  <el-button type="primary" @click="handleReply">发送回复</el-button>
                </div>
              </div>
            </div>

            <div class="extra-panels">
              <div class="extra-card">
                <div class="extra-title">转人工记录</div>
                <el-empty v-if="!detailData?.handovers?.length" description="暂无记录" :image-size="60" />
                <div v-else class="simple-list">
                  <div class="simple-item" v-for="item in detailData.handovers" :key="item.id">
                    <div>{{ item.latestQuestion }}</div>
                    <div class="simple-meta">{{ item.status === 0 ? '待处理' : '已处理' }} · {{ formatDateTime(item.createTime) }}</div>
                  </div>
                </div>
              </div>
              <div class="extra-card">
                <div class="extra-title">用户反馈</div>
                <el-empty v-if="!detailData?.feedbacks?.length" description="暂无反馈" :image-size="60" />
                <div v-else class="simple-list">
                  <div class="simple-item" v-for="item in detailData.feedbacks" :key="item.id">
                    <div>{{ item.rating === 1 ? '有帮助' : '无帮助' }}{{ item.comment ? ` · ${item.comment}` : '' }}</div>
                    <div class="simple-meta">{{ formatDateTime(item.createTime) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="chat-empty">
          <el-empty description="点击左侧任一会话即可开始处理" :image-size="120" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiHandovers,
  getAiSessionDetail,
  getAiSessions,
  replyAiSession,
  terminateAiSession,
  updateAiSessionStatus
} from '../../api/ai'
import { DEMO_MODE, isCommerceLabel, isCommercePath } from '../../config/demo-mode'

const loading = ref(false)
const detailLoading = ref(false)
const handoverLoading = ref(false)
const tableData = ref([])
const handoverData = ref([])
const total = ref(0)
const detailData = ref(null)
const selectedSessionId = ref(null)
const selectedConversationKey = ref(null)
const unreadMap = reactive({})
const bottomPanelHeight = ref(330)
const newMessageDividerId = ref(null)
let sessionTimer = null
let detailTimer = null
let resizeStartY = 0
let resizeStartHeight = 0

const replyForm = reactive({
  replyText: '',
  resolveAfterReply: false,
  terminateAfterReply: false
})

const query = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: null,
  needHandover: null
})

const quickReplies = [
  '您好，我来继续为您处理这个问题。',
  '请问还有其他问题吗？',
  '如果没有其他问题，本轮咨询先为您结束，后续可随时再次咨询。',
  '已为您记录，稍后继续跟进。'
]

const intentOptions = [
  { label: '课程推荐', value: 'course_recommend' },
  { label: '排课咨询', value: 'course_schedule_query' },
  { label: '订单查询', value: 'order_query' },
  { label: '退款咨询', value: 'refund_help' },
  { label: '课包查询', value: 'package_query' },
  { label: '优惠券咨询', value: 'coupon_query' },
  { label: '签到咨询', value: 'checkin_query' },
  { label: '账号帮助', value: 'account_help' },
  { label: '转人工', value: 'manual_service' },
  { label: '通用问答', value: 'general_faq' },
  { label: '会话结束', value: 'session_closed' }
]

const currentSession = computed(() => detailData.value?.session || null)
const currentUser = computed(() => detailData.value?.user || null)
const currentMessages = computed(() => detailData.value?.detail?.messages || [])
const sessionHistory = computed(() => detailData.value?.sessionHistory || [])
const currentModeText = computed(() => {
  if (!currentSession.value) return 'AI处理中'
  if (currentSession.value.status === 1) return '本轮已解决'
  if (currentSession.value.status === 3) return '本轮已结束'
  if (currentSession.value.status === 2) return '人工处理中'
  const lastService = [...currentMessages.value].reverse().find(item => item.role === 'assistant')
  return lastService?.sourceType === 'human' ? '人工处理中' : 'AI处理中'
})
const canResolveCurrentSession = computed(() => [0, 2].includes(currentSession.value?.status))
const canTerminateCurrentSession = computed(() => [0, 2].includes(currentSession.value?.status))

const loadSessions = async (keepSelection = true) => {
  loading.value = true
  try {
    const previousRecords = tableData.value.reduce((acc, item) => {
      acc[item.conversationKey] = item
      return acc
    }, {})
    const res = await getAiSessions(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    tableData.value.forEach((item) => {
      const previous = previousRecords[item.conversationKey]
      if (selectedConversationKey.value === item.conversationKey) {
        unreadMap[item.conversationKey] = 0
      } else if (!previous) {
        unreadMap[item.conversationKey] = unreadMap[item.conversationKey] || 0
      } else if (previous.lastMessageTime !== item.lastMessageTime) {
        unreadMap[item.conversationKey] = (unreadMap[item.conversationKey] || 0) + 1
      }
    })
    if (!tableData.value.length) {
      selectedSessionId.value = null
      selectedConversationKey.value = null
      detailData.value = null
      return
    }

    const selectedItem = keepSelection && selectedConversationKey.value
      ? tableData.value.find(item => item.conversationKey === selectedConversationKey.value)
      : null

    if (!selectedItem) {
      await loadDetail(tableData.value[0].latestSessionId, true, tableData.value[0].conversationKey)
      return
    }

    if (!currentSession.value || currentSession.value.id === selectedItem.latestSessionId) {
      await loadDetail(selectedItem.latestSessionId, true, selectedItem.conversationKey)
    }
  } finally {
    loading.value = false
  }
}

const loadHandovers = async () => {
  handoverLoading.value = true
  try {
    const res = await getAiHandovers({ status: 0 })
    handoverData.value = res.data || []
  } finally {
    handoverLoading.value = false
  }
}

const loadDetail = async (id, silent = false, conversationKey = selectedConversationKey.value) => {
  if (!id) return
  if (!silent) detailLoading.value = true
  try {
    const previousMessages = selectedSessionId.value === id ? currentMessages.value : []
    const previousIds = new Set(previousMessages.map(item => item.id))
    const res = await getAiSessionDetail(id)
    const nextMessages = res.data?.detail?.messages || []
    if (silent && selectedSessionId.value === id && previousMessages.length) {
      const firstNew = nextMessages.find(item => !previousIds.has(item.id))
      if (firstNew) {
        newMessageDividerId.value = firstNew.id
      }
    } else if (!silent) {
      newMessageDividerId.value = null
    }
    detailData.value = res.data
    selectedConversationKey.value = conversationKey || res.data?.conversationKey || null
    selectedSessionId.value = id
    if (selectedConversationKey.value) {
      unreadMap[selectedConversationKey.value] = 0
    }
  } finally {
    detailLoading.value = false
  }
}

const openDetail = async (item) => {
  replyForm.replyText = ''
  replyForm.resolveAfterReply = false
  replyForm.terminateAfterReply = false
  selectedConversationKey.value = item.conversationKey
  unreadMap[item.conversationKey] = 0
  newMessageDividerId.value = null
  await loadDetail(item.latestSessionId, false, item.conversationKey)
}

const refreshAll = async () => {
  await Promise.all([loadSessions(), loadHandovers()])
}

const markResolved = async (id) => {
  await updateAiSessionStatus(id, { status: 1, needHandover: 0 })
  ElMessage.success('已标记为已解决，本轮咨询已收口')
  await refreshAll()
}

const takeoverSession = async () => {
  if (!currentSession.value?.id) return
  await updateAiSessionStatus(currentSession.value.id, { status: 2, needHandover: 1 })
  ElMessage.success('已标记为人工处理中，请继续回复用户')
  await refreshAll()
}

const terminateCurrentSession = async () => {
  if (!currentSession.value?.id) return
  try {
    await ElMessageBox.confirm('结束后，本轮咨询会保留历史记录，但用户下次咨询将开启新的会话。是否继续？', '结束本轮咨询', {
      type: 'warning',
      confirmButtonText: '确认结束',
      cancelButtonText: '取消'
    })
    await terminateAiSession(currentSession.value.id, {})
    ElMessage.success('本轮咨询已结束')
    await refreshAll()
  } catch (error) {
    // 用户取消结束操作时不提示错误
  }
}

const handleReply = async () => {
  if (!currentSession.value?.id || !replyForm.replyText.trim()) return
  await replyAiSession(currentSession.value.id, {
    replyText: replyForm.replyText.trim(),
    resolveAfterReply: replyForm.resolveAfterReply,
    terminateAfterReply: replyForm.terminateAfterReply
  })
  ElMessage.success(replyForm.terminateAfterReply ? '已回复并结束本轮咨询' : (replyForm.resolveAfterReply ? '已回复并标记解决' : '已发送人工回复'))
  clearReply()
  await refreshAll()
}

const onResolveAfterReplyChange = (value) => {
  if (value) {
    replyForm.terminateAfterReply = false
  }
}

const onTerminateAfterReplyChange = (value) => {
  if (value) {
    replyForm.resolveAfterReply = false
  }
}

const clearReply = () => {
  replyForm.replyText = ''
  replyForm.resolveAfterReply = false
  replyForm.terminateAfterReply = false
}

const switchSessionRound = async (sessionId) => {
  if (!sessionId || sessionId === selectedSessionId.value) return
  newMessageDividerId.value = null
  await loadDetail(sessionId, false, selectedConversationKey.value)
}

const useQuickReply = (text) => {
  replyForm.replyText = text
}

const startPolling = () => {
  stopPolling()
  sessionTimer = window.setInterval(() => {
    loadSessions(true).catch(() => {})
  }, 8000)
  detailTimer = window.setInterval(() => {
    if (selectedSessionId.value) {
      loadDetail(selectedSessionId.value, true).catch(() => {})
      loadHandovers().catch(() => {})
    }
  }, 3000)
}

const stopPolling = () => {
  if (sessionTimer) {
    window.clearInterval(sessionTimer)
    sessionTimer = null
  }
  if (detailTimer) {
    window.clearInterval(detailTimer)
    detailTimer = null
  }
}

const startResize = (event) => {
  resizeStartY = event.clientY
  resizeStartHeight = bottomPanelHeight.value
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', stopResize)
}

const onResizeMove = (event) => {
  const delta = event.clientY - resizeStartY
  const nextHeight = resizeStartHeight - delta
  bottomPanelHeight.value = Math.min(460, Math.max(240, nextHeight))
}

const stopResize = () => {
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', stopResize)
}

const visibleCards = (cards = []) => {
  if (!DEMO_MODE.hideCommerce) return cards || []
  return (cards || []).filter(card => !isCommercePath(card.route || '') && !isCommerceLabel(card.title || ''))
}

const visibleActions = (actions = []) => {
  if (!DEMO_MODE.hideCommerce) return actions || []
  return (actions || []).filter(action => !isCommercePath(action.route || '') && !isCommerceLabel(action.label || ''))
}

const formatIntent = (value) => {
  if (DEMO_MODE.hideCommerce && value === 'product_recommend') {
    return '已下线功能'
  }
  const hit = intentOptions.find(item => item.value === value)
  return hit ? hit.label : (value || '-')
}

const statusText = (status) => ({ 0: 'AI处理中', 1: '已解决', 2: '待人工', 3: '本轮已结束' }[status] || 'AI处理中')
const statusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }[status] || 'info')

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

const formatHistoryLabel = (item, newestIndex) => {
  const label = formatDateTime(item.lastMessageTime)
  const index = sessionHistory.value.findIndex(history => history.sessionId === item.sessionId)
  return index === 0
    ? `最新咨询 · ${label}`
    : `第 ${newestIndex - index + 1} 轮 · ${label}`
}

const formatPreview = (row) => row.lastQuestion || row.lastReply || row.title || '暂无消息'

const getUserInitial = (name) => (name || '客').slice(0, 1)

const formatUnread = (value) => (value > 99 ? '99+' : value)

const isUserOnline = (item) => {
  if (!item?.lastMessageTime || item?.status === 3) return false
  const time = new Date(String(item.lastMessageTime).replace(' ', 'T')).getTime()
  if (Number.isNaN(time)) return false
  return Date.now() - time <= 5 * 60 * 1000
}

const getAssistantLabel = (message) => {
  if (message.sourceType === 'human') return '人工客服'
  if (message.sourceType === 'system') return '系统消息'
  return 'AI客服'
}

onMounted(async () => {
  try {
    await refreshAll()
    startPolling()
  } catch (error) {
    // 401 会由请求拦截器统一处理，这里只避免未捕获 Promise 打到控制台
  }
})

onBeforeUnmount(() => {
  stopPolling()
  stopResize()
})
</script>

<style scoped>
.workbench-page {
  height: calc(100vh - 84px);
  overflow: hidden;
}

.session-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.session-sidebar,
.chat-panel {
  border-radius: 18px;
  height: 100%;
  overflow: hidden;
}

.session-sidebar :deep(.el-card__body),
.chat-panel :deep(.el-card__body) {
  padding: 0;
}

.session-sidebar :deep(.el-card__body) {
  height: calc(100% - 73px);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-panel :deep(.el-card__body) {
  height: calc(100% - 73px);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sidebar-header,
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: #2c2f30;
}

.sidebar-sub,
.chat-placeholder-head {
  font-size: 13px;
  color: #8a8f98;
}

.sidebar-filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.handover-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 16px 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff4ef;
  color: #c45656;
}

.handover-strip-label {
  font-size: 13px;
  font-weight: 600;
}

.handover-strip-value {
  font-size: 18px;
  font-weight: 700;
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.session-item {
  display: flex;
  gap: 12px;
  padding: 14px 12px;
  margin: 0 8px 10px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.session-item:hover {
  background: #f7f8fa;
}

.session-item.active {
  background: #fff7f0;
  border-color: rgba(255, 122, 47, 0.18);
  box-shadow: 0 8px 24px rgba(156, 63, 0, 0.08);
}

.session-avatar {
  width: 46px;
  height: 46px;
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #9c3f00 0%, #ff7a2f 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.session-avatar.large {
  width: 52px;
  height: 52px;
}

.session-avatar img,
.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.session-name-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.session-row.meta {
  justify-content: flex-start;
  margin-top: 8px;
}

.session-name {
  font-weight: 700;
  color: #2c2f30;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.online-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c8cdd6;
  flex-shrink: 0;
}

.online-dot.active {
  background: #34c759;
  box-shadow: 0 0 0 4px rgba(52, 199, 89, 0.14);
}

.session-unread {
  flex-shrink: 0;
}

.session-time,
.session-phone,
.simple-meta,
.message-time,
.message-extra {
  font-size: 12px;
  color: #8a8f98;
}

.session-preview {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.5;
  color: #5d636b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-rounds {
  margin-top: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #9c3f00;
}

.sidebar-pagination {
  display: flex;
  justify-content: center;
  padding: 12px 16px 16px;
  border-top: 1px solid #f0f2f5;
}

.chat-panel {
  display: flex;
  flex-direction: column;
}

.chat-panel :deep(.el-card__header) {
  padding: 18px 20px;
}

.chat-body {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.history-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 20px;
  border-bottom: 1px solid #eef1f5;
  background: #fff;
}

.history-label {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: #5d636b;
}

.history-list {
  display: flex;
  gap: 10px;
  min-width: 0;
  overflow-x: auto;
}

.history-item {
  flex-shrink: 0;
  border: 1px solid #e7ebf0;
  background: #f8fafc;
  color: #5d636b;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-item:hover {
  border-color: rgba(255, 122, 47, 0.24);
  color: #9c3f00;
}

.history-item.active {
  border-color: rgba(255, 122, 47, 0.28);
  background: #fff4e8;
  color: #9c3f00;
  box-shadow: 0 6px 18px rgba(156, 63, 0, 0.08);
}

.chat-user {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chat-user-name {
  font-size: 18px;
  font-weight: 700;
  color: #2c2f30;
}

.chat-user-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 13px;
  color: #8a8f98;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, #f8f9fb 0%, #f4f6f8 100%);
}

.new-message-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 18px;
  color: #c45656;
}

.new-message-divider::before,
.new-message-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(196, 86, 86, 0.24);
}

.new-message-divider-text {
  font-size: 12px;
  font-weight: 700;
  color: #c45656;
}

.message-line {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.message-line.from-service {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #eceff3;
  color: #9c3f00;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 700;
}

.service-bolt {
  font-size: 22px;
  color: #ff7a2f;
  line-height: 1;
}

.message-main {
  max-width: 72%;
}

.message-line.from-service .message-main {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.message-name {
  font-size: 13px;
  font-weight: 600;
  color: #5d636b;
}

.message-bubble {
  border-radius: 18px;
  padding: 14px 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.message-bubble.user {
  background: #ffffff;
  border-top-left-radius: 6px;
}

.message-bubble.service {
  background: #fff6ef;
  border-top-right-radius: 6px;
}

.message-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  color: #2c2f30;
}

.card-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 10px;
}

.mini-card {
  border: 1px solid #f0e6dc;
  background: #ffffff;
  border-radius: 12px;
  padding: 10px;
}

.mini-card-title {
  font-weight: 700;
  color: #2c2f30;
}

.mini-card-sub,
.mini-card-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #8a8f98;
}

.action-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.resize-handle {
  height: 14px;
  flex-shrink: 0;
  background: #fff;
  border-top: 1px solid #eef1f5;
  border-bottom: 1px solid #eef1f5;
  cursor: row-resize;
  display: flex;
  align-items: center;
  justify-content: center;
}

.resize-line {
  width: 56px;
  height: 4px;
  border-radius: 999px;
  background: #d7dce3;
}

.composer-stack {
  flex-shrink: 0;
  min-height: 0;
  overflow-y: auto;
  background: #fff;
}

.quick-replies {
  flex-shrink: 0;
  padding: 14px 20px 10px;
  border-top: 1px solid #f0f2f5;
  background: #fff;
}

.quick-reply-label,
.extra-title {
  font-size: 13px;
  font-weight: 700;
  color: #5d636b;
}

.quick-reply-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.quick-reply-item {
  cursor: pointer;
}

.reply-panel {
  flex-shrink: 0;
  padding: 0 20px 18px;
  background: #fff;
  border-top: 1px solid #f5f6f8;
}

.reply-panel-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 12px;
}

.reply-options,
.reply-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.extra-panels {
  flex-shrink: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding: 0 20px 20px;
  background: #fff;
  max-height: 220px;
  overflow-y: auto;
  border-top: 1px solid #f5f6f8;
}

.extra-card {
  padding: 14px;
  border-radius: 16px;
  background: #f8f9fb;
  min-height: 160px;
}

.simple-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.simple-item {
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff;
}

.chat-empty {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 1280px) {
  .session-layout {
    grid-template-columns: 320px minmax(0, 1fr);
  }

  .message-main {
    max-width: 82%;
  }

  .extra-panels {
    grid-template-columns: 1fr;
  }
}
</style>
