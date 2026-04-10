const BASE_URL = 'http://localhost:8080'

export function request(options) {
  return uni.request({
    url: `${BASE_URL}${options.url}`,
    method: options.method || 'GET',
    data: options.data,
    header: {
      'Content-Type': 'application/json',
      ...(options.header || {})
    }
  })
}
