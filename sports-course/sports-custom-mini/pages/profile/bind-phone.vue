<template>
	<view class="page">
		<view class="header">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">绑定手机号</text>
		</view>

		<view class="content">
			<view class="tip-card">
				<text class="tip-icon">📱</text>
				<text class="tip-text">绑定手机号后方可预约课程与查看订单</text>
			</view>

			<view class="form-card">
				<view class="form-item">
					<text class="form-label">手机号</text>
					<input
						class="form-input"
						type="number"
						placeholder="请输入手机号"
						placeholder-class="input-ph"
						v-model="phone"
						maxlength="11"
					/>
					<text v-if="errorPhone" class="error-tip">{{ errorPhone }}</text>
				</view>

				<view class="form-item">
					<text class="form-label">验证码</text>
					<view class="code-row">
						<input
							class="form-input code-input"
							type="number"
							placeholder="请输入验证码"
							placeholder-class="input-ph"
							v-model="code"
							maxlength="6"
						/>
						<button
							class="code-btn"
							:disabled="cooldown > 0 || sendingCode"
							@tap="handleSendCode"
						>
							{{ cooldown > 0 ? cooldown + 's' : '获取验证码' }}
						</button>
					</view>
					<text v-if="errorCode" class="error-tip">{{ errorCode }}</text>
				</view>

				<button class="submit-btn" :disabled="submitting" @tap="handleBind">
					<text v-if="!submitting">确认绑定</text>
					<text v-else>绑定中...</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script>
import { sendSmsCode, bindPhone } from '../../api/auth'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			phone: '',
			code: '',
			errorPhone: '',
			errorCode: '',
			sendingCode: false,
			cooldown: 0,
			cooldownTimer: null,
			submitting: false
		}
	},
	beforeUnmount() {
		if (this.cooldownTimer) clearInterval(this.cooldownTimer)
	},
	onLoad() {
		if (!checkLogin()) return
	},
	methods: {
		async handleSendCode() {
			if (!this.phone || !/^1[3-9]\d{9}$/.test(this.phone)) {
				this.errorPhone = '请输入正确的手机号'
				return
			}
			this.errorPhone = ''
			this.sendingCode = true
			try {
				await sendSmsCode(this.phone, 1)
				uni.showToast({ title: '验证码已发送', icon: 'success' })
				this.cooldown = 60
				this.cooldownTimer = setInterval(() => {
					this.cooldown--
					if (this.cooldown <= 0) {
						clearInterval(this.cooldownTimer)
					}
				}, 1000)
			} catch (e) {
				// 错误已在http.js中处理
			} finally {
				this.sendingCode = false
			}
		},
		async handleBind() {
			this.errorPhone = ''
			this.errorCode = ''
			let hasError = false
			if (!this.phone || !/^1[3-9]\d{9}$/.test(this.phone)) {
				this.errorPhone = '请输入正确的手机号'
				hasError = true
			}
			if (!this.code || this.code.length < 4) {
				this.errorCode = '请输入验证码'
				hasError = true
			}
			if (hasError) return

			this.submitting = true
			try {
				const res = await bindPhone(this.phone, this.code)
				// 同步更新本地缓存
				const cached = uni.getStorageSync('userInfo')
				if (cached) {
					const info = JSON.parse(cached)
					info.phone = this.phone
					uni.setStorageSync('userInfo', JSON.stringify(info))
				}
				uni.showToast({ title: '绑定成功', icon: 'success' })
				setTimeout(() => {
					uni.navigateBack()
				}, 1500)
			} catch (e) {
				// 错误已在http.js中处理
			} finally {
				this.submitting = false
			}
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

.tip-card {
	display: flex;
	align-items: center;
	gap: 12rpx;
	background: rgba(255, 122, 47, 0.08);
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 24rpx;
}

.tip-icon {
	font-size: 32rpx;
}

.tip-text {
	font-size: 24rpx;
	color: #9c3f00;
	line-height: 1.5;
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

.input-ph {
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
</style>
