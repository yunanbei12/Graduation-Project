<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">课包详情</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll" v-if="pkg">
			<!-- 课包信息 -->
			<view class="info-card">
				<text class="info-name">{{ pkg.courseName || '课程包' }}</text>
				<view class="info-progress">
					<text class="info-used">{{ pkg.usedLessons }}</text>
					<text class="info-sep">/</text>
					<text class="info-total">{{ pkg.totalLessons }} 节</text>
				</view>
				<view class="progress-bar">
					<view class="progress-fill" :style="{ width: (pkg.totalLessons ? pkg.usedLessons / pkg.totalLessons * 100 : 0) + '%' }"></view>
				</view>
				<text class="info-expire">有效期至 {{ formatDate(pkg.expireTime) }}</text>
			</view>

			<!-- 上课记录 -->
			<view class="section-heading">
				<view class="heading-bar"></view>
				<text class="heading-text">上课记录</text>
			</view>
			<view v-if="checkins.length === 0" class="empty-wrap">
				<text class="empty-text">暂无上课记录</text>
			</view>
			<view class="checkin-card" v-for="ck in checkins" :key="ck.id">
				<view class="ck-header">
					<text class="ck-time">{{ formatDateTime(ck.checkinTime) }}</text>
					<view class="ck-status" :class="ck.status === 1 ? 'present' : 'absent'">
						<text class="ck-status-text">{{ ck.status === 1 ? '出勤' : '缺勤' }}</text>
					</view>
				</view>
				<text class="ck-coach" v-if="ck.coachName">👤 {{ ck.coachName }}</text>
				<text class="ck-location" v-if="ck.location">📍 {{ ck.location }}</text>
				<text class="ck-remark" v-if="ck.remark">{{ ck.remark }}</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getPackageDetail, getPackageCheckins } from '../../api/course'

export default {
	data() {
		return { pkgId: null, pkg: null, checkins: [] }
	},
	onLoad(options) {
		this.pkgId = options.id
		this.loadDetail()
		this.loadCheckins()
	},
	methods: {
		async loadDetail() {
			try { this.pkg = await getPackageDetail(this.pkgId) } catch(e) { console.error(e) }
		},
		async loadCheckins() {
			try { this.checkins = await getPackageCheckins(this.pkgId) } catch(e) { console.error(e) }
		},
		formatDate(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
		},
		formatDateTime(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getMonth()+1}月${d.getDate()}日 ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
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
.info-card { background: #fff; border-radius: 24rpx; padding: 32rpx; margin-bottom: 24rpx; }
.info-name { font-size: 36rpx; font-weight: 900; display: block; margin-bottom: 16rpx; }
.info-progress { display: flex; align-items: baseline; gap: 4rpx; margin-bottom: 12rpx; }
.info-used { font-size: 56rpx; font-weight: 900; color: #9c3f00; }
.info-sep { font-size: 32rpx; color: #ccc; }
.info-total { font-size: 28rpx; color: #999; }
.progress-bar { height: 16rpx; background: rgba(156,63,0,0.1); border-radius: 999rpx; overflow: hidden; margin-bottom: 12rpx; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #9c3f00, #ff7a2f); border-radius: 999rpx; }
.info-expire { font-size: 24rpx; color: #999; display: block; }
.section-heading { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.heading-bar { width: 8rpx; height: 32rpx; background: #9c3f00; border-radius: 999rpx; }
.heading-text { font-size: 32rpx; font-weight: 900; }
.empty-wrap { text-align: center; padding: 60rpx 0; }
.empty-text { font-size: 26rpx; color: #999; }
.checkin-card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 12rpx; }
.ck-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
.ck-time { font-size: 28rpx; font-weight: 700; }
.ck-status { padding: 4rpx 14rpx; border-radius: 8rpx; }
.ck-status.present { background: rgba(255,122,47,0.1); }
.ck-status.absent { background: #f5f6f7; }
.ck-status-text { font-size: 20rpx; font-weight: 700; }
.ck-status.present .ck-status-text { color: #9c3f00; }
.ck-status.absent .ck-status-text { color: #999; }
.ck-coach { font-size: 24rpx; color: #9c3f00; display: block; margin-bottom: 4rpx; font-weight: 600; }
.ck-location { font-size: 24rpx; color: #666; display: block; margin-bottom: 4rpx; }
.ck-remark { font-size: 22rpx; color: #999; }
</style>
