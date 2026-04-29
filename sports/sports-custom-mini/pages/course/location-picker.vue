<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">选择地点</text>
			<view style="width: 40rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view class="search-card">
				<text class="search-label">当前筛选</text>
				<text class="search-value">{{ selectedLocation }}</text>
			</view>

			<view class="location-list">
				<view class="location-item" :class="{ active: selectedLocation === '全部地点' }" @tap="chooseLocation('全部地点')">
					<view class="location-cover all-cover">
						<text class="all-cover-text">ALL</text>
					</view>
					<view class="location-main">
						<text class="location-name">全部地点</text>
						<text class="location-desc">查看所有团课地点</text>
					</view>
					<text class="location-check" v-if="selectedLocation === '全部地点'">✓</text>
				</view>

				<view
					class="location-item rich-item"
					:class="{ active: selectedLocation === loc.name }"
					v-for="loc in locations"
					:key="loc.id || loc.name"
					@tap="chooseLocation(loc.name)"
				>
					<image class="location-cover" :src="getImageUrl(loc.coverImage) || '/static/logo.png'" mode="aspectFill" />
					<view class="location-main">
						<text class="location-name">{{ loc.name }}</text>
						<text class="location-address" v-if="loc.address">{{ loc.address }}</text>
						<text class="location-desc" v-if="loc.description">{{ loc.description }}</text>
						<text class="location-desc" v-else>当前已有 {{ loc.courseCount || 0 }} 门团课使用该地点</text>
					</view>
					<text class="location-check" v-if="selectedLocation === loc.name">✓</text>
				</view>
			</view>

			<view v-if="locations.length === 0" class="empty-wrap">
				<text class="empty-text">当前分类下暂无可选地点</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getCourseLocationOptions } from '../../api/course'
import config from '../../utils/config'

export default {
	data() {
		return {
			categoryId: null,
			selectedLocation: '全部地点',
			locations: []
		}
	},
	onLoad(options) {
		this.categoryId = options.categoryId ? Number(options.categoryId) : null
		this.selectedLocation = options.selectedLocation ? decodeURIComponent(options.selectedLocation) : '全部地点'
		this.loadLocations()
	},
	methods: {
		getImageUrl(url) {
			return config.getImageUrl(url)
		},
		async loadLocations() {
			try {
				this.locations = await getCourseLocationOptions(this.categoryId) || []
			} catch (e) {
				console.error('加载地点失败', e)
				this.locations = []
			}
		},
		chooseLocation(location) {
			const app = getApp()
			app._courseLocationSelection = location
			uni.navigateBack()
		},
		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }
.nav-bar {
	position: fixed; top: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: center; justify-content: space-between;
	padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top); background: #f5f6f7;
}
.nav-back { font-size: 36rpx; color: #2c2f30; }
.nav-title { font-size: 32rpx; font-weight: 900; color: #2c2f30; }
.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
	padding: 16rpx 24rpx 32rpx;
}
.search-card {
	background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx;
}
.search-label { display: block; font-size: 22rpx; color: #abadae; margin-bottom: 8rpx; }
.search-value { display: block; font-size: 30rpx; color: #2c2f30; font-weight: 700; }
.location-list { display: flex; flex-direction: column; gap: 12rpx; }
.location-item {
	background: #fff; border-radius: 20rpx; padding: 24rpx;
	display: flex; align-items: center; justify-content: space-between;
	border: 2rpx solid transparent;
	gap: 20rpx;
}
.location-item.active {
	border-color: rgba(156,63,0,0.2);
	background: rgba(156,63,0,0.03);
	box-shadow: 0 16rpx 28rpx rgba(156,63,0,0.08);
}
.location-main { flex: 1; }
.location-cover {
	width: 120rpx;
	height: 120rpx;
	border-radius: 18rpx;
	background: #f0f2f4;
	flex-shrink: 0;
}
.all-cover {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #2c2f30, #595c5d);
}
.all-cover-text {
	font-size: 30rpx;
	color: #fff;
	font-weight: 800;
	letter-spacing: 2rpx;
}
.location-name { display: block; font-size: 28rpx; color: #2c2f30; font-weight: 700; margin-bottom: 6rpx; }
.location-address { display: block; font-size: 22rpx; color: #595c5d; margin-bottom: 6rpx; }
.location-desc { display: block; font-size: 22rpx; color: #abadae; }
.location-check { font-size: 30rpx; color: #9c3f00; font-weight: 900; }
.empty-wrap { text-align: center; padding: 120rpx 24rpx; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
