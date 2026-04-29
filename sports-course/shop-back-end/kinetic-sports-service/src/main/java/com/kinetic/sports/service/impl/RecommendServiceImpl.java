package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.CourseCheckin;
import com.kinetic.sports.bean.model.CourseSchedule;
import com.kinetic.sports.bean.model.Order;
import com.kinetic.sports.bean.model.UserBehavior;
import com.kinetic.sports.bean.vo.recommend.RecommendHomeVO;
import com.kinetic.sports.bean.vo.recommend.RecommendItemVO;
import com.kinetic.sports.service.CourseCheckinService;
import com.kinetic.sports.service.CourseScheduleService;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.OrderService;
import com.kinetic.sports.service.RecommendService;
import com.kinetic.sports.service.UserBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final int ACTIVE_STATUS = 1;
    private static final int COURSE_ORDER_TYPE = 1;
    private static final String VIEW_DETAIL = "view_detail";
    private static final String RECOMMEND_CLICK = "recommend_click";
    private static final Set<Integer> POSITIVE_ORDER_STATUS = Set.of(2, 3, 4);
    private static final DateTimeFormatter NEXT_SCHEDULE_FORMATTER = DateTimeFormatter.ofPattern("M/d HH:mm");

    private final CourseService courseService;
    private final OrderService orderService;
    private final CourseCheckinService courseCheckinService;
    private final CourseScheduleService courseScheduleService;
    private final UserBehaviorService userBehaviorService;

    @Override
    public RecommendHomeVO recommendHome(Long userId, Integer courseLimit) {
        UserProfile profile = buildUserProfile(userId);
        RecommendHomeVO vo = new RecommendHomeVO();
        vo.setCourseList(recommendCourses(profile, null, null, null, courseLimit));
        return vo;
    }

    @Override
    public List<RecommendItemVO> recommendCourses(Long userId, Integer type, Long categoryId, Long currentCourseId, Integer limit) {
        return recommendCourses(buildUserProfile(userId), type, categoryId, currentCourseId, limit);
    }

    private List<RecommendItemVO> recommendCourses(UserProfile profile, Integer type, Long categoryId, Long currentCourseId, Integer limit) {
        int safeLimit = normalizeLimit(limit, 6);
        List<Course> courses = courseService.list(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, ACTIVE_STATUS)
                .eq(type != null, Course::getType, type)
                .eq(categoryId != null, Course::getCategoryId, categoryId)
                .ne(currentCourseId != null, Course::getId, currentCourseId));
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        Course currentCourse = currentCourseId == null ? null : courseService.getById(currentCourseId);
        Map<Long, Course> courseMap = listToMap(courses, Course::getId);
        Map<Long, CourseAvailability> availabilityMap = buildCourseAvailabilityMap(courseMap);
        List<Course> availableCourses = courses.stream()
                .filter(course -> !Objects.equals(course.getType(), 2)
                        || hasAvailableGroupSchedule(availabilityMap.get(course.getId())))
                .collect(Collectors.toList());
        if (availableCourses.isEmpty()) {
            return Collections.emptyList();
        }

        int maxSales = availableCourses.stream()
                .map(Course::getSales)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        List<CandidateScore<Course>> scored = new ArrayList<>();
        double maxBehavior = 0D;
        for (Course course : availableCourses) {
            CandidateScore<Course> candidate = new CandidateScore<>();
            candidate.item = course;
            candidate.behaviorRaw = courseBehaviorRaw(course, profile);
            candidate.contentScore = clamp(courseContentScore(course, currentCourse, profile));
            candidate.popularityScore = normalizedSales(course.getSales(), maxSales);
            candidate.availabilityScore = clamp(courseAvailabilityScore(course, availabilityMap.get(course.getId())));
            candidate.reason = buildCourseReason(course, currentCourse, profile, availabilityMap.get(course.getId()));
            maxBehavior = Math.max(maxBehavior, candidate.behaviorRaw);
            scored.add(candidate);
        }

        for (CandidateScore<Course> candidate : scored) {
            double behaviorScore = maxBehavior <= 0 ? 0D : candidate.behaviorRaw / maxBehavior;
            if (currentCourse != null) {
                candidate.finalScore = 0.20D * behaviorScore
                        + 0.50D * candidate.contentScore
                        + 0.15D * candidate.popularityScore
                        + 0.15D * candidate.availabilityScore;
            } else {
                candidate.finalScore = 0.45D * behaviorScore
                        + 0.25D * candidate.contentScore
                        + 0.20D * candidate.popularityScore
                        + 0.10D * candidate.availabilityScore;
            }
        }

        scored.sort(Comparator
                .comparingDouble((CandidateScore<Course> it) -> it.finalScore).reversed()
                .thenComparing(it -> Optional.ofNullable(it.item.getSales()).orElse(0), Comparator.reverseOrder())
                .thenComparing(it -> it.item.getId(), Comparator.reverseOrder()));

        return scored.stream()
                .limit(safeLimit)
                .map(it -> toCourseVO(it.item, it.reason, availabilityMap.get(it.item.getId())))
                .collect(Collectors.toList());
    }

    private UserProfile buildUserProfile(Long userId) {
        UserProfile profile = new UserProfile();
        if (userId == null) {
            return profile;
        }

        List<Order> orders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getOrderType, COURSE_ORDER_TYPE)
                .in(Order::getStatus, POSITIVE_ORDER_STATUS)
                .orderByDesc(Order::getCreateTime));
        if (!orders.isEmpty()) {
            List<Long> courseIds = orders.stream()
                    .map(Order::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, Course> courseMap = listToMap(safeListCoursesByIds(courseIds), Course::getId);

            for (Order order : orders) {
                Course course = courseMap.get(order.getCourseId());
                if (course == null) {
                    continue;
                }
                double weight = recencyWeight(order.getCreateTime(), 5D);
                profile.addCourseItemWeight(course.getId(), weight);
                profile.addCourseCategoryWeight(course.getCategoryId(), weight * 0.9D);
                profile.addCourseTypeWeight(course.getType(), weight * 0.6D);
                profile.addCourseCoachWeight(course.getCoachId(), weight * 0.5D);
                profile.addCoursePrice(course.getPrice(), weight);
            }
        }

        List<CourseCheckin> checkins = courseCheckinService.list(new LambdaQueryWrapper<CourseCheckin>()
                .eq(CourseCheckin::getUserId, userId)
                .eq(CourseCheckin::getStatus, 1)
                .orderByDesc(CourseCheckin::getCheckinTime));
        if (!checkins.isEmpty()) {
            List<Long> courseIds = checkins.stream()
                    .map(CourseCheckin::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, Course> courseMap = listToMap(safeListCoursesByIds(courseIds), Course::getId);

            for (CourseCheckin checkin : checkins) {
                Course course = courseMap.get(checkin.getCourseId());
                if (course == null) {
                    continue;
                }
                double weight = recencyWeight(checkin.getCheckinTime(), 4D);
                profile.addCourseItemWeight(course.getId(), weight);
                profile.addCourseCategoryWeight(course.getCategoryId(), weight * 0.8D);
                profile.addCourseTypeWeight(course.getType(), weight * 0.6D);
                profile.addCourseCoachWeight(course.getCoachId(), weight * 0.5D);
                profile.addCoursePrice(course.getPrice(), weight);
            }
        }

        List<UserBehavior> behaviors = userBehaviorService.list(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getItemType, COURSE_ORDER_TYPE)
                .orderByDesc(UserBehavior::getCreateTime)
                .last("limit 200"));
        if (!behaviors.isEmpty()) {
            List<Long> courseIds = behaviors.stream()
                    .map(UserBehavior::getItemId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, Course> courseMap = listToMap(safeListCoursesByIds(courseIds), Course::getId);

            for (UserBehavior behavior : behaviors) {
                Course course = courseMap.get(behavior.getItemId());
                if (course == null) {
                    continue;
                }
                double weight = behaviorWeight(behavior);
                profile.addCourseItemWeight(course.getId(), weight);
                profile.addCourseCategoryWeight(course.getCategoryId(), weight * 0.8D);
                profile.addCourseTypeWeight(course.getType(), weight * 0.5D);
                profile.addCourseCoachWeight(course.getCoachId(), weight * 0.4D);
                profile.addCoursePrice(course.getPrice(), weight * 0.8D);
            }
        }

        return profile;
    }

    private Map<Long, CourseAvailability> buildCourseAvailabilityMap(Map<Long, Course> courseMap) {
        if (courseMap == null || courseMap.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime now = LocalDateTime.now();
        List<CourseSchedule> schedules = courseScheduleService.list(new LambdaQueryWrapper<CourseSchedule>()
                .in(CourseSchedule::getCourseId, courseMap.keySet())
                .in(CourseSchedule::getStatus, 0, 1)
                .orderByAsc(CourseSchedule::getStartTime));

        Map<Long, CourseAvailability> availabilityMap = new HashMap<>();
        for (CourseSchedule schedule : schedules) {
            Course course = courseMap.get(schedule.getCourseId());
            LocalDateTime scheduleStart = resolveScheduleStart(schedule, course);
            if (scheduleStart == null || !scheduleStart.isAfter(now)) {
                continue;
            }

            int totalSeats = Optional.ofNullable(schedule.getTotalSeats()).orElse(0);
            int enrolledSeats = Optional.ofNullable(schedule.getEnrolledSeats()).orElse(0);
            int availableSeats = Math.max(totalSeats - enrolledSeats, 0);
            if (totalSeats <= 0 || availableSeats <= 0) {
                continue;
            }

            CourseAvailability availability = availabilityMap.computeIfAbsent(schedule.getCourseId(), key -> new CourseAvailability());
            availability.availableCount++;
            availability.totalSeatRatio += (double) availableSeats / totalSeats;
            if (availability.nextStartTime == null || scheduleStart.isBefore(availability.nextStartTime)) {
                availability.nextStartTime = scheduleStart;
            }
        }
        return availabilityMap;
    }

    private double courseBehaviorRaw(Course course, UserProfile profile) {
        double raw = 0D;
        raw += profile.courseItemWeights.getOrDefault(course.getId(), 0D) * 1.2D;
        raw += profile.courseCategoryWeights.getOrDefault(course.getCategoryId(), 0D) * 0.7D;
        raw += profile.courseTypeWeights.getOrDefault(course.getType(), 0D) * 0.4D;
        raw += profile.courseCoachWeights.getOrDefault(course.getCoachId(), 0D) * 0.35D;
        raw += priceAffinity(profile.avgCoursePrice(), course.getPrice()) * 1.1D;
        return raw;
    }

    private double courseContentScore(Course course, Course currentCourse, UserProfile profile) {
        double score = 0D;
        if (currentCourse != null) {
            if (Objects.equals(currentCourse.getCategoryId(), course.getCategoryId())) {
                score += 0.5D;
            }
            if (Objects.equals(currentCourse.getType(), course.getType())) {
                score += 0.2D;
            }
            if (Objects.equals(currentCourse.getCoachId(), course.getCoachId())) {
                score += 0.15D;
            }
            score += priceSimilarity(currentCourse.getPrice(), course.getPrice()) * 0.15D;
            return score;
        }

        if (Objects.equals(profile.topCourseCategory(), course.getCategoryId())) {
            score += 0.55D;
        }
        if (Objects.equals(profile.topCourseType(), course.getType())) {
            score += 0.2D;
        }
        if (Objects.equals(profile.topCourseCoach(), course.getCoachId())) {
            score += 0.1D;
        }
        score += priceAffinity(profile.avgCoursePrice(), course.getPrice()) * 0.15D;
        return score;
    }

    private double courseAvailabilityScore(Course course, CourseAvailability availability) {
        if (!Objects.equals(course.getType(), 2)) {
            return 1D;
        }
        if (availability == null || availability.availableCount <= 0) {
            return 0.15D;
        }
        double avgSeatRatio = availability.totalSeatRatio / availability.availableCount;
        return 0.6D + 0.4D * clamp(avgSeatRatio);
    }

    private String buildCourseReason(Course course, Course currentCourse, UserProfile profile, CourseAvailability availability) {
        if (currentCourse != null) {
            if (Objects.equals(currentCourse.getCoachId(), course.getCoachId()) && course.getCoachId() != null) {
                return "与你当前查看的同教练课程";
            }
            if (Objects.equals(currentCourse.getCategoryId(), course.getCategoryId()) && course.getCategoryId() != null) {
                return availability != null && availability.availableCount > 0 && Objects.equals(course.getType(), 2)
                        ? "与你当前查看的同类课程，且近期可约"
                        : "与你当前查看的同类课程";
            }
            return "与你当前查看的课程相似";
        }

        if (Objects.equals(profile.topCourseCategory(), course.getCategoryId()) && course.getCategoryId() != null) {
            return "根据你近期课程偏好推荐";
        }
        if (Objects.equals(profile.topCourseCoach(), course.getCoachId()) && course.getCoachId() != null) {
            return "根据你常选教练推荐";
        }
        if (Objects.equals(course.getType(), 2) && availability != null && availability.availableCount > 0) {
            return "近期可预约的热门团课";
        }
        return "当前热门课程";
    }

    private boolean hasAvailableGroupSchedule(CourseAvailability availability) {
        return availability != null && availability.availableCount > 0;
    }

    private RecommendItemVO toCourseVO(Course course, String reason, CourseAvailability availability) {
        RecommendItemVO vo = new RecommendItemVO();
        vo.setId(course.getId());
        vo.setBizType("course");
        vo.setName(course.getName());
        vo.setPic(course.getPic());
        vo.setPrice(course.getPrice());
        vo.setOriginalPrice(course.getOriginalPrice());
        vo.setSales(course.getSales());
        vo.setCategoryId(course.getCategoryId());
        vo.setReason(reason);
        vo.setCourseType(course.getType());
        vo.setLessonCount(course.getLessonCount());
        vo.setMinGroupSize(course.getMinGroupSize());
        vo.setAvailableScheduleCount(availability == null ? 0 : availability.availableCount);
        vo.setNextScheduleText(availability == null || availability.nextStartTime == null
                ? ""
                : "最近场次 " + availability.nextStartTime.format(NEXT_SCHEDULE_FORMATTER));
        return vo;
    }

    private int normalizeLimit(Integer limit, int defaultValue) {
        if (limit == null || limit <= 0) {
            return defaultValue;
        }
        return Math.min(limit, 20);
    }

    private double normalizedSales(Integer sales, int maxSales) {
        if (sales == null || sales <= 0 || maxSales <= 0) {
            return 0D;
        }
        return clamp((double) sales / maxSales);
    }

    private double recencyWeight(LocalDateTime time, double baseWeight) {
        if (time == null) {
            return baseWeight * 0.6D;
        }
        long days = Math.max(0L, java.time.Duration.between(time, LocalDateTime.now()).toDays());
        double decay = Math.exp(-days / 30D);
        return baseWeight * Math.max(0.3D, decay);
    }

    private double behaviorWeight(UserBehavior behavior) {
        double baseWeight = RECOMMEND_CLICK.equals(behavior.getBehaviorType()) ? 2.2D : 1.4D;
        return recencyWeight(behavior.getCreateTime(), baseWeight);
    }

    private double priceAffinity(BigDecimal avgPrice, BigDecimal currentPrice) {
        if (avgPrice == null || currentPrice == null || avgPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0D;
        }
        BigDecimal diff = avgPrice.subtract(currentPrice).abs();
        BigDecimal ratio = diff.divide(avgPrice, 4, RoundingMode.HALF_UP);
        return clamp(1D - ratio.doubleValue());
    }

    private double priceSimilarity(BigDecimal left, BigDecimal right) {
        if (left == null || right == null || left.compareTo(BigDecimal.ZERO) <= 0) {
            return 0D;
        }
        BigDecimal diff = left.subtract(right).abs();
        BigDecimal ratio = diff.divide(left, 4, RoundingMode.HALF_UP);
        return clamp(1D - ratio.doubleValue());
    }

    private double clamp(double value) {
        if (value < 0D) {
            return 0D;
        }
        return Math.min(value, 1D);
    }

    private LocalDateTime resolveScheduleStart(CourseSchedule schedule, Course course) {
        if (schedule == null) {
            return null;
        }
        if (schedule.getScheduleDate() != null && course != null && StringUtils.hasText(course.getStartHour())) {
            try {
                return LocalDateTime.of(schedule.getScheduleDate(), LocalTime.parse(course.getStartHour()));
            } catch (Exception ignored) {
                return schedule.getStartTime();
            }
        }
        return schedule.getStartTime();
    }

    private List<Course> safeListCoursesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return courseService.listByIds(ids);
    }

    private <T, K> Map<K, T> listToMap(List<T> list, Function<T, K> keyMapper) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, Function.identity(), (left, right) -> left));
    }

    private static final class CandidateScore<T> {
        private T item;
        private double behaviorRaw;
        private double contentScore;
        private double popularityScore;
        private double availabilityScore;
        private double finalScore;
        private String reason;
    }

    private static final class CourseAvailability {
        private int availableCount;
        private double totalSeatRatio;
        private LocalDateTime nextStartTime;
    }

    private static final class UserProfile {
        private final Map<Long, Double> courseItemWeights = new HashMap<>();
        private final Map<Long, Double> courseCategoryWeights = new HashMap<>();
        private final Map<Integer, Double> courseTypeWeights = new HashMap<>();
        private final Map<Long, Double> courseCoachWeights = new HashMap<>();
        private BigDecimal coursePriceWeightedSum = BigDecimal.ZERO;
        private double coursePriceWeight;

        private void addCourseItemWeight(Long key, double weight) {
            merge(courseItemWeights, key, weight);
        }

        private void addCourseCategoryWeight(Long key, double weight) {
            merge(courseCategoryWeights, key, weight);
        }

        private void addCourseTypeWeight(Integer key, double weight) {
            if (key == null) {
                return;
            }
            courseTypeWeights.merge(key, weight, Double::sum);
        }

        private void addCourseCoachWeight(Long key, double weight) {
            merge(courseCoachWeights, key, weight);
        }

        private void addCoursePrice(BigDecimal price, double weight) {
            if (price == null || weight <= 0) {
                return;
            }
            coursePriceWeightedSum = coursePriceWeightedSum.add(price.multiply(BigDecimal.valueOf(weight)));
            coursePriceWeight += weight;
        }

        private BigDecimal avgCoursePrice() {
            if (coursePriceWeight <= 0) {
                return null;
            }
            return coursePriceWeightedSum.divide(BigDecimal.valueOf(coursePriceWeight), 2, RoundingMode.HALF_UP);
        }

        private Long topCourseCategory() {
            return topKey(courseCategoryWeights);
        }

        private Integer topCourseType() {
            return topKey(courseTypeWeights);
        }

        private Long topCourseCoach() {
            return topKey(courseCoachWeights);
        }

        private <K> void merge(Map<K, Double> map, K key, double weight) {
            if (key == null) {
                return;
            }
            map.merge(key, weight, Double::sum);
        }

        private <K> K topKey(Map<K, Double> map) {
            return map.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }
}
