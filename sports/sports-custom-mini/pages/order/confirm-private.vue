<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">确认订单</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll" v-if="course">
			<!-- 课包信息 -->
			<view class="package-card">
				<view class="pkg-top">
					<view class="pkg-tag"><text class="pkg-tag-text">PRIVATE PACKAGE</text></view>
					<view class="pkg-price-col">
						<text class="pkg-price">¥{{ course.price }}</text>
						<text class="pkg-original" v-if="course.originalPrice">¥{{ course.originalPrice }}</text>
					</view>
				</view>
				<text class="pkg-name">{{ course.name }}</text>
				<text class="pkg-desc">{{ course.description || '' }}</text>
				<view class="pkg-features">
					<view class="pkg-feat"><text class="feat-icon">📦</text><text class="feat-text">{{ course.lessonCount }}节课</text></view>
					<view class="pkg-feat"><text class="feat-icon">⏱</text><text class="feat-text">有效期{{ course.validityDays || 90 }}天</text></view>
					<view class="pkg-feat" v-if="course.isDoorService === 1"><text class="feat-icon">🏠</text><text class="feat-text">上门授课</text></view>
				</view>
			</view>

			<!-- 提示 -->
			<view class="tip-card">
				<text class="tip-icon">🔖</text>
				<view class="tip-content">
					<text class="tip-title">提示：安排上课流程</text>
					<text class="tip-desc">下单后将由专人邀请进微信群安排上课，请留意系统推送及联系电话。</text>
				</view>
			</view>

			<!-- 优惠券 -->
			<view class="coupon-card" @tap="showCouponPicker = true">
				<text class="coupon-label">🎫 优惠券</text>
				<view class="coupon-right">
					<text class="coupon-value" v-if="selectedCoupon">-¥{{ couponAmount }}</text>
					<text class="coupon-value none" v-else>{{ usableCoupons.length > 0 ? usableCoupons.length + '张可用' : '无可用券' }}</text>
					<text class="coupon-arrow">›</text>
				</view>
			</view>

			<!-- 价格明细 -->
			<view class="cost-card">
				<view class="cost-item"><text class="cost-label">课包原价</text><text class="cost-value">¥{{ course.price }}</text></view>
				<view class="cost-item" v-if="course.originalPrice"><text class="cost-label">优惠价</text><text class="cost-value hl">-¥{{ course.originalPrice - course.price }}</text></view>
				<view class="cost-item" v-if="couponAmount > 0"><text class="cost-label">优惠券抵扣</text><text class="cost-value hl">-¥{{ couponAmount }}</text></view>
				<view class="cost-divider"></view>
				<view class="cost-item total"><text class="cost-label bold">实付金额</text><text class="cost-value bold big">¥{{ actualAmount }}</text></view>
			</view>

			<view class="agreement">
				<text class="agreement-text">ℹ️ 点击提交订单即表示您已阅读并同意《私教服务购买协议》及《退款规则》。本课程一经开课不接受非正当理由退款。</text>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<!-- 优惠券弹窗 -->
		<view class="coupon-mask" v-if="showCouponPicker" @tap="showCouponPicker = false">
			<view class="coupon-popup" @tap.stop>
				<view class="popup-title">选择优惠券</view>
				<view class="popup-list">
					<view class="popup-item" :class="{ active: selectedCoupon && selectedCoupon.id === uc.id }" v-for="uc in usableCoupons" :key="uc.id" @tap="pickCoupon(uc)">
						<view class="popup-item-left">
							<text class="popup-item-amount" v-if="uc.couponType === 1 || uc.couponType === 3">¥{{ uc.couponDiscount }}</text>
							<text class="popup-item-amount" v-if="uc.couponType === 2">{{ (uc.couponDiscountRatio * 10).toFixed(1) }}折</text>
							<text class="popup-item-name">{{ uc.couponName }}</text>
						</view>
						<text class="popup-item-check" v-if="selectedCoupon && selectedCoupon.id === uc.id">✓</text>
					</view>
					<view class="popup-empty" v-if="usableCoupons.length === 0">
						<text>暂无可用优惠券</text>
					</view>
				</view>
				<view class="popup-btn" @tap="showCouponPicker = false">
					<text class="popup-btn-text">确定</text>
					</view>
			</view>
		</view>

		<view class="bottom-bar" v-if="course">
			<view class="total-info">
				<text class="total-label">TOTAL AMOUNT</text>
				<view class="total-price-row"><text class="ts">¥</text><text class="tn">{{ actualAmount }}</text></view>
			</view>
			<view class="pay-btn" @tap="onPay">
				<text class="pay-text">{{ paying ? '处理中...' : '提交订单 »' }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getCourseDetail } from '../../api/course'
import { createCourseOrder, payOrder } from '../../api/order'
import { getUsableCoupons } from '../../api/coupon'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			courseId: null,
			course: null,
			usableCoupons: [],
			selectedCoupon: null,
			couponAmount: 0,
			orderId: null,
			paying: false,
			showCouponPicker: false
		}
	},
	computed: {
		actualAmount() {
			if (!this.course) return 0
			const price = parseFloat(this.course.price) || 0
			return Math.max(0, price - this.couponAmount).toFixed(2)
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		this.courseId = options.courseId
		this.loadCourse()
		this.loadCoupons()
	},
	methods: {
		async loadCourse() {
			try {
				this.course = await getCourseDetail(this.courseId)
			} catch(e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		async loadCoupons() {
			try {
				const price = this.course ? this.course.price : 99999
				this.usableCoupons = await getUsableCoupons(1, price)
			} catch(e) {
				this.usableCoupons = []
			}
		},
		pickCoupon(uc) {
			if (this.selectedCoupon && this.selectedCoupon.id === uc.id) {
				this.selectedCoupon = null
				this.couponAmount = 0
			} else {
				this.selectedCoupon = uc
				const price = parseFloat(this.course.price) || 0
				if (uc.couponType === 1 || uc.couponType === 3) {
					this.couponAmount = parseFloat(uc.couponDiscount) || 0
				} else if (uc.couponType === 2) {
					this.couponAmount = price - price * parseFloat(uc.couponDiscountRatio)
				}
				this.couponAmount = Math.min(this.couponAmount, price)
			}
		},
		async onPay() {
			if (this.paying) return
			// 手机号绑定校验
			const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
			if (!userInfo.phone) {
				uni.showModal({ title: '提示', content: '下单前需绑定手机号', confirmText: '去绑定', success: (res) => { if (res.confirm) uni.navigateTo({ url: '/pages/profile/bind-phone' }) } })
				return
			}
			this.paying = true
			try {
				// 创建订单，跳转到订单详情页等待用户支付
				const orderData = { courseId: this.courseId }
				if (this.selectedCoupon) {
					orderData.couponId = this.selectedCoupon.id
				}
				const order = await createCourseOrder(orderData)
				uni.redirectTo({ url: `/pages/order/order-detail-course?id=${order.id}&fromPay=1` })
			} catch(e) {
				console.error('下单失败', e)
				const errorMsg = e.msg || e.message || '下单失败'
				uni.showModal({
					title: '提示',
					content: errorMsg,
					showCancel: false,
					confirmText: '我知道了'
				})
			} finally {
				this.paying = false
			}
		},
		goBack() { uni.navigateBack(); }
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }
.nav-bar { position: fixed; top: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top)); padding-top: env(safe-area-inset-top); background: #f5f6f7; }
.nav-back { font-size: 36rpx; }
.nav-title { font-size: 32rpx; font-weight: 900; }
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top)); margin-top: calc(88rpx + env(safe-area-inset-top)); padding: 16rpx 24rpx; }

.package-card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-bottom: 16rpx; position: relative; overflow: hidden; }
.pkg-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12rpx; }
.pkg-tag { background: rgba(156,63,0,0.1); padding: 6rpx 16rpx; border-radius: 12rpx; }
.pkg-tag-text { font-size: 20rpx; color: #9c3f00; font-weight: 700; letter-spacing: 2rpx; }
.pkg-price-col { text-align: right; }
.pkg-price { font-size: 44rpx; color: #9c3f00; font-weight: 900; letter-spacing: -2rpx; display: block; }
.pkg-original { font-size: 22rpx; color: #abadae; text-decoration: line-through; }
.pkg-name { font-size: 36rpx; font-weight: 900; display: block; margin-bottom: 4rpx; }
.pkg-desc { font-size: 24rpx; color: #595c5d; display: block; margin-bottom: 16rpx; }
.pkg-features { display: flex; gap: 16rpx; }
.pkg-feat { display: flex; align-items: center; gap: 6rpx; background: #eff1f2; padding: 8rpx 16rpx; border-radius: 12rpx; }
.feat-icon { font-size: 22rpx; }
.feat-text { font-size: 22rpx; font-weight: 600; }

.section-label { padding: 12rpx 0; }
.sl-text { font-size: 24rpx; color: #595c5d; font-weight: 600; }

.coach-card { background: #fff; border-radius: 24rpx; padding: 24rpx; display: flex; gap: 16rpx; align-items: center; margin-bottom: 16rpx; }
.coach-avatar { width: 100rpx; height: 100rpx; border-radius: 50%; }
.coach-info { flex: 1; }
.coach-row { display: flex; align-items: baseline; gap: 8rpx; margin-bottom: 4rpx; }
.coach-name { font-size: 30rpx; font-weight: 900; }
.coach-en { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.coach-desc { font-size: 22rpx; color: #595c5d; display: block; margin-bottom: 8rpx; }
.coach-stats { display: flex; gap: 16rpx; }
.coach-stat { font-size: 20rpx; color: #595c5d; }
.coach-arrow { font-size: 32rpx; color: #abadae; }

.tip-card { background: rgba(156,63,0,0.05); border: 2rpx dashed rgba(156,63,0,0.2); border-radius: 20rpx; padding: 20rpx; display: flex; gap: 12rpx; margin-bottom: 16rpx; }
.tip-icon { font-size: 28rpx; }
.tip-title { font-size: 26rpx; color: #9c3f00; font-weight: 700; display: block; margin-bottom: 4rpx; }
.tip-desc { font-size: 22rpx; color: #595c5d; line-height: 1.5; }

.cost-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 16rpx; }
.cost-item { display: flex; justify-content: space-between; align-items: center; padding: 10rpx 0; &.total { padding-top: 16rpx; } }
.cost-label { font-size: 26rpx; color: #595c5d; &.bold { font-weight: 900; color: #2c2f30; } }
.cost-value { font-size: 26rpx; &.hl { color: #9c3f00; } &.bold { font-weight: 900; color: #2c2f30; } &.big { font-size: 36rpx; } }
.cost-right { display: flex; align-items: center; gap: 8rpx; }
.cost-arrow { font-size: 24rpx; color: #abadae; }
.cost-divider { height: 2rpx; background: #eff1f2; margin: 12rpx 0; }

/* 优惠券 */
.coupon-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 16rpx; display: flex; justify-content: space-between; align-items: center; }
.coupon-label { font-size: 28rpx; font-weight: 700; }
.coupon-right { display: flex; align-items: center; gap: 8rpx; }
.coupon-value { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.coupon-value.none { color: #abadae; }
.coupon-arrow { font-size: 28rpx; color: #abadae; }

.coupon-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 999; display: flex; align-items: flex-end; }
.coupon-popup { width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0; padding: 32rpx; max-height: 60vh; }
.popup-title { font-size: 32rpx; font-weight: 900; margin-bottom: 24rpx; text-align: center; }
.popup-list { max-height: 40vh; overflow-y: auto; }
.popup-item { display: flex; justify-content: space-between; align-items: center; padding: 24rpx; border-radius: 16rpx; margin-bottom: 12rpx; background: #f5f6f7; &.active { background: rgba(255,122,47,0.1); } }
.popup-item-left { flex: 1; }
.popup-item-amount { font-size: 36rpx; font-weight: 900; color: #9c3f00; margin-right: 16rpx; }
.popup-item-name { font-size: 26rpx; color: #2c2f30; }
.popup-item-check { font-size: 32rpx; color: #9c3f00; font-weight: 900; }
.popup-empty { text-align: center; padding: 40rpx; color: #999; font-size: 26rpx; }
.popup-btn { margin-top: 24rpx; padding: 24rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); border-radius: 24rpx; text-align: center; }
.popup-btn-text { font-size: 30rpx; color: #fff; font-weight: 900; }

.agreement { padding: 0 8rpx; }
.agreement-text { font-size: 20rpx; color: #abadae; line-height: 1.6; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,0.95); box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.06); }
.total-info {}
.total-label { font-size: 18rpx; color: #595c5d; font-weight: 700; letter-spacing: 2rpx; display: block; }
.total-price-row { display: flex; align-items: baseline; }
.ts { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.tn { font-size: 44rpx; color: #9c3f00; font-weight: 900; letter-spacing: -2rpx; }
.pay-btn { padding: 22rpx 48rpx; border-radius: 24rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); }
.pay-text { font-size: 28rpx; color: #fff; font-weight: 900; letter-spacing: 2rpx; }
</style>
