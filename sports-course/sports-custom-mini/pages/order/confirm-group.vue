<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title-text">确认订单</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll" v-if="course">
			<view class="course-card">
				<image class="course-img" :src="getCourseCoverImage(course, '/static/images/default-course.png')" mode="aspectFill" />
				<text class="course-name">{{ course.name }}</text>
				<view class="course-tag"><text class="course-tag-text">GROUP CLASS</text></view>
				<view class="course-price-row"><text class="cp-s">¥</text><text class="cp-n">{{ course.price }}</text><text class="cp-u">/人</text></view>
			</view>

			<view class="info-card" v-if="schedules.length">
				<view class="info-icon-wrap"><text class="info-icon">📅</text></view>
				<view class="info-body">
					<text class="info-label">SELECTED SESSIONS</text>
					<text class="info-value-lg">已选择 {{ selectedCount }} 个未来场次</text>
					<view class="schedule-summary-list">
						<view class="schedule-summary-item" v-for="sch in schedules" :key="sch.id">
							<text class="summary-date">{{ formatScheduleDate(sch.scheduleDate || sch.startTime) }}</text>
							<text class="summary-time">{{ course && course.startHour ? course.startHour : formatTime(sch.startTime) }} - {{ course && course.endHour ? course.endHour : formatTime(sch.endTime) }}</text>
							<text class="summary-meta">{{ (course && course.location) || sch.location || '待确认' }} · 已报 {{ sch.enrolledSeats }}/{{ sch.totalSeats }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="tip-card">
				<view class="tip-bar"></view>
				<view class="tip-content">
					<view class="tip-header"><text class="tip-icon">ℹ️</text><text class="tip-title">支付提示</text></view>
					<text class="tip-desc">下单后将通过<text class="bold-text">微信群</text>沟通具体上课安排，请保持联系方式畅通。{{ singleScheduleMode ? '' : '多场次报名会拆分为多笔团课订单并一次性完成支付。' }}</text>
				</view>
			</view>

			<!-- 优惠券 -->
			<view class="coupon-card" v-if="singleScheduleMode" @tap="showCouponPicker = true">
				<text class="coupon-label">🎫 优惠券</text>
				<view class="coupon-right">
					<text class="coupon-value" v-if="selectedCoupon">-¥{{ couponAmount }}</text>
					<text class="coupon-value none" v-else>{{ usableCoupons.length > 0 ? usableCoupons.length + '张可用' : '无可用券' }}</text>
					<text class="coupon-arrow">›</text>
				</view>
			</view>
			<view class="coupon-card disabled" v-else>
				<text class="coupon-label">🎫 优惠券</text>
				<view class="coupon-right">
					<text class="coupon-value none">多场次报名暂不支持优惠券</text>
				</view>
			</view>

			<view class="cost-card">
				<text class="cost-section-title">费用明细</text>
				<view class="cost-item"><text class="cost-label">课程费用</text><text class="cost-val">¥{{ unitPrice.toFixed(2) }} x {{ selectedCount }}</text></view>
				<view class="cost-item" v-if="couponAmount > 0"><text class="cost-label">优惠券折扣</text><text class="cost-val hl">-¥{{ couponAmount.toFixed(2) }}</text></view>
				<view class="cost-divider"></view>
				<view class="cost-item total"><text class="cost-label bold">实付金额</text><text class="cost-val bold big hl">¥{{ actualAmount }}</text></view>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<!-- 优惠券弹窗 -->
		<view class="coupon-mask" v-if="showCouponPicker && singleScheduleMode" @tap="showCouponPicker = false">
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
					<view class="popup-empty" v-if="usableCoupons.length === 0"><text>暂无可用优惠券</text></view>
				</view>
				<view class="popup-btn" @tap="showCouponPicker = false"><text class="popup-btn-text">确定</text></view>
			</view>
		</view>

		<view class="bottom-bar" v-if="course">
			<view class="total-info">
				<text class="total-label">TOTAL AMOUNT</text>
				<view class="total-row"><text class="ts">¥</text><text class="tn">{{ actualAmount }}</text></view>
			</view>
			<view class="pay-btn" @tap="onPay"><text class="pay-text">{{ paying ? '处理中...' : '提交订单' }}</text></view>
		</view>
	</view>
</template>

<script>
import { getCourseDetail, getScheduleDetail } from '../../api/course'
import { createBatchCourseOrder, createCourseOrder, payBatchOrders } from '../../api/order'
import { getUsableCoupons } from '../../api/coupon'
import config from '../../utils/config'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			courseId: null,
			scheduleIds: [],
			course: null,
			schedules: [],
			usableCoupons: [],
			selectedCoupon: null,
			couponAmount: 0,
			paying: false,
			showCouponPicker: false
		}
	},
	computed: {
		selectedCount() {
			return this.scheduleIds.length
		},
		singleScheduleMode() {
			return this.selectedCount === 1
		},
		unitPrice() {
			return parseFloat((this.course && this.course.price) || 0) || 0
		},
		actualAmount() {
			if (!this.course) return '0.00'
			const total = this.unitPrice * this.selectedCount
			return Math.max(0, total - this.couponAmount).toFixed(2)
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		this.courseId = options.courseId
		this.scheduleIds = (options.scheduleIds || options.scheduleId || '')
			.split(',')
			.map(id => Number(id))
			.filter(Boolean)
		this.loadCourse()
		this.loadSchedules()
	},
	methods: {
		getImageUrl: config.getImageUrl,
		getCourseCoverImage(course, fallback) {
			return config.getCourseCoverImage(course, fallback)
		},
		async loadCourse() {
			try {
				this.course = await getCourseDetail(this.courseId)
				if (this.singleScheduleMode) {
					this.loadCoupons()
				}
			} catch(e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		async loadSchedules() {
			if (!this.scheduleIds.length) return
			try {
				this.schedules = await Promise.all(this.scheduleIds.map(id => getScheduleDetail(id)))
			} catch(e) {
				console.error('加载排课失败', e)
			}
		},
		async loadCoupons() {
			if (!this.singleScheduleMode || !this.course) {
				this.usableCoupons = []
				return
			}
			try {
				this.usableCoupons = await getUsableCoupons(1, this.unitPrice)
			} catch(e) {
				this.usableCoupons = []
			}
		},
		pickCoupon(uc) {
			if (!this.singleScheduleMode) return
			if (this.selectedCoupon && this.selectedCoupon.id === uc.id) {
				this.selectedCoupon = null
				this.couponAmount = 0
			} else {
				this.selectedCoupon = uc
				const price = this.unitPrice
				if (uc.couponType === 1 || uc.couponType === 3) {
					this.couponAmount = parseFloat(uc.couponDiscount) || 0
				} else if (uc.couponType === 2) {
					this.couponAmount = price - price * parseFloat(uc.couponDiscountRatio)
				}
				this.couponAmount = Math.min(this.couponAmount, price)
			}
		},
		formatScheduleDate(dt) {
			if (!dt) return ''
			const dateStr = typeof dt === 'string' ? dt.slice(0, 10) : ''
			if (!dateStr) return ''
			const parts = dateStr.split('-')
			if (parts.length < 3) return dateStr
			const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
			const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
			return `${parseInt(parts[1])}月${parseInt(parts[2])}日 ${weekDays[d.getDay()]}`
		},
		formatDate(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getMonth()+1}月${d.getDate()}日`
		},
		formatTime(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
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
				if (this.singleScheduleMode) {
					const orderData = { courseId: this.courseId, scheduleId: this.scheduleIds[0] }
					if (this.selectedCoupon) {
						orderData.couponId = this.selectedCoupon.id
					}
					const order = await createCourseOrder(orderData)
					uni.redirectTo({ url: `/pages/order/order-detail-course?id=${order.id}&fromPay=1` })
					return
				}

				const orders = await createBatchCourseOrder({
					courseId: this.courseId,
					scheduleIds: this.scheduleIds
				})
				const orderIds = (orders || []).map(item => item.id).filter(Boolean)
				if (!orderIds.length) {
					throw new Error('批量下单失败')
				}
				await payBatchOrders(orderIds)
				uni.redirectTo({
					url: `/pages/order/success-course?batch=1&count=${orderIds.length}&amount=${encodeURIComponent(this.actualAmount)}&orderIds=${orderIds.join(',')}`
				})
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
.nav-title-text { font-size: 32rpx; font-weight: 900; color: #9c3f00; }
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top)); margin-top: calc(88rpx + env(safe-area-inset-top)); padding: 16rpx 24rpx; }

.course-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 16rpx; }
.course-img { width: 100%; height: 280rpx; border-radius: 16rpx; margin-bottom: 16rpx; }
.course-name { font-size: 32rpx; font-weight: 900; display: block; margin-bottom: 8rpx; }
.course-tag { background: rgba(156,63,0,0.1); padding: 4rpx 14rpx; border-radius: 8rpx; display: inline-block; margin-bottom: 12rpx; }
.course-tag-text { font-size: 20rpx; color: #9c3f00; font-weight: 700; letter-spacing: 2rpx; }
.course-price-row { display: flex; align-items: baseline; }
.cp-s { font-size: 24rpx; color: #9c3f00; }
.cp-n { font-size: 48rpx; color: #9c3f00; font-weight: 900; letter-spacing: -2rpx; }
.cp-u { font-size: 24rpx; color: #595c5d; margin-left: 4rpx; }

.info-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 16rpx; }
.info-icon-wrap { width: 64rpx; height: 64rpx; background: rgba(255,122,47,0.1); border-radius: 16rpx; display: flex; align-items: center; justify-content: center; margin-bottom: 16rpx; }
.info-icon { font-size: 28rpx; }
.info-label { font-size: 22rpx; color: #595c5d; font-weight: 700; letter-spacing: 2rpx; display: block; margin-bottom: 8rpx; }
.info-date { font-size: 36rpx; font-weight: 900; display: block; }
.info-time { font-size: 28rpx; color: #9c3f00; font-weight: 600; }
.info-value-lg { font-size: 32rpx; font-weight: 900; display: block; }
.info-sub { font-size: 22rpx; color: #595c5d; margin-top: 4rpx; }
.schedule-summary-list { margin-top: 16rpx; display: flex; flex-direction: column; gap: 12rpx; }
.schedule-summary-item { padding: 20rpx; border-radius: 16rpx; background: #f5f6f7; }
.summary-date { display: block; font-size: 28rpx; font-weight: 900; color: #2c2f30; margin-bottom: 6rpx; }
.summary-time { display: block; font-size: 24rpx; color: #9c3f00; font-weight: 700; margin-bottom: 6rpx; }
.summary-meta { display: block; font-size: 22rpx; color: #595c5d; line-height: 1.5; }

.tip-card { background: rgba(156,63,0,0.04); border-radius: 20rpx; padding: 20rpx; display: flex; gap: 12rpx; margin-bottom: 16rpx; }
.tip-bar { width: 6rpx; border-radius: 999rpx; background: #ff7a2f; }
.tip-header { display: flex; align-items: center; gap: 8rpx; margin-bottom: 4rpx; }
.tip-icon { font-size: 24rpx; }
.tip-title { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.tip-desc { font-size: 24rpx; color: #595c5d; line-height: 1.5; }
.bold-text { font-weight: 900; color: #2c2f30; }

.cost-card { background: #fff; border-radius: 24rpx; padding: 24rpx; }
.cost-section-title { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 16rpx; letter-spacing: 2rpx; }
.cost-item { display: flex; justify-content: space-between; padding: 8rpx 0; &.total { padding-top: 12rpx; } }
.cost-label { font-size: 26rpx; color: #595c5d; &.bold { font-weight: 900; color: #2c2f30; } }
.cost-val { font-size: 26rpx; &.hl { color: #9c3f00; } &.bold { font-weight: 900; } &.big { font-size: 34rpx; } }
.cost-divider { height: 2rpx; background: #eff1f2; margin: 12rpx 0; }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,0.95); box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.06); }
.total-label { font-size: 18rpx; color: #595c5d; font-weight: 700; letter-spacing: 2rpx; display: block; }
.total-row { display: flex; align-items: baseline; }
.ts { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.tn { font-size: 44rpx; color: #9c3f00; font-weight: 900; }
.pay-btn { padding: 22rpx 56rpx; border-radius: 24rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); }
.pay-text { font-size: 28rpx; color: #fff; font-weight: 900; }

.coupon-card { background: #fff; border-radius: 24rpx; padding: 24rpx; margin-bottom: 16rpx; display: flex; justify-content: space-between; align-items: center; }
.coupon-card.disabled { opacity: 0.8; }
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
</style>
