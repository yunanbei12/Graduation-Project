<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">上课记录</text>
			<view style="width: 40rpx;"></view>
		</view>
		<scroll-view scroll-y class="content-scroll">
			<view v-if="checkins.length === 0" class="empty-wrap">
				<text class="empty-text">暂无上课记录</text>
			</view>
			<view class="checkin-card" v-for="ck in checkins" :key="ck.id">
				<view class="ck-header">
					<view class="ck-type-badge" :class="ck.checkinType === 1 ? 'private' : 'group'">
						<text class="ck-type-text">{{ ck.checkinType === 1 ? '私教' : '团课' }}</text>
					</view>
					<view class="ck-status" :class="ck.status === 1 ? 'present' : 'absent'">
						<text class="ck-status-text">{{ ck.status === 1 ? '出勤' : '缺勤' }}</text>
					</view>
				</view>
				<text class="ck-course">{{ ck.courseName || '' }}</text>
				<view class="ck-info">
					<text class="ck-time">📅 {{ formatDateTime(ck.checkinTime) }}</text>
					<text class="ck-location" v-if="ck.location">📍 {{ ck.location }}</text>
				</view>
				<text class="ck-remark" v-if="ck.remark">{{ ck.remark }}</text>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getGroupCheckins } from '../../api/course'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return { checkins: [] }
	},
	onShow() {
		if (!checkLogin()) return
		this.loadCheckins()
	},
	methods: {
		async loadCheckins() {
			try {
				this.checkins = await getGroupCheckins()
			} catch(e) {
				this.checkins = []
			}
		},
		formatDateTime(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
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
.checkin-card { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 12rpx; }
.ck-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.ck-type-badge { padding: 4rpx 14rpx; border-radius: 8rpx; }
.ck-type-badge.private { background: rgba(255,122,47,0.1); }
.ck-type-badge.group { background: rgba(156,63,0,0.08); }
.ck-type-text { font-size: 20rpx; font-weight: 700; color: #9c3f00; }
.ck-status { padding: 4rpx 14rpx; border-radius: 8rpx; }
.ck-status.present { background: rgba(255,122,47,0.1); }
.ck-status.absent { background: #f5f6f7; }
.ck-status-text { font-size: 20rpx; font-weight: 700; }
.ck-status.present .ck-status-text { color: #9c3f00; }
.ck-status.absent .ck-status-text { color: #999; }
.ck-course { font-size: 30rpx; font-weight: 900; display: block; margin-bottom: 8rpx; }
.ck-info { display: flex; flex-direction: column; gap: 4rpx; margin-bottom: 4rpx; }
.ck-time { font-size: 24rpx; color: #666; }
.ck-location { font-size: 24rpx; color: #666; }
.ck-remark { font-size: 22rpx; color: #999; }
</style>
