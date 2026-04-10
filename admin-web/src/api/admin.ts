import request from "@/utils/request";
import type {
  AdminLoginRequest,
  AdminLoginResponse,
  AdminProfile,
  CoachItem,
  CoachPayload,
  CoachWorkload,
  CourseItem,
  CoursePayload,
  PageResponse,
  ScheduleItem,
  SchedulePayload
} from "@/types/admin";

export function login(data: AdminLoginRequest) {
  return request.post<AdminLoginResponse>("/api/admin/v1/auth/login", data);
}

export function fetchAdminProfile() {
  return request.get<AdminProfile>("/api/admin/v1/auth/me");
}

export function logout() {
  return request.post<void>("/api/admin/v1/auth/logout");
}

export function fetchCourses(params: Record<string, unknown>) {
  return request.get<PageResponse<CourseItem>>("/api/admin/v1/courses", { params });
}

export function createCourse(data: CoursePayload) {
  return request.post<CourseItem>("/api/admin/v1/courses", data);
}

export function updateCourse(courseId: number, data: CoursePayload) {
  return request.put<CourseItem>(`/api/admin/v1/courses/${courseId}`, data);
}

export function updateCourseStatus(courseId: number, status: number) {
  return request.post<void>(`/api/admin/v1/courses/${courseId}/status`, { status });
}

export function fetchCoaches(params: Record<string, unknown>) {
  return request.get<PageResponse<CoachItem>>("/api/admin/v1/coaches", { params });
}

export function createCoach(data: CoachPayload) {
  return request.post<CoachItem>("/api/admin/v1/coaches", data);
}

export function updateCoach(coachId: number, data: CoachPayload) {
  return request.put<CoachItem>(`/api/admin/v1/coaches/${coachId}`, data);
}

export function fetchCoachWorkload(coachId: number) {
  return request.get<CoachWorkload>(`/api/admin/v1/coaches/${coachId}/workload`);
}

export function fetchSchedules(params: Record<string, unknown>) {
  return request.get<PageResponse<ScheduleItem>>("/api/admin/v1/schedules", { params });
}

export function createSchedule(data: SchedulePayload) {
  return request.post<ScheduleItem>("/api/admin/v1/schedules", data);
}

export function cancelSchedule(scheduleId: number, reason: string) {
  return request.post<void>(`/api/admin/v1/schedules/${scheduleId}/cancel`, { reason });
}
