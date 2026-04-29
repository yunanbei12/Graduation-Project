<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">网页内容</text>
			<view style="width: 40rpx;"></view>
		</view>
		<web-view v-if="webUrl" class="webview" :src="webUrl"></web-view>
		<view v-else class="empty-wrap">
			<text class="empty-text">链接无效</text>
		</view>
	</view>
</template>

<script>
import { normalizeSafeWebUrl } from '../../utils/content'

export default {
	data() {
		return {
			webUrl: ''
		}
	},
	onLoad(options) {
		const decodedUrl = options && options.url ? decodeURIComponent(options.url) : ''
		this.webUrl = normalizeSafeWebUrl(decodedUrl)
		if (!this.webUrl) {
			uni.showToast({ title: '链接无效', icon: 'none' })
		}
	},
	methods: {
		goBack() {
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
	background: #f5f6f7;
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

.webview {
	margin-top: calc(88rpx + env(safe-area-inset-top));
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
}

.empty-wrap {
	height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
}

.empty-text {
	font-size: 28rpx;
	color: #8c8f91;
}
</style>
