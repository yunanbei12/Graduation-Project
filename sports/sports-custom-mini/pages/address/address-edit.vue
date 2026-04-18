<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">{{ isEdit ? '编辑地址' : '新增地址' }}</text>
			<view style="width: 40rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view class="form-card">
				<!-- 收货人 -->
				<view class="form-row">
					<text class="form-label">收货人</text>
					<input
						class="form-input"
						type="text"
						placeholder="请输入收货人姓名"
						placeholder-class="input-ph"
						v-model="form.receiverName"
						maxlength="20"
					/>
				</view>
				<view class="form-divider"></view>

				<!-- 手机号 -->
				<view class="form-row">
					<text class="form-label">手机号</text>
					<input
						class="form-input"
						type="number"
						placeholder="请输入手机号"
						placeholder-class="input-ph"
						v-model="form.phone"
						maxlength="11"
					/>
				</view>
				<view class="form-divider"></view>

				<!-- 所在地区 -->
				<picker mode="region" :value="regionValue" @change="onRegionChange">
					<view class="form-row">
						<text class="form-label">所在地区</text>
						<view class="region-picker">
							<text v-if="regionText" class="region-text">{{ regionText }}</text>
							<text v-else class="region-placeholder">请选择省市区</text>
							<text class="arrow">›</text>
						</view>
					</view>
				</picker>
				<view class="form-divider"></view>

				<!-- 详细地址 -->
				<view class="form-row textarea-row">
					<text class="form-label">详细地址</text>
					<textarea
						class="form-textarea"
						placeholder="请输入详细地址，如街道、门牌号等"
						placeholder-class="input-ph"
						v-model="form.detailAddress"
						maxlength="100"
						:auto-height="true"
						:show-confirm-bar="false"
					/>
				</view>
				<view class="form-divider"></view>

				<!-- 设为默认 -->
				<view class="form-row default-row">
					<text class="form-label">设为默认</text>
					<switch
						:checked="form.isDefault === 1"
						@change="onDefaultChange"
						color="#9c3f00"
					/>
				</view>
			</view>

			<!-- 保存按钮 -->
			<view class="btn-area">
				<button class="save-btn" :disabled="saving" @tap="handleSave">
					<text v-if="!saving">保存地址</text>
					<text v-else>保存中...</text>
				</button>
				<button v-if="isEdit" class="delete-btn" @tap="handleDelete">
					<text>删除地址</text>
				</button>
			</view>

			<view style="height: 60rpx;"></view>
		</scroll-view>
	</view>
</template>

<script>
import { getAddressDetail, addAddress, updateAddress, deleteAddress } from '../../api/address'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			isEdit: false,
			addressId: null,
			form: {
				receiverName: '',
				phone: '',
				province: '',
				city: '',
				district: '',
				detailAddress: '',
				isDefault: 0
			},
			regionValue: [],      // picker 绑定值
			regionText: '',       // 显示文本
			saving: false
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		if (options.id) {
			this.isEdit = true
			this.addressId = options.id
			this.loadAddressDetail()
		}
	},
	methods: {
		async loadAddressDetail() {
			try {
				uni.showLoading({ title: '加载中...' })
				const res = await getAddressDetail(this.addressId)
				this.form.receiverName = res.receiverName || ''
				this.form.phone = res.phone || ''
				this.form.province = res.province || ''
				this.form.city = res.city || ''
				this.form.district = res.district || ''
				this.form.detailAddress = res.detailAddress || ''
				this.form.isDefault = res.isDefault || 0
				// 设置地区显示
				if (res.province) {
					this.regionValue = [res.province, res.city || '', res.district || '']
					this.regionText = [res.province, res.city, res.district].filter(p => p).join(' ')
				}
			} catch(e) {
				uni.showToast({ title: '加载失败', icon: 'none' })
			} finally {
				uni.hideLoading()
			}
		},

		// 地区选择变化
		onRegionChange(e) {
			const value = e.detail.value
			this.regionValue = value
			this.form.province = value[0] || ''
			this.form.city = value[1] || ''
			this.form.district = value[2] || ''
			this.regionText = value.filter(p => p).join(' ')
		},

		// 默认地址开关
		onDefaultChange(e) {
			this.form.isDefault = e.detail.value ? 1 : 0
		},

		// 表单验证
		validateForm() {
			if (!this.form.receiverName || !this.form.receiverName.trim()) {
				uni.showToast({ title: '请输入收货人姓名', icon: 'none' })
				return false
			}
			if (!this.form.phone || !/^1[3-9]\d{9}$/.test(this.form.phone)) {
				uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
				return false
			}
			if (!this.form.province || !this.form.city) {
				uni.showToast({ title: '请选择所在地区', icon: 'none' })
				return false
			}
			if (!this.form.detailAddress || !this.form.detailAddress.trim()) {
				uni.showToast({ title: '请输入详细地址', icon: 'none' })
				return false
			}
			return true
		},

		// 保存地址
		async handleSave() {
			if (!this.validateForm()) return
			this.saving = true
			uni.showLoading({ title: '保存中...' })
			try {
				const data = {
					receiverName: this.form.receiverName.trim(),
					phone: this.form.phone,
					province: this.form.province,
					city: this.form.city,
					district: this.form.district,
					detailAddress: this.form.detailAddress.trim(),
					isDefault: this.form.isDefault
				}
				if (this.isEdit) {
					data.id = this.addressId
					await updateAddress(data)
				} else {
					await addAddress(data)
				}
				uni.showToast({ title: '保存成功', icon: 'success' })
				setTimeout(() => {
					uni.navigateBack()
				}, 500)
			} catch(e) {
				uni.showToast({ title: '保存失败', icon: 'none' })
			} finally {
				this.saving = false
				uni.hideLoading()
			}
		},

		// 删除地址
		async handleDelete() {
			const res = await uni.showModal({ title: '提示', content: '确定删除该地址？' })
			if (res.confirm) {
				try {
					await deleteAddress(this.addressId)
					uni.showToast({ title: '删除成功', icon: 'success' })
					setTimeout(() => {
						uni.navigateBack()
					}, 500)
				} catch(e) {
					uni.showToast({ title: '删除失败', icon: 'none' })
				}
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

.nav-bar {
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
	background: #fff;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.nav-back {
	font-size: 36rpx;
	color: #2c2f30;
}

.nav-title {
	font-size: 32rpx;
	font-weight: 700;
	color: #2c2f30;
}

.content-scroll {
	height: calc(100vh - 88rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

/* 表单卡片 */
.form-card {
	margin: 24rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 0 28rpx;
	overflow: hidden;
}

.form-row {
	display: flex;
	align-items: center;
	padding: 28rpx 0;
	min-height: 48rpx;
}

.form-label {
	font-size: 28rpx;
	color: #2c2f30;
	font-weight: 500;
	width: 160rpx;
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

/* 地区选择 */
.region-picker {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: flex-end;
}

.region-text {
	font-size: 28rpx;
	color: #2c2f30;
}

.region-placeholder {
	font-size: 28rpx;
	color: #c0c2c3;
}

.arrow {
	font-size: 32rpx;
	color: #c0c2c3;
	margin-left: 8rpx;
}

/* 详细地址 */
.textarea-row {
	align-items: flex-start;
}

.form-textarea {
	flex: 1;
	font-size: 28rpx;
	color: #2c2f30;
	min-height: 80rpx;
	text-align: right;
}

/* 默认地址 */
.default-row {
	justify-content: space-between;
}

.default-row .form-label {
	flex: 1;
}

/* 按钮区域 */
.btn-area {
	padding: 40rpx 24rpx;
}

.save-btn {
	width: 100%;
	height: 88rpx;
	background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	color: #fff;
	font-size: 30rpx;
	font-weight: 700;
	border-radius: 48rpx;
	border: none;
	display: flex;
	align-items: center;
	justify-content: center;
}

.save-btn[disabled] {
	opacity: 0.6;
}

.delete-btn {
	width: 100%;
	height: 88rpx;
	background: #fff;
	color: #e74c3c;
	font-size: 28rpx;
	font-weight: 600;
	border-radius: 48rpx;
	border: 2rpx solid #e74c3c;
	margin-top: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}
</style>
