<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">我的优惠券</text>
			<view style="width: 40rpx;"></view>
		</view>
		<!-- Tab -->
		<view class="tab-row">
			<view class="tab-item" :class="{ active: activeTab === 0 }" @tap="switchTab(0)"><text class="tab-text" :class="{ active: activeTab === 0 }">未使用</text></view>
			<view class="tab-item" :class="{ active: activeTab === 1 }" @tap="switchTab(1)"><text class="tab-text" :class="{ active: activeTab === 1 }">已使用</text></view>
			<view class="tab-item" :class="{ active: activeTab === 2 }" @tap="switchTab(2)"><text class="tab-text" :class="{ active: activeTab === 2 }">已过期</text></view>
		</view>
		<scroll-view scroll-y class="content-scroll">
			<view v-if="coupons.length === 0" class="empty-wrap">
				<text class="empty-text">暂无优惠券</text>
			</view>
			<view class="coupon-card" v-for="uc in coupons" :key="uc.id">
				<view class="coupon-left">
					<text class="coupon-type-tag">{{ uc.couponType === 1 ? '满减券' : uc.couponType === 2 ? '折扣券' : '无门槛券' }}</text>
					<text class="coupon-amount" v-if="uc.couponType === 1 || uc.couponType === 3">¥{{ uc.couponDiscount }}</text>
					<text class="coupon-amount" v-if="uc.couponType === 2">{{ (uc.couponDiscountRatio * 10).toFixed(1) }}折</text>
					<text class="coupon-condition" v-if="uc.couponType === 1 || uc.couponType === 2">满{{ uc.couponMinAmount }}可用</text>
					<text class="coupon-condition" v-if="uc.couponType === 3">无门槛使用</text>
				</view>
				<view class="coupon-right">
					<text class="coupon-name">{{ uc.couponName }}</text>
					<text class="coupon-desc" v-if="uc.couponType === 1">满{{ uc.couponMinAmount }}元减{{ uc.couponDiscount }}元</text>
					<text class="coupon-desc" v-if="uc.couponType === 2">满{{ uc.couponMinAmount }}元打{{ (uc.couponDiscountRatio * 10).toFixed(1) }}折</text>
					<text class="coupon-desc" v-if="uc.couponType === 3">立减{{ uc.couponDiscount }}元</text>
					<text class="coupon-scope">{{ uc.couponScope === 1 ? '全场通用' : uc.couponScope === 2 ? '仅限课程' : '仅限商品' }}</text>
					<text class="coupon-expire">{{ formatDate(uc.couponEndTime) }} 到期</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getMyCoupons } from '../../api/coupon'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return { activeTab: 0, coupons: [] }
	},
	onShow() {
		if (!checkLogin()) return
		this.loadCoupons()
	},
	methods: {
		switchTab(tab) {
			this.activeTab = tab
			this.loadCoupons()
		},
		async loadCoupons() {
			try {
				this.coupons = await getMyCoupons(this.activeTab)
			} catch(e) {
				this.coupons = []
			}
		},
		formatDate(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`
		},
		goBack() { uni.navigateBack() }
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }
.nav-bar { position: fixed; top: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top)); padding-top: env(safe-area-inset-top); background: #f5f6f7; }
.nav-back { font-size: 36rpx; }
.nav-title { font-size: 32rpx; font-weight: 900; }
.tab-row { display: flex; background: #fff; padding: 16rpx 24rpx; margin-top: calc(88rpx + env(safe-area-inset-top)); gap: 24rpx; }
.tab-item { padding: 12rpx 0; }
.tab-text { font-size: 28rpx; color: #999; }
.tab-text.active { color: #9c3f00; font-weight: 900; }
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top) - 80rpx); padding: 16rpx 24rpx; }
.empty-wrap { text-align: center; padding-top: 200rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.coupon-card { display: flex; background: #fff; border-radius: 20rpx; overflow: hidden; margin-bottom: 16rpx; }
.coupon-left { width: 200rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0; padding: 16rpx 0; gap: 6rpx; }
.coupon-type-tag { font-size: 20rpx; color: rgba(255,255,255,0.85); background: rgba(255,255,255,0.2); border-radius: 20rpx; padding: 4rpx 12rpx; }
.coupon-amount { font-size: 48rpx; font-weight: 900; color: #fff; line-height: 1.1; }
.coupon-condition { font-size: 18rpx; color: rgba(255,255,255,0.8); }
.coupon-right { flex: 1; padding: 24rpx; display: flex; flex-direction: column; justify-content: center; gap: 6rpx; }
.coupon-name { font-size: 28rpx; font-weight: 700; display: block; }
.coupon-desc { font-size: 22rpx; color: #333; display: block; }
.coupon-scope { font-size: 22rpx; color: #9c3f00; display: block; }
.coupon-expire { font-size: 20rpx; color: #999; }
</style>
