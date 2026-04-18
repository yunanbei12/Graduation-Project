import http from '../utils/http'

// 私教课消课
export const privateCheckin = (data) => http.post('/checkin/private', data)

// 消课记录列表
export const getCheckinList = (params) => http.get('/checkin/list', { params })

// 用户课包列表
export const getPackageList = (params) => http.get('/checkin/package/list', { params })

// 标记缺勤
export const markAbsent = (id) => http.put(`/checkin/absent/${id}`)
