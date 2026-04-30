<template>
  <view class="page">
    <!-- 教练大图 -->
    <view class="hero-section">
      <image class="hero-img" :src="getImageUrl(coach.pic) || getImageUrl(coach.avatar) || '/static/logo.png'" mode="aspectFill" />
      <view class="hero-overlay">
        <view class="hero-nav">
          <text class="nav-back" @click="goBack">←</text>
          <text class="nav-share">⤴</text>
        </view>
        <view class="hero-badge">
          <text class="badge-text">精英教练</text>
        </view>
        <text class="hero-subtitle">COACH HERO</text>
      </view>
    </view>

    <scroll-view scroll-y class="scroll-area">
      <!-- 教练基本信息 -->
      <view class="profile-section">
        <text class="coach-name">{{ coach.name }}{{ coach.englishName ? ' ' + coach.englishName : '' }}</text>
        <view class="rating-row">
          <text class="rating-star">★</text>
          <text class="rating-num">{{ coach.rating || 5.0 }}</text>
        </view>

        <!-- 数据统计 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value">{{ coach.years || 0 }}</text>
            <text class="stat-label">教龄 (年)</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ coach.servedUserCount || 0 }}</text>
            <text class="stat-label">服务学员</text>
          </view>
        </view>
      </view>

      <!-- 个人简介 -->
      <view class="section" v-if="coach.bio">
        <view class="section-title-row">
          <text class="section-icon">📝</text>
          <text class="section-title">个人简介</text>
        </view>
        <text class="section-text">{{ coach.bio }}</text>
      </view>

      <!-- 擅长项目 -->
      <view class="section" v-if="skillList.length">
        <text class="section-title-bold">擅长项目</text>
        <view class="skill-tags">
          <view class="skill-tag" v-for="(skill, idx) in skillList" :key="idx">
            <text class="skill-icon">{{ getSkillIcon(skill) }}</text>
            <text class="skill-name">{{ getSkillName(skill) }}</text>
          </view>
        </view>
      </view>

      <!-- 认证资质 -->
      <view class="section" v-if="certList.length">
        <text class="section-title-bold">认证资质</text>
        <view class="cert-tags">
          <view class="cert-tag" v-for="(cert, idx) in certList" :key="idx">
            <text class="cert-icon">{{ getCertIcon(cert) }}</text>
            <text class="cert-text">{{ getCertName(cert) }}</text>
          </view>
        </view>
      </view>

      <!-- 购买提示 -->
      <view class="tip-bar">
        <text class="tip-icon">ℹ️</text>
        <text class="tip-text">购买私教课后，将由专人邀请进微信群安排上课</text>
      </view>

      <view style="height: 140rpx;"></view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left" @click="goConsult">
        <text class="bar-icon">💬</text>
        <text class="bar-label">立即咨询</text>
      </view>
      <view class="bar-btn" @click="goBook">
        <text class="bar-btn-icon">📅</text>
        <text class="bar-btn-text">预约课程</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app'
import { getCoachDetail } from '../../api/coach'
import config from '../../utils/config'

const coach = ref({})
const skillList = computed(() => coach.value.skills ? coach.value.skills.split(',').filter(s => s.trim()) : [])
const certList = computed(() => coach.value.certs ? coach.value.certs.split(',').filter(c => c.trim()) : [])

// 判断字符串是否以emoji开头
const hasEmoji = (str) => {
  const emojiRegex = /^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/u
  return emojiRegex.test(str.trim())
}

// 获取项目图标（如果已有emoji则提取，否则返回默认）
const getSkillIcon = (skill) => {
  const s = skill.trim()
  if (hasEmoji(s)) {
    // 提取开头的emoji
    const match = s.match(/^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{1F900}-\u{1F9FF}]+/u)
    return match ? match[0] : '⚡'
  }
  return '⚡'
}

// 获取项目名称（去掉emoji前缀）
const getSkillName = (skill) => {
  const s = skill.trim()
  if (hasEmoji(s)) {
    return s.replace(/^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{1F900}-\u{1F9FF}]+\s*/u, '')
  }
  return s
}

// 获取认证图标
const getCertIcon = (cert) => {
  const s = cert.trim()
  if (hasEmoji(s)) {
    const match = s.match(/^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{1F900}-\u{1F9FF}]+/u)
    return match ? match[0] : '🏆'
  }
  return '🏆'
}

// 获取认证名称（去掉emoji前缀）
const getCertName = (cert) => {
  const s = cert.trim()
  if (hasEmoji(s)) {
    return s.replace(/^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{1F900}-\u{1F9FF}]+\s*/u, '')
  }
  return s
}

const getImageUrl = (url) => config.getImageUrl(url)

onLoad((options) => {
  if (options.id) {
    loadCoachDetail(options.id)
  }
})

const loadCoachDetail = async (id) => {
  try {
    const res = await getCoachDetail(id)
    coach.value = res || {}
  } catch(e) {
    console.error('加载教练详情失败', e)
  }
}

const goBack = () => uni.navigateBack();
const goConsult = () => {};
const goBook = () => {
  uni.navigateTo({ url: '/pages/course/course-detail-private' });
};
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f6f7;
  display: flex;
  flex-direction: column;
}
.hero-section {
  position: relative;
  width: 100%;
  height: 640rpx;
}
.hero-img {
  width: 100%;
  height: 100%;
}
.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(transparent 40%, rgba(0,0,0,0.5));
  display: flex;
  flex-direction: column;
}
.hero-nav {
  display: flex;
  justify-content: space-between;
  padding: env(safe-area-inset-top) 32rpx 0;
  padding-top: calc(env(safe-area-inset-top) + 16rpx);
}
.nav-back { font-size: 44rpx; color: #fff; }
.nav-share { font-size: 36rpx; color: #fff; }
.hero-badge {
  margin-top: auto;
  margin-left: 32rpx;
  margin-bottom: 8rpx;
  background: #ff7a2f;
  border-radius: 8rpx;
  padding: 6rpx 20rpx;
  align-self: flex-start;
}
.badge-text { font-size: 22rpx; color: #fff; font-weight: 600; }
.hero-subtitle {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
  padding: 0 32rpx 24rpx;
  letter-spacing: 4rpx;
}

.scroll-area {
  flex: 1;
  height: 0;
  margin-top: -20rpx;
  border-radius: 24rpx 24rpx 0 0;
  background: #f5f6f7;
  position: relative;
  z-index: 1;
}

.profile-section {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx;
}
.coach-name {
  font-size: 52rpx;
  font-weight: 800;
  color: #2c2f30;
  display: block;
}
.rating-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
}
.rating-star { font-size: 28rpx; color: #ff7a2f; }
.rating-num { font-size: 28rpx; font-weight: 700; color: #ff7a2f; }
.followers { font-size: 26rpx; color: #999; margin-left: 12rpx; }

.stats-row {
  display: flex;
  margin-top: 32rpx;
  gap: 20rpx;
}
.stat-item {
  flex: 1;
  background: #f5f6f7;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.stat-value { font-size: 40rpx; font-weight: 700; color: #2c2f30; }
.stat-label { font-size: 22rpx; color: #999; }

.section {
  background: #fff;
  margin-top: 16rpx;
  padding: 32rpx;
}
.section-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.section-icon { font-size: 28rpx; }
.section-title { font-size: 30rpx; font-weight: 700; color: #2c2f30; }
.section-title-bold { font-size: 34rpx; font-weight: 800; color: #2c2f30; display: block; margin-bottom: 20rpx; }
.section-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  display: block;
  margin-bottom: 12rpx;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.section-link { font-size: 24rpx; color: #ff7a2f; }

.skill-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.skill-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #f5f6f7;
  border-radius: 32rpx;
  padding: 16rpx 28rpx;
}
.skill-icon { font-size: 28rpx; }
.skill-name { font-size: 26rpx; color: #2c2f30; }

.cert-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.cert-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #fff8f3;
  border-radius: 32rpx;
  padding: 16rpx 28rpx;
}
.cert-icon { font-size: 28rpx; }
.cert-text { font-size: 26rpx; color: #9c3f00; font-weight: 600; }

.course-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.course-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #f5f6f7;
  border-radius: 16rpx;
  padding: 20rpx;
}
.course-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
}
.course-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.course-name { font-size: 28rpx; font-weight: 600; color: #2c2f30; }
.course-desc { font-size: 22rpx; color: #999; }
.course-price { font-size: 28rpx; font-weight: 700; color: #ff7a2f; }
.course-arrow { font-size: 32rpx; color: #ccc; }

.review-card {
  background: #f5f6f7;
  border-radius: 16rpx;
  padding: 24rpx;
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
}
.reviewer-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.reviewer-avatar-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #9c3f00;
  display: flex;
  align-items: center;
  justify-content: center;
}
.reviewer-avatar-text { font-size: 28rpx; color: #fff; font-weight: 600; }
.reviewer-meta {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}
.reviewer-name { font-size: 28rpx; font-weight: 600; color: #2c2f30; }
.reviewer-stars { font-size: 22rpx; color: #ff7a2f; letter-spacing: 2rpx; }
.review-date { font-size: 22rpx; color: #999; }
.review-text { font-size: 26rpx; color: #666; line-height: 1.6; }

.tip-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 24rpx;
  margin: 16rpx 32rpx 0;
  background: #fff8f3;
  border-radius: 16rpx;
}
.tip-icon { font-size: 24rpx; }
.tip-text { font-size: 24rpx; color: #ff7a2f; }

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 110rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
  padding-bottom: env(safe-area-inset-bottom);
}
.bar-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  margin-right: 40rpx;
}
.bar-icon { font-size: 36rpx; }
.bar-label { font-size: 22rpx; color: #666; }
.bar-btn {
  flex: 1;
  height: 88rpx;
  background: linear-gradient(135deg, #ff7a2f, #ff9a5c);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}
.bar-btn-icon { font-size: 28rpx; }
.bar-btn-text { font-size: 32rpx; font-weight: 700; color: #fff; }
</style>
