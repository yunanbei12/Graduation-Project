<template>
	<view class="page">
		<view class="hero">
			<text class="title">体育教培机构管理系统</text>
			<text class="subtitle">小程序端正式开发将基于这个 HBuilderX 工程继续推进</text>
		</view>

		<view class="notice">
			<text class="notice-title">当前阶段</text>
			<text class="notice-desc">首页骨架已接管成功，后续我会继续在这里实现登录、课程列表、课程详情和个人中心。</text>
		</view>

		<view class="card" v-for="item in sections" :key="item.title">
			<text class="card-title">{{ item.title }}</text>
			<text class="card-desc">{{ item.desc }}</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				sections: [
					{
						title: '推荐课程',
						desc: '后续在这里展示课程推荐、热门课程和课程入口。'
					},
					{
						title: '热门拼团',
						desc: '后续在这里展示拼团活动、倒计时和参团入口。'
					},
					{
						title: '精彩瞬间',
						desc: '后续在这里展示机构动态、学员风采和活动内容。'
					}
				]
			}
		}
	}
</script>

<style>
	.page {
		min-height: 100vh;
		padding: 24rpx;
		background: #f6f7fb;
	}

	.hero {
		padding: 36rpx 28rpx;
		margin-bottom: 24rpx;
		border-radius: 24rpx;
		color: #ffffff;
		background: linear-gradient(135deg, #ff7a18, #ff9f43);
	}

	.title {
		display: block;
		font-size: 36rpx;
		font-weight: 700;
	}

	.subtitle {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		line-height: 1.6;
		opacity: 0.92;
	}

	.notice {
		padding: 24rpx 28rpx;
		margin-bottom: 20rpx;
		border-radius: 20rpx;
		background: linear-gradient(135deg, #fff9ef, #fff1dc);
	}

	.notice-title {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: #7a4c00;
	}

	.notice-desc {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		line-height: 1.6;
		color: #9b6c14;
	}

	.card {
		padding: 28rpx;
		margin-bottom: 20rpx;
		border-radius: 20rpx;
		background: #ffffff;
		box-shadow: 0 8rpx 24rpx rgba(32, 52, 89, 0.06);
	}

	.card-title {
		display: block;
		font-size: 30rpx;
		font-weight: 600;
		color: #222222;
	}

	.card-desc {
		display: block;
		margin-top: 10rpx;
		font-size: 24rpx;
		line-height: 1.6;
		color: #666666;
	}
</style>
