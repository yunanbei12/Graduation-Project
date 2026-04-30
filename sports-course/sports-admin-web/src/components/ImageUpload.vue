<template>
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      accept="image/*"
    >
      <div v-if="modelValue" class="preview-wrap">
        <el-image :src="imageUrl" fit="cover" class="preview-img" />
        <div class="preview-mask">
          <el-icon class="mask-icon"><Refresh /></el-icon>
          <span>更换</span>
        </div>
      </div>
      <div v-else class="upload-placeholder">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <span class="upload-text">上传图片</span>
      </div>
    </el-upload>
    <div v-if="tip" class="upload-tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  tip: { type: String, default: '支持 jpg/png/gif，不超过 10MB' }
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = '/api/file/upload'
const headers = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: token } : {}
})

// 拼接完整图片URL（如果已是完整URL则直接用，否则拼接后端地址）
const imageUrl = computed(() => {
  if (!props.modelValue) return ''
  if (props.modelValue.startsWith('http')) return props.modelValue
  return props.modelValue
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const handleSuccess = (response) => {
  if (response.code === 200) {
    emit('update:modelValue', response.data)
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.image-upload :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}
.image-upload :deep(.el-upload:hover) {
  border-color: #409eff;
}
.upload-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
}
.upload-icon { font-size: 28px; }
.upload-text { font-size: 12px; }
.preview-wrap {
  width: 120px;
  height: 120px;
  position: relative;
}
.preview-img { width: 100%; height: 100%; }
.preview-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}
.preview-wrap:hover .preview-mask { opacity: 1; }
.mask-icon { font-size: 20px; }
.upload-tip { font-size: 12px; color: #999; margin-top: 4px; }
</style>
