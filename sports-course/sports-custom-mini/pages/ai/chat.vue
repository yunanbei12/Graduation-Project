<template>
  <view class="page">
    <view class="nav-bar">
      <text class="nav-back" @tap="goBack">←</text>
      <view class="nav-center">
        <text class="nav-title">AI 智能客服</text>
        <text class="nav-sub">规则引擎 + 智能推荐</text>
      </view>
      <text class="nav-refresh" @tap="resetChat">⟳</text>
    </view>

    <scroll-view scroll-y class="chat-scroll" :scroll-into-view="scrollIntoView">
      <view v-if="statusBanner" :class="['handover-banner', statusBanner.type === 'ended' ? 'ended' : '']">
        <text class="handover-banner-dot"></text>
        <text class="handover-banner-text">{{ statusBanner.text }}</text>
      </view>

      <view class="message-list">
        <view v-for="(item, index) in messages" :key="item.localId || index" :id="'msg-' + index" :class="['message-row', item.role]">
          <view class="avatar">
            <image v-if="item.role === 'user' && currentUserAvatar" class="avatar-img" :src="currentUserAvatar.startsWith('/static/') ? currentUserAvatar : getImageUrl(currentUserAvatar)" mode="aspectFill" />
            <text v-else-if="item.role === 'assistant'" class="assistant-bolt">⚡</text>
            <text v-else>{{ item.role === 'user' ? '我' : 'AI' }}</text>
          </view>
          <view class="bubble-wrap">
            <view class="bubble">
              <text class="bubble-text" v-if="item.role === 'user'">{{ item.content }}</text>
              <template v-else>
                <view class="message-meta">
                  <text :class="['source-badge', item.sourceType === 'human' ? 'human' : 'ai']">{{ getAssistantLabel(item) }}</text>
                </view>
                <text class="bubble-text">{{ item.replyText }}</text>
                <view class="card-list" v-if="item.cards && item.cards.length">
                  <view class="biz-card" v-for="card in item.cards" :key="card.id + card.title" @tap="goByRoute(card.route)">
                    <image v-if="card.image" class="biz-card-img" :src="getImageUrl(card.image)" mode="aspectFill" />
                    <view class="biz-card-body">
                      <text class="biz-card-title">{{ card.title }}</text>
                      <text class="biz-card-sub">{{ card.subtitle }}</text>
                      <view class="biz-card-footer">
                        <text class="biz-card-price" v-if="card.price">{{ card.price }}</text>
                        <text class="biz-card-meta">{{ card.meta }}</text>
                      </view>
                    </view>
                  </view>
                </view>
                <view class="action-list" v-if="getDisplayActions(item).length">
                  <view class="action-chip" v-for="action in getDisplayActions(item)" :key="action.label + action.type" @tap="handleAction(action, item)">
                    <text class="action-chip-text">{{ action.label }}</text>
                  </view>
                </view>
                <view class="feedback-row" v-if="item.messageId && isLoggedIn && item.sourceType !== 'human' && item.sourceType !== 'system' && !item.feedbackSent">
                  <view class="feedback-btn feedback-good" @tap="submitFeedback(item, 1)">
                    <text class="feedback-icon">👍</text>
                    <text class="feedback-label">有用</text>
                  </view>
                  <view class="feedback-btn feedback-bad" @tap="submitFeedback(item, 0)">
                    <text class="feedback-icon">👎</text>
                    <text class="feedback-label">没用</text>
                  </view>
                </view>
              </template>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 160rpx;"></view>
    </scroll-view>

    <view class="quick-section">
      <text class="quick-title">快捷问题</text>
      <scroll-view scroll-x class="quick-scroll" :show-scrollbar="false">
        <view class="quick-list">
          <view class="quick-item" v-for="item in quickQuestions" :key="item" @tap="sendMessage(item)">
            <text class="quick-item-text">{{ item }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="composer">
      <input class="composer-input" v-model="inputValue" confirm-type="send" placeholder="请输入你的问题，比如：我的课包还剩几节" @confirm="sendMessage()" />
      <view class="composer-btn" @tap="sendMessage()">
        <text class="composer-btn-text">发送</text>
      </view>
    </view>
  </view>
</template>

<script>
import { chatWithAi, getAiSessionDetail, requestAiHandover, submitAiFeedback } from '../../api/ai'
import config from '../../utils/config'
import { TAB_BAR_PAGES, canAccessPage, filterRecommendActions, filterRecommendCards } from '../../utils/demo-mode'

export default {
  data() {
    return {
      sessionId: null,
      inputValue: '',
      loading: false,
      isSyncing: false,
      pollTimer: null,
      pollPauseUntil: 0,
      scrollIntoView: '',
      cacheScope: 'guest',
      sessionStatus: 0,
      sessionNeedHandover: false,
      currentUserAvatar: '',
      guestToken: '',
      messages: [
        {
          localId: 'welcome',
          role: 'assistant',
          replyText: '你好，我是 KINETIC 智能客服。我可以帮你推荐课程、查询订单、查看课包、筛优惠券，也能把复杂问题转到后台人工处理。',
          sourceType: 'system'
        }
      ],
      quickQuestions: [
        '推荐适合我的课程',
        '团课最近什么时候有名额',
        '我的课程订单怎么样了',
        '我的课包还剩几节',
        '我有哪些可用优惠券',
        '退款怎么操作'
      ]
    }
  },
  computed: {
    isLoggedIn() {
      return !!uni.getStorageSync('token')
    },
    statusBanner() {
      if (this.sessionStatus === 1) {
        return {
          type: 'ended',
          text: '当前问题已处理完成，继续发送消息将自动开启新的咨询'
        }
      }
      if (this.sessionStatus === 3) {
        return {
          type: 'ended',
          text: '本轮咨询已结束，继续发送消息将自动开启新的咨询'
        }
      }
      if (this.sessionStatus === 2 || this.sessionNeedHandover) {
        return {
          type: 'handover',
          text: '会话已转人工处理中，人工客服回复后会自动同步到当前窗口'
        }
      }
      return null
    }
  },
  onLoad() {
    this.cacheScope = this.getCacheScope()
    this.syncCurrentUser()
    this.restoreLocalMessages()
    if (this.sessionId) {
      this.loadSessionDetail()
    }
    this.startPolling()
  },
  onShow() {
    this.syncCurrentUser()
    const nextScope = this.getCacheScope()
    if (nextScope !== this.cacheScope) {
      this.cacheScope = nextScope
      this.restoreLocalMessages()
    }
    if (this.sessionId) {
      this.loadSessionDetail()
    }
    this.startPolling()
  },
  onHide() {
    this.stopPolling()
  },
  onUnload() {
    this.stopPolling()
  },
  methods: {
    getCacheScope() {
      const token = uni.getStorageSync('token')
      if (!token) return 'guest'
      try {
        const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
        return userInfo.userId ? `user_${userInfo.userId}` : `token_${String(token).slice(-12)}`
      } catch (e) {
        return `token_${String(token).slice(-12)}`
      }
    },
    getMessageStorageKey() {
      return `ai_chat_messages_${this.cacheScope}`
    },
    getSessionStorageKey() {
      return `ai_chat_session_id_${this.cacheScope}`
    },
    getGuestTokenStorageKey() {
      return 'ai_chat_guest_token'
    },
    buildWelcomeMessage() {
      return {
        localId: 'welcome',
        role: 'assistant',
        replyText: '你好，我是 KINETIC 智能客服。我可以帮你推荐课程、查询订单、查看课包、筛优惠券，也能把复杂问题转到后台人工处理。',
        sourceType: 'system'
      }
    },
    syncCurrentUser() {
      try {
        const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
        this.currentUserAvatar = userInfo.avatarUrl || '/static/头像.png'
      } catch (e) {
        this.currentUserAvatar = ''
      }
    },
    getImageUrl(url) {
      return config.getImageUrl(url)
    },
    sanitizeAssistantMessage(message = {}) {
      return {
        ...message,
        cards: filterRecommendCards(message.cards || []),
        actions: filterRecommendActions(message.actions || [])
      }
    },
    restoreLocalMessages() {
      this.guestToken = this.getGuestToken()
      this.sessionId = uni.getStorageSync(this.getSessionStorageKey()) || null
      this.sessionStatus = 0
      this.sessionNeedHandover = false
      const cached = uni.getStorageSync(this.getMessageStorageKey())
      if (cached && Array.isArray(cached) && cached.length) {
        this.messages = cached.map(item => item.role === 'assistant' ? this.sanitizeAssistantMessage(item) : item)
      } else {
        this.messages = [this.buildWelcomeMessage()]
      }
      this.scrollToBottom()
    },
    persistMessages() {
      uni.setStorageSync(this.getMessageStorageKey(), this.messages)
      if (this.sessionId) {
        uni.setStorageSync(this.getSessionStorageKey(), this.sessionId)
      }
      if (this.guestToken) {
        uni.setStorageSync(this.getGuestTokenStorageKey(), this.guestToken)
      }
    },
    getGuestToken() {
      if (this.isLoggedIn) return ''
      const cached = uni.getStorageSync(this.getGuestTokenStorageKey())
      if (cached) return cached
      const token = `guest_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
      uni.setStorageSync(this.getGuestTokenStorageKey(), token)
      return token
    },
    startPolling() {
      this.stopPolling()
      this.pollTimer = setInterval(() => {
        if (this.sessionId && !this.loading && !this.isSyncing && Date.now() >= this.pollPauseUntil) {
          this.loadSessionDetail(true)
        }
      }, 3000)
    },
    stopPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
    },
    buildMessageFingerprint(list) {
      return JSON.stringify((list || []).map(item => ({
        role: item.role,
        replyText: item.replyText,
        content: item.content,
        sourceType: item.sourceType,
        cards: item.cards || [],
        actions: item.actions || []
      })))
    },
    async loadSessionDetail(silent = false) {
      this.isSyncing = true
      try {
        const detail = await getAiSessionDetail(this.sessionId, this.isLoggedIn ? {} : { guestToken: this.guestToken })
        this.sessionStatus = Number(detail.status || 0)
        this.sessionNeedHandover = !!detail.needHandover
        const remoteMessages = (detail.messages || []).map(item => ({
          localId: `remote-${item.id}`,
          role: item.role,
          content: item.content,
          replyText: item.replyText,
          sourceType: item.sourceType,
          cards: item.cards || [],
          actions: item.actions || [],
          messageId: item.id,
          feedbackSent: this.findFeedbackState(item.id)
        })).map(item => item.role === 'assistant' ? this.sanitizeAssistantMessage(item) : item)
        if (remoteMessages.length) {
          const changed = this.buildMessageFingerprint(remoteMessages) !== this.buildMessageFingerprint(this.messages)
          if (!changed) return
          this.messages = remoteMessages
          this.persistMessages()
          if (!silent) {
            this.scrollToBottom()
          } else {
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (e) {
        // 未登录或历史会话不可读时，继续使用本地缓存
      } finally {
        this.isSyncing = false
      }
    },
    findFeedbackState(messageId) {
      const hit = this.messages.find(item => item.messageId === messageId)
      return hit ? !!hit.feedbackSent : false
    },
    beginFreshSession() {
      this.sessionId = null
      this.sessionStatus = 0
      this.sessionNeedHandover = false
      this.messages = [this.buildWelcomeMessage()]
      uni.removeStorageSync(this.getSessionStorageKey())
      uni.removeStorageSync(this.getMessageStorageKey())
    },
    async sendMessage(customText) {
      const text = (customText || this.inputValue || '').trim()
      if (!text || this.loading) return
      this.loading = true

      const previousSessionId = this.sessionId
      const shouldStartNewRound = this.sessionStatus === 1 || this.sessionStatus === 3
      if (shouldStartNewRound) {
        this.beginFreshSession()
      }

      const userMessage = {
        localId: `u-${Date.now()}`,
        role: 'user',
        content: text
      }
      this.messages.push(userMessage)
      this.inputValue = ''
      this.scrollToBottom()

      try {
        const res = await chatWithAi({ sessionId: this.sessionId, guestToken: this.guestToken, message: text })
        this.pollPauseUntil = Date.now() + 2500
        this.sessionId = res.sessionId
        this.guestToken = res.guestToken || this.guestToken
        this.sessionStatus = Number(res.status || this.sessionStatus || 0)
        this.sessionNeedHandover = !!res.needHandover
        if (res.replyText || (res.cards && res.cards.length) || (res.actions && res.actions.length)) {
          this.messages.push(this.sanitizeAssistantMessage({
            localId: `a-${Date.now()}`,
            role: 'assistant',
            replyText: res.replyText,
            sourceType: res.sourceType,
            cards: res.cards || [],
            actions: res.actions || [],
            messageId: res.messageId,
            feedbackSent: false
          }))
        }
        this.persistMessages()
        if (previousSessionId && res.sessionId && res.sessionId !== previousSessionId && !shouldStartNewRound) {
          await this.loadSessionDetail(true)
        }
        this.scrollToBottom()
        this.startPolling()
      } catch (e) {
        this.messages.push({
          localId: `a-err-${Date.now()}`,
          role: 'assistant',
          replyText: '网络有点忙，我暂时没收到回复。你可以重新发送，或者稍后再试。',
          sourceType: 'fallback'
        })
        this.scrollToBottom()
      } finally {
        this.loading = false
      }
    },
    getDisplayActions(message) {
      const actions = [...filterRecommendActions(message.actions || [])]
      const hasHandover = actions.some(item => item.type === 'handover')
      if (message.role === 'assistant' && message.sourceType !== 'human' && message.sourceType !== 'system' && !hasHandover && this.sessionId && this.sessionStatus !== 1 && this.sessionStatus !== 3) {
        actions.push({ type: 'handover', label: '转人工', value: 'manual_service' })
      }
      return actions
    },
    getAssistantLabel(message) {
      if (message.sourceType === 'human') {
        return '人工客服'
      }
      if (message.sourceType === 'system') {
        return '系统提示'
      }
      return 'AI 客服'
    },
    async handleAction(action, message) {
      if (!action) return
      if (action.type === 'navigate' || action.type === 'retry') {
        this.goByRoute(action.route)
        return
      }
      if (action.type === 'handover') {
        if (!this.sessionId) return
        try {
          await requestAiHandover(this.sessionId, { remark: message.replyText, guestToken: this.guestToken })
          this.sessionStatus = 2
          this.sessionNeedHandover = true
          uni.showToast({ title: '已提交人工处理', icon: 'success' })
        } catch (e) {
          uni.showToast({ title: '提交失败', icon: 'none' })
        }
      }
    },
    async submitFeedback(message, rating) {
      if (!this.sessionId || !message.messageId) return
      if (message.sourceType === 'human' || message.sourceType === 'system') return
      try {
        await submitAiFeedback(this.sessionId, { messageId: message.messageId, rating })
        message.feedbackSent = true
        this.persistMessages()
        uni.showToast({ title: '感谢反馈', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '反馈失败', icon: 'none' })
      }
    },
    goByRoute(route) {
      if (!route) return
      if (!canAccessPage(route)) {
        uni.showToast({ title: '答辩模式下该页面已隐藏', icon: 'none' })
        return
      }
      if (TAB_BAR_PAGES.includes(route)) {
        uni.switchTab({ url: route })
      } else {
        uni.navigateTo({ url: route })
      }
    },
    resetChat() {
      this.sessionId = null
      this.sessionStatus = 0
      this.sessionNeedHandover = false
      uni.removeStorageSync(this.getSessionStorageKey())
      uni.removeStorageSync(this.getMessageStorageKey())
      this.messages = [this.buildWelcomeMessage()]
      this.scrollToBottom()
      this.startPolling()
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const lastIndex = this.messages.length - 1
        this.scrollIntoView = `msg-${lastIndex}`
      })
    },
    goBack() {
      uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/profile/profile' }) })
    }
  }
}
</script>

<style lang="scss" scoped>
.page { height: 100vh; background: #f5f6f7; display: flex; flex-direction: column; overflow: hidden; }
.nav-bar {
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 28rpx; height: calc(88rpx + env(safe-area-inset-top));
  padding-top: env(safe-area-inset-top); background: #fff; border-bottom: 1rpx solid #ececec;
}
.nav-back, .nav-refresh { width: 60rpx; font-size: 40rpx; color: #2c2f30; }
.nav-center { flex: 1; text-align: center; }
.nav-title { display: block; font-size: 34rpx; font-weight: 800; color: #2c2f30; }
.nav-sub { display: block; font-size: 22rpx; color: #8c8f90; margin-top: 6rpx; }

.chat-scroll { flex: 1; height: 0; padding: 24rpx 0 0; }

.handover-banner {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 20rpx 24rpx 0;
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: #fff4e8;
  border: 2rpx solid rgba(255,122,47,0.2);
  box-shadow: 0 10rpx 24rpx rgba(156,63,0,0.06);
}
.handover-banner-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #ff7a2f;
  flex-shrink: 0;
}
.handover-banner-text {
  flex: 1;
  font-size: 24rpx;
  line-height: 1.6;
  color: #9c3f00;
  font-weight: 600;
}
.handover-banner.ended {
  background: #eef6ff;
  border-color: rgba(64, 158, 255, 0.18);
  box-shadow: 0 10rpx 24rpx rgba(64,158,255,0.08);
}
.handover-banner.ended .handover-banner-dot {
  background: #409eff;
}
.handover-banner.ended .handover-banner-text {
  color: #245b9e;
}

.quick-section {
  flex-shrink: 0;
  background: rgba(245, 246, 247, 0.96);
  backdrop-filter: blur(12rpx);
  padding: 12rpx 24rpx 14rpx;
  border-top: 1rpx solid rgba(236, 236, 236, 0.9);
}
.quick-title { font-size: 26rpx; font-weight: 700; color: #2c2f30; }
.quick-scroll { margin-top: 16rpx; white-space: nowrap; }
.quick-list { display: inline-flex; gap: 16rpx; padding-right: 24rpx; }
.quick-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 72rpx;
  padding: 0 26rpx;
  background: #fff;
  border-radius: 999rpx;
  border: 2rpx solid rgba(255,122,47,0.14);
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.04);
}
.quick-item-text { font-size: 24rpx; color: #2c2f30; line-height: 1; white-space: nowrap; }

.message-list {
  margin-top: 12rpx;
  padding: 0 24rpx;
}
.message-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
  align-items: flex-start;
  box-sizing: border-box;
  width: 100%;
}
.message-row.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 64rpx;
  height: 64rpx;
  min-width: 64rpx;
  min-height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e7e8ea;
  color: #2c2f30;
  font-size: 24rpx;
  font-weight: 800;
  flex-shrink: 0;
  flex-grow: 0;
  overflow: hidden;
  box-sizing: border-box;
}
.message-row.assistant .avatar { background: #9c3f00; color: #fff; }
.avatar-img {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: block;
}
.assistant-bolt {
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1;
  color: #fff7e8;
}
.bubble-wrap {
  flex: 1;
  display: flex;
  min-width: 0;
}
.message-row.user .bubble-wrap {
  justify-content: flex-end;
}
.message-row.assistant .bubble-wrap {
  justify-content: flex-start;
}
.bubble {
  max-width: 82%;
  background: #fff;
  border-radius: 24rpx;
  padding: 22rpx;
  box-shadow: 0 10rpx 24rpx rgba(0,0,0,0.04);
  box-sizing: border-box;
  word-wrap: break-word;
  overflow-wrap: break-word;
}
.message-row.user .bubble {
  max-width: 72%;
  background: #fff;
}
.message-row.assistant .bubble { background: #fffaf6; border: 2rpx solid rgba(255,122,47,0.12); }
.message-meta {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}
.source-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 700;
}
.source-badge.ai {
  background: rgba(255,122,47,0.14);
  color: #9c3f00;
}
.source-badge.human {
  background: rgba(44,47,48,0.08);
  color: #2c2f30;
}
.bubble-text { font-size: 27rpx; color: #2c2f30; line-height: 1.8; }

.card-list { display: flex; flex-direction: column; gap: 14rpx; margin-top: 18rpx; }
.biz-card {
  display: flex;
  gap: 16rpx;
  width: 100%;
  max-width: 470rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 16rpx;
  box-sizing: border-box;
}
.biz-card-img { width: 130rpx; height: 130rpx; border-radius: 16rpx; background: #eceff0; flex-shrink: 0; }
.biz-card-body { flex: 1; min-width: 0; }
.biz-card-title { display: block; font-size: 26rpx; font-weight: 700; color: #2c2f30; }
.biz-card-sub { display: block; font-size: 22rpx; color: #6f7374; margin-top: 8rpx; line-height: 1.5; }
.biz-card-footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 12rpx; gap: 12rpx; }
.biz-card-price { font-size: 28rpx; font-weight: 800; color: #9c3f00; }
.biz-card-meta { font-size: 20rpx; color: #86898a; }

.action-list { display: flex; gap: 12rpx; flex-wrap: wrap; margin-top: 18rpx; }
.action-chip { padding: 12rpx 20rpx; border-radius: 999rpx; background: rgba(255,122,47,0.12); }
.action-chip-text { font-size: 22rpx; color: #9c3f00; font-weight: 700; }
.feedback-row {
  display: flex;
  gap: 16rpx;
  margin-top: 18rpx;
}
.feedback-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #f5f6f7;
  transition: all 0.2s;
}
.feedback-btn:active {
  transform: scale(0.95);
}
.feedback-good {
  background: rgba(82, 196, 26, 0.08);
}
.feedback-good:active {
  background: rgba(82, 196, 26, 0.18);
}
.feedback-bad {
  background: rgba(255, 77, 79, 0.08);
}
.feedback-bad:active {
  background: rgba(255, 77, 79, 0.18);
}
.feedback-icon {
  font-size: 26rpx;
  line-height: 1;
}
.feedback-label {
  font-size: 22rpx;
  color: #6f7374;
  font-weight: 500;
}
.feedback-good .feedback-label {
  color: #52c41a;
}
.feedback-bad .feedback-label {
  color: #ff4d4f;
}

.composer {
  position: sticky; bottom: 0; display: flex; gap: 16rpx; align-items: center;
  padding: 20rpx 24rpx calc(20rpx + env(safe-area-inset-bottom)); background: #fff; border-top: 1rpx solid #ececec;
}
.composer-input {
  flex: 1; height: 84rpx; background: #f5f6f7; border-radius: 22rpx; padding: 0 24rpx; font-size: 26rpx;
}
.composer-btn {
  width: 132rpx; height: 84rpx; border-radius: 22rpx; background: #9c3f00;
  display: flex; align-items: center; justify-content: center;
}
.composer-btn-text { color: #fff; font-size: 26rpx; font-weight: 800; }
</style>
