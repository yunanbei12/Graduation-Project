<template>
	<view class="page">
		<view class="nav-bar">
			<text class="nav-back" @tap="goBack">←</text>
			<text class="nav-title">收货地址</text>
			<view style="width: 40rpx;"></view>
		</view>

		<scroll-view scroll-y class="content-scroll">
			<view v-if="addressList.length === 0" class="empty-area">
				<text class="empty-icon">📭</text>
				<text class="empty-text">暂无收货地址</text>
				<view class="empty-btn" @tap="goAdd">
					<text class="empty-btn-text">添加地址</text>
				</view>
			</view>

			<view v-else class="address-list">
				<view class="address-item" v-for="addr in addressList" :key="addr.id" @tap="selectAddress(addr)">
					<view class="addr-main">
						<view class="addr-header">
							<text class="addr-name">{{ addr.receiverName }}</text>
							<text class="addr-phone">{{ addr.phone }}</text>
							<view class="default-tag" v-if="addr.isDefault === 1">
								<text class="default-tag-text">默认</text>
							</view>
						</view>
						<text class="addr-detail">{{ formatAddress(addr) }}</text>
					</view>
					<view class="addr-actions" @tap.stop>
						<view class="action-btn" @tap="setDefault(addr)" v-if="addr.isDefault !== 1">
							<text class="action-text">设为默认</text>
						</view>
						<view class="action-btn" @tap="goEdit(addr.id)">
							<text class="action-text">编辑</text>
						</view>
						<view class="action-btn danger" @tap="handleDelete(addr.id)">
							<text class="action-text">删除</text>
						</view>
					</view>
				</view>
			</view>

			<view style="height: 140rpx;"></view>
		</scroll-view>

		<view class="bottom-bar">
			<view class="add-btn" @tap="goAdd">
				<text class="add-btn-text">+ 新增收货地址</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getAddressList, setDefaultAddress, deleteAddress } from '../../api/address'
import { checkLogin } from '../../utils/auth'

export default {
	data() {
		return {
			addressList: [],
			// 是否是选择模式（从确认订单页进入）
			selectMode: false
		}
	},
	onLoad(options) {
		if (!checkLogin()) return
		this.selectMode = options.select === '1'
	},
	onShow() {
		this.loadAddressList()
	},
	methods: {
		async loadAddressList() {
			try {
				this.addressList = await getAddressList()
			} catch(e) {
				console.error('加载地址失败', e)
			}
		},
		formatAddress(addr) {
			const parts = [addr.province, addr.city, addr.district, addr.detailAddress].filter(p => p)
			return parts.join(' ')
		},
		selectAddress(addr) {
			if (this.selectMode) {
				// 选择模式：通过 eventChannel 返回选中的地址
				const eventChannel = this.getOpenerEventChannel()
				if (eventChannel) {
					eventChannel.emit('selectAddress', addr)
				}
				uni.navigateBack()
			}
		},
		async setDefault(addr) {
			try {
				await setDefaultAddress(addr.id)
				uni.showToast({ title: '已设为默认', icon: 'success' })
				this.loadAddressList()
			} catch(e) {
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		},
		goEdit(id) {
			uni.navigateTo({ url: `/pages/address/address-edit?id=${id}` })
		},
		goAdd() {
			uni.navigateTo({ url: '/pages/address/address-edit' })
		},
		async handleDelete(id) {
			const res = await uni.showModal({ title: '提示', content: '确定删除该地址？' })
			if (res.confirm) {
				try {
					await deleteAddress(id)
					uni.showToast({ title: '删除成功', icon: 'success' })
					this.loadAddressList()
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
.page { min-height: 100vh; background: #f5f6f7; }
.nav-bar {
	position: fixed; top: 0; left: 0; right: 0; z-index: 99;
	display: flex; align-items: center; justify-content: space-between;
	padding: 0 24rpx; height: calc(88rpx + env(safe-area-inset-top));
	padding-top: env(safe-area-inset-top); background: #fff;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.nav-back { font-size: 36rpx; color: #2c2f30; }
.nav-title { font-size: 32rpx; font-weight: 700; color: #2c2f30; }
.content-scroll {
	height: calc(100vh - 88rpx - 120rpx - env(safe-area-inset-top));
	margin-top: calc(88rpx + env(safe-area-inset-top));
}

.empty-area {
	display: flex; flex-direction: column; align-items: center;
	justify-content: center; padding: 120rpx 0;
}
.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 28rpx; color: #999; margin-bottom: 32rpx; }
.empty-btn {
	padding: 20rpx 48rpx; background: linear-gradient(135deg, #9c3f00, #ff7a2f);
	border-radius: 40rpx;
}
.empty-btn-text { font-size: 28rpx; color: #fff; font-weight: 700; }

.address-list { padding: 20rpx 24rpx; }
.address-item {
	background: #fff; border-radius: 20rpx; padding: 24rpx; margin-bottom: 16rpx;
}
.addr-main { margin-bottom: 16rpx; }
.addr-header {
	display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx;
}
.addr-name { font-size: 30rpx; font-weight: 700; color: #2c2f30; }
.addr-phone { font-size: 26rpx; color: #595c5d; }
.default-tag {
	background: rgba(156, 63, 0, 0.1); padding: 4rpx 12rpx; border-radius: 8rpx;
}
.default-tag-text { font-size: 20rpx; color: #9c3f00; font-weight: 700; }
.addr-detail { font-size: 26rpx; color: #595c5d; line-height: 1.5; }

.addr-actions { display: flex; gap: 24rpx; padding-top: 16rpx; border-top: 1rpx solid #f0f0f0; }
.action-btn { padding: 8rpx 0; }
.action-text { font-size: 24rpx; color: #595c5d; }
.action-btn.danger .action-text { color: #e74c3c; }

.bottom-bar {
	position: fixed; bottom: 0; left: 0; right: 0; z-index: 99;
	padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
	background: #fff; box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04);
}
.add-btn {
	background: linear-gradient(135deg, #9c3f00, #ff7a2f); border-radius: 48rpx;
	padding: 24rpx 0; text-align: center;
}
.add-btn-text { font-size: 30rpx; color: #fff; font-weight: 700; }
</style>
