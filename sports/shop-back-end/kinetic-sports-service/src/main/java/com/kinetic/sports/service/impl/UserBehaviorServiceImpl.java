package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinetic.sports.bean.model.Course;
import com.kinetic.sports.bean.model.Prod;
import com.kinetic.sports.bean.model.UserBehavior;
import com.kinetic.sports.service.CourseService;
import com.kinetic.sports.service.ProdService;
import com.kinetic.sports.service.UserBehaviorService;
import com.kinetic.sports.service.mapper.UserBehaviorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {

    private static final int COURSE_ITEM_TYPE = 1;
    private static final int PROD_ITEM_TYPE = 2;
    private static final String VIEW_DETAIL = "view_detail";
    private static final String RECOMMEND_CLICK = "recommend_click";

    private final CourseService courseService;
    private final ProdService prodService;

    @Override
    public void trackBehavior(Long userId, UserBehavior behavior) {
        if (userId == null || behavior == null || behavior.getItemType() == null || behavior.getItemId() == null
                || !StringUtils.hasText(behavior.getBehaviorType())) {
            return;
        }
        behavior.setId(null);
        behavior.setUserId(userId);
        this.save(behavior);
    }

    @Override
    public Map<String, Object> getRecommendStatsSummary() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<UserBehavior> behaviors = this.list(new LambdaQueryWrapper<UserBehavior>()
                .ge(UserBehavior::getCreateTime, since)
                .orderByDesc(UserBehavior::getCreateTime));

        Map<String, Object> result = new LinkedHashMap<>();
        long totalCount = behaviors.size();
        long detailViews = behaviors.stream().filter(it -> VIEW_DETAIL.equals(it.getBehaviorType())).count();
        long recommendClicks = behaviors.stream().filter(it -> RECOMMEND_CLICK.equals(it.getBehaviorType())).count();
        long uniqueUsers = behaviors.stream().map(UserBehavior::getUserId).filter(Objects::nonNull).distinct().count();
        long courseViews = behaviors.stream().filter(it -> VIEW_DETAIL.equals(it.getBehaviorType()) && Objects.equals(it.getItemType(), COURSE_ITEM_TYPE)).count();
        long prodViews = behaviors.stream().filter(it -> VIEW_DETAIL.equals(it.getBehaviorType()) && Objects.equals(it.getItemType(), PROD_ITEM_TYPE)).count();
        long courseClicks = behaviors.stream().filter(it -> RECOMMEND_CLICK.equals(it.getBehaviorType()) && Objects.equals(it.getItemType(), COURSE_ITEM_TYPE)).count();
        long prodClicks = behaviors.stream().filter(it -> RECOMMEND_CLICK.equals(it.getBehaviorType()) && Objects.equals(it.getItemType(), PROD_ITEM_TYPE)).count();

        result.put("totalBehaviors", totalCount);
        result.put("detailViews", detailViews);
        result.put("recommendClicks", recommendClicks);
        result.put("uniqueUsers", uniqueUsers);
        result.put("courseViews", courseViews);
        result.put("prodViews", prodViews);
        result.put("courseClicks", courseClicks);
        result.put("prodClicks", prodClicks);

        result.put("trend", buildTrend(behaviors));
        result.put("topClickedItems", buildTopItems(behaviors, RECOMMEND_CLICK));
        result.put("topViewedItems", buildTopItems(behaviors, VIEW_DETAIL));
        result.put("sourceSections", buildSourceSections(behaviors));
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
                .filter(it -> Objects.equals(it.getItemType(), COURSE_ITEM_TYPE))
                .map(UserBehavior::getItemId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> prodIds = records.stream()
                .filter(it -> Objects.equals(it.getItemType(), PROD_ITEM_TYPE))
                .map(UserBehavior::getItemId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Course> courseMap = safeListCoursesByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        Map<Long, Prod> prodMap = safeListProdsByIds(prodIds).stream()
                .collect(Collectors.toMap(Prod::getId, Function.identity(), (left, right) -> left));

        List<Map<String, Object>> list = records.stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", item.getId());
            map.put("userId", item.getUserId());
            map.put("itemType", item.getItemType());
            map.put("itemTypeText", Objects.equals(item.getItemType(), COURSE_ITEM_TYPE) ? "课程" : "商品");
            map.put("itemId", item.getItemId());
            map.put("behaviorType", item.getBehaviorType());
            map.put("behaviorTypeText", VIEW_DETAIL.equals(item.getBehaviorType()) ? "详情浏览" : "推荐点击");
            map.put("sourcePage", item.getSourcePage());
            map.put("sourceSection", item.getSourceSection());
            map.put("sourceItemType", item.getSourceItemType());
            map.put("sourceItemId", item.getSourceItemId());
            map.put("extraInfo", item.getExtraInfo());
            map.put("createTime", item.getCreateTime());
            map.put("itemName", resolveItemName(item, courseMap, prodMap));
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> result = new Page<>(behaviorPage.getCurrent(), behaviorPage.getSize(), behaviorPage.getTotal());
        result.setRecords(list);
        return result;
    }

    private List<Map<String, Object>> buildTrend(List<UserBehavior> behaviors) {
        Map<LocalDate, long[]> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i), new long[]{0L, 0L});
        }
        for (UserBehavior behavior : behaviors) {
            if (behavior.getCreateTime() == null) {
                continue;
            }
            LocalDate date = behavior.getCreateTime().toLocalDate();
            long[] bucket = dailyMap.get(date);
            if (bucket == null) {
                continue;
            }
            if (VIEW_DETAIL.equals(behavior.getBehaviorType())) {
                bucket[0]++;
            } else if (RECOMMEND_CLICK.equals(behavior.getBehaviorType())) {
                bucket[1]++;
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

    private List<Map<String, Object>> buildTopItems(List<UserBehavior> behaviors, String behaviorType) {
        Map<String, Long> counter = behaviors.stream()
                .filter(it -> behaviorType.equals(it.getBehaviorType()))
                .collect(Collectors.groupingBy(it -> it.getItemType() + ":" + it.getItemId(), Collectors.counting()));
        if (counter.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> courseIds = new HashSet<>();
        Set<Long> prodIds = new HashSet<>();
        counter.keySet().forEach(key -> {
            String[] parts = key.split(":");
            if (parts.length != 2) {
                return;
            }
            Integer currentItemType = Integer.parseInt(parts[0]);
            Long itemId = Long.parseLong(parts[1]);
            if (Objects.equals(currentItemType, COURSE_ITEM_TYPE)) {
                courseIds.add(itemId);
            } else if (Objects.equals(currentItemType, PROD_ITEM_TYPE)) {
                prodIds.add(itemId);
            }
        });

        Map<Long, Course> courseMap = safeListCoursesByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
        Map<Long, Prod> prodMap = safeListProdsByIds(prodIds).stream()
                .collect(Collectors.toMap(Prod::getId, Function.identity(), (left, right) -> left));

        return counter.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    String[] parts = entry.getKey().split(":");
                    Integer currentItemType = Integer.parseInt(parts[0]);
                    Long itemId = Long.parseLong(parts[1]);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("itemType", currentItemType);
                    item.put("itemTypeText", Objects.equals(currentItemType, COURSE_ITEM_TYPE) ? "课程" : "商品");
                    item.put("itemId", itemId);
                    item.put("count", entry.getValue());
                    item.put("itemName", resolveItemName(currentItemType, itemId, courseMap, prodMap));
                    return item;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildSourceSections(List<UserBehavior> behaviors) {
        return behaviors.stream()
                .filter(it -> StringUtils.hasText(it.getSourceSection()))
                .collect(Collectors.groupingBy(UserBehavior::getSourceSection, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sourceSection", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String resolveItemName(UserBehavior behavior, Map<Long, Course> courseMap, Map<Long, Prod> prodMap) {
        return resolveItemName(behavior.getItemType(), behavior.getItemId(), courseMap, prodMap);
    }

    private List<Course> safeListCoursesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return courseService.listByIds(ids);
    }

    private List<Prod> safeListProdsByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return prodService.listByIds(ids);
    }

    private String resolveItemName(Integer itemType, Long itemId, Map<Long, Course> courseMap, Map<Long, Prod> prodMap) {
        if (Objects.equals(itemType, COURSE_ITEM_TYPE)) {
            Course course = courseMap.get(itemId);
            return course == null ? "课程#" + itemId : course.getName();
        }
        Prod prod = prodMap.get(itemId);
        return prod == null ? "商品#" + itemId : prod.getName();
    }
}
