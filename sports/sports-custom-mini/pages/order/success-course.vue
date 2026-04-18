<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-close" @click="handleClose">✕</text>
      <text class="nav-title">PAYMENT SUCCESS</text>
      <view style="width: 60rpx;"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 成功图标 -->
      <view class="success-icon-area">
        <view class="success-circle">
          <text class="success-check">✓</text>
        </view>
        <text class="success-title">支付成功</text>
        <text class="success-subtitle">Payment processed successfully</text>
      </view>

      <!-- 订单信息卡片 -->
      <view class="info-card">
        <view class="info-row">
          <text class="info-label">支付金额</text>
          <text class="info-value price">¥ {{ orderInfo.actualAmount || '0.00' }}</text>
        </view>
        <view class="divider"></view>
        <view class="info-row">
          <text class="info-label">订单编号</text>
          <text class="info-value">{{ orderInfo.orderNumber || '-' }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">支付时间</text>
          <text class="info-value">{{ orderInfo.paymentTime || formatNow() }}</text>
        </view>
        <view class="info-row">
          <text class="info-label">支付方式</text>
          <text class="info-value">模拟支付</text>
        </view>
      </view>

      <!-- 后续服务说明 -->
      <view class="service-card">
        <view class="service-icon">📢</view>
        <view class="service-content">
          <text class="service-title">后续服务说明</text>
          <text class="service-desc">系统已记录您的选课信息，请留意微信通知。专人将在1小时内邀请您进入课程专属微信群进行后续排课沟通。</text>
        </view>
      </view>

      <!-- 您的专业教练 -->
      <view class="section-header" v-if="coachInfo">
        <view class="section-line"></view>
        <text class="section-label">您的专业教练</text>
      </view>

      <view class="coach-card" v-if="coachInfo">
        <view class="coach-avatar-wrap">
          <image v-if="coachInfo.avatar" class="coach-avatar" :src="config.getImageUrl(coachInfo.avatar)" mode="aspectFill" />
          <view v-else class="coach-avatar-placeholder">
            <text class="placeholder-text">{{ (coachInfo.name || '教').slice(0, 1) }}</text>
          </view>
        </view>
        <view class="coach-info">
          <text class="coach-name">教练：{{ coachInfo.name }}</text>
          <text class="coach-desc" v-if="coachInfo.years">{{ coachInfo.years }}年专业执教经验</text>
          <text class="coach-desc" v-if="coachInfo.skills">擅长：{{ coachInfo.skills }}</text>
        </view>
        <view class="chat-icon">💬</view>
      </view>

      <!-- 底部按钮 -->
      <view class="btn-area">
        <view class="btn-home" @click="goHome">
          <text class="btn-home-text">返回首页</text>
          <text class="btn-home-icon">🏠</text>
        </view>
        <view class="btn-order" @click="goOrders">
          <text class="btn-order-text">查看订单详情</text>
          <text class="btn-order-icon">📋</text>
        </view>
      </view>

      <!-- 底部版权 -->
      <view class="footer">
        <text class="footer-text">APEX KINETIC © 2023 | SECURE PAYMENT POWERED BY STRIPE</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderDetail } from '../../api/order'
import config from '../../utils/config'

const orderInfo = ref({})
const courseInfo = ref(null)
const coachInfo = ref(null)
const scheduleInfo = ref(null)
const orderId = ref(null)

onMounted(() => {
  // 从URL参数获取orderId
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || currentPage.$route?.query || {}
  orderId.value = options.orderId
  if (orderId.value) {
    loadOrder()
  }
})

async function loadOrder() {
  try {
    const res = await getOrderDetail(orderId.value)
    orderInfo.value = res.order || res || {}
    courseInfo.value = res.course || null
    coachInfo.value = res.coach || null
    scheduleInfo.value = res.schedule || null
  } catch(e) {
    console.error('加载订单失败', e)
  }
}

function formatNow() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}:${String(d.getSeconds()).padStart(2,'0')}`
}

const handleClose = () => {
  uni.switchTab({ url: '/pages/index/index' });
};
const goHome = () => {
  uni.switchTab({ url: '/pages/index/index' });
};
const goOrders = () => {
  uni.navigateTo({ url: '/pages/order/my-orders-course' });
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f6f7;
  display: flex;
  flex-direction: column;
}
.nav-bar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 99;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  height: calc(88rpx + env(safe-area-inset-top));
  padding-top: env(safe-area-inset-top);
  background: #fff;
}
.nav-close {
  font-size: 36rpx;
  color: #9c3f00;
  width: 60rpx;
}
.nav-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #9c3f00;
  letter-spacing: 2rpx;
}
.scroll-area {
  flex: 1;
  height: 0;
  margin-top: calc(88rpx + env(safe-area-inset-top));
}
.success-icon-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0 40rpx;
  background: #fff;
}
.success-circle {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;
  box-shadow: 0 12rpx 40rpx rgba(255, 122, 47, 0.3);
}
.success-check {
  font-size: 72rpx;
  color: #fff;
  font-weight: 700;
}
.success-title {
  font-size: 48rpx;
  font-weight: 700;
  color: #2c2f30;
  margin-bottom: 12rpx;
}
.success-subtitle {
  font-size: 26rpx;
  color: #999;
}
.info-card {
  margin: 24rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}
.info-label {
  font-size: 28rpx;
  color: #999;
}
.info-value {
  font-size: 28rpx;
  color: #2c2f30;
  font-weight: 500;
}
.info-value.price {
  font-size: 36rpx;
  color: #9c3f00;
  font-weight: 700;
}
.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 4rpx 0;
}
.service-card {
  margin: 0 32rpx 24rpx;
  background: #9c3f00;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  gap: 20rpx;
}
.service-icon {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255,255,255,0.15);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  flex-shrink: 0;
}
.service-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.service-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
}
.service-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.85);
  line-height: 1.6;
}
.section-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 32rpx;
}
.section-line {
  width: 48rpx;
  height: 4rpx;
  background: #9c3f00;
  border-radius: 2rpx;
}
.section-label {
  font-size: 28rpx;
  color: #2c2f30;
  font-weight: 500;
}
.coach-card {
  margin: 0 32rpx 32rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.coach-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
}
.coach-avatar-wrap {
  flex-shrink: 0;
}
.coach-avatar-placeholder {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-text {
  font-size: 40rpx;
  color: #fff;
  font-weight: 700;
}
.coach-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.coach-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #2c2f30;
}
.coach-desc {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
}
.chat-icon {
  font-size: 40rpx;
  color: #9c3f00;
}
.btn-area {
  padding: 20rpx 32rpx 0;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.btn-home {
  height: 96rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.btn-home-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
.btn-home-icon {
  font-size: 32rpx;
}
.btn-order {
  height: 96rpx;
  background: #fff;
  border-radius: 48rpx;
  border: 2rpx solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.btn-order-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c2f30;
}
.btn-order-icon {
  font-size: 28rpx;
}
.footer {
  padding: 48rpx 32rpx;
  text-align: center;
}
.footer-text {
  font-size: 20rpx;
  color: #ccc;
  letter-spacing: 1rpx;
}
</style>
