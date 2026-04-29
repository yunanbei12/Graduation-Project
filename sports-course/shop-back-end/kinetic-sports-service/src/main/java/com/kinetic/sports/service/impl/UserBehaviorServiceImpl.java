package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.UserBehavior;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.UserBehaviorService;
import com.kinetic.sports.service.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {

    private static final int COURSE_ITEM_TYPE = 1;
    private static final String VIEW_DETAIL = "view_detail";
    private static final String RECOMMEND_CLICK = "recommend_click";
    private static final int ACTIVE_STATUS = 1;
    private static final Set<String> ALLOWED_BEHAVIOR_TYPES = Set.of(VIEW_DETAIL, RECOMMEND_CLICK);
    private static final long DUPLICATE_WINDOW_SECONDS = 30L;

    private final CourseService courseService;

    @Override
    public void trackBehavior(Long userId, UserBehavior behavior) {
        if (userId == null || behavior == null || behavior.getItemType() == null || behavior.getItemId() == null
                || !StringUtils.hasText(behavior.getBehaviorType())) {
            return;
        }
        UserBehavior normalized = normalizeBehavior(behavior);
        if (normalized == null || isDuplicateBehavior(userId, normalized)) {
            return;
        }
        normalized.setId(null);
        normalized.setUserId(userId);
        this.save(normalized);
    }

    @Override
    public Map<String, Object> getRecommendStatsSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(30);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalBehaviors", countByCondition(since, null, null));
        result.put("detailViews", countByCondition(since, VIEW_DETAIL, null));
        result.put("recommendClicks", countByCondition(since, RECOMMEND_CLICK, null));
        result.put("uniqueUsers", countDistinctUsers(since));
        result.put("courseViews", countByCondition(since, VIEW_DETAIL, COURSE_ITEM_TYPE));
        result.put("courseClicks", countByCondition(since, RECOMMEND_CLICK, COURSE_ITEM_TYPE));
        result.put("trend", buildTrend(now.minusDays(6).toLocalDate()));
        result.put("topClickedItems", buildTopItems(since, RECOMMEND_CLICK));
        result.put("topViewedItems", buildTopItems(since, VIEW_DETAIL));
        result.put("sourceSections", buildSourceSections(since));
        return result;
    }

    @Override
    public Page<Map<String, Object>> getBehaviorPage(Page<UserBehavior> page,
                                                     Integer itemType,
                                                     String behaviorType,
                                                     String sourceSection) {
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
                .eq(itemType != null, UserBehavior::getItemType, itemType)
                .eq(StringUtils.hasText(behaviorType), UserBehavior::getBehaviorType, behaviorType)
                .eq(StringUtils.hasText(sourceSection), UserBehavior::getSourceSection, sourceSection)
                .orderByDesc(UserBehavior::getCreateTime);

        Page<UserBehavior> behaviorPage = this.page(page, wrapper);
        List<UserBehavior> records = behaviorPage.getRecords();
        if (records.isEmpty()) {
            Page<Map<String, Object>> empty = new Page<>(behaviorPage.getCurrent(), behaviorPage.getSize(), behaviorPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        List<Long> courseIds = records.stream()
                .map(UserBehavior::getItemId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Course> courseMap = safeListCoursesByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));

        List<Map<String, Object>> list = records.stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("userId", item.getUserId());
            map.put("itemType", item.getItemType());
            map.put("itemTypeText", "课程");
            map.put("itemId", item.getItemId());
            map.put("behaviorType", item.getBehaviorType());
            map.put("behaviorTypeText", VIEW_DETAIL.equals(item.getBehaviorType()) ? "详情浏览" : "推荐点击");
            map.put("sourcePage", item.getSourcePage());
            map.put("sourceSection", item.getSourceSection());
            map.put("sourceItemType", item.getSourceItemType());
            map.put("sourceItemId", item.getSourceItemId());
            map.put("extraInfo", item.getExtraInfo());
            map.put("createTime", item.getCreateTime());
            map.put("itemName", resolveItemName(item.getItemId(), courseMap));
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(behaviorPage.getCurrent(), behaviorPage.getSize(), behaviorPage.getTotal());
        result.setRecords(list);
        return result;
    }

    private UserBehavior normalizeBehavior(UserBehavior behavior) {
        if (!ALLOWED_BEHAVIOR_TYPES.contains(behavior.getBehaviorType())) {
            return null;
        }
        if (!Objects.equals(behavior.getItemType(), COURSE_ITEM_TYPE) || !isActiveCourse(behavior.getItemId())) {
            return null;
        }
        if (behavior.getSourceItemType() != null || behavior.getSourceItemId() != null) {
            if (!Objects.equals(behavior.getSourceItemType(), COURSE_ITEM_TYPE)
                    || behavior.getSourceItemId() == null
                    || !isActiveCourse(behavior.getSourceItemId())) {
                behavior.setSourceItemType(null);
                behavior.setSourceItemId(null);
            }
        }
        return behavior;
    }

    private boolean isDuplicateBehavior(Long userId, UserBehavior behavior) {
        UserBehavior latest = this.getOne(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getItemType, behavior.getItemType())
                .eq(UserBehavior::getItemId, behavior.getItemId())
                .eq(UserBehavior::getBehaviorType, behavior.getBehaviorType())
                .orderByDesc(UserBehavior::getCreateTime)
                .last("limit 1"));
        if (latest == null || latest.getCreateTime() == null) {
            return false;
        }
        boolean sameSource = Objects.equals(defaultString(latest.getSourcePage()), defaultString(behavior.getSourcePage()))
                && Objects.equals(defaultString(latest.getSourceSection()), defaultString(behavior.getSourceSection()))
                && Objects.equals(latest.getSourceItemType(), behavior.getSourceItemType())
                && Objects.equals(latest.getSourceItemId(), behavior.getSourceItemId());
        if (!sameSource) {
            return false;
        }
        return Duration.between(latest.getCreateTime(), LocalDateTime.now()).getSeconds() < DUPLICATE_WINDOW_SECONDS;
    }

    private boolean isActiveCourse(Long itemId) {
        if (itemId == null) {
            return false;
        }
        Course course = courseService.getById(itemId);
        return course != null && Objects.equals(course.getStatus(), ACTIVE_STATUS);
    }

    private long countByCondition(LocalDateTime since, String behaviorType, Integer itemType) {
        return this.count(new LambdaQueryWrapper<UserBehavior>()
                .ge(UserBehavior::getCreateTime, since)
                .eq(StringUtils.hasText(behaviorType), UserBehavior::getBehaviorType, behaviorType)
                .eq(itemType != null, UserBehavior::getItemType, itemType));
    }

    private long countDistinctUsers(LocalDateTime since) {
        QueryWrapper<UserBehavior> wrapper = new QueryWrapper<>();
        wrapper.select("COUNT(DISTINCT user_id) AS val")
                .ge("create_time", since);
        return queryLongValue(wrapper);
    }

    private List<Map<String, Object>> buildTrend(LocalDate startDate) {
        Map<LocalDate, long[]> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i), new long[]{0L, 0L});
        }
        QueryWrapper<UserBehavior> wrapper = new QueryWrapper<>();
        wrapper.select(
                        "DATE(create_time) AS behaviorDate",
                        "SUM(CASE WHEN behavior_type = 'view_detail' THEN 1 ELSE 0 END) AS viewCount",
                        "SUM(CASE WHEN behavior_type = 'recommend_click' THEN 1 ELSE 0 END) AS clickCount"
                )
                .ge("create_time", startDate.atStartOfDay())
                .groupBy("DATE(create_time)")
                .orderByAsc("behaviorDate");
        for (Map<String, Object> item : this.baseMapper.selectMaps(wrapper)) {
            LocalDate date = parseDate(item.get("behaviorDate"));
            long[] bucket = date == null ? null : dailyMap.get(date);
            if (bucket != null) {
                bucket[0] = toLong(item.get("viewCount"));
                bucket[1] = toLong(item.get("clickCount"));
            }
        }
        List<Map<String, Object>> list = new ArrayList<>();
        dailyMap.forEach((date, bucket) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("viewCount", bucket[0]);
            item.put("clickCount", bucket[1]);
            list.add(item);
        });
        return list;
    }

    private List<Map<String, Object>> buildTopItems(LocalDateTime since, String behaviorType) {
        QueryWrapper<UserBehavior> wrapper = new QueryWrapper<>();
        wrapper.select("item_type AS itemType", "item_id AS itemId", "COUNT(*) AS totalCount")
                .ge("create_time", since)
                .eq("behavior_type", behaviorType)
                .groupBy("item_type", "item_id")
                .orderByDesc("totalCount")
                .last("limit 10");
        List<Map<String, Object>> rows = this.baseMapper.selectMaps(wrapper);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = rows.stream()
                .map(row -> toLong(row.get("itemId")))
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Course> courseMap = safeListCoursesByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));

        return rows.stream().map(row -> {
            Long itemId = toLong(row.get("itemId"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", COURSE_ITEM_TYPE);
            item.put("itemTypeText", "课程");
            item.put("itemId", itemId);
            item.put("count", toLong(row.get("totalCount")));
            item.put("itemName", resolveItemName(itemId, courseMap));
            return item;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildSourceSections(LocalDateTime since) {
        QueryWrapper<UserBehavior> wrapper = new QueryWrapper<>();
        wrapper.select("source_section AS sourceSection", "COUNT(*) AS totalCount")
                .ge("create_time", since)
                .isNotNull("source_section")
                .ne("source_section", "")
                .groupBy("source_section")
                .orderByDesc("totalCount")
                .last("limit 20");
        return this.baseMapper.selectMaps(wrapper).stream()
                .map(row -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sourceSection", row.get("sourceSection"));
                    item.put("count", toLong(row.get("totalCount")));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Course> safeListCoursesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return courseService.listByIds(ids);
    }

    private String resolveItemName(Long itemId, Map<Long, Course> courseMap) {
        Course course = courseMap.get(itemId);
        return course == null ? "课程#" + itemId : course.getName();
    }

    private long queryLongValue(QueryWrapper<UserBehavior> wrapper) {
        List<Map<String, Object>> rows = this.baseMapper.selectMaps(wrapper);
        if (rows.isEmpty()) {
            return 0L;
        }
        return toLong(rows.get(0).get("val"));
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
