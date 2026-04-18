<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style lang="scss">
	/* 全局公共样式 */
	page {
		background-color: #f5f6f7;
		color: #2c2f30;
		font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
		-webkit-font-smoothing: antialiased;
	}

	/* 设计系统色彩工具类 */
	.text-primary { color: #9c3f00; }
	.text-primary-container { color: #ff7a2f; }
	.text-on-primary { color: #fff0ea; }
	.text-on-surface { color: #2c2f30; }
	.text-on-surface-variant { color: #595c5d; }
	.text-outline { color: #757778; }
	.text-outline-variant { color: #abadae; }
	.text-error { color: #b02500; }
	.text-white { color: #ffffff; }

	.bg-primary { background-color: #9c3f00; }
	.bg-primary-container { background-color: #ff7a2f; }
	.bg-surface { background-color: #f5f6f7; }
	.bg-surface-low { background-color: #eff1f2; }
	.bg-surface-lowest { background-color: #ffffff; }
	.bg-surface-high { background-color: #e0e3e4; }
	.bg-surface-container { background-color: #e6e8ea; }
	.bg-secondary-container { background-color: #e2e2e5; }

	/* 渐变按钮 */
	.btn-gradient {
		background: linear-gradient(135deg, #9c3f00 0%, #ff7a2f 100%);
		color: #ffffff;
		border: none;
		border-radius: 24rpx;
		font-weight: 700;
		text-align: center;
	}

	.btn-primary {
		background-color: #9c3f00;
		color: #fff0ea;
		border: none;
		border-radius: 24rpx;
		font-weight: 700;
		text-align: center;
	}

	.btn-secondary {
		background-color: #e0e3e4;
		color: #2c2f30;
		border: none;
		border-radius: 24rpx;
		font-weight: 700;
		text-align: center;
	}

	.btn-outline {
		background-color: transparent;
		color: #9c3f00;
		border: 2rpx solid rgba(156, 63, 0, 0.2);
		border-radius: 24rpx;
		font-weight: 700;
		text-align: center;
	}

	/* 卡片样式 */
	.card {
		background-color: #ffffff;
		border-radius: 24rpx;
		overflow: hidden;
	}

	.card-shadow {
		box-shadow: 0 12rpx 24rpx rgba(44, 47, 48, 0.06);
	}

	/* 导航栏文字 */
	.nav-title {
		font-weight: 700;
		font-size: 36rpx;
		color: #9c3f00;
	}

	/* 品牌标题 */
	.brand-title {
		font-weight: 900;
		font-style: italic;
		font-size: 40rpx;
		color: #9c3f00;
		letter-spacing: -2rpx;
		text-transform: uppercase;
	}

	/* 价格样式 */
	.price-text {
		color: #9c3f00;
		font-weight: 900;
		letter-spacing: -2rpx;
	}

	/* 标签样式 */
	.tag-hot {
		background-color: rgba(156, 63, 0, 0.1);
		color: #9c3f00;
		font-size: 20rpx;
		font-weight: 700;
		padding: 4rpx 12rpx;
		border-radius: 8rpx;
	}

	.tag-primary {
		background-color: rgba(255, 122, 47, 0.1);
		color: #9c3f00;
		font-size: 20rpx;
		font-weight: 700;
		padding: 4rpx 12rpx;
		border-radius: 8rpx;
	}

	/* flex 工具类 */
	.flex { display: flex; }
	.flex-col { flex-direction: column; }
	.flex-row { flex-direction: row; }
	.flex-wrap { flex-wrap: wrap; }
	.flex-1 { flex: 1; }
	.items-center { align-items: center; }
	.items-start { align-items: flex-start; }
	.items-end { align-items: flex-end; }
	.justify-center { justify-content: center; }
	.justify-between { justify-content: space-between; }
	.justify-around { justify-content: space-around; }
	.gap-sm { gap: 8rpx; }
	.gap-md { gap: 16rpx; }
	.gap-lg { gap: 24rpx; }
	.gap-xl { gap: 32rpx; }

	/* 文字工具类 */
	.text-xs { font-size: 20rpx; }
	.text-sm { font-size: 24rpx; }
	.text-base { font-size: 28rpx; }
	.text-lg { font-size: 32rpx; }
	.text-xl { font-size: 36rpx; }
	.text-2xl { font-size: 40rpx; }
	.text-3xl { font-size: 48rpx; }
	.text-4xl { font-size: 56rpx; }
	.font-medium { font-weight: 500; }
	.font-bold { font-weight: 700; }
	.font-black { font-weight: 900; }
	.text-center { text-align: center; }
	.text-right { text-align: right; }
	.line-through { text-decoration: line-through; }
	.line-clamp-1 { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
	.line-clamp-2 { overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }

	/* 间距工具 */
	.p-sm { padding: 16rpx; }
	.p-md { padding: 24rpx; }
	.p-lg { padding: 32rpx; }
	.px-md { padding-left: 24rpx; padding-right: 24rpx; }
	.px-lg { padding-left: 32rpx; padding-right: 32rpx; }
	.py-sm { padding-top: 12rpx; padding-bottom: 12rpx; }
	.py-md { padding-top: 24rpx; padding-bottom: 24rpx; }
	.mt-sm { margin-top: 16rpx; }
	.mt-md { margin-top: 24rpx; }
	.mt-lg { margin-top: 32rpx; }
	.mb-sm { margin-bottom: 16rpx; }
	.mb-md { margin-bottom: 24rpx; }
	.mb-lg { margin-bottom: 32rpx; }

	/* 圆角 */
	.rounded-sm { border-radius: 8rpx; }
	.rounded-md { border-radius: 16rpx; }
	.rounded-lg { border-radius: 24rpx; }
	.rounded-xl { border-radius: 32rpx; }
	.rounded-full { border-radius: 50%; }

	/* 安全底部间距 */
	.safe-bottom {
		padding-bottom: calc(env(safe-area-inset-bottom) + 20rpx);
	}

	/* 分割用留白 */
	.section-gap {
		margin-top: 24rpx;
	}
</style>
