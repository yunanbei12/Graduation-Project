<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="nav-back">←</text>
				<text class="nav-title-text">详情</text>
			</view>
			<view class="nav-right">
				<text class="nav-icon">↗</text>
				<text class="nav-icon">🛒</text>
			</view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- 商品图片轮播 -->
			<view class="swiper-section">
				<swiper class="product-swiper" @change="onSwiperChange" circular>
					<swiper-item v-for="(img, i) in images" :key="i">
						<image class="swiper-img" :src="img" mode="aspectFill" />
					</swiper-item>
				</swiper>
				<view class="swiper-dots">
					<view class="dot" :class="{ active: currentIndex === i }" v-for="(img, i) in images" :key="i"></view>
				</view>
				<view class="swiper-counter">
					<text class="counter-text">{{ currentIndex + 1 }}/{{ images.length }}</text>
				</view>
			</view>

			<!-- 价格信息卡 -->
			<view class="price-card">
				<view class="price-row">
					<view class="price-main">
						<text class="price-symbol">¥</text>
						<text class="price-num">{{ product?.price || 0 }}</text>
						<text class="price-original" v-if="product?.originalPrice">¥{{ product.originalPrice }}</text>
					</view>
					<view class="new-tag" v-if="product?.sales > 100">
						<text class="new-tag-text">HOT</text>
					</view>
				</view>
				<text class="product-title">{{ product?.name || '商品详情' }}</text>
				<text class="product-desc">{{ product?.description || '暂无描述' }}</text>
				<view class="rating-row">
					<view class="rating-item">
						<text class="fire-icon">🔥</text>
						<text class="rating-val">销量 {{ product?.sales || 0 }}</text>
					</view>
				</view>
			</view>

			<!-- 技术参数 -->
			<view class="specs-card">
				<text class="specs-title">TECHNICAL SPECS</text>
				<view class="spec-item" v-for="(spec, i) in specs" :key="i">
					<text class="spec-label">{{ spec.label }}</text>
					<text class="spec-value" :class="{ highlight: spec.highlight }">{{ spec.value }}</text>
				</view>
			</view>

			<!-- 商品详情 -->
			<view class="detail-section" v-if="product?.detail || product?.description">
				<view class="detail-header">
					<text class="detail-title">商品详情</text>
					<view class="detail-line"></view>
				</view>
				<view class="detail-content">
					<rich-text v-if="product?.detail" :nodes="product.detail" class="rich-text"></rich-text>
					<text v-else class="detail-text">{{ product?.description || '暂无详情' }}</text>
				</view>
			</view>

			<view class="related-section" v-if="relatedProducts.length">
				<view class="related-header">
					<text class="related-title">相似推荐</text>
					<text class="related-subtitle">基于当前商品特征与用户偏好生成</text>
				</view>
				<scroll-view scroll-x class="related-scroll" show-scrollbar="false">
					<view class="related-row">
						<view class="related-card" v-for="item in relatedProducts" :key="item.id" @tap="openRelated(item)">
							<image class="related-img" :src="item.image" mode="aspectFill" />
							<view class="related-info">
								<text class="related-reason">{{ item.reason }}</text>
								<text class="related-name">{{ item.name }}</text>
								<text class="related-sales">销量 {{ item.sales || 0 }}</text>
								<text class="related-price price-text">¥{{ item.price }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<!-- 数量选择弹窗(单规格) -->
		<view class="sku-mask" v-if="showSku" @tap="showSku = false">
			<view class="sku-popup" @tap.stop>
				<view class="sku-close" @tap="showSku = false">
					<text class="sku-close-icon">✕</text>
				</view>
				<view class="sku-header">
					<image class="sku-img" :src="images[0] || ''" mode="aspectFill" />
					<view class="sku-price-info">
						<view class="sku-price-row">
							<text class="sku-price-symbol">¥</text>
							<text class="sku-price-val">{{ product?.price || 0 }}</text>
						</view>
						<text class="sku-selected">已选: 数量 {{ qty }}</text>
					</view>
				</view>

				<view class="sku-section">
					<text class="sku-label">购买数量</text>
					<view class="qty-row">
						<view class="qty-btn" @tap="qty > 1 && qty--">
							<text class="qty-btn-text">－</text>
						</view>
						<text class="qty-num">{{ qty }}</text>
						<view class="qty-btn" @tap="qty++">
							<text class="qty-btn-text">＋</text>
						</view>
					</view>
				</view>

				<view class="sku-confirm" @tap="confirmSku">
					<text class="sku-confirm-text">{{ confirmBtnText }} 🛒</text>
				</view>
				<text class="sku-footer-note">FREE SHIPPING & RETURNS FOR MEMBERS</text>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view class="bottom-bar">
			<view class="bar-icons">
				<view class="bar-icon-item">
					<text class="bar-icon">🎧</text>
					<text class="bar-icon-label">客服</text>
				</view>
				<view class="bar-icon-item">
					<text class="bar-icon">❤️</text>
					<text class="bar-icon-label">收藏</text>
				</view>
			</view>
			<view class="bar-btn-outline" @tap="openSku('cart')">
				<text class="bar-btn-outline-text">加入购物车</text>
			</view>
			<view class="bar-btn-primary" @tap="openSku('buy')">
				<text class="bar-btn-primary-text">立即购买</text>
			</view>
		</view>
	</view>
</template>

<script>
import { trackDetailView, trackRecommendClick } from '../../api/behavior'
import { getProdDetail } from '../../api/prod'
import { getRelatedProdRecommend } from '../../api/recommend'
import { addToCart } from '../../api/cart'
import { getUsableCoupons } from '../../api/coupon'
import config from '../../utils/config'
import { sanitizeRichText } from '../../utils/content'

export default {
	data() {
		return {
			prodId: null,
			currentIndex: 0,
			showSku: false,
			skuAction: 'cart', // cart or buy
			qty: 1,
			images: [],
			product: null,
			specs: [],
			relatedProducts: []
		}
	},
	onLoad(options) {
		this.prodId = options.id
		if (this.prodId) {
			this.loadProduct()
		}
		if (options.action === 'cart') {
			this.skuAction = 'cart'
			this.showSku = true
		}
	},
	methods: {
		async loadProduct() {
			try {
				const res = await getProdDetail(this.prodId)
				this.product = {
					...res,
					detail: sanitizeRichText(res.detail || '')
				}
				// 图片路径处理
				const rawImages = res.pics ? res.pics.split(',').filter(p => p) : (res.pic ? [res.pic] : [])
				this.images = rawImages.map(img => config.getImageUrl(img))
				this.specs = [
					{ label: '描述', value: res.description || '暂无描述', highlight: false }
				]
				this.trackView()
				this.loadRelatedProducts()
			} catch (e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		trackView() {
			if (!uni.getStorageSync('token')) return
			trackDetailView({
				itemType: 2,
				itemId: this.prodId,
				sourcePage: 'prod_detail',
				sourceSection: 'detail_main'
			}).catch(() => {})
		},
		async loadRelatedProducts() {
			try {
				const list = await getRelatedProdRecommend(this.prodId, { limit: 4 })
				this.relatedProducts = (list || []).map(item => ({
					...item,
					image: config.getImageUrl(item.pic)
				}))
			} catch (e) {
				this.relatedProducts = []
			}
		},
		openRelated(item) {
			if (uni.getStorageSync('token')) {
				trackRecommendClick({
					itemType: 2,
					itemId: item.id,
					sourcePage: 'prod_detail',
					sourceSection: 'detail_related',
					sourceItemType: 2,
					sourceItemId: this.prodId
				}).catch(() => {})
			}
			uni.redirectTo({ url: `/pages/mall/product-detail?id=${item.id}` })
		},
		goBack() { uni.navigateBack(); },
		onSwiperChange(e) { this.currentIndex = e.detail.current; },
		openSku(action) {
			this.skuAction = action
			this.qty = 1
			this.showSku = true
		},
		async confirmSku() {
			if (!this.product) return
			if (this.skuAction === 'cart') {
				try {
					await addToCart({
						prodId: this.product.id,
						qty: this.qty
					})
					uni.showToast({ title: '已加入购物车', icon: 'success' })
					this.showSku = false
				} catch (e) {
					uni.showToast({ title: '添加失败', icon: 'none' })
				}
			} else {
				// 立即购买 - 跳转到确认页
				this.showSku = false
				const params = `prodId=${this.product.id}&qty=${this.qty}`
				uni.navigateTo({ url: `/pages/order/confirm-product?${params}` })
			}
		}
	},
	computed: {
		currentPrice() {
			return this.product?.price || 0
		},
		currentOriginalPrice() {
			return this.product?.originalPrice || 0
		},
		confirmBtnText() {
			return this.skuAction === 'cart' ? '加入购物车' : '立即购买'
		}
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }

.related-section { padding: 0 24rpx; }
.related-header { margin: 24rpx 0 16rpx; }
.related-title { display: block; font-size: 34rpx; font-weight: 900; color: #2c2f30; }
.related-subtitle { display: block; margin-top: 6rpx; font-size: 22rpx; color: #757778; }
.related-scroll { white-space: nowrap; }
.related-row { display: flex; gap: 16rpx; }
.related-card {
	width: 260rpx; flex-shrink: 0; background: #fff;
	border-radius: 24rpx; overflow: hidden;
	box-shadow: 0 10rpx 26rpx rgba(0,0,0,0.05);
}
.related-img { width: 100%; height: 200rpx; background: #eff1f2; }
.related-info { padding: 16rpx; display: flex; flex-direction: column; gap: 6rpx; }
.related-reason { font-size: 20rpx; color: #9c3f00; line-height: 1.4; }
.related-name { font-size: 24rpx; font-weight: 700; color: #2c2f30; line-height: 1.4; }
.related-sales { font-size: 20rpx; color: #757778; }
.related-price { font-size: 32rpx; }

.nav-bar {
	position: fixed; top: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: center; justify-content: space-between;
	padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top); background: #f5f6f7;
}
.nav-left { display: flex; align-items: center; gap: 12rpx; }
.nav-back { font-size: 36rpx; color: #9c3f00; }
.nav-title-text { font-size: 34rpx; font-weight: 700; color: #9c3f00; }
.nav-right { display: flex; gap: 24rpx; }
.nav-icon { font-size: 32rpx; color: #2c2f30; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

/* 轮播 */
.swiper-section { position: relative; }
.product-swiper { height: 600rpx; background: #eff1f2; }
.swiper-img { width: 100%; height: 100%; }
.swiper-dots {
	position: absolute; bottom: 40rpx; left: 50%;
	transform: translateX(-50%); display: flex; gap: 8rpx;
}
.dot {
	width: 12rpx; height: 8rpx; border-radius: 999rpx;
	background: rgba(44,47,48,0.2);
	&.active { width: 40rpx; background: #ff7a2f; }
}
.swiper-counter {
	position: absolute; top: 20rpx; right: 20rpx;
	background: rgba(255,255,255,0.8); padding: 6rpx 16rpx;
	border-radius: 24rpx;
}
.counter-text { font-size: 22rpx; font-weight: 700; color: #9c3f00; }

/* 价格卡 */
.price-card {
	margin: -40rpx 24rpx 0; position: relative; z-index: 10;
	background: #fff; border-radius: 24rpx; padding: 32rpx;
	box-shadow: 0 12rpx 24rpx rgba(44,47,48,0.06);
}
.price-row { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12rpx; }
.price-main { display: flex; align-items: baseline; }
.price-symbol { color: #9c3f00; font-weight: 700; font-size: 32rpx; }
.price-num { color: #9c3f00; font-weight: 900; font-size: 60rpx; letter-spacing: -3rpx; }
.price-original { color: #abadae; font-size: 24rpx; text-decoration: line-through; margin-left: 12rpx; opacity: 0.6; }
.new-tag { background: rgba(156,63,0,0.1); padding: 6rpx 14rpx; border-radius: 12rpx; }
.new-tag-text { font-size: 18rpx; color: #9c3f00; font-weight: 700; letter-spacing: 2rpx; }
.product-title { font-size: 34rpx; font-weight: 900; color: #2c2f30; margin-bottom: 8rpx; display: block; }
.product-desc { font-size: 26rpx; color: #595c5d; line-height: 1.6; display: block; margin-bottom: 16rpx; }
.rating-row {
	display: flex; gap: 32rpx; padding-top: 16rpx;
	border-top: 2rpx solid rgba(171,173,174,0.1);
}
.rating-item { display: flex; align-items: center; gap: 8rpx; }
.star-icon, .fire-icon { font-size: 28rpx; }
.rating-val { font-size: 26rpx; font-weight: 700; }
.rating-label { font-size: 22rpx; color: #595c5d; }

/* 参数 */
.specs-card {
	margin: 20rpx 24rpx; background: #eff1f2; border-radius: 24rpx; padding: 28rpx;
}
.specs-title { font-size: 20rpx; font-weight: 700; color: #595c5d; letter-spacing: 4rpx; display: block; margin-bottom: 16rpx; }
.spec-item {
	display: flex; justify-content: space-between; align-items: center;
	padding: 16rpx 0; font-size: 26rpx;
	border-bottom: 2rpx solid rgba(171,173,174,0.1);
	&:last-child { border-bottom: none; }
}
.spec-label { color: #595c5d; }
.spec-value { font-weight: 700; color: #2c2f30; &.highlight { color: #9c3f00; } }

/* 详情 */
.detail-section { padding: 0 24rpx; margin-top: 24rpx; }
.detail-header { margin-bottom: 20rpx; }
.detail-title { font-size: 36rpx; font-weight: 900; }
.detail-line { width: 80rpx; height: 6rpx; background: #9c3f00; margin-top: 12rpx; border-radius: 999rpx; }
.detail-content {
	background: #fff; border-radius: 24rpx; padding: 24rpx;
}
.detail-text {
	font-size: 26rpx; color: #595c5d; line-height: 1.8;
}
.rich-text {
	font-size: 26rpx; color: #2c2f30; line-height: 1.8;
}

/* 底部操作栏 */
.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: center; padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255,255,255,0.9); backdrop-filter: blur(20px);
	box-shadow: 0 -12rpx 24rpx rgba(44,47,48,0.06);
	gap: 16rpx;
}
.bar-icons { display: flex; gap: 24rpx; margin-right: 12rpx; }
.bar-icon-item { display: flex; flex-direction: column; align-items: center; gap: 2rpx; }
.bar-icon { font-size: 36rpx; opacity: 0.6; }
.bar-icon-label { font-size: 18rpx; color: rgba(44,47,48,0.6); }
.bar-btn-outline {
	flex: 1; padding: 20rpx 0; border: 3rpx solid rgba(156,63,0,0.2);
	border-radius: 24rpx; text-align: center;
}
.bar-btn-outline-text { font-size: 26rpx; color: #9c3f00; font-weight: 700; }
.bar-btn-primary {
	flex: 1; padding: 22rpx 0; border-radius: 24rpx; text-align: center;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	box-shadow: 0 8rpx 20rpx rgba(156,63,0,0.2);
}
.bar-btn-primary-text { font-size: 26rpx; color: #fff; font-weight: 900; letter-spacing: 2rpx; }

/* SKU弹窗 */
.sku-mask {
	position: fixed; inset: 0; z-index: 200;
	background: rgba(0,0,0,0.4); display: flex;
	align-items: flex-end; justify-content: center;
}
.sku-popup {
	width: 100%; background: #fff; border-radius: 32rpx 32rpx 0 0;
	padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
	max-height: 85vh; overflow-y: auto; position: relative;
}
.sku-close { position: absolute; top: 24rpx; right: 24rpx; }
.sku-close-icon { font-size: 32rpx; color: #595c5d; }
.sku-header { display: flex; gap: 20rpx; margin-bottom: 32rpx; }
.sku-img { width: 160rpx; height: 160rpx; border-radius: 16rpx; }
.sku-price-info { flex: 1; }
.sku-price-row { display: flex; align-items: baseline; }
.sku-price-symbol { font-size: 28rpx; color: #9c3f00; font-weight: 700; }
.sku-price-val { font-size: 52rpx; color: #9c3f00; font-weight: 900; letter-spacing: -3rpx; }
.sku-stock { font-size: 24rpx; color: #595c5d; display: block; margin-top: 4rpx; }
.sku-selected { font-size: 22rpx; color: #595c5d; display: block; margin-top: 4rpx; }
.sku-section { margin-bottom: 28rpx; }
.sku-label { font-size: 28rpx; font-weight: 700; display: block; margin-bottom: 16rpx; }
.sku-label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.sku-guide { font-size: 24rpx; color: #9c3f00; text-decoration: underline; }
.sku-options { display: flex; gap: 16rpx; }
.sku-option {
	padding: 12rpx 24rpx; border: 2rpx solid #e0e3e4;
	border-radius: 16rpx; display: flex; align-items: center; gap: 8rpx;
	&.active { border-color: #ff7a2f; background: rgba(255,122,47,0.05); }
}
.sku-opt-text { font-size: 26rpx; }
.sku-sizes { display: flex; flex-wrap: wrap; gap: 16rpx; }
.size-btn {
	width: 120rpx; height: 80rpx; border: 2rpx solid #e0e3e4;
	border-radius: 16rpx; display: flex; align-items: center;
	justify-content: center;
	&.active { background: #ff7a2f; border-color: #ff7a2f; }
}
.size-text { font-size: 28rpx; font-weight: 700; color: #2c2f30; &.active { color: #fff; } }
.qty-row { display: flex; align-items: center; gap: 0; }
.qty-btn {
	width: 72rpx; height: 72rpx; background: #eff1f2;
	display: flex; align-items: center; justify-content: center;
	border-radius: 12rpx;
}
.qty-btn-text { font-size: 32rpx; font-weight: 700; color: #2c2f30; }
.qty-num { width: 80rpx; text-align: center; font-size: 30rpx; font-weight: 700; }
.sku-confirm {
	margin-top: 16rpx; padding: 24rpx; border-radius: 24rpx;
	text-align: center;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
}
.sku-confirm-text { color: #fff; font-size: 30rpx; font-weight: 900; }
.sku-footer-note { display: block; text-align: center; font-size: 20rpx; color: #abadae; margin-top: 16rpx; letter-spacing: 2rpx; }
</style>
