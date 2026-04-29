<template>
	<view class="page">
		<view class="header">
			<view class="back-btn" @tap="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">编辑个人信息</text>
			<view style="width: 64rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<!-- 头像 -->
			<view class="section">
				<view class="avatar-area" @tap="chooseAvatar">
					<image
						class="avatar-img"
						:src="form.avatarUrl || '/static/头像.png'"
						mode="aspectFill"
					/>
					<view class="avatar-edit-mask">
						<text class="camera-icon">📷</text>
					</view>
				</view>
				<text class="avatar-tip">点击更换头像</text>
			</view>

			<!-- 基本信息 -->
			<view class="form-card">
				<view class="form-title">基本信息</view>

				<view class="form-row">
					<text class="form-label">昵称</text>
					<input
						class="form-input"
						type="text"
						placeholder="请输入昵称"
						placeholder-class="input-ph"
						v-model="form.nickName"
						maxlength="20"
					/>
				</view>

				<view class="form-divider"></view>

				<view class="form-row">
					<text class="form-label">手机号</text>
					<template v-if="form.phone">
						<text class="form-value phone-readonly">{{ form.phone }}</text>
						<text class="phone-locked-tip">已绑定</text>
					</template>
					<template v-else>
						<text class="form-value phone-unbind">未绑定</text>
						<view class="bind-btn" @tap="goBindPhone">
							<text class="bind-btn-text">去绑定</text>
						</view>
					</template>
				</view>

				<view class="form-divider"></view>

				<view class="form-row" @tap="goChangePassword">
					<text class="form-label">登录密码</text>
					<text class="form-value-right">{{ hasPassword ? '已设置' : '未设置' }}</text>
					<text class="form-arrow">›</text>
				</view>

				<!-- 保存基本信息按钮 -->
				<button class="save-btn-bottom" :disabled="saving" @tap="handleSave">
					<text v-if="!saving">保存信息</text>
					<text v-else>保存中...</text>
				</button>
			</view>

			<view style="height: 60rpx;"></view>
		</scroll-view>
	</view>
</template>

<script>
import { getUserInfo, updateUserInfo } from '../../api/auth'
import config from '../../utils/config'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			form: {
				nickName: '',
				phone: '',
				avatarUrl: ''
			},
			hasPassword: false,
			saving: false,
			uploadingAvatar: false
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
				this.form.nickName  = res.nickName  || ''
				this.form.phone     = res.phone     || ''
				this.form.avatarUrl = res.avatarUrl || ''
				// 判断是否已有密码（loginPassword字段后端未返回，用phone/nickName来间接判断不可靠）
				// 通过后端 /profile/info 返回的 loginPassword 是否为 null 来判断
				// 后端已脱敏不返回密码值，但我们可以通过返回一个 hasPassword 布尔值来判断
				// 这里先默认：有 openId 无密码的微信用户 hasPassword=false
				// 实际通过后端增加 hasPassword 字段更准确（见下方说明）
				this.hasPassword = !!res.hasPassword
			} catch(e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			}
		},

		// 选择头像
		chooseAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					const tempPath = res.tempFilePaths[0]
					this.uploadAvatar(tempPath)
				}
			})
		},

		// 上传头像到后端
		uploadAvatar(tempPath) {
			this.uploadingAvatar = true
			uni.showLoading({ title: '上传中...' })
			uni.uploadFile({
				url: config.BASE_URL + '/file/upload',
				filePath: tempPath,
				name: 'file',
				header: {
					'Authorization': uni.getStorageSync('token') || ''
				},
				success: (uploadRes) => {
					try {
						const data = JSON.parse(uploadRes.data)
						if (data.code === 200) {
							this.form.avatarUrl = data.data
						} else {
							uni.showToast({ title: data.msg || '上传失败', icon: 'none' })
						}
					} catch(e) {
						uni.showToast({ title: '上传失败', icon: 'none' })
					}
				},
				fail: () => {
					uni.showToast({ title: '上传失败', icon: 'none' })
				},
				complete: () => {
					this.uploadingAvatar = false
					uni.hideLoading()
				}
			})
		},

		// 保存基本信息
		async handleSave() {
			if (!this.form.nickName || !this.form.nickName.trim()) {
				uni.showToast({ title: '昵称不能为空', icon: 'none' })
				return
			}
			this.saving = true
			uni.showLoading({ title: '保存中...' })
			try {
				await updateUserInfo({
					nickName: this.form.nickName.trim(),
					avatarUrl: this.form.avatarUrl
				})
				// 同步更新本地缓存
				const cached = uni.getStorageSync('userInfo')
				if (cached) {
					const info = JSON.parse(cached)
					info.nickName  = this.form.nickName.trim()
					info.avatarUrl = this.form.avatarUrl
					uni.setStorageSync('userInfo', JSON.stringify(info))
				}
				uni.showToast({ title: '保存成功', icon: 'success' })
			} catch(e) {
				// 错误已在 http.js 中 toast
			} finally {
				this.saving = false
				uni.hideLoading()
			}
		},

		// 修改/设置密码入口
		goChangePassword() {
			uni.navigateTo({ url: '/pages/profile/change-password' })
		},

		goBack() {
			uni.navigateBack()
		},
		goBindPhone() {
			uni.navigateTo({ url: '/pages/profile/bind-phone' })
		},
		goChangePassword() {
			uni.navigateTo({ url: '/pages/profile/change-password' })
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
	justify-content: space-between;
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
}

.save-btn-bottom {
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

.save-btn-bottom[disabled] {
	opacity: 0.6;
}

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

/* 头像区域 */
.section {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 40rpx 0 24rpx;
}

.avatar-area {
	position: relative;
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	overflow: hidden;
}

.avatar-img {
	width: 100%;
	height: 100%;
}

.avatar-edit-mask {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	height: 50rpx;
	background: rgba(0, 0, 0, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
}

.camera-icon {
	font-size: 24rpx;
}

.avatar-tip {
	font-size: 22rpx;
	color: #abadae;
	margin-top: 12rpx;
}

/* 表单卡片 */
.form-card {
	margin: 0 24rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 0 28rpx;
	overflow: hidden;
}

.form-title {
	font-size: 24rpx;
	color: #9c3f00;
	font-weight: 700;
	padding: 24rpx 0 8rpx;
	letter-spacing: 2rpx;
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

.input-ph {
	color: #c0c2c3;
}

.form-divider {
	height: 2rpx;
	background: #f5f6f7;
}

/* 密码区域 */
.phone-readonly {
	font-size: 28rpx;
	color: #2c2f30;
	font-weight: 500;
	flex: 1;
	text-align: right;
}
.phone-locked-tip {
	font-size: 22rpx;
	color: #67c23a;
	margin-left: 12rpx;
}
.phone-unbind {
	font-size: 28rpx;
	color: #abadae;
	flex: 1;
	text-align: right;
}
.bind-btn {
	margin-left: 12rpx;
	padding: 8rpx 20rpx;
	background: rgba(156, 63, 0, 0.08);
	border-radius: 8rpx;
}
.bind-btn-text {
	font-size: 24rpx;
	color: #9c3f00;
	font-weight: 600;
}

.form-value-right {
	font-size: 28rpx;
	color: #abadae;
	flex: 1;
	text-align: right;
}

.form-arrow {
	font-size: 40rpx;
	color: #c0c2c3;
	margin-left: 8rpx;
	line-height: 1;
}
</style>
