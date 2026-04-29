<template>
  <view class="page">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-left">
        <text class="brand-logo">⚡</text>
        <text class="brand-name">KINETIC</text>
      </view>
    </view>

    <scroll-view scroll-y class="content-scroll">
      <!-- 教练列表 -->
      <view class="coach-list">
        <view class="coach-card" v-for="(coach, idx) in coaches" :key="idx" @click="goDetail(coach)">
          <view class="coach-main">
            <view class="avatar-wrap">
              <image class="coach-avatar" :src="coach.avatar" mode="aspectFill" />
              <view class="rating-badge">
                <text class="rating-star">★</text>
                <text class="rating-num">{{ coach.rating }}</text>
              </view>
            </view>
            <view class="coach-info">
              <view class="name-row">
                <text class="coach-name">{{ coach.name }}</text>
                <text class="coach-ename">{{ coach.ename }}</text>
              </view>
              <text class="coach-title">{{ coach.years }}年教龄</text>
              <view class="cert-tags">
                <view class="cert-tag" v-for="(cert, ci) in coach.certs" :key="ci">
                  <text class="cert-text">{{ cert }}</text>
                </view>
              </view>
              <view class="skill-row">
                <text class="skill-item" v-for="(skill, si) in coach.skills" :key="si">
                  {{ skill.icon }} {{ skill.name }}
                </text>
              </view>
              <text class="coach-desc">{{ coach.desc }}</text>
            </view>
          </view>
        </view>
      </view>

      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getCoachList } from '../../api/coach'
import config from '../../utils/config'

const coaches = ref([]);
const loading = ref(false);

const hasEmoji = (str) => {
  const emojiRegex = /^[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/u
  return emojiRegex.test(str.trim())
}

const parseSkill = (s) => {
  const str = s.trim()
  if (hasEmoji(str)) {
    const match = str.match(/^([\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}\u{1F1E0}-\u{1F1FF}\u{1F900}-\u{1F9FF}]+)\s*(.*)$/u)
    if (match) {
      return { icon: match[1], name: match[2] || str }
    }
  }
  return { icon: '⚡', name: str }
}

const loadCoaches = async () => {
  loading.value = true
  try {
    const res = await getCoachList({ pageNum: 1, pageSize: 50 })
    coaches.value = (res.records || []).map(c => ({
      id: c.id,
      name: c.name,
      ename: c.englishName || '',
      years: c.years || 0,
      certs: c.certs ? c.certs.split(',') : [],
      skills: c.skills ? c.skills.split(',').map(s => parseSkill(s)) : [],
      desc: c.bio || '',
      rating: c.rating || 5.0,
      avatar: config.getImageUrl(c.avatar) || '',
    }))
  } catch(e) {
    console.error('加载教练列表失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => loadCoaches())

const goDetail = (coach) => uni.navigateTo({ url: `/pages/coach/coach-detail?id=${coach.id}` });
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f6f7;
}

.header {
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
  background-color: #f5f6f7;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.brand-logo {
  font-size: 36rpx;
}

.brand-name {
  font-weight: 900;
  font-style: italic;
  font-size: 38rpx;
  color: #9c3f00;
  letter-spacing: -2rpx;
}

.content-scroll {
  height: calc(100vh - 88rpx - env(safe-area-inset-top));
  margin-top: calc(88rpx + env(safe-area-inset-top));
}

.coach-list {
  padding: 24rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.coach-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 28rpx;
}
.coach-main {
  display: flex;
  gap: 24rpx;
}
.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.coach-avatar {
  width: 180rpx;
  height: 200rpx;
  border-radius: 16rpx;
}
.rating-badge {
  position: absolute;
  bottom: 8rpx;
  left: 8rpx;
  background: rgba(0,0,0,0.7);
  border-radius: 12rpx;
  padding: 4rpx 12rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
}
.rating-star { font-size: 20rpx; color: #ff7a2f; }
.rating-num { font-size: 22rpx; color: #fff; font-weight: 600; }

.coach-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}
.coach-name { font-size: 34rpx; font-weight: 700; color: #2c2f30; }
.coach-ename { font-size: 24rpx; color: #999; }

.coach-title { font-size: 24rpx; color: #ff7a2f; font-weight: 600; }

.cert-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}
.cert-tag {
  background: #f5f6f7;
  border-radius: 8rpx;
  padding: 6rpx 16rpx;
}
.cert-text { font-size: 22rpx; color: #666; }

.skill-row {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}
.skill-item { font-size: 22rpx; color: #2c2f30; }

.coach-desc {
  font-size: 24rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
