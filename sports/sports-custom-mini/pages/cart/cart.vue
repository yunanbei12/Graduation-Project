<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-left">
        <text class="nav-back" @click="goBack">←</text>
        <text class="nav-title">购物车</text>
      </view>
      <text class="nav-edit" @click="toggleEdit">{{ isEditing ? '完成' : '编辑' }}</text>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 购物车商品列表 -->
      <view class="cart-list">
        <view
          class="cart-item"
          v-for="(item, idx) in cartItems"
          :key="idx"
        >
          <!-- 选择框 -->
          <view class="checkbox" @click="toggleSelect(idx)">
            <view v-if="item.selected" class="checkbox-inner active">
              <view class="checkbox-dot"></view>
            </view>
            <view v-else class="checkbox-inner"></view>
          </view>

          <!-- 商品图片 -->
          <image class="item-img" :src="item.image" mode="aspectFill" @click="goDetail(item)" />

          <!-- 商品信息 -->
          <view class="item-info">
            <text class="item-name">{{ item.name }}</text>
            <text class="item-spec">规格：{{ item.spec }}</text>
            <view class="item-bottom">
              <text class="item-price">¥{{ item.price.toFixed(2) }}</text>
              <view class="qty-stepper">
                <view class="qty-btn" @click="changeQty(idx, -1)">
                  <text class="qty-btn-text">−</text>
                </view>
                <text class="qty-num">{{ item.qty }}</text>
                <view class="qty-btn" @click="changeQty(idx, 1)">
                  <text class="qty-btn-text">＋</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空购物车 -->
        <view v-if="cartItems.length === 0" class="empty-cart">
          <text class="empty-icon">🛒</text>
          <text class="empty-text">购物车空空如也</text>
          <view class="empty-btn" @click="goMall">
            <text class="empty-btn-text">去逛逛</text>
          </view>
        </view>
      </view>

      <view style="height: 280rpx;"></view>
    </scroll-view>

    <!-- 底部结算栏 -->
    <view class="checkout-bar" v-if="cartItems.length > 0">
      <view class="checkout-inner">
        <view class="checkout-left" @click="toggleSelectAll">
          <view class="checkbox">
            <view v-if="isAllSelected" class="checkbox-inner active">
              <view class="checkbox-dot"></view>
            </view>
            <view v-else class="checkbox-inner"></view>
          </view>
          <text class="select-all-text">全选</text>
        </view>
        <view class="checkout-right">
          <view class="total-area">
            <text class="total-label">TOTAL</text>
            <text class="total-price">¥{{ totalPrice }}</text>
          </view>
          <view
            :class="['checkout-btn', { disabled: selectedCount === 0 }]"
            @click="handleCheckout"
          >
            <text class="checkout-btn-text">结算({{ selectedCount }})</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 编辑模式下的删除栏 -->
    <view class="delete-bar" v-if="isEditing && cartItems.length > 0">
      <view class="delete-inner">
        <view class="checkout-left" @click="toggleSelectAll">
          <view class="checkbox">
            <view v-if="isAllSelected" class="checkbox-inner active">
              <view class="checkbox-dot"></view>
            </view>
            <view v-else class="checkbox-inner"></view>
          </view>
          <text class="select-all-text">全选</text>
        </view>
        <view class="delete-btn" @click="deleteSelected">
          <text class="delete-btn-text">删除({{ selectedCount }})</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getCartList, updateCartQty, deleteCartItem, clearCart } from '../../api/cart';
import config from '../../utils/config';
import { checkLogin } from '../../utils/auth';

const isEditing = ref(false);
const cartItems = ref([]);
const loading = ref(false);

// 加载购物车
const loadCart = async () => {
  // 先检查登录状态
  if (!checkLogin()) return;
  
  loading.value = true;
  try {
    const res = await getCartList();
    cartItems.value = (res || []).map(item => ({
      id: item.id,
      prodId: item.prodId,
      skuId: item.skuId,
      name: item.prodName || '商品',
      spec: item.skuProperties || '默认规格',
      price: item.skuPrice || 0,
      qty: item.qty,
      selected: true,
      image: config.getImageUrl(item.prodPic) || ''
    }));
  } catch (e) {
    console.error('加载购物车失败', e);
  } finally {
    loading.value = false;
  }
};

onShow(() => {
  loadCart();
});

// 计算选中数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).reduce((sum, item) => sum + item.qty, 0);
});

// 计算总价
const totalPrice = computed(() => {
  const total = cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.qty, 0);
  return total.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
});

// 是否全选
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && cartItems.value.every(item => item.selected);
});

// 切换单个选择
const toggleSelect = (idx) => {
  cartItems.value[idx].selected = !cartItems.value[idx].selected;
};

// 切换全选
const toggleSelectAll = () => {
  const newVal = !isAllSelected.value;
  cartItems.value.forEach(item => {
    item.selected = newVal;
  });
};

// 修改数量
const changeQty = async (idx, delta) => {
  const item = cartItems.value[idx];
  const newQty = item.qty + delta;
  if (newQty < 1) {
    uni.showModal({
      title: '提示',
      content: '确定要删除该商品吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await deleteCartItem(item.id);
            cartItems.value.splice(idx, 1);
          } catch (e) {
            uni.showToast({ title: '删除失败', icon: 'none' });
          }
        }
      },
    });
    return;
  }
  if (newQty > 99) return;
  try {
    await updateCartQty({ id: item.id, qty: newQty });
    item.qty = newQty;
  } catch (e) {
    uni.showToast({ title: '修改失败', icon: 'none' });
  }
};

// 切换编辑模式
const toggleEdit = () => {
  isEditing.value = !isEditing.value;
};

// 删除选中
const deleteSelected = async () => {
  const selectedItems = cartItems.value.filter(item => item.selected);
  const count = selectedItems.length;
  if (count === 0) return;
  uni.showModal({
    title: '提示',
    content: `确定要删除选中的 ${count} 件商品吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          for (const item of selectedItems) {
            await deleteCartItem(item.id);
          }
          cartItems.value = cartItems.value.filter(item => !item.selected);
          if (cartItems.value.length === 0) {
            isEditing.value = false;
          }
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' });
        }
      }
    },
  });
};

// 结算
const handleCheckout = () => {
  if (selectedCount.value === 0) return;
  const selectedItems = cartItems.value.filter(item => item.selected);
  const cartIds = selectedItems.map(item => item.id).join(',');
  uni.navigateTo({ url: '/pages/order/confirm-product?cartIds=' + cartIds });
};

const goBack = () => uni.navigateBack();
const goMall = () => uni.switchTab({ url: '/pages/mall/mall' });
const goDetail = (item) => {
  uni.navigateTo({ url: '/pages/mall/product-detail?id=' + item.prodId });
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
  background: #f5f6f7;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.nav-back {
  font-size: 40rpx;
  color: #9c3f00;
}
.nav-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #2c2f30;
  letter-spacing: 1rpx;
}
.nav-edit {
  font-size: 28rpx;
  font-weight: 700;
  color: #9c3f00;
}

.scroll-area {
  flex: 1;
  height: 0;
  margin-top: calc(88rpx + env(safe-area-inset-top));
}

.cart-list {
  padding: 0 24rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.cart-item {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.checkbox {
  flex-shrink: 0;
}
.checkbox-inner {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  border: 3rpx solid #abadae;
  display: flex;
  align-items: center;
  justify-content: center;
}
.checkbox-inner.active {
  border-color: #ff7a2f;
}
.checkbox-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #ff7a2f;
}

.item-img {
  width: 180rpx;
  height: 180rpx;
  border-radius: 20rpx;
  background: #eff1f2;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}
.item-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #2c2f30;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.item-spec {
  font-size: 24rpx;
  color: #595c5d;
  font-weight: 500;
}
.item-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 12rpx;
}
.item-price {
  font-size: 34rpx;
  font-weight: 700;
  color: #9c3f00;
}

.qty-stepper {
  display: flex;
  align-items: center;
  background: #eff1f2;
  border-radius: 16rpx;
  padding: 4rpx;
  gap: 0;
}
.qty-btn {
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.qty-btn-text {
  font-size: 32rpx;
  color: #595c5d;
  font-weight: 500;
}
.qty-num {
  font-size: 28rpx;
  font-weight: 600;
  color: #2c2f30;
  min-width: 48rpx;
  text-align: center;
}

/* 空购物车 */
.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  gap: 24rpx;
}
.empty-icon {
  font-size: 96rpx;
}
.empty-text {
  font-size: 30rpx;
  color: #999;
}
.empty-btn {
  margin-top: 16rpx;
  padding: 20rpx 64rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 40rpx;
}
.empty-btn-text {
  font-size: 28rpx;
  font-weight: 700;
  color: #fff;
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;
  z-index: 40;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.checkout-inner {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  padding: 24rpx 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -12rpx 48rpx rgba(44, 47, 48, 0.06);
  border: 1rpx solid rgba(171, 173, 174, 0.1);
}
.checkout-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.select-all-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #2c2f30;
}
.checkout-right {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.total-area {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
.total-label {
  font-size: 18rpx;
  font-weight: 700;
  color: #595c5d;
  letter-spacing: 4rpx;
}
.total-price {
  font-size: 40rpx;
  font-weight: 900;
  color: #2c2f30;
  line-height: 1.1;
}
.checkout-btn {
  background: linear-gradient(135deg, #9c3f00, #ff7a2f);
  border-radius: 20rpx;
  padding: 20rpx 48rpx;
  box-shadow: 0 8rpx 24rpx rgba(156, 63, 0, 0.2);
}
.checkout-btn.disabled {
  opacity: 0.5;
}
.checkout-btn-text {
  font-size: 30rpx;
  font-weight: 800;
  color: #fff0ea;
}

/* 编辑模式删除栏 */
.delete-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;
  z-index: 41;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
.delete-inner {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 32rpx;
  padding: 24rpx 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -12rpx 48rpx rgba(44, 47, 48, 0.06);
}
.delete-btn {
  background: #b02500;
  border-radius: 20rpx;
  padding: 20rpx 48rpx;
}
.delete-btn-text {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
}
</style>
