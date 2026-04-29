<template>
  <view class="page">
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">←</text>
      <text class="nav-title">我的订单</text>
      <view class="nav-placeholder"></view>
    </view>

    <view class="sub-tabs">
      <view
        v-for="(tab, idx) in subTabs"
        :key="idx"
        :class="['sub-tab', { active: activeSubTab === idx }]"
        @click="switchSubTab(idx)"
      >
        <text :class="['sub-tab-text', { active: activeSubTab === idx }]">{{ tab.label }}</text>
        <view v-if="activeSubTab === idx" class="sub-tab-line"></view>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area" @scrolltolower="loadMore">
      <view v-if="orders.length === 0 && !loading" class="empty-wrap">
        <text class="empty-text">暂无订单</text>
      </view>
      <view class="order-card" v-for="order in orders" :key="order.id" @tap="goDetail(order)">
        <view class="order-header">
          <view class="order-icon-wrap">
            <text class="order-icon">🏋️</text>
          </view>
          <view class="order-title-area">
            <text class="order-name">{{ order.orderItemName || '课程订单' }}</text>
            <text class="order-coach">订单号：{{ order.orderNumber }}</text>
          </view>
          <view class="status-badge" :class="statusClass(order.status)">
            <text class="status-text">{{ statusLabel(order.status) }}</text>
          </view>
        </view>
        <view class="order-price-row">
          <text class="order-price">¥ {{ order.actualAmount }}</text>
          <view class="order-actions">
            <view class="btn-outline" v-if="order.status === 1" @click.stop="cancelOrder(order)">
              <text class="btn-outline-text">取消订单</text>
            </view>
            <view class="btn-outline" v-if="order.status === 1" @click.stop="payOrderNow(order)">
              <text class="btn-outline-text">继续支付</text>
            </view>
            <view class="btn-outline" v-if="order.status === 2 || order.status === 3" @click.stop="goRefund(order)">
              <text class="btn-outline-text">申请退款</text>
            </view>
          </view>
        </view>
      </view>

      <view class="loading-more" v-if="loading">
        <text class="loading-text">加载中...</text>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getOrderList, getOrderItems, cancelOrder as cancelOrderApi } from '../../api/order'
import { checkLogin } from '../../utils/auth'

const subTabs = [
  { label: '全部', status: null },
  { label: '待支付', status: 1 },
  { label: '进行中', status: 3 },
  { label: '已完成', status: 4 },
  { label: '已取消', status: 5 },
  { label: '退款/售后', status: 'refund' }
]
const activeSubTab = ref(0)
const orders = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)

onShow(() => {
  if (!checkLogin()) return
  loadOrders(true)
})

async function loadOrders(reset = false) {
  if (loading.value) return
  if (reset) { pageNum.value = 1; orders.value = [] }
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize, orderType: 1 }
    const tab = subTabs[activeSubTab.value]
    const isRefundTab = tab.status === 'refund'
    // 退款/售后 tab 不传 status，拉全量后前端过滤
    if (!isRefundTab && tab.status !== null) params.status = tab.status
    const res = await getOrderList(params)
    let records = res.records || []
    // 退款/售后 tab：前端过滤 status 6/7/8
    if (isRefundTab) {
      records = records.filter(o => [6, 7, 8].includes(o.status))
    }
    // 加载每个订单的订单项名称
    for (const order of records) {
      try {
        const items = await getOrderItems(order.id)
        if (items.length > 0) {
          order.orderItemName = items[0].itemName
        }
      } catch(e) {}
    }
    if (reset) {
      orders.value = records
    } else {
      orders.value = [...orders.value, ...records]
    }
    total.value = res.total || 0
  } catch(e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
  }
}

function switchSubTab(idx) {
  activeSubTab.value = idx
  loadOrders(true)
}

function loadMore() {
  if (orders.value.length < total.value) {
    pageNum.value++
    loadOrders()
  }
}

function statusLabel(status) {
  const map = { 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成', 5: '已取消', 6: '退款中', 7: '已退款', 8: '退款驳回' }
  return map[status] || '未知'
}

function statusClass(status) {
  const map = { 1: 'pending', 2: 'paid', 3: 'booked', 4: 'done', 5: 'cancelled', 6: 'refunding', 7: 'refunded', 8: 'cancelled' }
  return map[status] || 'pending'
}

async function cancelOrder(order) {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrderApi(order.id)
          uni.showToast({ title: '已取消', icon: 'success' })
          loadOrders(true)
        } catch(e) { console.error(e) }
      }
    }
  })
}

function payOrderNow(order) {
  uni.navigateTo({ url: `/pages/order/order-detail-course?id=${order.id}&fromPay=1` })
}

function goRefund(order) {
  uni.navigateTo({ url: `/pages/order/refund?id=${order.id}` })
}

function goDetail(order) {
  uni.navigateTo({ url: `/pages/order/order-detail-course?id=${order.id}` })
}

function goBack() { uni.navigateBack() }
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
.nav-back { font-size: 40rpx; color: #2c2f30; width: 60rpx; }
.nav-title { font-size: 34rpx; font-weight: 700; color: #2c2f30; }
.nav-placeholder { width: 60rpx; }

.sub-tabs {
  display: flex;
  padding: 24rpx 32rpx 0;
  gap: 40rpx;
  background: #fff;
  margin-top: calc(88rpx + env(safe-area-inset-top) + 16rpx);
}
.sub-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 16rpx;
}
.sub-tab-text {
  font-size: 28rpx;
  color: #999;
}
.sub-tab-text.active {
  color: #2c2f30;
  font-weight: 700;
}
.sub-tab-line {
  width: 40rpx;
  height: 6rpx;
  background: #9c3f00;
  border-radius: 3rpx;
  margin-top: 8rpx;
}

.scroll-area {
  flex: 1;
  height: 0;
  padding-top: 16rpx;
}

.order-card {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
}
.order-header {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  margin-bottom: 20rpx;
}
.order-icon-wrap {
  width: 100rpx;
  height: 100rpx;
  background: #fff3eb;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44rpx;
  flex-shrink: 0;
}
.order-icon { font-size: 44rpx; }
.order-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}
.order-title-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.order-name { font-size: 30rpx; font-weight: 700; color: #2c2f30; }
.order-coach { font-size: 24rpx; color: #999; }
.order-location { font-size: 24rpx; color: #999; }

.status-badge {
  border-radius: 8rpx;
  padding: 6rpx 16rpx;
  flex-shrink: 0;
}
.status-badge.cancelled { background: #f5f6f7; }
.status-badge.paid { background: #e3f2fd; }
.status-badge.booked { background: #fff3eb; }
.status-badge.done { background: #e8f5e9; }
.status-badge.refunding { background: #fff3e0; }
.status-badge.refunded { background: #f5f6f7; }
.status-text { font-size: 22rpx; font-weight: 500; }
.status-badge.cancelled .status-text { color: #999; }
.status-badge.paid .status-text { color: #2196f3; }
.status-badge.booked .status-text { color: #ff7a2f; }
.status-badge.done .status-text { color: #4caf50; }
.status-badge.refunding .status-text { color: #ff9800; }
.status-badge.refunded .status-text { color: #999; }

.order-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  flex-wrap: wrap;
  gap: 16rpx;
}
.order-price { font-size: 36rpx; font-weight: 700; color: #ff7a2f; }
.order-actions { display: flex; gap: 12rpx; flex-wrap: wrap; }
.btn-outline { border: 2rpx solid #e0e0e0; border-radius: 32rpx; padding: 12rpx 24rpx; }
.btn-outline-text { font-size: 24rpx; color: #666; }

.empty-wrap { text-align: center; padding-top: 200rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.loading-more { text-align: center; padding: 20rpx; }
.loading-text { font-size: 24rpx; color: #999; }
</style>
