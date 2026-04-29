<template>
	<view class="page">
		<!-- 顶部导航栏 -->
		<view class="header">
			<view class="header-left">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
			</view>
		
		</view>

		<!-- 滚动内容区 -->
		<scroll-view scroll-y class="content-scroll">
			<!-- Banner 轮播 -->
			<view class="banner-section">
				<swiper class="banner-swiper" autoplay circular indicator-dots indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#ff7a2f">
					<swiper-item v-for="banner in banners" :key="banner.id" @tap="onBannerTap(banner)">
						<view class="banner-card">
							<image class="banner-bg" :src="getImageUrl(banner.imageUrl)" mode="aspectFill" />
						</view>
					</swiper-item>
					<!-- 无数据时显示占位 -->
					<swiper-item v-if="banners.length === 0">
						<view class="banner-card banner-placeholder">
							<text class="banner-placeholder-text">KINETIC SPORTS</text>
						</view>
					</swiper-item>
				</swiper>
			</view>

			<!-- 四宫格快捷入口 -->
			<view class="quick-nav">
				<view class="quick-nav-grid">
					<view class="quick-item" @tap="goPrivateCourse">
						<view class="quick-icon-wrap qi-orange">
							<text class="quick-icon-text">📋</text>
						</view>
						<text class="quick-label">私教课包</text>
					</view>
					<view class="quick-item" @tap="goGroupCourse">
						<view class="quick-icon-wrap qi-orange-light">
							<text class="quick-icon-text">🏠</text>
						</view>
						<text class="quick-label">上门团课</text>
					</view>
					<view class="quick-item" @tap="navigateTo('/pages/ai/chat')">
						<view class="quick-icon-wrap qi-gray">
							<text class="quick-icon-text">💬</text>
						</view>
						<text class="quick-label">咨询顾问</text>
					</view>
					<view class="quick-item" @tap="navigateTo('/pages/coach/coach-list')">
						<view class="quick-icon-wrap qi-orange-light">
							<text class="quick-icon-text">⭐</text>
						</view>
						<text class="quick-label">热门教练</text>
					</view>
				</view>
			</view>

			<view class="section" v-if="recommendedCourses.length">
				<view class="section-header">
					<text class="section-title">为你推荐课程</text>
					<text class="section-subtitle">结合偏好、热度和可预约状态动态生成</text>
				</view>
				<scroll-view scroll-x class="recommend-scroll" show-scrollbar="false">
					<view class="recommend-row">
						<view class="recommend-card" v-for="item in recommendedCourses" :key="'course-' + item.id" @tap="openRecommendCourse(item)">
							<image class="recommend-img" :src="getImageUrl(item.pic)" mode="aspectFill" />
							<view class="recommend-body">
								<text class="recommend-tag">{{ item.courseType === 2 ? '团课' : '私教' }}</text>
								<text class="recommend-name">{{ item.name }}</text>
								<text class="recommend-reason">{{ item.reason }}</text>
								<text class="recommend-extra" v-if="item.nextScheduleText">{{ item.nextScheduleText }}</text>
								<text class="recommend-price price-text">¥{{ item.price }}</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</view>

			<!-- 周边团课 -->
			<view class="section" v-if="nearbyClasses.length">
				<view class="section-header">
					<text class="section-title">近期团课</text>
					<text class="section-subtitle">近 7 天可预约场次</text>
					<text class="section-more" @tap="navigateTo('/pages/course/course')">查看全部</text>
				</view>
				<view class="course-grid">
					<view class="course-card" v-for="(item, index) in nearbyClasses" :key="index" @tap="goGroupDetail(item)">
						<view class="course-img-wrap">
							<image class="course-img" :src="getImageUrl(item.image) || '/static/logo.png'" mode="aspectFill" />
							<view class="course-location-tag" v-if="item.location">
								<text class="location-tag-text">📍 {{ item.location }}</text>
							</view>
						</view>
						<view class="course-info">
							<text class="course-name">{{ item.name }}</text>
							<view class="course-meta">
								<text class="course-price price-text">¥{{ item.price }}</text>
								<text class="course-seats">余{{ item.seats }}席</text>
							</view>
							<view class="course-time-row">
								<text class="course-time-icon">🕐</text>
								<text class="course-time">{{ item.time }}</text>
							</view>
							<view class="course-book-btn" @tap.stop="goGroupDetail(item)">
								<text class="course-book-text">立即预约</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 底部间距 -->
			<view style="height: 40rpx;"></view>
		</scroll-view>
	</view>
</template>

<script>
import { trackRecommendClick } from '../../api/behavior'
import { getHomeRecommend } from '../../api/recommend'
import { getBannerList } from '../../api/banner'
import { getUpcomingSchedules } from '../../api/course'
import config from '../../utils/config'
import { normalizeMiniPagePath, normalizeSafeWebUrl } from '../../utils/content'
import { TAB_BAR_PAGES, canAccessPage } from '../../utils/demo-mode'

export default {
	data() {
		return {
			banners: [],
			recommendedCourses: [],
			nearbyClasses: [],
			coaches: [],
			communityPosts: []
		}
	},
	onLoad() {
		this.loadBanners()
		this.loadRecommendations()
		this.loadNearbyClasses()
	},
	onShow() {
		this.loadRecommendations()
	},
	methods: {
		getImageUrl(url) {
			return config.getImageUrl(url)
		},
		async loadBanners() {
			try {
				this.banners = await getBannerList(1)
			} catch (e) {
				this.banners = []
			}
		},
		async loadNearbyClasses() {
			try {
				const list = await getUpcomingSchedules(4)
				this.nearbyClasses = (list || []).map(item => {
					// 格式化日期显示
					const dateStr = item.scheduleDate ? String(item.scheduleDate) : ''
					let timeLabel = ''
					if (dateStr) {
						const today = new Date()
						const todayStr = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`
						const tomorrow = new Date(today)
						tomorrow.setDate(today.getDate() + 1)
						const tomorrowStr = `${tomorrow.getFullYear()}-${String(tomorrow.getMonth()+1).padStart(2,'0')}-${String(tomorrow.getDate()).padStart(2,'0')}`
						const prefix = dateStr === todayStr ? '今天' : dateStr === tomorrowStr ? '明天' : dateStr.slice(5).replace('-', '/')
						const startHour = item.startHour || ''
						timeLabel = startHour ? `${prefix} ${startHour}` : prefix
					}
					return {
						scheduleId: item.scheduleId,
						courseId: item.courseId,
						name: item.name,
						price: item.price,
						seats: item.remainSeats,
						time: timeLabel,
						location: item.location || '待确认',
						image: item.pic
					}
				})
			} catch(e) {
				this.nearbyClasses = []
			}
		},
		async loadRecommendations() {
			try {
				const data = await getHomeRecommend({ courseLimit: 4 })
				this.recommendedCourses = data.courseList || []
			} catch (e) {
				this.recommendedCourses = []
			}
		},
		navigateTo(url) {
			const pageUrl = normalizeMiniPagePath(url)
			if (!pageUrl) {
				uni.showToast({ title: '页面地址无效', icon: 'none' })
				return
			}
			if (!canAccessPage(pageUrl)) {
				uni.showToast({ title: '答辩模式下该页面已隐藏', icon: 'none' })
				return
			}
			if (TAB_BAR_PAGES.includes(pageUrl)) {
				uni.switchTab({ url: pageUrl })
			} else {
				uni.navigateTo({ url: pageUrl })
			}
		},
		goPrivateCourse() {
			getApp()._courseTabIntent = 'private'
			uni.switchTab({ url: '/pages/course/course' })
		},
		goGroupCourse() {
			getApp()._courseTabIntent = 'group'
			uni.switchTab({ url: '/pages/course/course' })
		},
		onBannerTap(banner) {
			if (!banner || !banner.linkUrl) return
			if (banner.linkType === 1) {
				const pageUrl = normalizeMiniPagePath(banner.linkUrl)
				if (!pageUrl) {
					uni.showToast({ title: '页面地址无效', icon: 'none' })
					return
				}
				this.navigateTo(pageUrl)
			} else if (banner.linkType === 3) {
				const webUrl = normalizeSafeWebUrl(banner.linkUrl)
				if (!webUrl) {
					uni.showToast({ title: '外链地址无效', icon: 'none' })
					return
				}
				uni.navigateTo({ url: `/pages/webview/webview?url=${encodeURIComponent(webUrl)}` })
			}
		},
		goGroupDetail(item) {
			if (item.courseId && item.scheduleId) {
				uni.navigateTo({ url: `/pages/course/course-detail-group?id=${item.courseId}&scheduleId=${item.scheduleId}` })
			} else {
				uni.navigateTo({ url: '/pages/course/course-detail-group' })
			}
		},
		openRecommendCourse(item) {
			this.trackRecommend('course', item, 'home_recommend')
			const url = item.courseType === 2
				? `/pages/course/course-detail-group?id=${item.id}`
				: `/pages/course/course-detail-private?id=${item.id}`
			uni.navigateTo({ url })
		},
		trackRecommend(bizType, item, sourceSection) {
			if (!uni.getStorageSync('token')) return
			trackRecommendClick({
				itemType: bizType === 'course' ? 1 : 2,
				itemId: item.id,
				sourcePage: 'index',
				sourceSection
			}).catch(() => {})
		}
	}
}
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background-color: #f5f6f7;
}

.header {
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
	background-color: #f5f6f7;
}

.header-left {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.brand-logo {
	font-size: 36rpx;
}

.brand-name {
	font-weight: 900;
	font-style: italic;
	font-size: 38rpx;
	color: #9c3f00;
	letter-spacing: -2rpx;
}

.header-right {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.search-mini {
	display: flex;
	align-items: center;
	background-color: #eff1f2;
	border-radius: 24rpx;
	padding: 12rpx 20rpx;
	gap: 8rpx;
}

.icon-text { font-size: 24rpx; }
.search-placeholder { font-size: 22rpx; color: #abadae; }

.avatar-wrap {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	overflow: hidden;
	border: 4rpx solid #ff7a2f;
}

.avatar-img { width: 100%; height: 100%; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.recommend-scroll {
	white-space: nowrap;
}

.recommend-row {
	display: flex;
	gap: 20rpx;
	padding: 0 24rpx;
}

.recommend-card {
	width: 300rpx;
	flex-shrink: 0;
	background: #fff;
	border-radius: 24rpx;
	overflow: hidden;
	box-shadow: 0 10rpx 28rpx rgba(0, 0, 0, 0.05);
}

.recommend-img {
	width: 100%;
	height: 190rpx;
	background: #eff1f2;
}

.recommend-body {
	padding: 18rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.recommend-tag {
	align-self: flex-start;
	padding: 6rpx 14rpx;
	border-radius: 999rpx;
	background: rgba(255,122,47,0.12);
	color: #9c3f00;
	font-size: 20rpx;
	font-weight: 700;
}

.recommend-name {
	font-size: 28rpx;
	font-weight: 700;
	color: #2c2f30;
	line-height: 1.35;
}

.recommend-reason {
	font-size: 22rpx;
	color: #9c3f00;
	line-height: 1.4;
}

.recommend-extra {
	font-size: 20rpx;
	color: #757778;
}

.recommend-price {
	font-size: 34rpx;
}

/* Banner */
.banner-section { padding: 0 24rpx; margin-top: 16rpx; }
.banner-swiper { height: 320rpx; border-radius: 24rpx; overflow: hidden; }
.banner-card { position: relative; width: 100%; height: 100%; }
.banner-bg { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
.banner-overlay {
	position: absolute;
	left: 0;
	bottom: 0;
	right: 0;
	padding: 24rpx 32rpx;
	background: linear-gradient(to top, rgba(0,0,0,0.55) 0%, transparent 100%);
}
.banner-title {
	font-size: 36rpx;
	color: #ffffff;
	font-weight: 900;
	line-height: 1.3;
}
.banner-placeholder {
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	display: flex;
	align-items: center;
	justify-content: center;
}
.banner-placeholder-text {
	font-size: 40rpx;
	font-weight: 900;
	color: rgba(255,255,255,0.6);
	letter-spacing: 4rpx;
}

/* 四宫格 */
.quick-nav { padding: 24rpx; }
.quick-nav-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16rpx;
}
.quick-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
}
.quick-icon-wrap {
	width: 96rpx;
	height: 96rpx;
	border-radius: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
.qi-orange { background-color: rgba(255,122,47,0.15); }
.qi-orange-light { background-color: rgba(255,122,47,0.08); }
.qi-gray { background-color: #eff1f2; }
.quick-icon-text { font-size: 40rpx; }
.quick-label { font-size: 24rpx; font-weight: 600; color: #2c2f30; }

/* Section */
.section { padding: 0 24rpx; margin-top: 32rpx; }
.section-header {
	display: flex;
	align-items: baseline;
	margin-bottom: 20rpx;
	flex-wrap: wrap;
	gap: 8rpx;
}
.section-title { font-size: 36rpx; font-weight: 900; color: #2c2f30; }
.section-subtitle { font-size: 22rpx; color: #595c5d; flex: 1; }
.section-more { font-size: 24rpx; color: #9c3f00; font-weight: 600; }

/* 课程卡片网格 */
.course-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16rpx;
}
.course-card {
	background: #fff;
	border-radius: 24rpx;
	overflow: hidden;
}
.course-img-wrap {
	position: relative;
	width: 100%;
	height: 220rpx;
}
.course-img { width: 100%; height: 100%; }
.course-location-tag {
	position: absolute;
	top: 12rpx;
	left: 12rpx;
	background: rgba(156,63,0,0.85);
	border-radius: 12rpx;
	padding: 4rpx 12rpx;
}
.location-tag-text { color: #fff; font-size: 18rpx; font-weight: 600; }
.course-info { padding: 16rpx; }
.course-name { font-size: 26rpx; font-weight: 700; color: #2c2f30; display: block; margin-bottom: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.course-meta { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8rpx; }
.course-price { font-size: 30rpx; }
.course-seats { font-size: 20rpx; color: #595c5d; }
.course-time-row { display: flex; align-items: center; gap: 4rpx; margin-bottom: 12rpx; }
.course-time-icon { font-size: 20rpx; }
.course-time { font-size: 20rpx; color: #757778; }
.course-book-btn {
	background: #ffffff;
	border: 2rpx solid rgba(156,63,0,0.2);
	border-radius: 12rpx;
	padding: 8rpx 0;
	text-align: center;
}
.course-book-text { font-size: 22rpx; color: #9c3f00; font-weight: 600; }


</style>
