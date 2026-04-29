import http from '../utils/http'

export const getAiSessions = (params) => http.get('/admin/ai/session/list', { params })

export const getAiSessionDetail = (id) => http.get(`/admin/ai/session/${id}`)

export const updateAiSessionStatus = (id, data) => http.put(`/admin/ai/session/${id}/status`, data)

export const replyAiSession = (id, data) => http.post(`/admin/ai/session/${id}/reply`, data)

export const terminateAiSession = (id, data) => http.post(`/admin/ai/session/${id}/terminate`, data)

export const getAiHandovers = (params) => http.get('/admin/ai/handover/list', { params })

export const getAiKnowledges = (params) => http.get('/admin/ai/knowledge/list', { params })

export const createAiKnowledge = (data) => http.post('/admin/ai/knowledge', data)

export const updateAiKnowledge = (data) => http.put('/admin/ai/knowledge', data)

export const deleteAiKnowledge = (id) => http.delete(`/admin/ai/knowledge/${id}`)

export const getAiStatsSummary = () => http.get('/admin/ai/stats/summary')
