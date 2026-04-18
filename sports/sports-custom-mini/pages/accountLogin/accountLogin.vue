<template>
	<view class="page">
		<view class="header">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">用户登录</text>
		</view>

		<view class="content">
			<view class="logo-area">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
				<text class="brand-sub">登录您的账号</text>
			</view>

			<view class="form-card">
				<!-- 登录方式切换 -->
				<view class="tab-bar">
					<view
						:class="['tab-item', loginType === 'password' ? 'tab-active' : '']"
						@tap="loginType = 'password'"
					>
						<text>密码登录</text>
					</view>
					<view
						:class="['tab-item', loginType === 'sms' ? 'tab-active' : '']"
						@tap="loginType = 'sms'"
					>
						<text>验证码登录</text>
					</view>
				</view>

				<view class="form-item">
					<text class="form-label">手机号</text>
					<input
						class="form-input"
						type="number"
						placeholder="请输入手机号"
						placeholder-class="input-placeholder"
						v-model="form.phone"
						maxlength="11"
					/>
					<text v-if="errors.phone" class="error-tip">{{ errors.phone }}</text>
				</view>

				<!-- 密码登录 -->
				<view v-if="loginType === 'password'" class="form-item">
					<text class="form-label">密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="请输入密码"
						placeholder-class="input-placeholder"
						v-model="form.passWord"
					/>
					<text v-if="errors.passWord" class="error-tip">{{ errors.passWord }}</text>
				</view>

				<!-- 验证码登录 -->
				<view v-if="loginType === 'sms'" class="form-item">
					<text class="form-label">验证码</text>
					<view class="code-row">
						<input
							class="form-input code-input"
							type="number"
							placeholder="请输入验证码"
							placeholder-class="input-placeholder"
							v-model="form.code"
							maxlength="6"
						/>
						<button
							class="code-btn"
							:disabled="countdown > 0"
							@tap="sendCode"
						>
							{{ countdown > 0 ? countdown + 's' : '获取验证码' }}
						</button>
					</view>
					<text v-if="errors.code" class="error-tip">{{ errors.code }}</text>
				</view>

				<button class="submit-btn" :disabled="loading" @tap="handleLogin">
					<text v-if="!loading">立即登录</text>
					<text v-else>登录中...</text>
				</button>

				<view class="footer-link">
					<text class="link-text">还没有账号？</text>
					<text class="link-action" @tap="goRegister">去注册 ›</text>
				</view>
			</view>

			<!-- 游客模式 -->
			<view class="guest-area">
				<text class="guest-text" @tap="goHome">暂不登录，先逛逛 ›</text>
			</view>
		</view>
	</view>
</template>

<script>
import { accountLogin, smsLogin, sendSmsCode } from '../../api/auth'
import { navigateBackAfterLogin } from '../../utils/auth'

export default {
	data() {
		return {
			loading: false,
			loginType: 'password', // 'password' | 'sms'
			countdown: 0,
			countdownTimer: null,
			redirect: '', // 登录后跳转的页面
			form: {
				phone: '',
				passWord: '',
				code: ''
			},
			errors: {
				phone: '',
				passWord: '',
				code: ''
			}
		}
	},
	onLoad(options) {
		// 获取跳转参数
		if (options.redirect) {
			this.redirect = options.redirect
		}
	},
	methods: {
		validate() {
			this.errors = { phone: '', passWord: '', code: '' }
			let valid = true
			if (!this.form.phone || !this.form.phone.trim()) {
				this.errors.phone = '请输入手机号'
				valid = false
			}
			if (this.loginType === 'password') {
				if (!this.form.passWord) {
					this.errors.passWord = '请输入密码'
					valid = false
				}
			} else {
				if (!this.form.code || !this.form.code.trim()) {
					this.errors.code = '请输入验证码'
					valid = false
				}
			}
			return valid
		},
		async handleLogin() {
			if (!this.validate()) return
			this.loading = true
			try {
				if (this.loginType === 'password') {
					const res = await accountLogin(this.form.phone.trim(), this.form.passWord)
					this.onLoginSuccess(res)
				} else {
					await this.doSmsLogin(false)
				}
			} catch (e) {
				// 错误已在http.js中处理
			} finally {
				this.loading = false
			}
		},
		// 执行短信登录，confirm=true表示用户已确认注册
		async doSmsLogin(confirm) {
			const res = await smsLogin(this.form.phone.trim(), this.form.code.trim(), confirm)
			if (res.notRegistered) {
				// 手机号未注册，弹窗询问用户
				uni.showModal({
					title: '手机号未注册',
					content: '该手机号尚未注册，是否立即注册并登录？',
					confirmText: '注册并登录',
					cancelText: '取消',
					success: async (modalRes) => {
						if (modalRes.confirm) {
							// 用户确认，带 confirm=true 重新请求
							this.loading = true
							try {
								const confirmRes = await smsLogin(this.form.phone.trim(), this.form.code.trim(), true)
								this.onLoginSuccess(confirmRes)
							} catch (e) {
								// 错误已在http.js中处理
							} finally {
								this.loading = false
							}
						}
						// 用户取消则什么都不做
					}
				})
			} else {
				this.onLoginSuccess(res)
			}
		},
		onLoginSuccess(res) {
			uni.setStorageSync('token', res.token)
			uni.setStorageSync('userInfo', JSON.stringify({
				userId: res.userId,
				nickName: res.nickName,
				avatarUrl: res.avatarUrl,
				phone: res.phone
			}))
			uni.showToast({ title: '登录成功', icon: 'success', duration: 1200 })
			setTimeout(() => {
				// 使用封装好的跳转方法
				navigateBackAfterLogin(this.redirect)
			}, 1300)
		},
		async sendCode() {
			if (!this.form.phone || !/^1[3-9]\d{9}$/.test(this.form.phone.trim())) {
				this.errors.phone = '请输入正确的手机号'
				return
			}
			try {
				await sendSmsCode(this.form.phone.trim(), 0)
				uni.showToast({ title: '验证码已发送', icon: 'none' })
				this.countdown = 60
				this.countdownTimer = setInterval(() => {
					this.countdown--
					if (this.countdown <= 0) {
						clearInterval(this.countdownTimer)
					}
				}, 1000)
			} catch (e) {
				// 错误已在http.js中处理
			}
		},
		goRegister() {
			uni.navigateTo({ url: '/pages/accountLogin/register' })
		},
		goHome() {
			uni.switchTab({ url: '/pages/index/index' })
		},
		goBack() {
			const pages = getCurrentPages()
			if (pages.length > 1) {
				uni.navigateBack()
			} else {
				uni.switchTab({ url: '/pages/profile/profile' })
			}
		}
	},
	beforeUnmount() {
		if (this.countdownTimer) {
			clearInterval(this.countdownTimer)
		}
	}
}
</script>

<style lang="scss" scoped>
.page {
	min-height: 100vh;
	background: #f5f6f7;
}

.header {
	display: flex;
	align-items: center;
	padding: 0 24rpx;
	height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top);
	background: #f5f6f7;
}

.back-btn {
	width: 64rpx;
	height: 64rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 56rpx;
	color: #2c2f30;
	line-height: 1;
}

.header-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #2c2f30;
	margin-left: 16rpx;
}

.content {
	padding: 20rpx 32rpx 60rpx;
}

.logo-area {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 0 48rpx;
}

.brand-logo {
	font-size: 80rpx;
}

.brand-name {
	font-size: 52rpx;
	font-weight: 900;
	font-style: italic;
	color: #9c3f00;
	letter-spacing: -2rpx;
	margin-top: 8rpx;
}

.brand-sub {
	font-size: 26rpx;
	color: #abadae;
	margin-top: 12rpx;
}

.form-card {
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx 32rpx;
}

.tab-bar {
	display: flex;
	margin-bottom: 32rpx;
	background: #f5f6f7;
	border-radius: 12rpx;
	padding: 6rpx;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 16rpx 0;
	font-size: 26rpx;
	color: #abadae;
	border-radius: 10rpx;
	transition: all 0.2s;
}

.tab-active {
	background: #fff;
	color: #9c3f00;
	font-weight: 700;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}

.form-item {
	margin-bottom: 32rpx;
}

.form-label {
	font-size: 26rpx;
	font-weight: 600;
	color: #2c2f30;
	display: block;
	margin-bottom: 12rpx;
}

.form-input {
	width: 100%;
	height: 88rpx;
	background: #f5f6f7;
	border-radius: 16rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
	color: #2c2f30;
	box-sizing: border-box;
}

.code-row {
	display: flex;
	gap: 16rpx;
}

.code-input {
	flex: 1;
}

.code-btn {
	width: 200rpx;
	height: 88rpx;
	background: #9c3f00;
	color: #fff;
	font-size: 24rpx;
	border-radius: 16rpx;
	border: none;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.code-btn[disabled] {
	background: #ccc;
	color: #fff;
}

.input-placeholder {
	color: #c0c2c3;
}

.error-tip {
	font-size: 22rpx;
	color: #f56c6c;
	margin-top: 8rpx;
	display: block;
}

.submit-btn {
	width: 100%;
	height: 96rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	color: #fff;
	font-size: 32rpx;
	font-weight: 700;
	border-radius: 16rpx;
	border: none;
	margin-top: 8rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.submit-btn[disabled] {
	opacity: 0.6;
}

.footer-link {
	display: flex;
	justify-content: center;
	align-items: center;
	margin-top: 32rpx;
	gap: 8rpx;
}

.link-text {
	font-size: 26rpx;
	color: #abadae;
}

.link-action {
	font-size: 26rpx;
	color: #9c3f00;
	font-weight: 600;
}

.guest-area {
	display: flex;
	justify-content: center;
	margin-top: 40rpx;
}

.guest-text {
	font-size: 26rpx;
	color: #abadae;
}
</style>
