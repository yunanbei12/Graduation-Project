<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">←</text>
      <text class="nav-title">我的订单</text>
      <text class="nav-cart">🛒</text>
    </view>

    <!-- 订单类型切换 -->
    <view class="type-switch">
      <view class="switch-item active">
        <text class="switch-text active">商品订单</text>
      </view>
      <view class="switch-item" @click="goCourseOrders">
        <text class="switch-text">课程订单</text>
      </view>
    </view>

    <!-- 状态标签 -->
    <view class="status-tabs">
      <view
        v-for="(tab, idx) in statusTabs"
        :key="idx"
        :class="['status-tab', { active: activeTab === idx }]"
        @click="activeTab = idx"
      >
        <text :class="['status-tab-text', { active: activeTab === idx }]">{{ tab }}</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 订单列表 -->
      <view class="order-card" v-for="order in orderList" :key="order.id" @click="goDetail(order.id)">
        <view class="card-top">
          <text class="order-no">订单号: {{ order.orderNumber }}</text>
          <text class="order-status" :class="{ orange: order.status <= 3, gray: order.status >= 4 }">{{ statusTextMap[order.status] || '未知' }}</text>
        </view>
        <view class="product-row">
          <image class="product-img" :src="getImageUrl(order.itemPic) || '/static/logo.png'" mode="aspectFill" />
          <view class="product-info">
            <text class="product-name">{{ order.itemName || '商品订单' }}</text>
            <text class="product-spec">共{{ order.itemCount || 1 }}件商品</text>
            <view class="product-price-row">
              <text class="product-price">¥{{ order.actualAmount }}</text>
              <text class="product-qty">{{ formatTime(order.createTime) }}</text>
            </view>
          </view>
        </view>
        <view class="card-actions">
          <text class="action-link" v-if="order.status === 2 || order.status === 3" @click.stop="handleRefund(order.id)">申请退款</text>
          <view class="action-btns">
            <view class="btn-outline" v-if="order.status === 1" @click.stop="handleCancel(order.id)">
              <text class="btn-outline-text">取消订单</text>
            </view>
            <view class="btn-primary-sm" v-if="order.status === 1" @click.stop="handlePay(order.id)">
              <text class="btn-primary-text">继续支付</text>
            </view>
            <view class="btn-outline" v-if="order.status === 3 || order.status === 4 || order.status === 5 || order.status === 7 || order.status === 8" @click.stop="goDetail(order.id)">
              <text class="btn-outline-text">查看详情</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="orderList.length === 0 && !loading">
        <text class="empty-icon">📦</text>
        <text class="empty-text">暂无订单</text>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue';
import { onShow } from '@dcloudio/uni-app'
import { getOrderList, getOrderItems, cancelOrder } from '../../api/order';
import { checkLogin } from '../../utils/auth';
import config from '../../utils/config';

const activeTab = ref(0);
const statusTabs = ['全部', '待付款', '待发货', '待收货', '已完成', '退款/售后'];
const orderList = ref([]);
const loading = ref(false);

// 状态映射: 1=待支付 2=已支付 3=待排课/待发货 4=已完成 5=已取消 6=退款中 7=已退款 8=退款驳回
const statusMap = {
  0: null, // 全部
  1: 1,    // 待付款
  2: 2,    // 待发货
  3: 3,    // 待收货
  4: 4,    // 已完成
  5: null  // 退款/售后：前端过滤 6/7/8
};

const statusTextMap = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款',
  8: '退款驳回'
};
const loadOrders = async () => {
  loading.value = true;
  try {
    const params = { pageNum: 1, pageSize: 20, orderType: 2 };
    const status = statusMap[activeTab.value];
    if (status !== null) params.status = status;
    const res = await getOrderList(params);
    const orders = res.records || [];
    // 退款/售后 tab 前端过滤 status 6/7/8
    const filtered = activeTab.value === 5
      ? orders.filter(o => [6, 7, 8].includes(o.status))
      : orders;
    // 为每个订单加载第一个订单项
    for (const order of filtered) {
      try {
        const items = await getOrderItems(order.id);
        if (items && items.length > 0) {
          order.itemPic = items[0].itemPic;
          order.itemName = items.length > 1 ? items[0].itemName + '等' : items[0].itemName;
          order.itemCount = items.reduce((sum, i) => sum + i.qty, 0);
        }
      } catch (e) {
        // ignore
      }
    }
    orderList.value = filtered;
  } catch (e) {
    console.error('加载订单失败', e);
  } finally {
    loading.value = false;
  }
};

const formatTime = (t) => {
  if (!t) return '';
  return t.replace(/T/, ' ').substring(0, 16);
};

watch(activeTab, () => {
  loadOrders();
});

onShow(() => {
  if (!checkLogin()) return
  loadOrders();
});

const goBack = () => uni.navigateBack();
const goCourseOrders = () => {
  uni.redirectTo({ url: '/pages/order/my-orders-course' });
};

const goDetail = (orderId) => {
  uni.navigateTo({ url: '/pages/order/order-detail-product?id=' + orderId });
};

const getImageUrl = config.getImageUrl;

const handlePay = (orderId) => {
  uni.navigateTo({ url: `/pages/order/order-detail-product?id=${orderId}&fromPay=1` });
};

const handleCancel = async (orderId) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelOrder(orderId);
          uni.showToast({ title: '已取消', icon: 'success' });
          loadOrders();
        } catch (e) {
          uni.showToast({ title: '取消失败', icon: 'none' });
        }
      }
    }
  });
};

const handleRefund = (orderId) => {
  uni.navigateTo({ url: '/pages/order/refund?id=' + orderId });
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
.nav-back { font-size: 40rpx; color: #2c2f30; width: 60rpx; }
.nav-title { font-size: 34rpx; font-weight: 700; color: #2c2f30; }
.nav-cart { font-size: 36rpx; width: 60rpx; text-align: right; }

.type-switch {
  display: flex;
  margin: calc(88rpx + env(safe-area-inset-top) + 24rpx) 32rpx 0;
  background: #f5f6f7;
  border-radius: 48rpx;
  padding: 6rpx;
}
.switch-item {
  flex: 1;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 40rpx;
}
.switch-item.active {
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
}
.switch-text {
  font-size: 28rpx;
  color: #666;
  font-weight: 500;
}
.switch-text.active {
  color: #fff;
  font-weight: 700;
}

.status-tabs {
  display: flex;
  padding: 20rpx 32rpx;
  gap: 32rpx;
  background: #fff;
  margin-top: 16rpx;
}
.status-tab {
  padding: 8rpx 0;
}
.status-tab-text {
  font-size: 26rpx;
  color: #999;
}
.status-tab.active .status-tab-text,
.status-tab-text.active {
  color: #ff7a2f;
  font-weight: 700;
  border-bottom: 4rpx solid #ff7a2f;
  padding-bottom: 8rpx;
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
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.order-no { font-size: 24rpx; color: #999; }
.order-status { font-size: 26rpx; font-weight: 600; }
.order-status.orange { color: #ff7a2f; }
.order-status.gray { color: #999; }

.product-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}
.product-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 16rpx;
  background: #f0f0f0;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.product-name { font-size: 28rpx; font-weight: 700; color: #2c2f30; }
.product-spec { font-size: 24rpx; color: #999; }
.product-price-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}
.product-price { font-size: 34rpx; font-weight: 700; color: #2c2f30; }
.product-qty { font-size: 24rpx; color: #999; }

.multi-product-row {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.multi-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #2c2f30;
}
.multi-more {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #f5f6f7;
  display: flex;
  align-items: center;
  justify-content: center;
}
.multi-more-text { font-size: 32rpx; color: #999; font-weight: 600; }

.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  flex-wrap: wrap;
  gap: 12rpx;
}
.action-link { font-size: 24rpx; color: #999; }
.action-btns {
  display: flex;
  gap: 12rpx;
  align-items: center;
}
.btn-outline {
  border: 2rpx solid #e0e0e0;
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
}
.btn-outline-text { font-size: 24rpx; color: #666; }

.btn-primary-sm {
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
}
.btn-primary-text { font-size: 24rpx; color: #fff; font-weight: 600; }

.btn-outline-primary {
  border: 2rpx solid #ff7a2f;
  border-radius: 32rpx;
  padding: 12rpx 28rpx;
}
.btn-outline-primary-text { font-size: 24rpx; color: #ff7a2f; font-weight: 600; }

.multi-summary {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}
.multi-count { font-size: 24rpx; color: #666; }
.multi-price { font-size: 30rpx; font-weight: 700; color: #ff7a2f; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0;
  gap: 16rpx;
}
.empty-icon { font-size: 80rpx; }
.empty-text { font-size: 28rpx; color: #999; }
</style>
