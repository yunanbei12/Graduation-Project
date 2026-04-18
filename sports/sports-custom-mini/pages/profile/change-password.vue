<template>
	<view class="page">
		<view class="header">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">{{ hasPassword ? '修改密码' : '设置密码' }}</text>
			<view style="width: 64rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view class="form-card">
				<text class="pwd-hint">{{ hasPassword ? '修改登录密码，需验证原密码' : '设置后可使用手机号 + 密码登录' }}</text>

				<view v-if="hasPassword" class="form-row">
					<text class="form-label">原密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="请输入原密码"
						placeholder-class="input-ph"
						v-model="pwd.old"
					/>
				</view>
				<view class="form-divider" v-if="hasPassword"></view>

				<view class="form-row">
					<text class="form-label">新密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="至少6位"
						placeholder-class="input-ph"
						v-model="pwd.new"
					/>
				</view>
				<view class="form-divider"></view>

				<view class="form-row">
					<text class="form-label">确认密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="再次输入新密码"
						placeholder-class="input-ph"
						v-model="pwd.confirm"
					/>
				</view>

				<button class="save-btn" :disabled="saving" @tap="handleSubmit">
					<text v-if="!saving">{{ hasPassword ? '确认修改' : '确认设置' }}</text>
					<text v-else>保存中...</text>
				</button>
			</view>
		</scroll-view>
	</view>
</template>

<script>
import { getUserInfo, updatePassword } from '../../api/auth'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			hasPassword: false,
			pwd: { old: '', new: '', confirm: '' },
			saving: false
		}
	},
	onLoad() {
		if (!checkLogin()) return
		this.loadUserInfo()
	},
	methods: {
		async loadUserInfo() {
			try {
				const res = await getUserInfo()
				this.hasPassword = !!res.hasPassword
			} catch(e) {}
		},
		async handleSubmit() {
			if (!this.pwd.new || this.pwd.new.length < 6) {
				uni.showToast({ title: '新密码不能少于6位', icon: 'none' })
				return
			}
			if (this.pwd.new !== this.pwd.confirm) {
				uni.showToast({ title: '两次密码不一致', icon: 'none' })
				return
			}
			if (this.hasPassword && !this.pwd.old) {
				uni.showToast({ title: '请输入原密码', icon: 'none' })
				return
			}
			this.saving = true
			uni.showLoading({ title: '保存中...' })
			try {
				await updatePassword(this.hasPassword ? this.pwd.old : '', this.pwd.new)
				uni.showToast({ title: '密码设置成功', icon: 'success' })
				setTimeout(() => uni.navigateBack(), 1500)
			} catch(e) {
				// 错误已在 http.js 中 toast
			} finally {
				this.saving = false
				uni.hideLoading()
			}
		},
		goBack() {
			uni.navigateBack()
		}
	}
}
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f5f6f7; }

.header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 24rpx;
	height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top);
	background: #f5f6f7;
}
.back-btn { width: 64rpx; height: 64rpx; display: flex; align-items: center; }
.back-icon { font-size: 56rpx; color: #2c2f30; line-height: 1; }
.header-title { font-size: 34rpx; font-weight: 700; color: #2c2f30; }

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.form-card {
	margin: 24rpx 24rpx 0;
	background: #fff;
	border-radius: 24rpx;
	padding: 0 28rpx;
	overflow: hidden;
}

.pwd-hint {
	font-size: 22rpx;
	color: #abadae;
	padding: 24rpx 0 8rpx;
	display: block;
	line-height: 1.6;
}

.form-row {
	display: flex;
	align-items: center;
	padding: 28rpx 0;
}
.form-label {
	font-size: 28rpx;
	color: #2c2f30;
	font-weight: 500;
	width: 140rpx;
	flex-shrink: 0;
}
.form-input {
	flex: 1;
	font-size: 28rpx;
	color: #2c2f30;
	text-align: right;
}
.input-ph { color: #c0c2c3; }
.form-divider { height: 2rpx; background: #f5f6f7; }

.save-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	color: #fff;
	font-size: 30rpx;
	font-weight: 700;
	border-radius: 16rpx;
	border: none;
	margin: 24rpx 0;
	display: flex;
	align-items: center;
	justify-content: center;
}
.save-btn[disabled] { opacity: 0.6; }
</style>
