<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">←</text>
      <text class="nav-title">ORDER COMPLETE</text>
      <text class="nav-share">⤴</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 成功图标 -->
      <view class="success-icon-area">
        <view class="success-circle">
          <text class="success-check">✓</text>
        </view>
        <text class="success-title">购买成功</text>
        <text class="success-subtitle">您的运动装备即将启程！</text>
      </view>

      <!-- 订单详情卡片 -->
      <view class="info-card">
        <view class="card-header">
          <text class="card-icon">📋</text>
          <text class="card-title">订单详情</text>
        </view>
        <view class="price-section">
          <text class="price-label">实付款</text>
          <text class="price-value">¥{{ orderInfo.actualAmount || '0.00' }}</text>
        </view>
        <view class="divider"></view>
        <view class="order-no">
          <text class="order-label">订单编号</text>
          <text class="order-value">{{ orderInfo.orderNumber || '-' }}</text>
        </view>
      </view>

      <!-- 配送信息卡片 -->
      <view class="info-card">
        <view class="card-header">
          <text class="card-icon">📦</text>
          <text class="card-title">配送信息</text>
        </view>
        <view class="address-info">
          <view class="delivery-tag">
            <text class="tag-icon">⏱</text>
            <text class="tag-text">预计 2-3 个工作日送达</text>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view class="btn-area">
        <view class="btn-primary" @click="goOrders">
          <text class="btn-text-w">查看订单</text>
        </view>
        <view class="btn-secondary" @click="goMall">
          <text class="btn-text-d">继续逛逛</text>
        </view>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOrderDetail } from '../../api/order'

const orderInfo = ref({})
const orderId = ref('')

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  orderId.value = currentPage.options?.orderId || ''
  if (orderId.value) {
    loadOrder(orderId.value)
  }
})

const loadOrder = async (id) => {
  try {
    const res = await getOrderDetail(id)
    orderInfo.value = res.order || res || {}
  } catch (e) {
    console.error('加载订单失败', e)
  }
}

const goBack = () => {
  uni.switchTab({ url: '/pages/mall/mall' })
}
const goOrders = () => {
  if (orderId.value) {
    uni.navigateTo({ url: '/pages/order/order-detail-product?id=' + orderId.value })
    return
  }
  uni.navigateTo({ url: '/pages/order/my-orders-product' })
}
const goMall = () => {
  uni.switchTab({ url: '/pages/mall/mall' })
}
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
.nav-back {
  font-size: 40rpx;
  color: #2c2f30;
  width: 60rpx;
}
.nav-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #2c2f30;
  letter-spacing: 2rpx;
}
.nav-share {
  font-size: 36rpx;
  color: #ff7a2f;
  width: 60rpx;
  text-align: right;
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
  padding: 60rpx 0 48rpx;
  background: #fff;
}
.success-circle {
  width: 140rpx;
  height: 140rpx;
  border-radius: 24rpx;
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
  margin-bottom: 16rpx;
}
.success-subtitle {
  font-size: 26rpx;
  color: #999;
  line-height: 1.6;
}
.info-card {
  margin: 24rpx 32rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 32rpx;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}
.card-icon {
  font-size: 32rpx;
}
.card-title {
  font-size: 28rpx;
  color: #999;
  font-weight: 500;
}
.price-section {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.price-label {
  font-size: 24rpx;
  color: #ff7a2f;
}
.price-value {
  font-size: 56rpx;
  font-weight: 700;
  color: #ff7a2f;
}
.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 24rpx 0;
}
.order-no {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.order-label {
  font-size: 24rpx;
  color: #ff7a2f;
}
.order-value {
  font-size: 28rpx;
  color: #2c2f30;
}
.address-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}
.delivery-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
}
.tag-icon {
  font-size: 24rpx;
  color: #ff7a2f;
}
.tag-text {
  font-size: 24rpx;
  color: #ff7a2f;
}
.btn-area {
  padding: 32rpx 32rpx 0;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.btn-primary {
  height: 96rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-text-w {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
}
.btn-secondary {
  height: 96rpx;
  background: #fff;
  border-radius: 48rpx;
  border: 2rpx solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.btn-text-d {
  font-size: 32rpx;
  font-weight: 600;
  color: #2c2f30;
}
</style>
