<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">订单详情</text>
			<view style="width: 40rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll" v-if="order.id">
			<!-- 状态卡片 -->
			<view class="status-card" :class="'status-bg-' + order.status">
				<text class="status-icon">{{ statusIcon }}</text>
				<text class="status-main">{{ statusText }}</text>
				<text class="status-tip">{{ statusTip }}</text>
				<!-- 待支付倒计时：仅在非支付流程的待支付状态显示 -->
				<view class="countdown-wrap" v-if="order.status === 1 && !fromPay && countdownText">
					<text class="countdown-label">剩余支付时间</text>
					<text class="countdown-time">{{ countdownText }}</text>
				</view>
			</view>

			<!-- 课程信息 -->
			<view class="section-card">
				<view class="section-header">
					<text class="section-icon">🏋️</text>
					<text class="section-title">课程信息</text>
				</view>
				<view class="course-row">
					<view class="course-icon-wrap">
						<text class="course-icon-emoji">{{ courseType === 1 ? '🧘' : '👥' }}</text>
					</view>
					<view class="course-info">
						<text class="course-name">{{ courseName }}</text>
						<text class="course-type-tag">{{ courseType === 1 ? '私教课' : '团课' }}</text>
					</view>
				</view>
				<!-- 私教课：课包进度 -->
				<view class="package-progress" v-if="courseType === 1 && pkg">
					<view class="progress-row">
						<text class="progress-label">课包进度</text>
						<text class="progress-value">{{ pkg.usedLessons }} / {{ pkg.totalLessons }} 节</text>
					</view>
					<view class="progress-bar-wrap">
						<view class="progress-bar-fill" :style="{ width: progressPercent + '%' }"></view>
					</view>
					<view class="progress-row" style="margin-top: 12rpx;">
						<text class="progress-label">有效期至</text>
						<text class="progress-value">{{ formatDate(pkg.expireTime) }}</text>
					</view>
					<view class="progress-row">
						<text class="progress-label">课包状态</text>
						<text class="progress-value" :class="pkg.status === 3 ? 'text-green' : pkg.status === 1 ? 'text-orange' : 'text-gray'">
							{{ pkgStatusText }}
						</text>
					</view>
				</view>
				<!-- 团课：排课信息 -->
				<view class="schedule-info" v-if="courseType === 2 && schedule">
					<view class="info-row">
						<text class="info-label">上课时间</text>
						<text class="info-value">{{ formatTime(schedule.startTime) }}</text>
					</view>
					<view class="info-row" v-if="schedule.location">
						<text class="info-label">上课地点</text>
						<text class="info-value">{{ schedule.location }}</text>
					</view>
					<view class="info-row" v-if="schedule.coachName">
						<text class="info-label">授课教练</text>
						<text class="info-value">{{ schedule.coachName }}</text>
					</view>
				</view>
			</view>

			<!-- 消课记录（私教课） -->
			<view class="section-card" v-if="courseType === 1 && checkins.length > 0">
				<view class="section-header">
					<text class="section-icon">📋</text>
					<text class="section-title">消课记录</text>
					<text class="section-count">共 {{ checkins.length }} 节</text>
				</view>
				<view class="checkin-item" v-for="(c, idx) in checkins" :key="c.id">
					<view class="checkin-index">
						<text class="checkin-no">{{ idx + 1 }}</text>
					</view>
					<view class="checkin-info">
						<text class="checkin-time">{{ formatTime(c.checkinTime) }}</text>
						<text class="checkin-coach" v-if="c.coachName">教练：{{ c.coachName }}</text>
					</view>
					<view class="checkin-status" :class="c.status === 1 ? 'attended' : 'absent'">
						<text class="checkin-status-text">{{ c.status === 1 ? '已出勤' : '缺勤' }}</text>
					</view>
				</view>
			</view>

			<!-- 订单信息 -->
			<view class="section-card">
				<view class="section-header">
					<text class="section-icon">📄</text>
					<text class="section-title">订单信息</text>
				</view>
				<view class="info-row">
					<text class="info-label">订单编号</text>
					<view class="info-value-row">
						<text class="info-value">{{ order.orderNumber }}</text>
						<text class="copy-btn" @tap="copyOrderNo">复制</text>
					</view>
				</view>
				<view class="info-row">
					<text class="info-label">下单时间</text>
					<text class="info-value">{{ formatTime(order.createTime) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">订单状态</text>
					<text class="info-value" :class="'status-text-' + order.status">{{ statusText }}</text>
				</view>
				<view class="info-row" v-if="order.paymentTime">
					<text class="info-label">支付时间</text>
					<text class="info-value">{{ formatTime(order.paymentTime) }}</text>
				</view>
				<view class="info-row" v-if="order.paymentMethod">
					<text class="info-label">支付方式</text>
					<text class="info-value">{{ payMethodText }}</text>
				</view>
				<view class="info-row" v-if="order.finishTime">
					<text class="info-label">完成时间</text>
					<text class="info-value">{{ formatTime(order.finishTime) }}</text>
				</view>
				<view class="info-row" v-if="order.closeTime">
					<text class="info-label">关闭时间</text>
					<text class="info-value">{{ formatTime(order.closeTime) }}</text>
				</view>
				<view class="info-row" v-if="order.remark">
					<text class="info-label">备注</text>
					<text class="info-value">{{ order.remark }}</text>
				</view>
			</view>

			<!-- 费用明细 -->
			<view class="section-card">
				<view class="section-header">
					<text class="section-icon">💳</text>
					<text class="section-title">费用明细</text>
				</view>
				<view class="cost-row">
					<text class="cost-label">课程原价</text>
					<text class="cost-value">¥{{ order.totalAmount || '0.00' }}</text>
				</view>
				<view class="cost-row" v-if="order.couponAmount > 0">
					<text class="cost-label">优惠券抵扣</text>
					<text class="cost-value discount">-¥{{ order.couponAmount }}</text>
				</view>
				<view class="cost-divider"></view>
				<view class="cost-row total-row">
					<text class="cost-label-total">实付金额</text>
					<text class="cost-value-total">¥{{ order.actualAmount || '0.00' }}</text>
				</view>
			</view>

			<!-- 退款信息 -->
			<view class="section-card" v-if="order.status >= 6 && order.status <= 8">
				<view class="section-header">
					<text class="section-icon">💰</text>
					<text class="section-title">退款信息</text>
				</view>
				<view class="info-row" v-if="order.refundReason">
					<text class="info-label">退款原因</text>
					<text class="info-value">{{ order.refundReason }}</text>
				</view>
				<view class="info-row" v-if="order.refundAmount">
					<text class="info-label">退款金额</text>
					<text class="info-value highlight">¥{{ order.refundAmount }}</text>
				</view>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar" v-if="order.id">
			<view class="btn-area">
				<!-- 待支付 -->
				<template v-if="order.status === 1">
					<view class="btn-outline" @tap="handlePendingSecondaryAction">
						<text class="btn-outline-text">{{ pendingSecondaryText }}</text>
					</view>
					<view class="btn-primary" @tap="handlePay">
						<text class="btn-primary-text">{{ pendingPrimaryText }}</text>
					</view>
				</template>
				<!-- 进行中（待排课）：可申请退款 -->
				<template v-if="order.status === 2 || order.status === 3">
					<view class="btn-outline" @tap="handleRefund">
						<text class="btn-outline-text">申请退款</text>
					</view>
					<!-- 私教课：查看课包 -->
					<view class="btn-primary" v-if="courseType === 1" @tap="goPackage">
						<text class="btn-primary-text">查看课包</text>
					</view>
				</template>
				<!-- 已完成 -->
				<template v-if="order.status === 4">
					<view class="btn-primary" @tap="goPackage" v-if="courseType === 1">
						<text class="btn-primary-text">查看课包</text>
					</view>
				</template>
				<!-- 退款中 -->
				<template v-if="order.status === 6">
					<view class="btn-outline disabled">
						<text class="btn-outline-text">退款审核中</text>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script>
import { getOrderDetail, cancelOrder, payOrder } from '../../api/order'
import { checkLogin } from '../../utils/auth'
import http from '../../utils/http'

export default {
	data() {
		return {
			orderId: null,
			order: {},
			pkg: null,
			schedule: null,
			checkins: [],
			courseName: '',
			courseType: null,
			countdownText: '',
			countdownTimer: null,
			fromPay: false
		}
	},
	computed: {
		isPaymentScene() {
			return this.fromPay && this.order.status === 1
		},
		statusText() {
			if (this.isPaymentScene) return '请完成支付'
			const map = { 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成', 5: '已取消', 6: '退款中', 7: '已退款', 8: '退款驳回' }
			return map[this.order.status] || '未知'
		},
		statusIcon() {
			const map = { 1: '⏳', 2: '✅', 3: '🏃', 4: '🎉', 5: '❌', 6: '💰', 7: '✅', 8: '⚠️' }
			return map[this.order.status] || '📋'
		},
		statusTip() {
			const s = this.order.status
			if (this.isPaymentScene) {
				return '订单已创建，请完成本次支付。离开后订单会进入待支付列表，可稍后继续支付。'
			}
			if (s === 1) return '订单待支付，超时后系统会自动取消。'
			if (s === 2) return '订单已支付，等待排课安排。'
			if (s === 3) {
				if (this.courseType === 1) {
					const used = this.pkg ? this.pkg.usedLessons : 0
					const total = this.pkg ? this.pkg.totalLessons : 0
					return `课包进行中，已完成 ${used}/${total} 节课。`
				}
				return '已成功预约，请按时参加课程。'
			}
			if (s === 4) return '课程已全部完成，感谢您的参与！'
			if (s === 5) return '订单已取消。'
			if (s === 6) return '退款申请已提交，请等待后台审核处理。'
			if (s === 7) return '退款已完成，相关权益已同步回滚。'
			if (s === 8) return '退款申请已被驳回，如有疑问请联系客服。'
			return ''
		},
		progressPercent() {
			if (!this.pkg || !this.pkg.totalLessons) return 0
			return Math.min(100, Math.round(this.pkg.usedLessons / this.pkg.totalLessons * 100))
		},
		pkgStatusText() {
			if (!this.pkg) return ''
			const map = { 0: '已过期', 1: '进行中', 2: '已退费', 3: '已完成' }
			return map[this.pkg.status] || ''
		},
		payMethodText() {
			const map = { wechat: '微信支付', alipay: '支付宝', cash: '现金支付' }
			return map[this.order.paymentMethod] || this.order.paymentMethod || '-'
		},
		pendingSecondaryText() {
			return this.isPaymentScene ? '放弃支付' : '取消订单'
		},
		pendingPrimaryText() {
			return this.isPaymentScene ? '继续支付' : '立即支付'
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		this.orderId = options.id
		this.fromPay = options.fromPay === '1'
		this.loadDetail()
	},
	onShow() {
		if (this.orderId) this.loadDetail()
	},
	onUnload() {
		this.clearCountdown()
	},
	onHide() {
		this.clearCountdown()
	},
	methods: {
		async loadDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				// 订单详情接口已包含 order、course、coach、schedule
				const res = await getOrderDetail(this.orderId)
				this.order = res.order || {}
				if (this.order.status !== 1) {
					this.fromPay = false
				}
				const course = res.course || {}
				this.courseName = course.name || '课程'
				this.courseType = course.type || null // 1=私教 2=团课
				this.schedule = null
				this.pkg = null
				this.checkins = []

				// 团课：排课信息已在详情接口中返回
				if (this.courseType === 2) {
					this.schedule = res.schedule || null
					// 附加教练名到 schedule 上方便模板使用
					if (this.schedule && res.coach) {
						this.schedule.coachName = res.coach.name || null
					}
				}

				// 私教课：加载课包和消课记录
				if (this.courseType === 1) {
					try {
						const pkgs = await http.get('/user/package/list')
						this.pkg = (pkgs || []).find(p => String(p.orderId) === String(this.orderId)) || null
					} catch(e) {}

					if (this.pkg) {
						try {
							this.checkins = await http.get(`/user/package/checkin/${this.pkg.id}`) || []
						} catch(e) {}
					}
				}

				// 待支付状态启动倒计时
				this.clearCountdown()
				if (this.order.status === 1 && this.order.createTime) {
					this.startCountdown(this.order.createTime)
				}
			} catch(e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		// 启动倒计时（超时 15 分钟）
		startCountdown(createTime) {
			const TIMEOUT_MS = 15 * 60 * 1000
			// 兼容 "2026-04-17T15:33:00" 和 "2026-04-17 15:33:00" 两种格式
			const normalized = createTime.replace('T', ' ').replace(/-/g, '/').substring(0, 19)
			const created = new Date(normalized)
			if (isNaN(created.getTime())) return
			const expireAt = created.getTime() + TIMEOUT_MS

			const tick = () => {
				const remaining = expireAt - Date.now()
				if (remaining <= 0) {
					this.countdownText = '00:00'
					this.clearCountdown()
					// 时间到，刷新订单状态
					this.loadDetail()
					return
				}
				const m = Math.floor(remaining / 60000)
				const s = Math.floor((remaining % 60000) / 1000)
				this.countdownText = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
			}
			tick()
			this.countdownTimer = setInterval(tick, 1000)
		},
		clearCountdown() {
			if (this.countdownTimer) {
				clearInterval(this.countdownTimer)
				this.countdownTimer = null
			}
		},
		formatTime(t) {
			if (!t) return ''
			return t.replace(/T/, ' ').substring(0, 16)
		},
		formatDate(t) {
			if (!t) return ''
			return t.substring(0, 10)
		},
		copyOrderNo() {
			uni.setClipboardData({
				data: this.order.orderNumber,
				success: () => uni.showToast({ title: '已复制', icon: 'success' })
			})
		},
		handlePendingSecondaryAction() {
			if (this.isPaymentScene) {
				this.confirmGiveUpPayment()
				return
			}
			this.handleCancel()
		},
		async handleCancel() {
			uni.showModal({
				title: '确认取消',
				content: '确定要取消该订单吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await cancelOrder(this.orderId)
							this.fromPay = false
							uni.showToast({ title: '已取消', icon: 'success' })
							this.loadDetail()
						} catch(e) {
							uni.showModal({ title: '提示', content: e.msg || '取消失败', showCancel: false })
						}
					}
				}
			})
		},
		confirmGiveUpPayment() {
			uni.showModal({
				title: '是否放弃本次支付？',
				content: '放弃后订单会进入待支付列表，您之后仍可继续支付。',
				cancelText: '继续支付',
				confirmText: '放弃支付',
				success: (res) => {
					if (res.confirm) {
						this.fromPay = false
						uni.redirectTo({ url: '/pages/order/my-orders-course' })
					}
				}
			})
		},
		async handlePay() {
			try {
				uni.showLoading({ title: '支付中...' })
				await payOrder(this.orderId)
				this.fromPay = false
				uni.hideLoading()
				uni.redirectTo({ url: `/pages/order/success-course?orderId=${this.orderId}` })
			} catch(e) {
				uni.hideLoading()
				uni.showModal({ title: '提示', content: e.msg || '支付失败', showCancel: false })
			}
		},
		handleRefund() {
			uni.navigateTo({ url: `/pages/order/refund?id=${this.orderId}` })
		},
		goPackage() {
			uni.navigateTo({ url: '/pages/profile/my-packages' })
		},
		goBack() {
			if (this.isPaymentScene) {
				this.confirmGiveUpPayment()
				return
			}
			uni.navigateBack()
		}
	}
}
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #f5f6f7;
}

.nav-bar {
	position: fixed;
	top: 0; left: 0; right: 0;
	z-index: 99;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 24rpx;
	height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top);
	background: #fff;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.nav-back { font-size: 36rpx; color: #2c2f30; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #2c2f30; }

.content-scroll {
	height: calc(100vh - 88rpx - 120rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
	padding: 24rpx;
}

/* 状态卡片 */
.status-card {
	border-radius: 24rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 24rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
}
.status-bg-4 { background: linear-gradient(135deg, #2e7d32, #66bb6a); }
.status-bg-5 { background: linear-gradient(135deg, #616161, #9e9e9e); }
.status-bg-7 { background: linear-gradient(135deg, #616161, #9e9e9e); }
.status-bg-6 { background: linear-gradient(135deg, #e65100, #ff9800); }
.status-bg-8 { background: linear-gradient(135deg, #b71c1c, #ef5350); }
.status-icon { font-size: 64rpx; margin-bottom: 12rpx; }
.status-main { font-size: 36rpx; font-weight: 700; color: #fff; }
.status-tip { font-size: 24rpx; color: rgba(255,255,255,0.85); margin-top: 8rpx; text-align: center; line-height: 1.5; }
.countdown-wrap {
	margin-top: 20rpx;
	background: rgba(0,0,0,0.15);
	border-radius: 16rpx;
	padding: 12rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
}
.countdown-label { font-size: 22rpx; color: rgba(255,255,255,0.8); }
.countdown-time { font-size: 48rpx; font-weight: 900; color: #fff; letter-spacing: 4rpx; font-variant-numeric: tabular-nums; }

/* 通用卡片 */
.section-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}
.section-header {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #f5f5f5;
	margin-bottom: 16rpx;
}
.section-icon { font-size: 28rpx; }
.section-title { font-size: 28rpx; font-weight: 700; color: #2c2f30; flex: 1; }
.section-count { font-size: 24rpx; color: #999; }

/* 课程信息 */
.course-row {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 20rpx;
}
.course-icon-wrap {
	width: 96rpx;
	height: 96rpx;
	background: #fff3eb;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.course-icon-emoji { font-size: 44rpx; }
.course-info { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.course-name { font-size: 30rpx; font-weight: 700; color: #2c2f30; }
.course-type-tag {
	display: inline-block;
	font-size: 22rpx;
	color: #9c3f00;
	background: rgba(156,63,0,0.08);
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	align-self: flex-start;
}

/* 课包进度 */
.package-progress {
	background: #fafafa;
	border-radius: 16rpx;
	padding: 20rpx;
}
.progress-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10rpx;
}
.progress-label { font-size: 24rpx; color: #999; }
.progress-value { font-size: 26rpx; color: #2c2f30; font-weight: 600; }
.text-green { color: #4caf50 !important; }
.text-orange { color: #ff7a2f !important; }
.text-gray { color: #999 !important; }
.progress-bar-wrap {
	height: 12rpx;
	background: rgba(156,63,0,0.1);
	border-radius: 999rpx;
	overflow: hidden;
	margin-bottom: 4rpx;
}
.progress-bar-fill {
	height: 100%;
	background: linear-gradient(90deg, #9c3f00, #ff7a2f);
	border-radius: 999rpx;
	transition: width 0.3s;
}

/* 消课记录 */
.checkin-item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 16rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
}
.checkin-item:last-child { border-bottom: none; }
.checkin-index {
	width: 48rpx;
	height: 48rpx;
	background: #fff3eb;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}
.checkin-no { font-size: 22rpx; font-weight: 700; color: #9c3f00; }
.checkin-info { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.checkin-time { font-size: 26rpx; color: #2c2f30; }
.checkin-coach { font-size: 22rpx; color: #999; }
.checkin-status {
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
	flex-shrink: 0;
}
.checkin-status.attended { background: #e8f5e9; }
.checkin-status.absent { background: #f5f6f7; }
.checkin-status-text { font-size: 22rpx; font-weight: 600; }
.checkin-status.attended .checkin-status-text { color: #4caf50; }
.checkin-status.absent .checkin-status-text { color: #999; }

/* 订单信息 */
.info-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 12rpx 0;
	border-bottom: 1rpx solid #f9f9f9;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 26rpx; color: #999; flex-shrink: 0; }
.info-value-row { display: flex; align-items: center; gap: 12rpx; }
.info-value { font-size: 26rpx; color: #2c2f30; text-align: right; }
.info-value.highlight { color: #ff7a2f; font-weight: 600; }
.status-text-1 { color: #ff9800; font-weight: 600; }
.status-text-2 { color: #2196f3; font-weight: 600; }
.status-text-3 { color: #ff7a2f; font-weight: 600; }
.status-text-4 { color: #4caf50; font-weight: 600; }
.status-text-5 { color: #999; }
.status-text-6 { color: #ff9800; font-weight: 600; }
.status-text-7 { color: #999; }
.status-text-8 { color: #f44336; font-weight: 600; }
.copy-btn {
	font-size: 22rpx;
	color: #9c3f00;
	padding: 4rpx 12rpx;
	border: 1rpx solid #9c3f00;
	border-radius: 8rpx;
}

/* 费用明细 */
.cost-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12rpx 0;
}
.cost-label { font-size: 26rpx; color: #595c5d; }
.cost-value { font-size: 26rpx; color: #2c2f30; }
.cost-value.discount { color: #ff7a2f; }
.cost-divider { height: 1rpx; background: #f0f0f0; margin: 8rpx 0; }
.total-row { padding-top: 16rpx; }
.cost-label-total { font-size: 28rpx; font-weight: 700; color: #2c2f30; }
.cost-value-total { font-size: 36rpx; font-weight: 700; color: #ff7a2f; }

/* 底部操作栏 */
.bottom-bar {
	position: fixed;
	bottom: 0; left: 0; right: 0;
	z-index: 99;
	padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04);
}
.btn-area {
	display: flex;
	justify-content: flex-end;
	gap: 16rpx;
}
.btn-outline {
	padding: 20rpx 40rpx;
	border: 2rpx solid #e0e0e0;
	border-radius: 40rpx;
}
.btn-outline.disabled { opacity: 0.5; }
.btn-outline-text { font-size: 26rpx; color: #666; }
.btn-primary {
	padding: 20rpx 48rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	border-radius: 40rpx;
}
.btn-primary-text { font-size: 26rpx; color: #fff; font-weight: 600; }
</style>
