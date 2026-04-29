<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">我的课包</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll">
			<view v-if="packages.length === 0" class="empty-wrap">
				<text class="empty-text">暂无课包</text>
			</view>
			<view class="pkg-card" v-for="pkg in packages" :key="pkg.id" @tap="goDetail(pkg)">
				<view class="pkg-header">
					<text class="pkg-name">{{ pkg.courseName || '课程包' }}</text>
					<view class="pkg-status" :class="getStatusClass(pkg)">
						<text class="pkg-status-text">{{ getStatusText(pkg) }}</text>
					</view>
				</view>
				<view class="pkg-progress">
					<view class="progress-info">
						<text class="progress-used">已上 {{ pkg.usedLessons }} 节</text>
						<text class="progress-total">/ 共 {{ pkg.totalLessons }} 节</text>
					</view>
					<view class="progress-bar">
						<view class="progress-fill" :style="{ width: (pkg.totalLessons ? pkg.usedLessons / pkg.totalLessons * 100 : 0) + '%' }"></view>
					</view>
				</view>
				<view class="pkg-footer">
					<text class="pkg-expire">有效期至 {{ formatDate(pkg.expireTime) }}</text>
					<text class="pkg-arrow">›</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getMyPackages } from '../../api/course'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return { packages: [] }
	},
	onShow() {
		if (!checkLogin()) return
		this.loadPackages()
	},
	methods: {
		getStatusClass(pkg) {
			// status: 0=已过期 1=正常 2=已退费 3=已完成
			if (pkg.status === 3 || (pkg.usedLessons >= pkg.totalLessons && pkg.totalLessons > 0)) {
				return 'finished'
			}
			if (pkg.status === 1) return 'active'
			return 'expired'
		},
		getStatusText(pkg) {
			if (pkg.status === 3 || (pkg.usedLessons >= pkg.totalLessons && pkg.totalLessons > 0)) {
				return '已完成'
			}
			if (pkg.status === 1) return '进行中'
			if (pkg.status === 0) return '已过期'
			if (pkg.status === 2) return '已退费'
			return '未知'
		},
		async loadPackages() {
			try {
				this.packages = await getMyPackages()
			} catch(e) {
				console.error('加载课包失败', e)
			}
		},
		formatDate(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
		},
		goDetail(pkg) {
			uni.navigateTo({ url: `/pages/profile/package-detail?id=${pkg.id}` })
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
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top)); margin-top: calc(88rpx + env(safe-area-inset-top)); padding: 16rpx 24rpx; }
.empty-wrap { text-align: center; padding-top: 200rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.pkg-card { background: #fff; border-radius: 24rpx; padding: 28rpx; margin-bottom: 16rpx; }
.pkg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.pkg-name { font-size: 32rpx; font-weight: 900; }
.pkg-status { padding: 6rpx 16rpx; border-radius: 8rpx; }
.pkg-status.active { background: rgba(255,122,47,0.1); }
.pkg-status.finished { background: rgba(103,194,58,0.1); }
.pkg-status.expired { background: #f5f6f7; }
.pkg-status-text { font-size: 22rpx; font-weight: 700; }
.pkg-status.active .pkg-status-text { color: #9c3f00; }
.pkg-status.finished .pkg-status-text { color: #67c23a; }
.pkg-status.expired .pkg-status-text { color: #999; }
.pkg-progress { margin-bottom: 16rpx; }
.progress-info { display: flex; align-items: baseline; gap: 4rpx; margin-bottom: 8rpx; }
.progress-used { font-size: 28rpx; font-weight: 900; color: #9c3f00; }
.progress-total { font-size: 24rpx; color: #999; }
.progress-bar { height: 12rpx; background: rgba(156,63,0,0.1); border-radius: 999rpx; overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #9c3f00, #ff7a2f); border-radius: 999rpx; }
.pkg-footer { display: flex; justify-content: space-between; align-items: center; }
.pkg-expire { font-size: 24rpx; color: #999; }
.pkg-arrow { font-size: 28rpx; color: #ccc; }
</style>
