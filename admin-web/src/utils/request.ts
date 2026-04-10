import axios, { type AxiosRequestConfig, type AxiosResponse } from "axios";
import { ElMessage } from "element-plus";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  requestId?: string;
}

const instance = axios.create({
  baseURL: "http://localhost:8080",
  timeout: 10000
});

instance.interceptors.request.use((config) => {
  const token = localStorage.getItem("admin_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message ?? error?.message ?? "请求失败";
    ElMessage.error(message);
    if (error?.response?.status === 401) {
      localStorage.removeItem("admin_token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

async function unwrap<T>(promise: Promise<AxiosResponse<ApiResponse<T>>>) {
  const response = await promise;
  const body = response.data;
  if (body.code !== 200) {
    ElMessage.error(body.message || "请求失败");
    if (body.code === 40101) {
      localStorage.removeItem("admin_token");
      window.location.href = "/login";
    }
    throw new Error(body.message || "请求失败");
  }
  return body.data;
}

const request = {
  get<T>(url: string, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.get<ApiResponse<T>>(url, config));
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.post<ApiResponse<T>>(url, data, config));
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
    return unwrap<T>(instance.put<ApiResponse<T>>(url, data, config));
  }
};

export default request;
