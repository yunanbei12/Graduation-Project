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
					<view class="hero-tags">
						<view class="elite-tag"><text class="elite-tag-text">PRIVATE</text></view>
						<text class="hero-sport">{{ categoryName }}</text>
					</view>
					<text class="hero-title">{{ course.name }}</text>
					<view class="hero-price-row">
						<text class="hero-price">¥{{ course.price }}</text>
						<text class="hero-unit">/{{ course.lessonCount }}节课</text>
					</view>
				</view>
			</view>

			<!-- 亮点标签 -->
			<view class="highlights-row">
				<view class="highlight-card primary">
					<text class="hl-title">{{ course.lessonCount }}节课</text>
					<text class="hl-desc">有效期{{ course.validityDays || 90 }}天</text>
				</view>
				<view class="highlight-card accent" v-if="course.isDoorService === 1">
					<text class="hl-icon">🏠</text>
					<text class="hl-title-sm">上门授课</text>
				</view>
				<view class="highlight-card accent" v-else>
					<text class="hl-icon">⏱</text>
					<text class="hl-title-sm">时间灵活</text>
				</view>
			</view>

			<!-- 上门授课 -->
			<view class="info-card" v-if="course.isDoorService === 1">
				<text class="info-icon">🏠</text>
				<view class="info-content">
					<text class="info-title">上门授课</text>
					<text class="info-desc">全城覆盖，家门口的专业课</text>
				</view>
			</view>

			<!-- 详细介绍 -->
			<view class="detail-section">
				<view class="section-heading">
					<view class="heading-bar"></view>
					<text class="heading-text">详细介绍</text>
				</view>
				<view class="detail-content">
					<text class="detail-text">{{ course.description || '暂无介绍' }}</text>
					<view class="detail-features" v-if="parsedFeatures.length">
						<view class="df-item" v-for="(feat, fi) in parsedFeatures" :key="fi">
							<text class="df-title">{{ feat.icon }} {{ feat.text }}</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 教练信息 -->
			<view class="section-block" v-if="coachInfo">
				<view class="section-heading">
					<view class="heading-bar"></view>
					<text class="heading-text">教练信息</text>
				</view>
				<view class="coach-card">
					<image class="coach-avatar" :src="getImageUrl(coachInfo.avatar) || '/static/images/default-coach.png'" mode="aspectFill" />
					<view class="coach-info">
						<view class="coach-name-row">
							<text class="coach-name">{{ coachInfo.name }}</text>
						</view>
						<text class="coach-exp">{{ coachInfo.exp || '' }}</text>
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

		<!-- 底部操作栏 -->
		<view class="bottom-bar" v-if="course">
			<view class="bar-btn-secondary">
				<text class="bar-icon-text">💬</text>
				<text class="bar-btn-label">立即咨询</text>
				</view>
			<view class="bar-btn-primary" @tap="goConfirm">
				<text class="bar-btn-icon">⚡</text>
				<text class="bar-btn-text">立即购买</text>
			</view>
		</view>
	</view>
</template>

<script>
import { trackDetailView, trackRecommendClick } from '../../api/behavior'
import { getCourseDetail } from '../../api/course'
import { getRelatedCourseRecommend } from '../../api/recommend'
import config from '../../utils/config'

export default {
	data() {
		return {
			courseId: null,
			course: null,
			coachInfo: null,
			categoryName: '',
			relatedCourses: []
		}
	},
	computed: {
		parsedFeatures() {
			if (!this.course || !this.course.features) return []
			try {
				const f = this.course.features
				return typeof f === 'string' ? JSON.parse(f) : f
			} catch(e) {
				return []
			}
		}
	},
	onLoad(options) {
		this.courseId = options.id
		this.loadDetail()
	},
	methods: {
		getImageUrl: config.getImageUrl,
		async loadDetail() {
			try {
				const data = await getCourseDetail(this.courseId)
				this.course = data
				this.trackView()
				this.loadRelatedCourses()
			} catch(e) {
				console.error('加载课程详情失败', e)
				uni.showToast({ title: '加载失败', icon: 'none' })
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
					sourcePage: 'course_detail_private',
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
				sourcePage: 'course_detail_private',
				sourceSection: 'detail_main'
			}).catch(() => {})
		},
		goBack() { uni.navigateBack(); },
		goConfirm() {
			uni.navigateTo({ url: `/pages/order/confirm-private?courseId=${this.courseId}` });
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
.nav-icon { font-size: 32rpx; color: #2c2f30; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.hero-section { position: relative; }
.hero-img { width: 100%; display: block; }
.hero-overlay {
	position: absolute; bottom: 0; left: 0; right: 0; padding: 32rpx;
	background: linear-gradient(transparent, rgba(0,0,0,0.7));
}
.hero-tags { display: flex; align-items: center; gap: 12rpx; margin-bottom: 8rpx; }
.elite-tag { background: #ff7a2f; padding: 4rpx 14rpx; border-radius: 8rpx; }
.elite-tag-text { font-size: 18rpx; color: #fff; font-weight: 700; letter-spacing: 2rpx; }
.hero-sport { font-size: 22rpx; color: rgba(255,255,255,0.8); }
.hero-title { font-size: 40rpx; font-weight: 900; color: #fff; margin-bottom: 8rpx; }
.hero-price-row { display: flex; align-items: baseline; }
.hero-price { font-size: 40rpx; font-weight: 900; color: #ff7a2f; }
.hero-unit { font-size: 22rpx; color: rgba(255,255,255,0.6); margin-left: 4rpx; }

.highlights-row { display: flex; gap: 12rpx; padding: 20rpx 24rpx; }
.highlight-card {
	border-radius: 20rpx; padding: 20rpx;
	&.primary { flex: 2; background: #fff; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04); }
	&.accent { flex: 1; background: #ff7a2f; display: flex; flex-direction: column; align-items: center; justify-content: center; }
}
.hl-title { font-size: 30rpx; font-weight: 900; color: #9c3f00; display: block; }
.hl-desc { font-size: 22rpx; color: #595c5d; margin-top: 4rpx; }
.hl-icon { font-size: 36rpx; margin-bottom: 4rpx; }
.hl-title-sm { font-size: 26rpx; font-weight: 700; color: #fff; }

.info-card {
	margin: 0 24rpx; background: #fff; border-radius: 20rpx;
	padding: 20rpx; display: flex; align-items: center; gap: 16rpx;
}
.info-icon { font-size: 32rpx; }
.info-title { font-size: 28rpx; font-weight: 700; display: block; }
.info-desc { font-size: 22rpx; color: #595c5d; }

.detail-section, .section-block { padding: 24rpx; }
.section-heading { display: flex; align-items: center; gap: 12rpx; margin-bottom: 20rpx; }
.heading-bar { width: 8rpx; height: 32rpx; background: #9c3f00; border-radius: 999rpx; }
.heading-text { font-size: 32rpx; font-weight: 900; flex: 1; }
.see-all { font-size: 24rpx; color: #9c3f00; font-weight: 600; }

.detail-content { background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx; }
.detail-text { font-size: 26rpx; color: #2c2f30; line-height: 1.7; }
.bold-text { font-weight: 700; color: #9c3f00; }
.detail-features { display: flex; gap: 24rpx; margin-top: 20rpx; }
.df-item { flex: 1; }
.df-title { font-size: 28rpx; font-weight: 900; display: block; }
.df-desc { font-size: 22rpx; color: #595c5d; }
.training-banner { border-radius: 20rpx; overflow: hidden; position: relative; height: 280rpx; }
.training-img { width: 100%; height: 100%; }
.training-label {
	position: absolute; bottom: 20rpx; left: 20rpx;
	font-size: 36rpx; font-weight: 900; color: #fff;
	letter-spacing: 2rpx; line-height: 1.2;
}

.coach-card {
	background: #fff; border-radius: 20rpx; padding: 24rpx;
	display: flex; gap: 20rpx; align-items: center;
}
.coach-avatar { width: 120rpx; height: 120rpx; border-radius: 16rpx; }
.coach-info { flex: 1; }
.coach-name-row { display: flex; align-items: baseline; gap: 8rpx; margin-bottom: 4rpx; }
.coach-name { font-size: 30rpx; font-weight: 900; }
.coach-en { font-size: 26rpx; color: #9c3f00; font-weight: 700; font-style: italic; }
.coach-exp { font-size: 22rpx; color: #595c5d; display: block; margin-bottom: 8rpx; }
.coach-tags { display: flex; gap: 8rpx; }
.coach-tag {
	font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 8rpx;
	&.orange { background: rgba(255,122,47,0.1); color: #9c3f00; }
	&.gray { background: #eff1f2; color: #595c5d; }
}

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

.loading-wrap { display: flex; align-items: center; justify-content: center; height: 60vh; }
.loading-text { font-size: 28rpx; color: #999; }

.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0; z-index: 99;
	display: flex; gap: 16rpx; padding: 16rpx 24rpx;
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255,255,255,0.95); backdrop-filter: blur(20px);
	box-shadow: 0 -8rpx 24rpx rgba(0,0,0,0.06);
}
.bar-btn-secondary {
	flex: 1; padding: 20rpx; border-radius: 24rpx;
	border: 2rpx solid #e0e3e4; display: flex;
	align-items: center; justify-content: center; gap: 8rpx;
}
.bar-icon-text { font-size: 28rpx; }
.bar-btn-label { font-size: 26rpx; font-weight: 700; }
.bar-btn-primary {
	flex: 1.5; padding: 20rpx; border-radius: 24rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	display: flex; align-items: center; justify-content: center; gap: 8rpx;
}
.bar-btn-icon { font-size: 28rpx; }
.bar-btn-text { font-size: 28rpx; color: #fff; font-weight: 900; }
</style>
