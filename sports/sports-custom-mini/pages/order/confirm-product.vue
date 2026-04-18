<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">确认订单</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll">
			<!-- 收货地址 -->
			<view class="address-card" @tap="goSelectAddress">
				<view class="addr-icon-wrap"><text class="addr-icon">📍</text></view>
				<view class="addr-content">
					<template v-if="selectedAddress">
						<view class="addr-header-row">
							<text class="addr-name">{{ selectedAddress.receiverName }}</text>
							<text class="addr-phone">{{ selectedAddress.phone }}</text>
						</view>
						<text class="addr-detail">{{ formatAddress(selectedAddress) }}</text>
					</template>
					<text v-else class="addr-placeholder">请添加收货地址</text>
				</view>
				<text class="addr-arrow">›</text>
			</view>

			<!-- 商品列表 -->
			<view class="order-card">
				<view class="order-shop">
					<text class="shop-icon">🏋️</text>
					<text class="shop-name">KINETIC STORE</text>
				</view>
				<view class="product-row" v-for="(item, idx) in prodItems" :key="idx">
					<image class="product-img" :src="getImageUrl(item.pic) || '/static/logo.png'" mode="aspectFill" />
					<view class="product-info">
						<text class="product-name">{{ item.name }}</text>
						<text class="product-spec">数量: x{{ item.qty }}</text>
						<view class="product-price-row">
							<view class="pp-left"><text class="pp-symbol">¥</text><text class="pp-num">{{ item.price }}</text></view>
						</view>
					</view>
				</view>

				<!-- 优惠券 -->
				<view class="option-list">
					<view class="option-item" @tap="openCouponPicker">
						<text class="option-label">优惠券</text>
						<view class="option-value-wrap">
							<text class="option-value" :class="{ highlight: selectedCoupon }">
								{{ selectedCoupon ? couponDesc(selectedCoupon) : '选择优惠券' }}
							</text>
							<text class="option-arrow">›</text>
						</view>
					</view>
					<view class="option-item column">
						<text class="option-label">订单备注</text>
						<textarea class="remark-input" v-model="remark" placeholder="对配送有特殊要求？" />
					</view>
				</view>
			</view>

			<!-- 费用明细 -->
			<view class="cost-card">
				<view class="cost-item">
					<text class="cost-label">商品小计</text>
					<text class="cost-value">¥{{ totalAmount }}</text>
				</view>
				<view class="cost-item" v-if="couponAmount > 0">
					<text class="cost-label">优惠金额</text>
					<text class="cost-value highlight">-¥{{ couponAmount }}</text>
				</view>
				<view class="cost-item">
					<text class="cost-label">运费</text>
					<text class="cost-value">¥0.00</text>
				</view>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="total-info">
				<text class="total-label">TOTAL</text>
				<view class="total-price-row">
					<text class="total-symbol">¥</text>
					<text class="total-num">{{ actualAmount }}</text>
				</view>
			</view>
			<view class="submit-btn" @tap="submitOrder">
				<text class="submit-text">提交订单</text>
			</view>
		</view>

		<!-- 优惠券选择弹窗 -->
		<view class="coupon-mask" v-if="showCouponPicker" @tap="showCouponPicker = false">
			<view class="coupon-popup" @tap.stop>
				<view class="coupon-popup-header">
					<text class="coupon-popup-title">选择优惠券</text>
					<text class="coupon-popup-close" @tap="showCouponPicker = false">✕</text>
				</view>
				<scroll-view scroll-y class="coupon-list-scroll">
					<view class="coupon-pick-item" :class="{ active: !selectedCoupon }" @tap="pickCoupon(null)">
						<text class="coupon-pick-name">不使用优惠券</text>
					</view>
					<view
						v-for="(c, i) in couponList"
						:key="i"
						class="coupon-pick-item"
						:class="{ active: selectedCoupon && selectedCoupon.id === c.id }"
						@tap="pickCoupon(c)"
					>
						<view class="coupon-pick-left">
							<text class="coupon-pick-amount">{{ c.couponType === 2 ? (c.couponDiscountRatio * 10).toFixed(1) + '折' : '¥' + c.couponDiscount }}</text>
							<text class="coupon-pick-condition">{{ c.couponMinAmount > 0 ? '满' + c.couponMinAmount + '可用' : '无门槛' }}</text>
						</view>
						<view class="coupon-pick-right">
							<text class="coupon-pick-name">{{ c.couponName }}</text>
							<text class="coupon-pick-scope">{{ scopeText(c.couponScope) }}</text>
						</view>
					</view>
					<view v-if="couponList.length === 0" class="coupon-empty">
						<text class="coupon-empty-text">暂无可用优惠券</text>
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script>
import { getProdDetail } from '../../api/prod'
import { getCartList } from '../../api/cart'
import { createProdOrder, payOrder } from '../../api/order'
import { getUsableCoupons } from '../../api/coupon'
import { getDefaultAddress } from '../../api/address'
import config from '../../utils/config'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			// 入口类型: 'single'=单品购买, 'cart'=购物车结算
			entryType: 'cart',
			prodId: null,
			singleQty: 1,
			cartIds: [],
			prodItems: [],
			totalAmount: '0.00',
			couponAmount: 0,
			actualAmount: '0.00',
			remark: '',
			selectedCoupon: null,
			couponList: [],
			showCouponPicker: false,
			selectedAddress: null
		}
	},
	async onLoad(options) {
		// 检查登录状态
		if (!checkLogin()) return
		
		if (options.prodId) {
			// 单品购买入口
			this.entryType = 'single'
			this.prodId = Number(options.prodId)
			this.singleQty = Number(options.qty) || 1
			await this.loadSingleProd()
		} else if (options.cartIds) {
			// 购物车结算入口
			this.entryType = 'cart'
			this.cartIds = options.cartIds.split(',').map(Number)
			await this.loadCartItems()
		}
		this.loadCoupons()
		this.loadDefaultAddress()
	},
	onShow() {
		// 从地址选择页返回时会触发
	},
	methods: {
		getImageUrl: config.getImageUrl,
		async loadSingleProd() {
			try {
				const prod = await getProdDetail(this.prodId)
				this.prodItems = [{
					prodId: prod.id,
					name: prod.name,
					pic: prod.pic,
					price: prod.price,
					qty: this.singleQty
				}]
				this.calcAmount()
			} catch (e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		async loadCartItems() {
			try {
				const res = await getCartList()
				const allCart = res || []
				// 只取选中的购物车项
				const selected = allCart.filter(c => this.cartIds.includes(c.id))
				
				// 检查是否所有购物车项都存在（防止用户切换后数据不一致）
				if (selected.length !== this.cartIds.length) {
					uni.showModal({
						title: '提示',
						content: '购物车数据已变更，请重新选择商品',
						showCancel: false,
						success: () => {
							uni.navigateTo({ url: '/pages/cart/cart' })
						}
					})
					return
				}
				
				this.prodItems = selected.map(c => ({
					cartId: c.id,
					prodId: c.prodId,
					name: c.prodName || '商品',
					pic: c.prodPic || '',
					price: c.skuPrice || 0,
					qty: c.qty
				}))
				this.calcAmount()
			} catch (e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		calcAmount() {
			let total = 0
			this.prodItems.forEach(item => {
				total += Number(item.price) * item.qty
			})
			this.totalAmount = total.toFixed(2)
			this.recalcActual()
		},
		recalcActual() {
			const total = Number(this.totalAmount)
			const discount = this.couponAmount || 0
			const actual = Math.max(0, total - discount)
			this.actualAmount = actual.toFixed(2)
		},
		async loadCoupons() {
			try {
				const total = Number(this.totalAmount) || 0
				const res = await getUsableCoupons(2, total)
				this.couponList = res || []
			} catch (e) {
				this.couponList = []
			}
		},
		openCouponPicker() {
			this.showCouponPicker = true
		},
		pickCoupon(coupon) {
			this.selectedCoupon = coupon
			this.showCouponPicker = false
			this.applyCoupon()
		},
		applyCoupon() {
			if (!this.selectedCoupon) {
				this.couponAmount = 0
				this.recalcActual()
				return
			}
			const c = this.selectedCoupon
			const total = Number(this.totalAmount)
			let discount = 0
			if (c.couponType === 1) { // 满减
				discount = Number(c.couponDiscount)
			} else if (c.couponType === 2) { // 折扣
				discount = total - total * Number(c.couponDiscountRatio)
			} else if (c.couponType === 3) { // 无门槛
				discount = Number(c.couponDiscount)
			}
			discount = Math.min(discount, total)
			this.couponAmount = Number(discount.toFixed(2))
			this.recalcActual()
		},
		couponDesc(c) {
			if (c.couponType === 2) return (c.couponDiscountRatio * 10).toFixed(1) + '折'
			return '-¥' + c.couponDiscount
		},
		scopeText(scope) {
			if (scope === 1) return '全场通用'
			if (scope === 2) return '仅课程'
			if (scope === 3) return '仅商品'
			return ''
		},
		showAddressTip() {
			uni.showToast({ title: '地址管理开发中', icon: 'none' })
		},
		async loadDefaultAddress() {
			try {
				const addr = await getDefaultAddress()
				if (addr) {
					this.selectedAddress = addr
				}
			} catch (e) {
				console.log('暂无默认地址')
			}
		},
		formatAddress(addr) {
			const parts = [addr.province, addr.city, addr.district, addr.detailAddress].filter(p => p)
			return parts.join(' ')
		},
		goSelectAddress() {
			uni.navigateTo({ 
				url: '/pages/address/address-list?select=1',
				events: {
					selectAddress: (addr) => {
						this.selectedAddress = addr
					}
				}
			})
		},
			async submitOrder() {
				if (this.prodItems.length === 0) return
				// 地址校验
				if (!this.selectedAddress) {
					uni.showModal({ title: '提示', content: '请先选择收货地址', confirmText: '去选择', success: (res) => { if (res.confirm) this.goSelectAddress() } })
					return
				}
				// 手机号绑定校验
				const userInfo = JSON.parse(uni.getStorageSync('userInfo') || '{}')
				if (!userInfo.phone) {
					uni.showModal({ title: '提示', content: '下单前需绑定手机号', confirmText: '去绑定', success: (res) => { if (res.confirm) uni.navigateTo({ url: '/pages/profile/bind-phone' }) } })
					return
				}
				try {
					const orderData = { 
						remark: this.remark,
						addressId: this.selectedAddress.id
					}
					if (this.entryType === 'cart' && this.cartIds.length > 0) {
						orderData.cartIds = this.cartIds
					} else if (this.entryType === 'single' && this.prodId) {
						orderData.prodId = this.prodId
						orderData.buyQty = this.singleQty
					}
					if (this.selectedCoupon) {
						orderData.couponId = this.selectedCoupon.id
					}
					const order = await createProdOrder(orderData)
					// 跳转到支付页，由用户确认是否继续支付
					uni.redirectTo({ url: `/pages/order/order-detail-product?id=${order.id}&fromPay=1` })
			} catch (e) {
				console.error('下单失败', e)
				// 显示后端返回的错误信息
				const errorMsg = e.msg || e.message || '下单失败'
				uni.showModal({
					title: '提示',
					content: errorMsg,
					showCancel: false,
					confirmText: '我知道了'
				})
			}
		},
		goBack() { uni.navigateBack(); }
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }
.nav-bar { position: fixed; top: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top)); padding-top: env(safe-area-inset-top); background: #f5f6f7; }
.nav-back { font-size: 36rpx; color: #2c2f30; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #9c3f00; }
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top)); margin-top: calc(88rpx + env(safe-area-inset-top)); padding: 16rpx 24rpx; }

.address-card { background: #fff; border-radius: 24rpx; padding: 24rpx; display: flex; gap: 16rpx; align-items: center; margin-bottom: 16rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.04); }
.addr-icon-wrap { width: 72rpx; height: 72rpx; background: rgba(156,63,0,0.05); border-radius: 16rpx; display: flex; align-items: center; justify-content: center; }
.addr-icon { font-size: 32rpx; }
.addr-content { flex: 1; }
.addr-header-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 4rpx; }
.addr-name { font-size: 28rpx; font-weight: 700; color: #2c2f30; }
.addr-phone { font-size: 24rpx; color: #595c5d; }
.addr-detail { font-size: 24rpx; color: #595c5d; display: block; }
.addr-placeholder { font-size: 28rpx; color: #999; }
.addr-arrow { font-size: 32rpx; color: #abadae; }

.order-card { background: #fff; border-radius: 24rpx; overflow: hidden; margin-bottom: 16rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.04); }
.order-shop { display: flex; align-items: center; gap: 8rpx; padding: 20rpx 24rpx; border-bottom: 2rpx solid rgba(171,173,174,0.1); }
.shop-icon { font-size: 28rpx; }
.shop-name { font-size: 26rpx; font-weight: 700; letter-spacing: 1rpx; }
.product-row { display: flex; gap: 16rpx; padding: 20rpx 24rpx; }
.product-img { width: 140rpx; height: 140rpx; border-radius: 16rpx; background: #f0f0f0; }
.product-info { flex: 1; }
.product-name { font-size: 28rpx; font-weight: 700; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-spec { font-size: 22rpx; color: #595c5d; display: block; margin-top: 4rpx; }
.product-price-row { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 12rpx; }
.pp-left { display: flex; align-items: baseline; }
.pp-symbol { font-size: 22rpx; color: #9c3f00; font-weight: 700; }
.pp-num { font-size: 36rpx; color: #9c3f00; font-weight: 900; letter-spacing: -2rpx; }

.option-list { padding: 0 24rpx 20rpx; }
.option-item { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; &.column { flex-direction: column; align-items: flex-start; gap: 12rpx; } }
.option-label { font-size: 26rpx; color: #595c5d; }
.option-value-wrap { display: flex; align-items: center; gap: 8rpx; }
.option-value { font-size: 26rpx; font-weight: 700; &.highlight { color: #9c3f00; } }
.option-arrow { font-size: 28rpx; color: #abadae; }
.remark-input { width: 100%; height: 140rpx; background: #eff1f2; border: none; border-radius: 16rpx; padding: 16rpx; font-size: 26rpx; }

.cost-card { background: #fff; border-radius: 24rpx; padding: 24rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.04); }
.cost-item { display: flex; justify-content: space-between; padding: 10rpx 0; }
.cost-label { font-size: 26rpx; color: #595c5d; }
.cost-value { font-size: 26rpx; font-weight: 500; &.highlight { color: #9c3f00; } }

.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 99; display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; padding-bottom: calc(16rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,0.95); backdrop-filter: blur(20px); box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.06); }
.total-label { font-size: 18rpx; color: #595c5d; font-weight: 700; letter-spacing: 2rpx; display: block; }
.total-price-row { display: flex; align-items: baseline; }
.total-symbol { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.total-num { font-size: 44rpx; color: #9c3f00; font-weight: 900; letter-spacing: -2rpx; }
.submit-btn { padding: 20rpx 60rpx; border-radius: 24rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); box-shadow: 0 8rpx 20rpx rgba(255,122,47,0.2); }
.submit-text { font-size: 28rpx; color: #fff; font-weight: 700; }

/* 优惠券弹窗 */
.coupon-mask { position: fixed; inset: 0; z-index: 200; background: rgba(0,0,0,0.4); display: flex; align-items: flex-end; }
.coupon-popup { width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0; padding: 32rpx; padding-bottom: calc(32rpx + env(safe-area-inset-bottom)); max-height: 70vh; }
.coupon-popup-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24rpx; }
.coupon-popup-title { font-size: 32rpx; font-weight: 700; }
.coupon-popup-close { font-size: 32rpx; color: #999; }
.coupon-list-scroll { max-height: 55vh; }
.coupon-pick-item { display: flex; align-items: center; gap: 20rpx; padding: 20rpx; border-radius: 16rpx; margin-bottom: 12rpx; border: 2rpx solid #e0e3e4; &.active { border-color: #ff7a2f; background: rgba(255,122,47,0.05); } }
.coupon-pick-left { text-align: center; min-width: 140rpx; }
.coupon-pick-amount { font-size: 36rpx; font-weight: 900; color: #9c3f00; display: block; }
.coupon-pick-condition { font-size: 20rpx; color: #999; display: block; }
.coupon-pick-right { flex: 1; }
.coupon-pick-name { font-size: 28rpx; font-weight: 700; display: block; }
.coupon-pick-scope { font-size: 22rpx; color: #999; display: block; margin-top: 4rpx; }
.coupon-empty { text-align: center; padding: 60rpx 0; }
.coupon-empty-text { font-size: 28rpx; color: #999; }
</style>
