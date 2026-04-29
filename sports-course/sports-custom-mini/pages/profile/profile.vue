<template>
	<view class="page">
		<view class="header">
			<view class="header-left">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
			</view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- 用户信息卡 -->
			<view class="profile-hero">
				<view class="hero-bg-circle"></view>
				<view class="hero-content">
					<!-- 未登录状态 -->
					<view v-if="!isLoggedIn" class="not-login-area">
						<text class="not-login-icon">👤</text>
						<text class="not-login-tip">登录后可查看训练记录、订单等</text>
						<view class="login-btns">
							<view class="btn-login" @tap="goLogin">登录</view>
							<view class="btn-register" @tap="goRegister">注册</view>
						</view>
					</view>
					<!-- 已登录状态 -->
					<view v-else class="hero-top" @tap="goEditProfile">
						<image class="hero-avatar" :src="userInfo?.avatarUrl || '/static/头像.png'" mode="aspectFill" />
						<view class="hero-info">
							<view class="hero-name-row">
								<text class="hero-name">{{ userInfo?.nickName || '用户' }}</text>
							</view>
							<text class="hero-motto">{{ userInfo?.phone || '点击编辑资料' }}</text>
						</view>
						<view class="edit-btn">
							<text class="edit-icon">☰</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 快捷导航 -->
			<view :class="['quick-cards', { single: orderCards.length === 1 }]">
				<view class="quick-card" v-for="item in orderCards" :key="item.type" @tap="goOrders(item.type)">
					<view class="qc-icon-wrap">
						<text class="qc-icon">{{ item.icon }}</text>
					</view>
					<text class="qc-title">{{ item.title }}</text>
					<text class="qc-sub">{{ item.subTitle }}</text>
				</view>
			</view>

			<!-- 功能列表 -->
			<view class="func-section">
				<view class="func-header">
					<text class="func-title">常用功能</text>
					<text class="func-sub">服务与设置</text>
				</view>
				<view class="func-list">
					<view class="func-item" v-for="(item, index) in funcItems" :key="index" @tap="onFuncTap(item)">
						<view class="func-left">
							<text class="func-icon">{{ item.icon }}</text>
							<text class="func-name">{{ item.name }}</text>
						</view>
						<view class="func-right">
							<text class="func-tag" v-if="item.tag">{{ item.tag }}</text>
							<text class="func-arrow">›</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 会员推广 -->
			<view class="member-card">
				<view class="member-content">
					<text class="member-label">PREMIUM MEMBERSHIP</text>
					<text class="member-title">开通超级会员\n解锁更多专属特权</text>
					<text class="member-desc">课程 8.8 折 · 专属教练 · 免费体测</text>
					<view class="member-btn">
						<text class="member-btn-text">立即开启</text>
					</view>
				</view>
				<image class="member-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuC6p0gew4Wp3NiWc0h_UkvDzh47ILzpHKABQ9BDvporu7HDQOEEuoSin20jfi5h2mgSSh6Pv2NhwKVHtdMDDlh2P-p7QP2KXxJI_WH9tDkN42aaUCCoEnd7gbULcwYfnRdb5Ig5hnKv8UJyn9nQIS8GiGXfpOUlKkwDTjG1WPNE6gq5pSP8971r8zrc_5x-nmki8qqUfpMhY-2kHKB2l5xHX1MCPgIxJG9gf5J3bvWT60Z3hA2NuUtZlBJO3eYBDCQijjCPsuBZ8x4" mode="aspectFill" />
			</view>

			<view class="version-text">
				<text class="version">VERSION 2.4.0 · KINETIC SPORTS TECH</text>
			</view>

			<!-- 退出登录 -->
			<view v-if="isLoggedIn" class="logout-area" @tap="handleLogout">
				<text class="logout-text">退出登录</text>
			</view>

			<view style="height: 40rpx;"></view>
		</scroll-view>
	</view>
</template>

<script>
import { getUserInfo } from '../../api/auth'

export default {
	data() {
		return {
			userInfo: null,
			isLoggedIn: false,
			funcItems: [
				{ icon: '📦', name: '我的课包', route: '/pages/profile/my-packages' },
				{ icon: '🎫', name: '我的优惠券', route: '/pages/profile/my-coupons' },
				{ icon: '🤖', name: 'AI客服', route: '/pages/ai/chat', tag: 'NEW' },
				{ icon: '⚙️', name: '系统设置', tag: '' }
			]
		}
	},
	computed: {
		orderCards() {
			return [
				{ type: 'course', icon: '📋', title: '课程订单', subTitle: '预约与消课' }
			]
		}
	},
	onShow() {
		this.isLoggedIn = !!uni.getStorageSync('token')
		this.loadUserInfo()
	},
	methods: {
		async loadUserInfo() {
			if (!uni.getStorageSync('token')) return
			try {
				const res = await getUserInfo()
				this.userInfo = res
			} catch (e) {
				console.error('获取用户信息失败', e)
			}
		},
		goOrders(type) {
			if (!this.isLoggedIn) {
				uni.navigateTo({ url: '/pages/accountLogin/accountLogin' })
				return
			}
			uni.navigateTo({ url: '/pages/order/my-orders-course' })
		},
		onFuncTap(item) {
			if (!this.isLoggedIn && item.route && item.route !== '/pages/ai/chat') {
				uni.navigateTo({ url: '/pages/accountLogin/accountLogin' })
				return
			}
			if (item.route) {
				uni.navigateTo({ url: item.route })
			} else {
				uni.showToast({ title: item.name, icon: 'none' })
			}
		},
		goLogin() {
			uni.navigateTo({ url: '/pages/accountLogin/accountLogin' })
		},
		goRegister() {
			uni.navigateTo({ url: '/pages/accountLogin/register' })
		},
		goEditProfile() {
			uni.navigateTo({ url: '/pages/profile/edit-profile' })
		},
		handleLogout() {
			uni.showModal({
				title: '提示',
				content: '确认退出登录？',
				success: (res) => {
					if (res.confirm) {
						uni.removeStorageSync('token')
						uni.removeStorageSync('userInfo')
						this.isLoggedIn = false
						this.userInfo = null
						uni.showToast({ title: '已退出登录', icon: 'none' })
					}
				}
			})
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
.header-icon { font-size: 36rpx; color: #595c5d; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

/* 用户信息卡 */
.profile-hero {
	margin: 16rpx 24rpx 0; border-radius: 24rpx; overflow: hidden;
	background: linear-gradient(135deg, #9c3f00 0%, #ff7a2f 100%);
	position: relative; padding: 32rpx;
}
.hero-bg-circle {
	position: absolute; top: -60rpx; right: -60rpx;
	width: 260rpx; height: 260rpx; border-radius: 50%;
	background: rgba(255,255,255,0.08);
}
.hero-content { position: relative; z-index: 1; }
.hero-top { display: flex; align-items: flex-start; gap: 20rpx; }
.hero-avatar {
	width: 120rpx; height: 120rpx; border-radius: 50%;
	border: 4rpx solid rgba(255,255,255,0.2);
}
.hero-info { flex: 1; }
.hero-name-row { display: flex; align-items: center; gap: 8rpx; flex-wrap: wrap; }
.hero-name { font-size: 40rpx; font-weight: 900; color: #fff; }
.hero-name-en { font-size: 32rpx; font-weight: 700; color: rgba(255,255,255,0.8); }
.level-badge {
	background: rgba(255,255,255,0.2); padding: 4rpx 12rpx;
	border-radius: 999rpx; border: 2rpx solid rgba(255,255,255,0.3);
}
.level-text { font-size: 16rpx; color: #fff; font-weight: 700; text-align: center; }
.hero-motto { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }
.edit-btn {
	width: 64rpx; height: 64rpx; border-radius: 16rpx;
	background: rgba(255,255,255,0.1); display: flex;
	align-items: center; justify-content: center;
}
.edit-icon { font-size: 28rpx; color: #fff; }

/* 快捷导航 */
.quick-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; padding: 24rpx; }
.quick-cards.single { grid-template-columns: 1fr; }
.quick-card {
	background: #fff; border-radius: 24rpx; padding: 28rpx;
	display: flex; flex-direction: column; align-items: center;
	text-align: center;
}
.qc-icon-wrap {
	width: 80rpx; height: 80rpx; border-radius: 16rpx;
	background: rgba(255,122,47,0.08); display: flex;
	align-items: center; justify-content: center; margin-bottom: 16rpx;
}
.qc-icon { font-size: 36rpx; }
.qc-title { font-size: 28rpx; font-weight: 700; color: #2c2f30; }
.qc-sub { font-size: 20rpx; color: #abadae; margin-top: 4rpx; }

/* 功能列表 */
.func-section { margin: 0 24rpx; background: #fff; border-radius: 24rpx; overflow: hidden; }
.func-header {
	display: flex; justify-content: space-between; align-items: center;
	padding: 24rpx 28rpx; border-bottom: 2rpx solid #eff1f2;
}
.func-title { font-size: 30rpx; font-weight: 700; }
.func-sub { font-size: 22rpx; color: #abadae; }
.func-item {
	display: flex; justify-content: space-between; align-items: center;
	padding: 28rpx;
}
.func-left { display: flex; align-items: center; gap: 20rpx; }
.func-icon { font-size: 32rpx; opacity: 0.6; }
.func-name { font-size: 28rpx; font-weight: 500; }
.func-right { display: flex; align-items: center; gap: 8rpx; }
.func-tag {
	font-size: 18rpx; background: rgba(255,122,47,0.08);
	color: #9c3f00; padding: 4rpx 12rpx; border-radius: 8rpx;
}
.func-arrow { font-size: 32rpx; color: #abadae; }

/* 会员推广 */
.member-card {
	margin: 24rpx; border-radius: 24rpx; overflow: hidden;
	background: #1a1a1a; display: flex; position: relative;
	min-height: 280rpx;
}
.member-content { flex: 1; padding: 32rpx; position: relative; z-index: 1; }
.member-label {
	font-size: 18rpx; color: #ff7a2f; font-weight: 700;
	letter-spacing: 4rpx; margin-bottom: 12rpx; display: block;
}
.member-title { font-size: 34rpx; color: #fff; font-weight: 900; line-height: 1.4; }
.member-desc { font-size: 22rpx; color: #757778; margin-top: 12rpx; display: block; }
.member-btn {
	margin-top: 20rpx; background: #9c3f00; padding: 14rpx 28rpx;
	border-radius: 16rpx; display: inline-flex;
}
.member-btn-text { color: #fff; font-size: 24rpx; font-weight: 700; }
.member-img {
	position: absolute; right: 0; top: 0; bottom: 0;
	width: 40%; opacity: 0.5;
}

.version-text { text-align: center; padding: 20rpx; }
.version { font-size: 18rpx; color: #abadae; letter-spacing: 2rpx; }

/* 未登录 */
.not-login-area {
	display: flex; flex-direction: column; align-items: center;
	padding: 40rpx 0 20rpx;
}
.not-login-icon { font-size: 80rpx; }
.not-login-tip { font-size: 26rpx; color: rgba(255,255,255,0.8); margin-top: 16rpx; }
.login-btns {
	display: flex; gap: 24rpx; margin-top: 32rpx;
}
.btn-login {
	padding: 16rpx 56rpx; border-radius: 12rpx;
	background: #fff; color: #9c3f00;
	font-size: 28rpx; font-weight: 700;
}
.btn-register {
	padding: 16rpx 56rpx; border-radius: 12rpx;
	border: 2rpx solid rgba(255,255,255,0.6); color: #fff;
	font-size: 28rpx; font-weight: 700;
}

/* 退出登录 */
.logout-area {
	margin: 8rpx 24rpx 0; background: #fff; border-radius: 16rpx;
	padding: 32rpx; display: flex; justify-content: center;
}
.logout-text { font-size: 28rpx; color: #f56c6c; font-weight: 600; }
</style>
