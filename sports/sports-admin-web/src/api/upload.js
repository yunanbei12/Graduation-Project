import http from '../utils/http'

// 上传文件
export const uploadFile = (formData) => {
  return http.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
