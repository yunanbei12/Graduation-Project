<template>
	<view class="page">
		<view class="header">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">用户注册</text>
		</view>

		<view class="content">
			<view class="logo-area">
				<text class="brand-logo">⚡</text>
				<text class="brand-name">KINETIC</text>
				<text class="brand-sub">创建您的账号</text>
			</view>

			<view class="form-card">
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

				<view class="form-item">
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
							:disabled="codeCooldown > 0 || sendingCode"
							@tap="handleSendCode"
						>
							{{ codeCooldown > 0 ? codeCooldown + 's' : '获取验证码' }}
						</button>
					</view>
					<text v-if="errors.code" class="error-tip">{{ errors.code }}</text>
				</view>

				<view class="form-item">
					<text class="form-label">密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="请输入密码（6位以上）"
						placeholder-class="input-placeholder"
						v-model="form.passWord"
					/>
					<text v-if="errors.passWord" class="error-tip">{{ errors.passWord }}</text>
				</view>

				<view class="form-item">
					<text class="form-label">确认密码</text>
					<input
						class="form-input"
						type="password"
						placeholder="请再次输入密码"
						placeholder-class="input-placeholder"
						v-model="form.confirmPassword"
					/>
					<text v-if="errors.confirmPassword" class="error-tip">{{ errors.confirmPassword }}</text>
				</view>

				<button class="submit-btn" :disabled="loading" @tap="handleRegister">
					<text v-if="!loading">立即注册</text>
					<text v-else>注册中...</text>
				</button>

				<view class="footer-link">
					<text class="link-text">已有账号？</text>
					<text class="link-action" @tap="goLogin">去登录 ›</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { register, sendSmsCode } from '../../api/auth'

export default {
	data() {
		return {
			loading: false,
			sendingCode: false,
			codeCooldown: 0,
			cooldownTimer: null,
			form: {
				phone: '',
				code: '',
				passWord: '',
				confirmPassword: ''
			},
			errors: {
				phone: '',
				code: '',
				passWord: '',
				confirmPassword: ''
			}
		}
	},
	beforeUnmount() {
		if (this.cooldownTimer) clearInterval(this.cooldownTimer)
	},
	methods: {
		validate() {
			this.errors = { phone: '', code: '', passWord: '', confirmPassword: '' }
			let valid = true
			if (!this.form.phone || !/^1[3-9]\d{9}$/.test(this.form.phone)) {
				this.errors.phone = '请输入正确的手机号'
				valid = false
			}
			if (!this.form.code || this.form.code.length < 4) {
				this.errors.code = '请输入验证码'
				valid = false
			}
			if (!this.form.passWord || this.form.passWord.length < 6) {
				this.errors.passWord = '密码不能少于6位'
				valid = false
			}
			if (this.form.passWord !== this.form.confirmPassword) {
				this.errors.confirmPassword = '两次密码输入不一致'
				valid = false
			}
			return valid
		},
		async handleSendCode() {
			if (!this.form.phone || !/^1[3-9]\d{9}$/.test(this.form.phone)) {
				this.errors = { phone: '请输入正确的手机号', code: '', passWord: '', confirmPassword: '' }
				return
			}
			this.sendingCode = true
			try {
				await sendSmsCode(this.form.phone, 0)
				uni.showToast({ title: '验证码已发送', icon: 'success' })
				this.codeCooldown = 60
				this.cooldownTimer = setInterval(() => {
					this.codeCooldown--
					if (this.codeCooldown <= 0) {
						clearInterval(this.cooldownTimer)
					}
				}, 1000)
			} catch (e) {
				// 错误已在http.js中处理
			} finally {
				this.sendingCode = false
			}
		},
		async handleRegister() {
			if (!this.validate()) return
			this.loading = true
			try {
				await register(this.form.phone.trim(), this.form.passWord, this.form.code.trim())
				uni.showToast({ title: '注册成功，请登录', icon: 'success', duration: 1500 })
				setTimeout(() => {
					uni.redirectTo({ url: '/pages/accountLogin/accountLogin' })
				}, 1600)
			} catch (e) {
				// 错误已在http.js中处理
			} finally {
				this.loading = false
			}
		},
		goLogin() {
			uni.navigateTo({ url: '/pages/accountLogin/accountLogin' })
		},
		goBack() {
			uni.navigateBack()
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

.input-placeholder {
	color: #c0c2c3;
}

.error-tip {
	font-size: 22rpx;
	color: #f56c6c;
	margin-top: 8rpx;
	display: block;
}

.code-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
}
.code-input {
	flex: 1;
}
.code-btn {
	flex-shrink: 0;
	height: 88rpx;
	line-height: 88rpx;
	padding: 0 24rpx;
	font-size: 24rpx;
	color: #9c3f00;
	background: rgba(156, 63, 0, 0.08);
	border: none;
	border-radius: 16rpx;
	white-space: nowrap;
}
.code-btn[disabled] {
	opacity: 0.5;
	color: #abadae;
	background: #f5f6f7;
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
</style>
