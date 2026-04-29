<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">课程详情</text>
			<text class="nav-icon">↗</text>
		</view>
		<scroll-view scroll-y class="content-scroll" v-if="course">
			<!-- 课程大图 -->
			<view class="hero-section">
				<image class="hero-img" :src="getImageUrl(course.pic) || '/static/images/default-course.png'" mode="aspectFill" />
				<view class="hero-overlay">
					<view class="hero-tag"><text class="hero-tag-text">GROUP CLASS</text></view>
					<text class="hero-title">{{ course.name }}</text>
				</view>
			</view>

			<!-- 价格与名额 -->
			<view class="price-card">
				<view class="price-row">
					<view class="price-left">
						<text class="price-num price-text">¥{{ course.price }}</text>
						<text class="price-unit">/人</text>
					</view>
					<text class="sales-text">已售 {{ course.sales || 0 }}</text>
				</view>
			</view>

			<!-- 选择排课 -->
			<view class="section-block">
				<view class="section-heading">
					<view class="heading-bar"></view>
					<text class="heading-text">选择上课时间</text>
				</view>
				<view v-if="schedules.length === 0" class="empty-schedule">
					<text class="empty-text">暂无可预约的排课</text>
				</view>
				<view v-else class="schedule-list">
					<view
						v-for="(sch, si) in schedules" :key="sch.id"
						class="schedule-card" :class="{ active: isScheduleSelected(sch.id) }"
						@tap="toggleSchedule(sch)"
					>
						<view class="sch-top">
							<text class="sch-date">📅 {{ formatScheduleDate(sch.scheduleDate || sch.startTime) }}</text>
							<view class="sch-select-indicator" :class="{ active: isScheduleSelected(sch.id) }">
								<text class="sch-select-text">{{ isScheduleSelected(sch.id) ? '已选' : '选择' }}</text>
							</view>
						</view>
						<text class="sch-time">{{ course.startHour || formatTime(sch.startTime) }} - {{ course.endHour || formatTime(sch.endTime) }}</text>
						<view class="sch-bottom">
							<text class="sch-location">📍 {{ course.location || sch.location || '待确认' }}</text>
							<text class="sch-seats">{{ sch.enrolledSeats }}/{{ sch.totalSeats }}人</text>
						</view>
						<view class="progress-bar" v-if="sch.totalSeats">
							<view class="progress-fill" :style="{ width: (sch.enrolledSeats / sch.totalSeats * 100) + '%' }"></view>
						</view>
					</view>
				</view>
				<text v-if="selectedCount > 0" class="schedule-picked-tip">已选择 {{ selectedCount }} 个未来场次</text>
			</view>

			<!-- 课程亮点 -->
			<view class="section-block">
				<view class="section-heading">
					<view class="heading-bar"></view>
					<text class="heading-text">课程介绍</text>
				</view>
				<view class="detail-card">
					<text class="detail-text">{{ course.description || '暂无介绍' }}</text>
				</view>
			</view>

			<!-- 成团人数 -->
			<view class="section-block" v-if="course.minGroupSize">
				<view class="info-item">
					<text class="info-icon">👥</text>
					<view class="info-content">
						<text class="info-label">成团人数</text>
						<text class="info-value">{{ course.minGroupSize }}人起开课</text>
					</view>
				</view>
			</view>

			<view class="section-block" v-if="relatedCourses.length">
				<view class="section-heading">
					<view class="heading-bar"></view>
					<text class="heading-text">相似推荐</text>
				</view>
				<scroll-view scroll-x class="related-scroll" show-scrollbar="false">
					<view class="related-row">
						<view class="related-card" v-for="item in relatedCourses" :key="item.id" @tap="openRelatedCourse(item)">
							<image class="related-img" :src="getImageUrl(item.pic) || '/static/logo.png'" mode="aspectFill" />
							<view class="related-info">
								<text class="related-type">{{ item.courseType === 2 ? '团课' : '私教' }}</text>
								<text class="related-name">{{ item.name }}</text>
								<text class="related-reason">{{ item.reason }}</text>
								<text class="related-extra" v-if="item.nextScheduleText">{{ item.nextScheduleText }}</text>
								<text class="related-price price-text">¥{{ item.price }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>

			<view style="height: 180rpx;"></view>
		</scroll-view>

		<!-- 加载中 -->
		<view class="loading-wrap" v-if="!course">
			<text class="loading-text">加载中...</text>
		</view>

		<view class="bottom-bar" v-if="course">
			<view class="bar-btn-secondary">
				<text class="bar-icon-text">💬</text>
				<text class="bar-btn-label">立即咨询</text>
			</view>
			<view class="bar-btn-primary" :class="{ disabled: selectedCount === 0 }" @tap="goConfirm">
				<text class="bar-btn-icon">⚡</text>
				<text class="bar-btn-text">{{ selectedCount > 0 ? `报名 ${selectedCount} 个场次` : '请选择时间' }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { trackDetailView, trackRecommendClick } from '../../api/behavior'
import { getCourseDetail, getScheduleList } from '../../api/course'
import { getRelatedCourseRecommend } from '../../api/recommend'
import config from '../../utils/config'

export default {
	data() {
		return {
			courseId: null,
			course: null,
			schedules: [],
			selectedScheduleIds: [],
			initialScheduleId: null,
			relatedCourses: []
		}
	},
	onLoad(options) {
		this.courseId = options.id
		this.initialScheduleId = options.scheduleId ? Number(options.scheduleId) : null
		this.loadDetail()
	},
	computed: {
		selectedCount() {
			return this.selectedScheduleIds.length
		}
	},
	methods: {
		getImageUrl: config.getImageUrl,
		async loadDetail() {
			try {
				const data = await getCourseDetail(this.courseId)
				this.course = data
				this.trackView()
				this.loadRelatedCourses()
				this.loadSchedules()
			} catch(e) {
				console.error('加载课程详情失败', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},
		async loadSchedules() {
			try {
				const res = await getScheduleList(this.courseId)
				this.schedules = res.records || []
				if (this.schedules.length > 0) {
					const presetId = this.initialScheduleId && this.schedules.some(item => item.id === this.initialScheduleId)
						? this.initialScheduleId
						: this.schedules[0].id
					this.selectedScheduleIds = presetId ? [presetId] : []
				}
			} catch(e) {
				console.error('加载排课失败', e)
			}
		},
		async loadRelatedCourses() {
			try {
				this.relatedCourses = await getRelatedCourseRecommend(this.courseId, { limit: 4 }) || []
			} catch (e) {
				this.relatedCourses = []
			}
		},
		openRelatedCourse(item) {
			if (uni.getStorageSync('token')) {
				trackRecommendClick({
					itemType: 1,
					itemId: item.id,
					sourcePage: 'course_detail_group',
					sourceSection: 'detail_related',
					sourceItemType: 1,
					sourceItemId: this.courseId
				}).catch(() => {})
			}
			const url = item.courseType === 2
				? `/pages/course/course-detail-group?id=${item.id}`
				: `/pages/course/course-detail-private?id=${item.id}`
			uni.redirectTo({ url })
		},
		trackView() {
			if (!uni.getStorageSync('token')) return
			trackDetailView({
				itemType: 1,
				itemId: this.courseId,
				sourcePage: 'course_detail_group',
				sourceSection: 'detail_main'
			}).catch(() => {})
		},
		isScheduleSelected(scheduleId) {
			return this.selectedScheduleIds.includes(scheduleId)
		},
		toggleSchedule(sch) {
			if (this.isScheduleSelected(sch.id)) {
				this.selectedScheduleIds = this.selectedScheduleIds.filter(id => id !== sch.id)
				return
			}
			this.selectedScheduleIds = [...this.selectedScheduleIds, sch.id]
		},
		formatScheduleDate(dt) {
			if (!dt) return ''
			// 支持 'YYYY-MM-DD' 和 'YYYY-MM-DDTHH:mm:ss' 格式
			const dateStr = typeof dt === 'string' ? dt.slice(0, 10) : ''
			if (!dateStr) return ''
			const parts = dateStr.split('-')
			if (parts.length < 3) return dateStr
			const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]))
			const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
			return `${parseInt(parts[1])}月${parseInt(parts[2])}日 ${weekDays[d.getDay()]}`
		},
		formatDate(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			const m = d.getMonth() + 1
			const day = d.getDate()
			const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
			return `${m}月${day}日 ${weekDays[d.getDay()]}`
		},
		formatTime(dt) {
			if (!dt) return ''
			const d = new Date(dt)
			return `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
		},
		goBack() { uni.navigateBack(); },
		goConfirm() {
			if (this.selectedScheduleIds.length === 0) {
				uni.showToast({ title: '请选择上课时间', icon: 'none' })
				return
			}
			uni.navigateTo({ url: `/pages/order/confirm-group?courseId=${this.courseId}&scheduleIds=${this.selectedScheduleIds.join(',')}` })
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
.nav-title { font-size: 32rpx; font-weight: 700; }
.nav-icon { font-size: 32rpx; }
.content-scroll { height: calc(100vh - 88rpx - env(safe-area-inset-top)); margin-top: calc(88rpx + env(safe-area-inset-top)); }

.hero-section { position: relative; }
.hero-img { width: 100%; display: block; }
.hero-overlay { position: absolute; bottom: 0; left: 0; right: 0; padding: 24rpx; background: linear-gradient(transparent, rgba(0,0,0,0.6)); }
.hero-tag { background: #ff7a2f; padding: 6rpx 16rpx; border-radius: 8rpx; display: inline-block; margin-bottom: 8rpx; }
.hero-tag-text { font-size: 20rpx; color: #fff; font-weight: 700; }
.hero-title { font-size: 36rpx; font-weight: 900; color: #fff; }

.price-card { margin: -20rpx 24rpx 0; position: relative; z-index: 10; background: #fff; border-radius: 24rpx; padding: 24rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.price-row { display: flex; justify-content: space-between; align-items: flex-start; }
.price-left { display: flex; align-items: baseline; }
.price-num { font-size: 48rpx; }
.price-unit { font-size: 24rpx; color: #595c5d; margin-left: 4rpx; }
.sales-text { font-size: 24rpx; color: #9c3f00; font-weight: 700; }

.empty-schedule { background: #fff; border-radius: 20rpx; padding: 60rpx 24rpx; text-align: center; }
.empty-text { font-size: 26rpx; color: #999; }

.schedule-list { display: flex; flex-direction: column; gap: 12rpx; }
.schedule-card {
	background: #fff; border-radius: 20rpx; padding: 24rpx; border: 2rpx solid transparent;
	transition: all 0.2s;
	&.active { border-color: #ff7a2f; background: rgba(255,122,47,0.03); }
}
.sch-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.sch-date { font-size: 28rpx; font-weight: 700; color: #2c2f30; }
.sch-time { font-size: 26rpx; color: #9c3f00; font-weight: 600; display: block; margin-bottom: 12rpx; }
.sch-bottom { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.sch-location { font-size: 24rpx; color: #595c5d; }
.sch-seats { font-size: 24rpx; color: #9c3f00; font-weight: 700; }
.sch-select-indicator { padding: 6rpx 16rpx; border-radius: 999rpx; background: #eff1f2; }
.sch-select-indicator.active { background: rgba(156,63,0,0.12); }
.sch-select-text { font-size: 20rpx; color: #595c5d; font-weight: 700; }
.schedule-picked-tip { display: block; margin-top: 8rpx; font-size: 22rpx; color: #9c3f00; font-weight: 700; }

.loading-wrap { display: flex; align-items: center; justify-content: center; height: 60vh; }
.loading-text { font-size: 28rpx; color: #999; }

.bar-btn-primary.disabled { opacity: 0.5; }

.section-block { padding: 0 24rpx 16rpx; }
.section-heading { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.heading-bar { width: 8rpx; height: 32rpx; background: #9c3f00; border-radius: 999rpx; }
.heading-text { font-size: 32rpx; font-weight: 900; flex: 1; }

.detail-card { background: #fff; border-radius: 20rpx; padding: 24rpx; }
.detail-text { font-size: 26rpx; color: #2c2f30; line-height: 1.7; }

.info-item { background: #fff; border-radius: 20rpx; padding: 20rpx; display: flex; align-items: center; gap: 16rpx; }
.info-icon { font-size: 32rpx; }
.info-label { font-size: 22rpx; color: #9c3f00; display: block; }
.info-value { font-size: 28rpx; font-weight: 700; }
.info-content { flex: 1; }

.progress-bar { height: 10rpx; background: rgba(156,63,0,0.1); border-radius: 999rpx; overflow: hidden; margin-top: 8rpx; }
.progress-fill { height: 100%; background: linear-gradient(90deg, #9c3f00, #ff7a2f); border-radius: 999rpx; }

.related-scroll { white-space: nowrap; }
.related-row { display: flex; gap: 16rpx; }
.related-card {
	width: 280rpx; flex-shrink: 0; background: #fff;
	border-radius: 24rpx; overflow: hidden;
	box-shadow: 0 10rpx 26rpx rgba(0,0,0,0.05);
}
.related-img { width: 100%; height: 190rpx; background: #eff1f2; }
.related-info { padding: 16rpx; display: flex; flex-direction: column; gap: 8rpx; }
.related-type {
	align-self: flex-start; padding: 6rpx 14rpx; border-radius: 999rpx;
	background: rgba(255,122,47,0.12); color: #9c3f00; font-size: 20rpx; font-weight: 700;
}
.related-name { font-size: 26rpx; font-weight: 700; color: #2c2f30; line-height: 1.4; }
.related-reason { font-size: 20rpx; color: #9c3f00; line-height: 1.4; }
.related-extra { font-size: 20rpx; color: #757778; }
.related-price { font-size: 32rpx; }

.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0; z-index: 99;
	display: flex; gap: 16rpx; padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255,255,255,0.95); backdrop-filter: blur(20px);
	box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.06);
}
.bar-btn-secondary { flex: 1; padding: 20rpx; border-radius: 24rpx; border: 2rpx solid #e0e3e4; display: flex; align-items: center; justify-content: center; gap: 8rpx; }
.bar-icon-text { font-size: 28rpx; }
.bar-btn-label { font-size: 26rpx; font-weight: 700; }
.bar-btn-primary { flex: 1.5; padding: 20rpx; border-radius: 24rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f); display: flex; align-items: center; justify-content: center; gap: 8rpx; }
.bar-btn-icon { font-size: 28rpx; }
.bar-btn-text { font-size: 28rpx; color: #fff; font-weight: 900; }
</style>
