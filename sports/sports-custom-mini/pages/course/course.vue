<template>
	<view class="page">
		<!-- 顶部导航 -->
		<view class="header">
			<view class="header-left">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
			</view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- 私教课/团课 Tab -->
			<view class="tab-section">
				<view class="main-tabs">
					<view class="main-tab" :class="{ active: activeTab === 'private' }" @tap="activeTab = 'private'">
						<text class="main-tab-text" :class="{ active: activeTab === 'private' }">私教课</text>
					</view>
					<view class="main-tab" :class="{ active: activeTab === 'group' }" @tap="activeTab = 'group'">
						<text class="main-tab-text" :class="{ active: activeTab === 'group' }">团课</text>
					</view>
				</view>

				<!-- 分类标签 -->
				<scroll-view scroll-x class="category-scroll">
					<view class="category-list">
						<view class="category-tag" :class="{ active: activeCategory === cat }" v-for="cat in categories" :key="cat" @tap="onCategoryChange(cat)">
							<text class="category-tag-text" :class="{ active: activeCategory === cat }">{{ cat }}</text>
						</view>
					</view>
				</scroll-view>
			</view>

			<!-- 私教课列表 -->
			<view v-if="activeTab === 'private'" class="list-section">
				<view class="private-card" v-for="(item, index) in privatePackages" :key="index" @tap="goPrivateDetail(item)">
					<!-- 课程图片（上，宽度100%自适应） -->
					<image class="private-img" :src="getImageUrl(item.pic) || '/static/logo.png'" mode="widthFix" />
					<!-- 信息区（下） -->
					<view class="private-info">
						<text class="private-name">{{ item.name }}</text>
						<view class="private-tags" v-if="item.features && item.features.length">
							<view class="feature-tag" v-for="(tag, ti) in item.features" :key="ti">
								<text class="feature-tag-icon">{{ tag.icon }}</text>
								<text class="feature-tag-text">{{ tag.text }}</text>
							</view>
						</view>
						<view class="private-footer">
							<view class="private-meta">
								<view class="private-price-row">
									<text class="private-price price-text">¥{{ item.price }}</text>
								</view>
								<text class="private-unit">{{ item.lessonCount }}节课</text>
							</view>
							<view class="order-btn" @tap.stop="goPrivateDetail(item)">
								<text class="order-btn-text">立即购买</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 团课列表 -->
			<view v-if="activeTab === 'group'" class="list-section">
				<view class="group-card" v-for="(item, index) in groupClasses" :key="index" @tap="goGroupDetail(item)">
					<view class="group-header">
						<view class="group-time-tag" :class="{ today: item.isToday }">
							<text class="group-time-icon">📅</text>
							<text class="group-time-text">{{ item.time }}</text>
						</view>
						<view class="group-seats">
							<text class="group-seats-text">已报 </text>
							<text class="group-seats-num">{{ item.enrolled }}</text>
							<text class="group-seats-text">/{{ item.total }}人</text>
						</view>
					</view>
					<text class="group-name">{{ item.name }}</text>
					<view class="group-location">
						<text class="group-loc-icon">📍</text>
						<text class="group-loc-text">{{ item.location }}</text>
					</view>
					<view class="group-footer">
						<view class="group-price-row">
							<text class="group-price price-text">¥{{ item.price }}</text>
							<text class="group-unit">/人</text>
						</view>
						<view class="enroll-btn" @tap.stop="goGroupDetail(item)">
							<text class="enroll-btn-text">立即报名</text>
						</view>
					</view>
				</view>
			</view>

			<view style="height: 40rpx;"></view>
		</scroll-view>
	</view>
</template>

<script>
import { getCourseList, getCourseCategoryList, getScheduleList } from '../../api/course'
import config from '../../utils/config'

export default {
	data() {
		return {
			activeTab: 'private',
			activeCategory: '全部',
			categoryId: null,
			categoryMap: {},
			categories: ['全部'],
			privatePackages: [],
			groupClasses: [],
			loading: false,
			pageNum: 1,
			pageSize: 20,
			total: 0
		}
	},
	watch: {
		activeTab() {
			this.pageNum = 1
			if (!this._tabChanging) {
				this.loadCourses()
			}
		}
	},
	onLoad() {
		// 读取来自其他页面的 tab 跳转意图（首次加载时设置，避免切换动画）
		const app = getApp()
		if (app._courseTabIntent) {
			this.activeTab = app._courseTabIntent
			app._courseTabIntent = null
		}
		this.loadCategories()
		this.loadCourses()
	},
	onShow() {
		this.loadCategories()
		// 处理来自其他页面的 tab 跳转意图
		const app = getApp()
		if (app._courseTabIntent) {
			const intent = app._courseTabIntent
			app._courseTabIntent = null
			if (this.activeTab !== intent) {
				// 先清空数据，再切换 tab，避免看到切换动画
				this._tabChanging = true
				this.privatePackages = []
				this.groupClasses = []
				this.activeTab = intent
				this._tabChanging = false
				this.pageNum = 1
				this.loadCourses()
			} else {
				this.loadCourses()
			}
		}
	},
	methods: {
		getImageUrl(url) {
			return config.getImageUrl(url)
		},
		async loadCategories() {
			try {
				const list = await getCourseCategoryList()
				this.categories = ['全部']
				this.categoryMap = {}
				list.forEach(c => {
					this.categories.push(c.name)
					this.categoryMap[c.name] = c.id
				})
			} catch(e) {
				console.error('加载分类失败', e)
			}
		},
		onCategoryChange(name) {
			this.activeCategory = name
			this.categoryId = name === '全部' ? null : (this.categoryMap[name] || null)
			this.pageNum = 1
			this.loadCourses()
		},
		async loadCourses() {
			if (this.loading) return
			this.loading = true
			const type = this.activeTab === 'private' ? 1 : 2
			const params = { pageNum: this.pageNum, pageSize: this.pageSize, type }
			if (this.categoryId) params.categoryId = this.categoryId
			try {
				const res = await getCourseList(params)
				const records = res.records || []
				this.total = res.total || 0
				if (type === 1) {
					this.privatePackages = records.map(c => ({
						id: c.id,
						name: c.name,
						pic: c.pic || '',
						desc: c.description || '',
						price: c.price,
						lessonCount: c.lessonCount || 0,
						hot: c.sales > 0,
						features: c.features ? (typeof c.features === 'string' ? JSON.parse(c.features) : c.features) : []
					}))
				} else {
					// 团课：加载每个课程的最新排课
					const groupItems = records.map(c => ({
						id: c.id,
						name: c.name,
						time: '',
						location: '',
						price: c.price,
						enrolled: 0,
						total: 0,
						isToday: false,
						scheduleLoaded: false
					}))
					this.groupClasses = groupItems
					// 异步加载每个团课的排课
					groupItems.forEach((item, idx) => {
						this.loadScheduleForItem(item.id, idx)
					})
				}
			} catch(e) {
				console.error('加载课程失败', e)
			} finally {
				this.loading = false
			}
		},
		async loadScheduleForItem(courseId, idx) {
			try {
				const res = await getScheduleList(courseId)
				const list = res.records || []
				if (list.length > 0 && this.groupClasses[idx]) {
					const sch = list[0]
					const d = new Date(sch.startTime)
					const today = new Date()
					const isToday = d.toDateString() === today.toDateString()
					const m = d.getMonth() + 1
					const day = d.getDate()
					const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
					this.groupClasses[idx].time = `${m}/${day} ${weekDays[d.getDay()]} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
					this.groupClasses[idx].location = sch.location || ''
					this.groupClasses[idx].enrolled = sch.enrolledSeats || 0
					this.groupClasses[idx].total = sch.totalSeats || 0
					this.groupClasses[idx].isToday = isToday
					this.groupClasses[idx].scheduleLoaded = true
				}
			} catch(e) {
				// ignore
			}
		},
		goPrivateDetail(item) {
			uni.navigateTo({ url: `/pages/course/course-detail-private?id=${item.id}` });
		},
		goGroupDetail(item) {
			uni.navigateTo({ url: `/pages/course/course-detail-group?id=${item.id}` });
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
	padding-top: env(safe-area-inset-top);
	background: #eff1f2;
}
.header-left { display: flex; align-items: center; gap: 8rpx; }
.brand-logo { font-size: 36rpx; }
.brand-name { font-weight: 900; font-style: italic; font-size: 38rpx; color: #9c3f00; letter-spacing: -2rpx; }
.header-right { display: flex; align-items: center; gap: 24rpx; }
.header-icon { font-size: 36rpx; color: #595c5d; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.tab-section {
	position: sticky; top: 0; z-index: 50;
	background: rgba(245,246,247,0.95);
	padding: 16rpx 24rpx 0;
	backdrop-filter: blur(20rpx);
}

.main-tabs {
	display: flex; background: #e0e3e4; border-radius: 24rpx; padding: 4rpx;
}
.main-tab {
	flex: 1; padding: 16rpx 0; text-align: center; border-radius: 20rpx;
	transition: all 0.3s;
	&.active { background: #ff7a2f; box-shadow: 0 4rpx 16rpx rgba(255,122,47,0.3); }
}
.main-tab-text {
	font-size: 28rpx; font-weight: 700; color: #595c5d;
	&.active { color: #fff; }
}

.category-scroll { margin-top: 16rpx; white-space: nowrap; padding-bottom: 16rpx; }
.category-list { display: flex; gap: 16rpx; }
.category-tag {
	padding: 12rpx 32rpx; border-radius: 999rpx; background: transparent;
	flex-shrink: 0;
	&.active { background: #fff; border: 2rpx solid rgba(171,173,174,0.3); }
}
.category-tag-text {
	font-size: 26rpx; color: #595c5d; font-weight: 500;
	&.active { color: #9c3f00; font-weight: 700; }
}

.list-section { padding: 16rpx 24rpx; }

/* 私教课卡片 */
.private-card {
	display: flex;
	flex-direction: column;
	background: #fff; border-radius: 24rpx;
	overflow: hidden;
	margin-bottom: 20rpx;
}
.private-img {
	width: 100%;
	/* height 不设置，mode=widthFix 会根据原始图片比例自动计算高度 */
	display: block;
	background: #f0f0f0;
}
.private-info {
	padding: 20rpx 24rpx 24rpx;
}
.private-name {
	font-size: 30rpx; font-weight: 700; color: #2c2f30;
	margin-bottom: 8rpx;
}

.private-tags { display: flex; flex-wrap: wrap; gap: 8rpx; margin: 12rpx 0; }
.feature-tag {
	display: flex; align-items: center; gap: 4rpx;
	background: #eff1f2; border-radius: 12rpx; padding: 4rpx 12rpx;
}
.feature-tag-icon { font-size: 20rpx; }
.feature-tag-text { font-size: 20rpx; color: #595c5d; }

.private-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 16rpx; }
.private-meta { display: flex; align-items: baseline; gap: 12rpx; }
.private-price-row { display: flex; align-items: baseline; }
.private-price { font-size: 36rpx; }
.private-unit { font-size: 22rpx; color: #abadae; }

.order-btn {
	padding: 12rpx 28rpx; border-radius: 20rpx; background: #9c3f00;
	box-shadow: 0 4rpx 12rpx rgba(156,63,0,0.2);
}
.order-btn-text {
	font-size: 24rpx; font-weight: 700; color: #fff;
}

/* 团课卡片 */
.group-card {
	background: #fff; border-radius: 32rpx; padding: 24rpx;
	margin-bottom: 16rpx;
}
.group-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12rpx; }
.group-time-tag {
	display: flex; align-items: center; gap: 6rpx;
	background: #eff1f2; padding: 6rpx 16rpx; border-radius: 8rpx;
	&.today { background: rgba(255,122,47,0.08); }
}
.group-time-icon { font-size: 20rpx; }
.group-time-text { font-size: 20rpx; color: #595c5d; font-weight: 700; }
.group-seats-text { font-size: 22rpx; color: #595c5d; font-weight: 700; }
.group-seats-num { font-size: 22rpx; color: #9c3f00; font-weight: 900; }
.group-name { font-size: 32rpx; font-weight: 700; color: #2c2f30; margin-bottom: 8rpx; }
.group-location { display: flex; align-items: center; gap: 6rpx; margin-bottom: 16rpx; }
.group-loc-icon { font-size: 24rpx; }
.group-loc-text { font-size: 26rpx; color: #595c5d; }
.group-footer {
	display: flex; align-items: center; justify-content: space-between;
	padding-top: 16rpx;
	border-top: 2rpx dashed rgba(171,173,174,0.3);
}
.group-price-row { display: flex; align-items: baseline; }
.group-price { font-size: 36rpx; }
.group-unit { font-size: 20rpx; color: #abadae; }
.enroll-btn {
	padding: 12rpx 32rpx; background: #9c3f00; border-radius: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(156,63,0,0.2);
}
.enroll-btn-text { font-size: 26rpx; color: #fff; font-weight: 700; }
</style>
