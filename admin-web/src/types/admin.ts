export interface PageResponse<T> {
  list: T[];
  pageNo: number;
  pageSize: number;
  total: number;
}

export interface AdminLoginRequest {
  username: string;
  password: string;
}

export interface AdminLoginResponse {
  token: string;
  adminId: number;
  username: string;
  realName: string;
  roles: string[];
}

export interface AdminProfile {
  adminId: number;
  username: string;
  realName: string;
  phone: string | null;
  email: string | null;
  status: number;
  roles: string[];
}

export interface CourseItem {
  courseId: number;
  courseName: string;
  courseCode: string | null;
  courseType: number;
  sportType: string;
  coverUrl: string | null;
  description: string | null;
  detailImages: string[];
  price: number;
  originalPrice: number | null;
  lessonCount: number | null;
  validityDays: number | null;
  isDoorToDoor: number;
  serviceAreas: string[];
  fixedScheduleDesc: string | null;
  fixedLocation: string | null;
  maxParticipants: number | null;
  groupSuccessCount: number | null;
  operationWeight: number;
  status: number;
}

export interface CoursePayload {
  courseName: string;
  courseCode?: string;
  courseType: number;
  sportType: string;
  coverUrl?: string;
  description?: string;
  detailImages?: string[];
  price: number;
  originalPrice?: number | null;
  lessonCount?: number | null;
  validityDays?: number | null;
  isDoorToDoor?: number;
  serviceAreas?: string[];
  fixedScheduleDesc?: string;
  fixedLocation?: string;
  maxParticipants?: number | null;
  groupSuccessCount?: number | null;
  operationWeight?: number;
  status?: number;
}

export interface CoachItem {
  coachId: number;
  coachName: string;
  phone: string | null;
  idCardNo: string | null;
  gender: number | null;
  sportItems: string[];
  introduction: string | null;
  hourlyRate: number | null;
  availableTimes: string[];
  status: number;
}

export interface CoachPayload {
  coachName: string;
  phone?: string;
  idCardNo?: string;
  gender?: number | null;
  sportItems?: string[];
  introduction?: string;
  hourlyRate?: number | null;
  availableTimes?: string[];
  status?: number;
}

export interface CoachWorkload {
  coachId: number;
  totalSchedules: number;
  upcomingSchedules: number;
  completedSchedules: number;
  cancelledSchedules: number;
}

export interface ScheduleItem {
  scheduleId: number;
  courseId: number;
  courseName: string | null;
  coachId: number;
  coachName: string | null;
  scheduleDate: string;
  startTime: string;
  endTime: string;
  location: string;
  capacity: number;
  minGroupCount: number;
  enrolledCount: number;
  waitlistCount: number;
  status: number;
  cancelReason: string | null;
}

export interface SchedulePayload {
  courseId: number;
  coachId: number;
  scheduleDate: string;
  startTime: string;
  endTime: string;
  location: string;
  capacity: number;
  minGroupCount: number;
}
