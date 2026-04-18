<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">订单详情</text>
			<view style="width: 40rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- 订单状态 -->
			<view class="status-card" :class="'status-' + order.status">
				<text class="status-icon">{{ statusIcon }}</text>
				<text class="status-text">{{ statusText }}</text>
				<text class="status-tip">{{ statusTip }}</text>
				<!-- 待支付倒计时：仅在非支付流程的待支付状态显示 -->
				<view class="countdown-wrap" v-if="order.status === 1 && !fromPay && countdownText">
					<text class="countdown-label">剩余支付时间</text>
					<text class="countdown-time">{{ countdownText }}</text>
				</view>
			</view>

			<!-- 物流信息（待收货状态显示） -->
			<view class="logistics-card" v-if="order.status === 3">
				<view class="logistics-header">
					<text class="logistics-icon">🚚</text>
					<text class="logistics-title">物流信息</text>
				</view>
				<view class="logistics-info">
					<text class="logistics-text">您的包裹正在配送中，预计1-3天送达</text>
				</view>
			</view>

			<!-- 收货地址 -->
			<view class="address-card" v-if="address">
				<view class="addr-title">
					<text class="addr-icon">📍</text>
					<text class="addr-label">收货信息</text>
				</view>
				<view class="addr-info">
					<view class="addr-row">
						<text class="addr-name">{{ address.receiverName }}</text>
						<text class="addr-phone">{{ address.phone }}</text>
					</view>
					<text class="addr-detail">{{ formatAddress(address) }}</text>
				</view>
			</view>

			<!-- 商品列表 -->
			<view class="products-card">
				<view class="card-header">
					<text class="shop-icon">🏋️</text>
					<text class="shop-name">KINETIC STORE</text>
				</view>
				<view class="product-item" v-for="(item, idx) in orderItems" :key="idx">
					<image class="product-img" :src="getImageUrl(item.itemPic) || '/static/logo.png'" mode="aspectFill" />
					<view class="product-info">
						<text class="product-name">{{ item.itemName }}</text>
						<text class="product-spec" v-if="item.skuProperties">{{ item.skuProperties }}</text>
						<view class="product-bottom">
							<text class="product-price">¥{{ item.price }}</text>
							<text class="product-qty">x{{ item.qty }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 订单信息 -->
			<view class="info-card">
				<view class="info-row">
					<text class="info-label">订单编号</text>
					<text class="info-value">{{ order.orderNumber }}</text>
					<text class="copy-btn" @tap="copyOrderNo">复制</text>
				</view>
				<view class="info-row">
					<text class="info-label">下单时间</text>
					<text class="info-value">{{ formatTime(order.createTime) }}</text>
				</view>
				<view class="info-row">
					<text class="info-label">订单状态</text>
					<text class="info-value highlight">{{ statusText }}</text>
				</view>
				<view class="info-row" v-if="order.paymentTime">
					<text class="info-label">支付时间</text>
					<text class="info-value">{{ formatTime(order.paymentTime) }}</text>
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
					<text class="info-label">订单备注</text>
					<text class="info-value">{{ order.remark }}</text>
				</view>
			</view>

			<!-- 费用明细 -->
			<view class="cost-card">
				<view class="cost-row">
					<text class="cost-label">商品总额</text>
					<text class="cost-value">¥{{ order.totalAmount || '0.00' }}</text>
				</view>
				<view class="cost-row" v-if="order.couponAmount > 0">
					<text class="cost-label">优惠金额</text>
					<text class="cost-value discount">-¥{{ order.couponAmount }}</text>
				</view>
				<view class="cost-row">
					<text class="cost-label">运费</text>
					<text class="cost-value">¥0.00</text>
				</view>
				<view class="cost-divider"></view>
				<view class="cost-row total">
					<text class="cost-label">实付金额</text>
					<text class="cost-value final">¥{{ order.actualAmount || '0.00' }}</text>
				</view>
			</view>

			<!-- 退款信息 -->
			<view class="refund-card" v-if="order.status >= 6 && order.status <= 8">
				<view class="refund-header">
					<text class="refund-icon">💰</text>
					<text class="refund-title">退款信息</text>
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
		<view class="bottom-bar" v-if="showBottomBar">
			<view class="btn-area">
				<!-- 待付款 -->
				<template v-if="order.status === 1">
					<view class="btn-outline" @tap="handlePendingSecondaryAction">
						<text class="btn-outline-text">{{ pendingSecondaryText }}</text>
					</view>
					<view class="btn-primary" @tap="handlePay">
						<text class="btn-primary-text">{{ pendingPrimaryText }}</text>
					</view>
				</template>

				<!-- 待发货/待收货 -->
				<template v-if="order.status === 2 || order.status === 3">
					<view class="btn-outline" @tap="handleRefund">
						<text class="btn-outline-text">申请退款</text>
					</view>
					<view class="btn-primary" v-if="order.status === 3" @tap="handleConfirm">
						<text class="btn-primary-text">确认收货</text>
					</view>
				</template>

				<!-- 已完成 -->
				<template v-if="order.status === 4">
					<view class="btn-outline" @tap="handleRefund" v-if="canRefund">
						<text class="btn-outline-text">申请退款</text>
					</view>
					<view class="btn-primary" @tap="handleRebuy">
						<text class="btn-primary-text">再次购买</text>
					</view>
				</template>

				<!-- 已取消/已退款 -->
				<template v-if="order.status === 5 || order.status === 7">
					<view class="btn-primary" @tap="handleRebuy">
						<text class="btn-primary-text">再次购买</text>
					</view>
				</template>

				<!-- 退款中 -->
				<template v-if="order.status === 6">
					<view class="btn-outline disabled">
						<text class="btn-outline-text">退款审核中</text>
					</view>
				</template>

				<!-- 退款驳回 -->
				<template v-if="order.status === 8">
					<view class="btn-primary" @tap="handleRebuy">
						<text class="btn-primary-text">再次购买</text>
					</view>
				</template>
			</view>
		</view>
	</view>
</template>

<script>
import { getOrderDetail, getOrderItems, cancelOrder, payOrder, confirmReceive } from '../../api/order'
import { checkLogin } from '../../utils/auth'
import config from '../../utils/config'

export default {
	data() {
		return {
			orderId: null,
			order: {},
			orderItems: [],
			address: null,
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
			const map = {
				1: '待付款',
				2: '待发货',
				3: '待收货',
				4: '已完成',
				5: '已取消',
				6: '退款中',
				7: '已退款',
				8: '退款驳回'
			}
			return map[this.order.status] || '未知'
		},
		statusTip() {
			if (this.isPaymentScene) {
				return '订单已创建，请完成本次支付。离开后订单会进入待付款列表，可稍后继续支付。'
			}
			if (this.order.status === 1) return '订单待支付，超时后系统会自动取消。'
			if (this.order.status === 2) return '订单已支付，商家正在备货发货，请耐心等待。'
			if (this.order.status === 3) return '商品已发出，确认收货后订单将完成。'
			if (this.order.status === 4) {
				return this.canRefund
					? '订单已完成，如需售后请在确认收货 7 天内申请退款。'
					: '订单已完成，当前已超过售后申请时限。'
			}
			if (this.order.status === 5) return '订单已取消，如使用优惠券且满足条件会自动退回。'
			if (this.order.status === 6) return '退款申请已提交，请等待后台审核处理。'
			if (this.order.status === 7) return '退款已完成，商品库存和相关权益已同步回滚。'
			if (this.order.status === 8) return '退款申请未通过，订单已恢复到退款前状态，可继续按订单状态处理。'
			return '当前订单状态已更新。'
		},
		statusIcon() {
			const icons = {
				1: '⏳',
				2: '📦',
				3: '🚚',
				4: '✅',
				5: '❌',
				6: '💰',
				7: '✅',
				8: '⚠️'
			}
			return icons[this.order.status] || '📦'
		},
		pendingSecondaryText() {
			return this.isPaymentScene ? '放弃支付' : '取消订单'
		},
		pendingPrimaryText() {
			return this.isPaymentScene ? '继续支付' : '立即支付'
		},
		showBottomBar() {
			return [1, 2, 3, 4, 5, 6, 7, 8].includes(this.order.status)
		},
		canRefund() {
			if (this.order.status !== 4 || !this.order.finishTime) return false
			const finishTime = new Date(this.order.finishTime)
			const now = new Date()
			const days = (now - finishTime) / (1000 * 60 * 60 * 24)
			return days <= 7
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		this.orderId = options.id
		this.fromPay = options.fromPay === '1'
		this.loadOrderDetail()
	},
	onShow() {
		if (this.orderId) {
			this.loadOrderDetail()
		}
	},
	onUnload() {
		this.clearCountdown()
	},
	onHide() {
		this.clearCountdown()
	},
	methods: {
		getImageUrl: config.getImageUrl,
		async loadOrderDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				const res = await getOrderDetail(this.orderId)
				const order = res.order || res || {}
				this.order = order
				if (order.status !== 1) {
					this.fromPay = false
				}

				const items = await getOrderItems(this.orderId)
				this.orderItems = items || []

				this.address = null
				if (order.addressSnapshot) {
					try {
						this.address = JSON.parse(order.addressSnapshot)
					} catch (e) {
						console.log('地址解析失败')
					}
				}

				this.clearCountdown()
				if (order.status === 1 && order.createTime) {
					this.startCountdown(order.createTime)
				}
			} catch (e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},
		startCountdown(createTime) {
			const TIMEOUT_MS = 15 * 60 * 1000
			const normalized = createTime.replace('T', ' ').replace(/-/g, '/').substring(0, 19)
			const created = new Date(normalized)
			if (isNaN(created.getTime())) return
			const expireAt = created.getTime() + TIMEOUT_MS
			const tick = () => {
				const remaining = expireAt - Date.now()
				if (remaining <= 0) {
					this.countdownText = '00:00'
					this.clearCountdown()
					this.loadOrderDetail()
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
		formatAddress(addr) {
			const parts = [addr.province, addr.city, addr.district, addr.detailAddress].filter(p => p)
			return parts.join(' ')
		},
		formatTime(t) {
			if (!t) return ''
			return t.replace(/T/, ' ').substring(0, 16)
		},
		copyOrderNo() {
			uni.setClipboardData({
				data: this.order.orderNumber,
				success: () => {
					uni.showToast({ title: '已复制', icon: 'success' })
				}
			})
		},
		async handlePendingSecondaryAction() {
			if (this.isPaymentScene) {
				this.confirmGiveUpPayment()
				return
			}
			this.handleCancel()
		},
		async handleCancel() {
			uni.showModal({
				title: '提示',
				content: '确定要取消该订单吗？',
				success: async (res) => {
					if (res.confirm) {
						try {
							await cancelOrder(this.orderId)
							this.fromPay = false
							uni.showToast({ title: '已取消', icon: 'success' })
							this.loadOrderDetail()
						} catch (e) {
							const msg = e.msg || '取消失败'
							uni.showModal({
								title: '提示',
								content: msg,
								showCancel: false
							})
						}
					}
				}
			})
		},
		confirmGiveUpPayment() {
			uni.showModal({
				title: '是否放弃本次支付？',
				content: '放弃后订单会进入待付款列表，您之后仍可继续支付。',
				cancelText: '继续支付',
				confirmText: '放弃支付',
				success: (res) => {
					if (res.confirm) {
						this.fromPay = false
						uni.redirectTo({ url: '/pages/order/my-orders-product' })
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
				uni.redirectTo({ url: `/pages/order/success-product?orderId=${this.orderId}` })
			} catch (e) {
				uni.hideLoading()
				const msg = e.msg || '支付失败'
				uni.showModal({
					title: '提示',
					content: msg,
					showCancel: false
				})
			}
		},
		async handleConfirm() {
			uni.showModal({
				title: '确认收货',
				content: '确认已收到商品吗？确认后订单将完成。',
				success: async (res) => {
					if (res.confirm) {
						try {
							await confirmReceive(this.orderId)
							uni.showToast({ title: '已确认收货', icon: 'success' })
							this.loadOrderDetail()
						} catch (e) {
							const msg = e.msg || '操作失败'
							uni.showModal({
								title: '提示',
								content: msg,
								showCancel: false
							})
						}
					}
				}
			})
		},
		handleRefund() {
			uni.navigateTo({ url: '/pages/order/refund?id=' + this.orderId })
		},
		handleRebuy() {
			uni.switchTab({ url: '/pages/mall/mall' })
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
	top: 0;
	left: 0;
	right: 0;
	z-index: 99;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 24rpx;
	height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top);
	background: #fff;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.nav-back {
	font-size: 36rpx;
	color: #2c2f30;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #2c2f30;
}

.content-scroll {
	height: calc(100vh - 88rpx - 120rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
	padding: 24rpx;
}

/* 状态卡片 */
.status-card {
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	border-radius: 24rpx;
	padding: 40rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 24rpx;
}

.status-icon {
	font-size: 60rpx;
	margin-bottom: 12rpx;
}

.status-text {
	font-size: 34rpx;
	font-weight: 700;
	color: #fff;
}

.status-tip {
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
	margin-top: 8rpx;
}

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

/* 地址卡片 */
.address-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.addr-title {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-bottom: 16rpx;
}

.addr-icon {
	font-size: 28rpx;
}

.addr-label {
	font-size: 26rpx;
	font-weight: 600;
	color: #2c2f30;
}

.addr-info {
	padding-left: 36rpx;
}

.addr-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 8rpx;
}

.addr-name {
	font-size: 28rpx;
	font-weight: 600;
	color: #2c2f30;
}

.addr-phone {
	font-size: 26rpx;
	color: #595c5d;
}

.addr-detail {
	font-size: 24rpx;
	color: #595c5d;
}

/* 商品卡片 */
.products-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.card-header {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #f5f5f5;
	margin-bottom: 16rpx;
}

.shop-icon {
	font-size: 28rpx;
}

.shop-name {
	font-size: 26rpx;
	font-weight: 700;
	color: #2c2f30;
}

.product-item {
	display: flex;
	gap: 16rpx;
	padding: 16rpx 0;
}

.product-img {
	width: 140rpx;
	height: 140rpx;
	border-radius: 16rpx;
	background: #f5f5f5;
}

.product-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.product-name {
	font-size: 28rpx;
	font-weight: 600;
	color: #2c2f30;
}

.product-spec {
	font-size: 22rpx;
	color: #999;
}

.product-bottom {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: auto;
}

.product-price {
	font-size: 28rpx;
	font-weight: 700;
	color: #2c2f30;
}

.product-qty {
	font-size: 24rpx;
	color: #999;
}

/* 订单信息 */
.info-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.info-row {
	display: flex;
	align-items: center;
	padding: 12rpx 0;
}

.info-label {
	font-size: 26rpx;
	color: #999;
	width: 160rpx;
	flex-shrink: 0;
}

.info-value {
	flex: 1;
	font-size: 26rpx;
	color: #2c2f30;
	text-align: right;
}

.info-value.highlight {
	color: #ff7a2f;
	font-weight: 600;
}

.copy-btn {
	font-size: 22rpx;
	color: #ff7a2f;
	margin-left: 16rpx;
	padding: 4rpx 12rpx;
	border: 1rpx solid #ff7a2f;
	border-radius: 8rpx;
}

/* 费用明细 */
.cost-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
}

.cost-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12rpx 0;
}

.cost-label {
	font-size: 26rpx;
	color: #595c5d;
}

.cost-value {
	font-size: 26rpx;
	color: #2c2f30;
}

.cost-value.discount {
	color: #ff7a2f;
}

.cost-value.final {
	font-size: 32rpx;
	font-weight: 700;
	color: #ff7a2f;
}

/* 物流信息 */
.logistics-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.logistics-header {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-bottom: 12rpx;
}

.logistics-icon {
	font-size: 28rpx;
}

.logistics-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #2c2f30;
}

.logistics-info {
	padding-left: 36rpx;
}

.logistics-text {
	font-size: 24rpx;
	color: #595c5d;
}

/* 退款信息 */
.refund-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.refund-header {
	display: flex;
	align-items: center;
	gap: 8rpx;
	margin-bottom: 12rpx;
}

.refund-icon {
	font-size: 28rpx;
}

.refund-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #2c2f30;
}

/* 底部栏 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	z-index: 99;
	padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.04);
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

.btn-outline-text {
	font-size: 26rpx;
	color: #666;
}

.btn-primary {
	padding: 20rpx 48rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	border-radius: 40rpx;
}

.btn-primary-text {
	font-size: 26rpx;
	color: #fff;
	font-weight: 600;
}
</style>
