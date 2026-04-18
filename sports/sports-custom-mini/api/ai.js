import http from '../utils/http'

export const chatWithAi = (data) => http.post('/ai/chat', data)

export const getAiSessionDetail = (id) => http.get(`/ai/session/${id}`)

export const submitAiFeedback = (sessionId, data) => http.post(`/ai/session/${sessionId}/feedback`, data)

export const requestAiHandover = (sessionId, data) => http.post(`/ai/session/${sessionId}/handover`, data || {})
