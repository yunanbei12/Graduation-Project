<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="nav-back" @click="goBack">←</text>
      <text class="nav-title">申请退款</text>
      <view style="width: 60rpx;"></view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 课程订单信息 -->
      <view class="product-card">
        <image class="product-img" :src="orderInfo.itemPic || '/static/logo.png'" mode="aspectFill" />
        <view class="product-info">
          <text class="product-name">{{ orderInfo.itemName || '订单' }}</text>
          <text class="product-spec">订单号: {{ orderInfo.orderNumber || '' }}</text>
          <text class="product-price">¥{{ orderInfo.actualAmount || '0.00' }}</text>
        </view>
      </view>

      <!-- 退款原因 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-title">退款原因</text>
          <view class="required-tag">
            <text class="required-text">必选</text>
          </view>
        </view>
        <view class="reason-list">
          <view
            v-for="(reason, idx) in reasons"
            :key="idx"
            class="reason-item"
            @click="selectedReason = idx"
          >
            <text class="reason-text">{{ reason }}</text>
            <view :class="['radio', { active: selectedReason === idx }]">
              <text v-if="selectedReason === idx" class="radio-check">✓</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 退款金额 -->
      <view class="section-card">
        <view class="refund-amount-row">
          <text class="section-title">退款金额</text>
          <view class="amount-info">
            <text class="amount-value">¥{{ estimatedRefundAmount }}</text>
            <text class="amount-note">原支付金额：¥{{ orderInfo.actualAmount || '0.00' }} (实付)</text>
          </view>
        </view>
      </view>

      <!-- 退款规则 -->
      <view class="section-card">
        <view class="section-header">
          <text class="section-title">退款规则</text>
        </view>
        <view class="rule-card" :class="{ disabled: !canRefund }">
          <text class="rule-title">{{ canRefund ? '当前可提交退款申请' : '当前不可退款' }}</text>
          <text class="rule-desc">{{ ruleTip }}</text>
          <text class="rule-desc" v-if="refundPreview && refundPreview.usedLessons !== undefined">已消课 {{ refundPreview.usedLessons }} / {{ refundPreview.totalLessons }} 节</text>
          <text class="rule-desc" v-if="refundPreview && refundPreview.minutesBeforeStart !== undefined && refundPreview.minutesBeforeStart > 0">距开课约 {{ Math.floor(refundPreview.minutesBeforeStart / 60) }} 小时 {{ refundPreview.minutesBeforeStart % 60 }} 分</text>
        </view>
      </view>

      <!-- 退款说明 -->
      <view class="section-card">
        <text class="section-title">退款说明</text>
        <view class="textarea-wrap">
          <textarea
            class="textarea"
            v-model="refundNote"
            placeholder="请详细描述您退款的原因，以便我们更快为您处理..."
            :maxlength="200"
          />
          <text class="char-count">{{ refundNote.length }} / 200</text>
        </view>
      </view>

      <!-- 上传凭证 -->
      <view class="section-card">
        <view class="upload-header">
          <text class="section-title">上传凭证</text>
          <text class="upload-tip">选填，最多3张</text>
        </view>
        <view class="upload-area">
          <view class="upload-btn" @click="chooseImage">
            <text class="upload-icon">📷</text>
          </view>
          <view class="upload-preview" v-for="(img, idx) in images" :key="idx">
            <image class="preview-img" :src="img" mode="aspectFill" />
            <text class="remove-btn" @click="removeImage(idx)">✕</text>
          </view>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tip-card">
        <text class="tip-icon">ℹ️</text>
        <text class="tip-text">提交申请后，我们将在1-3个工作日内完成审核。审核通过后，资金将原路退回至您的支付账户。</text>
      </view>

      <view style="height: 140rpx;"></view>
    </scroll-view>

    <!-- 底部提交按钮 -->
    <view class="bottom-bar">
      <view class="submit-btn" :class="{ disabled: !canRefund }" @click="submitRefund">
        <text class="submit-text">{{ submitButtonText }}</text>
        <text class="submit-arrow">➤</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getOrderDetail, getOrderItems, getRefundPreview, refundOrder } from '../../api/order';
import { checkLogin } from '../../utils/auth';

const orderId = ref(null);
const orderInfo = ref({});
const refundPreview = ref(null);
const selectedReason = ref(0);
const reasons = ['计划有变，无法参加', '课程选择有误', '服务不满意', '其他个人原因'];
const refundNote = ref('');
const images = ref([]);

const estimatedRefundAmount = computed(() => {
  return refundPreview.value?.estimatedRefundAmount ?? orderInfo.value.actualAmount ?? '0.00';
});

const ruleTip = computed(() => {
  return refundPreview.value?.ruleTip || '提交申请后，系统会根据订单类型和当前履约进度校验退款规则。';
});

const canRefund = computed(() => {
  return refundPreview.value?.canRefund !== false;
});

const submitButtonText = computed(() => {
  return canRefund.value ? '提交申请' : '当前不可退款';
});

onMounted(() => {
  if (!checkLogin()) return;
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1];
  const opts = currentPage.options || {};
  orderId.value = opts.orderId || opts.id;
  if (orderId.value) {
    loadOrder();
  }
});

const loadOrder = async () => {
  try {
    const res = await getOrderDetail(orderId.value);
    orderInfo.value = res.order || res || {};
    // 加载订单项
    const items = await getOrderItems(orderId.value);
    if (items && items.length > 0) {
      orderInfo.value.itemPic = items[0].itemPic;
      orderInfo.value.itemName = items[0].itemName;
    }
    refundPreview.value = await getRefundPreview(orderId.value);
  } catch (e) {
    console.error('加载订单失败', e);
  }
};

const goBack = () => uni.navigateBack();

const chooseImage = () => {
  if (images.value.length >= 3) return;
  uni.chooseImage({
    count: 3 - images.value.length,
    success: (res) => {
      images.value.push(...res.tempFilePaths);
    },
  });
};

const removeImage = (idx) => {
  images.value.splice(idx, 1);
};

const submitRefund = async () => {
  if (!orderId.value || !canRefund.value) return;
  const reason = reasons[selectedReason.value] + (refundNote.value ? ' - ' + refundNote.value : '');
  try {
    await refundOrder(orderId.value, reason);
    uni.showToast({ title: '退款申请已提交', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e) {
    uni.showToast({ title: '提交失败', icon: 'none' });
  }
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
.nav-title { font-size: 34rpx; font-weight: 700; color: #9c3f00; }
.scroll-area { flex: 1; height: 0; margin-top: calc(88rpx + env(safe-area-inset-top)); }

.product-card {
  margin: 24rpx 32rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  gap: 20rpx;
}
.product-img {
  width: 160rpx;
  height: 120rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.product-name { font-size: 30rpx; font-weight: 700; color: #2c2f30; }
.product-spec { font-size: 24rpx; color: #999; }
.product-price { font-size: 32rpx; font-weight: 700; color: #ff7a2f; }

.section-card {
  margin: 16rpx 32rpx 0;
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
}
.rule-card {
  background: #fff7f1;
  border: 2rpx solid rgba(255,122,47,0.18);
  border-radius: 20rpx;
  padding: 24rpx;
}
.rule-card.disabled {
  background: #f5f6f7;
  border-color: rgba(0,0,0,0.06);
}
.rule-title {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #9c3f00;
  margin-bottom: 10rpx;
}
.rule-card.disabled .rule-title {
  color: #666;
}
.rule-desc {
  display: block;
  font-size: 24rpx;
  color: #595c5d;
  line-height: 1.7;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-title { font-size: 32rpx; font-weight: 700; color: #2c2f30; display: block; margin-bottom: 16rpx; }
.required-tag {
  background: #fff3eb;
  border-radius: 8rpx;
  padding: 4rpx 16rpx;
}
.required-text { font-size: 22rpx; color: #ff7a2f; font-weight: 600; }

.reason-list {
  display: flex;
  flex-direction: column;
}
.reason-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 20rpx;
  border-radius: 16rpx;
}
.reason-item:first-child {
  background: #f5f6f7;
}
.reason-text { font-size: 28rpx; color: #2c2f30; }
.radio {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 3rpx solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.radio.active {
  background: #ff7a2f;
  border-color: #ff7a2f;
}
.radio-check { font-size: 24rpx; color: #fff; font-weight: 700; }

.refund-amount-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.refund-amount-row .section-title { margin-bottom: 0; }
.amount-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}
.amount-value { font-size: 40rpx; font-weight: 700; color: #ff7a2f; }
.amount-note { font-size: 22rpx; color: #999; }

.textarea-wrap {
  background: #f5f6f7;
  border-radius: 16rpx;
  padding: 24rpx;
  position: relative;
}
.textarea {
  width: 100%;
  min-height: 160rpx;
  font-size: 26rpx;
  color: #2c2f30;
  line-height: 1.6;
}
.char-count {
  position: absolute;
  right: 24rpx;
  bottom: 16rpx;
  font-size: 22rpx;
  color: #ccc;
}

.upload-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.upload-header .section-title { margin-bottom: 0; }
.upload-tip { font-size: 24rpx; color: #999; }
.upload-area {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}
.upload-btn {
  width: 140rpx;
  height: 140rpx;
  background: #f5f6f7;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-icon { font-size: 48rpx; opacity: 0.5; }
.upload-preview {
  position: relative;
}
.preview-img {
  width: 140rpx;
  height: 140rpx;
  border-radius: 16rpx;
}
.remove-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 36rpx;
  height: 36rpx;
  background: rgba(0,0,0,0.6);
  border-radius: 50%;
  color: #fff;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tip-card {
  margin: 16rpx 32rpx 0;
  background: #fff8f3;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  gap: 12rpx;
  align-items: flex-start;
}
.tip-icon { font-size: 28rpx; flex-shrink: 0; }
.tip-text { font-size: 24rpx; color: #9c3f00; line-height: 1.6; }

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 32rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.submit-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.submit-btn.disabled {
  opacity: 0.55;
}
.submit-text { font-size: 32rpx; font-weight: 700; color: #fff; }
.submit-arrow { font-size: 28rpx; color: #fff; }
</style>
