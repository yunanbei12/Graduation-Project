<template>
	<view class="page">
		<view class="header">
			<view class="header-left">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
			</view>
		</view>
		<scroll-view scroll-y class="content-scroll">
			<!-- 搜索栏 -->
			<view class="search-section">
				<view class="search-bar">
					<text class="search-icon">🔍</text>
					<input 
						class="search-input" 
						placeholder="搜索训练装备或补剂..." 
						v-model="keyword"
						@confirm="onSearch"
						@input="onSearchInput"
					/>
					<text class="search-clear" v-if="keyword" @tap="clearSearch">✕</text>
				</view>
			</view>

			<!-- 分类 -->
			<view class="category-section">
				<view class="category-grid">
					<view class="cat-item" v-for="(cat, i) in categories" :key="cat.id" @tap="selectCategory(i, cat)">
						<view class="cat-icon-wrap" :class="{ active: activeCat === i }">
							<text class="cat-icon">{{ cat.icon || '📦' }}</text>
						</view>
						<text class="cat-label">{{ cat.name }}</text>
					</view>
				</view>
			</view>

			<!-- 促销Banner -->
			<view class="promo-banner">
				<image class="promo-bg" src="https://lh3.googleusercontent.com/aida-public/AB6AXuC5hFCJPU1lOdh6hp1iuBlVnmdPqSMhqMNG8j6C-b5VaP2en2vF3Vk3onmqslYe1J6PLyvDaYortXXSyEvgIAZQkPzU9lm9nXRzOg4K23WYmru3z-dnyNJjC6MEmJN5i13ArKRguUXMSOcq39WI060M8lFf3k5EbjDhdnvtti0GSCl_drRUGewZBEVBk3bujAEra-6QlYqyi0qSVdOP-FotTmmBGtbNGCpbJnW-oymADSAIyiWHHo8eNMb9nraRlGzbxuatMd85HgU" mode="aspectFill" />
				<view class="promo-overlay">
					<text class="promo-title">PERFORMANCE\nREDEFINED</text>
					<text class="promo-sub">夏季训练营装备限时 8.5 折</text>
				</view>
			</view>

			<!-- 商品网格 -->
			<view class="product-grid">
				<view class="product-card" v-for="(item, index) in products" :key="index" @tap="goDetail(item)">
					<view class="product-img-wrap">
						<image class="product-img" :src="item.image" mode="aspectFill" />
						<text class="product-badge" v-if="item.badge">{{ item.badge }}</text>
					</view>
					<view class="product-info">
						<text class="product-name">{{ item.name }}</text>
						<text class="product-sales">销量 {{ item.sales }}</text>
						<view class="product-bottom">
							<text class="product-price price-text">¥{{ item.price }}</text>
							<view class="cart-btn" @tap.stop="addToCart(item)">
								<text class="cart-btn-icon">🛒</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view style="height: 120rpx;"></view>
		</scroll-view>

		<!-- 浮动购物车 -->
		<view class="float-cart" @tap="goCart">
			<text class="float-cart-icon">🛒</text>
			<view class="cart-badge" v-if="cartCount > 0">
				<text class="cart-badge-text">{{ cartCount }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getProdList, getProdCategoryList } from '../../api/prod'
import { getCartCount } from '../../api/cart'
import config from '../../utils/config'

export default {
	data() {
		return {
			activeCat: 0,
			cartCount: 0,
			categoryId: null,
			categories: [],
			products: [],
			loading: false,
			keyword: '',
			searchTimer: null
		}
	},
	onLoad() {
		this.loadCategories()
		this.loadProducts()
		this.loadCartCount()
	},
	onShow() {
		this.loadCartCount()
	},
	methods: {
		async loadCategories() {
			try {
				const res = await getProdCategoryList()
				// 在分类列表前面添加"全部"选项
				this.categories = [
					{ id: null, name: '全部', icon: '📋' },
					...(res || [])
				]
			} catch (e) {
				console.error('加载分类失败', e)
			}
		},
		async loadProducts() {
			this.loading = true
			try {
				const params = { pageNum: 1, pageSize: 20 }
				if (this.categoryId) params.categoryId = this.categoryId
				if (this.keyword && this.keyword.trim()) {
					params.keyword = this.keyword.trim()
				}
				const res = await getProdList(params)
				this.products = (res.records || []).map(item => ({
					...item,
					image: config.getImageUrl(item.pic),
					badge: item.sales > 100 ? 'HOT' : ''
				}))
			} catch (e) {
				console.error('加载商品失败', e)
			} finally {
				this.loading = false
			}
		},
		async loadCartCount() {
			if (!uni.getStorageSync('token')) {
				this.cartCount = 0
				return
			}
			try {
				const res = await getCartCount()
				this.cartCount = res || 0
			} catch (e) {
				this.cartCount = 0
			}
		},
		selectCategory(index, cat) {
			this.activeCat = index
			this.categoryId = cat.id ? cat.id : null
			this.loadProducts()
		},
		onSearchInput() {
			// 防抖处理，输入停止300ms后自动搜索
			if (this.searchTimer) {
				clearTimeout(this.searchTimer)
			}
			this.searchTimer = setTimeout(() => {
				this.loadProducts()
			}, 300)
		},
		onSearch() {
			// 点击键盘确认按钮时搜索
			this.loadProducts()
		},
		clearSearch() {
			this.keyword = ''
			this.loadProducts()
		},
		goDetail(item) {
			uni.navigateTo({ url: '/pages/mall/product-detail?id=' + item.id })
		},
		addToCart(item) {
			uni.navigateTo({ url: '/pages/mall/product-detail?id=' + item.id + '&action=cart' })
		},
		goCart() {
			uni.navigateTo({ url: '/pages/cart/cart' })
		}
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }

.header {
	position: fixed; top: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: center; justify-content: space-between;
	padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top); background: #f5f6f7;
}
.header-left { display: flex; align-items: center; gap: 8rpx; }
.brand-logo { font-size: 36rpx; }
.brand-name { font-weight: 900; font-style: italic; font-size: 38rpx; color: #9c3f00; letter-spacing: -2rpx; }
.header-right { display: flex; gap: 24rpx; }
.header-icon { font-size: 36rpx; color: #595c5d; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.search-section { padding: 16rpx 24rpx; }
.search-bar {
	display: flex; align-items: center; background: #fff; border-radius: 24rpx;
	padding: 16rpx 24rpx; gap: 12rpx;
}
.search-icon { font-size: 28rpx; }
.search-input { flex: 1; font-size: 26rpx; }
.search-clear { font-size: 28rpx; color: #abadae; padding: 8rpx; }

.category-section { padding: 0 24rpx 16rpx; }
.category-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; }
.cat-item { display: flex; flex-direction: column; align-items: center; gap: 8rpx; }
.cat-icon-wrap {
	width: 96rpx; height: 96rpx; border-radius: 24rpx;
	display: flex; align-items: center; justify-content: center;
	background: #e0e3e4;
	&.active { background: #ff7a2f; }
}
.cat-icon { font-size: 40rpx; }
.cat-label { font-size: 24rpx; font-weight: 600; }

.promo-banner {
	margin: 0 24rpx 24rpx; height: 260rpx; border-radius: 24rpx;
	overflow: hidden; position: relative;
}
.promo-bg { width: 100%; height: 100%; position: absolute; }
.promo-overlay {
	position: absolute; inset: 0; padding: 32rpx;
	display: flex; flex-direction: column; justify-content: center;
	background: rgba(156,63,0,0.75);
}
.promo-title { color: #fff; font-weight: 900; font-size: 44rpx; font-style: italic; line-height: 1.2; }
.promo-sub { color: rgba(255,255,255,0.8); font-size: 24rpx; margin-top: 8rpx; }

.product-grid {
	display: grid; grid-template-columns: repeat(2, 1fr);
	gap: 16rpx; padding: 0 24rpx;
}
.product-card { background: #fff; border-radius: 24rpx; overflow: hidden; }
.product-img-wrap { position: relative; width: 100%; height: 320rpx; background: #eff1f2; }
.product-img { width: 100%; height: 100%; }
.product-badge {
	position: absolute; top: 12rpx; left: 12rpx;
	background: #9c3f00; color: #fff0ea; font-size: 18rpx;
	padding: 4rpx 14rpx; border-radius: 999rpx; font-weight: 700;
}
.product-info { padding: 16rpx; }
.product-name { font-size: 26rpx; font-weight: 700; color: #2c2f30; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-sales { font-size: 20rpx; color: #757778; display: block; margin-top: 4rpx; }
.product-bottom { display: flex; align-items: flex-end; justify-content: space-between; margin-top: 12rpx; }
.product-price { font-size: 34rpx; }
.cart-btn {
	width: 56rpx; height: 56rpx; border-radius: 16rpx;
	background: rgba(255,122,47,0.15); display: flex;
	align-items: center; justify-content: center;
}
.cart-btn-icon { font-size: 28rpx; }

.float-cart {
	position: fixed; right: 40rpx; bottom: 160rpx; z-index: 90;
	width: 100rpx; height: 100rpx; border-radius: 50%;
	background: #9c3f00; display: flex; align-items: center;
	justify-content: center;
	box-shadow: 0 12rpx 24rpx rgba(156,63,0,0.3);
}
.float-cart-icon { font-size: 40rpx; }
.cart-badge {
	position: absolute; top: -4rpx; right: -4rpx;
	width: 36rpx; height: 36rpx; border-radius: 50%;
	background: #ff7a2f; display: flex; align-items: center;
	justify-content: center; border: 4rpx solid #f5f6f7;
}
.cart-badge-text { font-size: 18rpx; color: #000; font-weight: 900; }
</style>
