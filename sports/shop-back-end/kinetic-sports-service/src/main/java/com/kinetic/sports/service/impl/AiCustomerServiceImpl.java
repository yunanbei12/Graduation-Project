package com.kinetic.sports.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kinetic.sports.bean.dto.ai.AiAction;
import com.kinetic.sports.bean.dto.ai.AiCard;
import com.kinetic.sports.bean.dto.ai.AiChatRequest;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.bean.vo.ai.AiChatResponse;
import com.kinetic.sports.bean.vo.ai.AiMessageVO;
import com.kinetic.sports.bean.vo.ai.AiSessionDetailVO;
import com.kinetic.sports.bean.vo.ai.AiSessionItemVO;
import com.kinetic.sports.common.exception.KineticSportsBindException;
import com.kinetic.sports.service.*;
import com.kinetic.sports.service.ai.AiCustomerService;
import com.kinetic.sports.service.ai.AiModelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCustomerServiceImpl implements AiCustomerService {

    private static final String LOGIN_ROUTE = "/pages/accountLogin/accountLogin";
    private static final String COURSE_ROUTE = "/pages/course/course";
    private static final String COURSE_ORDER_ROUTE = "/pages/order/my-orders-course";
    private static final String PRODUCT_ORDER_ROUTE = "/pages/order/my-orders-product";
    private static final String PACKAGE_ROUTE = "/pages/profile/my-packages";
    private static final String COUPON_ROUTE = "/pages/profile/my-coupons";
    private static final String CHECKIN_ROUTE = "/pages/profile/my-checkins";
    private static final String BIND_PHONE_ROUTE = "/pages/profile/bind-phone";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final int SESSION_STATUS_PROCESSING = 0;
    private static final int SESSION_STATUS_RESOLVED = 1;
    private static final int SESSION_STATUS_WAIT_MANUAL = 2;
    private static final int SESSION_STATUS_ENDED = 3;
    private static final String RESOLVED_SYSTEM_REPLY = "当前问题已处理完成，后续如果你还有新的问题，继续发送消息即可自动开启新的咨询。";
    private static final String ENDED_SYSTEM_REPLY = "本轮咨询已结束，后续如果你再次发送消息，系统会自动开启新的咨询。";

    private final AiSessionService aiSessionService;
    private final AiMessageService aiMessageService;
    private final AiKnowledgeService aiKnowledgeService;
    private final AiHandoverService aiHandoverService;
    private final AiFeedbackService aiFeedbackService;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CourseService courseService;
    private final CourseScheduleService courseScheduleService;
    private final UserCoursePackageService userCoursePackageService;
    private final UserCouponService userCouponService;
    private final CouponService couponService;
    private final CourseCheckinService courseCheckinService;
    private final ProdService prodService;
    private final CoachService coachService;
    private final ObjectMapper objectMapper;
    private final AiModelClient aiModelClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatResponse chat(AiChatRequest request, Long userId) {
        String message = request == null ? null : request.getMessage();
        if (!StringUtils.hasText(message)) {
            throw new KineticSportsBindException("请输入咨询内容");
        }
        message = message.trim();

        AiSession session = prepareSession(request.getSessionId(), userId, message);
        aiMessageService.save(buildUserMessage(session.getId(), userId, message));

        String intent = detectIntent(message);
        BigDecimal confidence = estimateConfidence(intent, message);
        ChatDraft draft = buildReply(intent, message, userId, session.getId());

        AiChatResponse response = new AiChatResponse();
        response.setSessionId(session.getId());
        response.setReplyText(draft.replyText);
        response.setIntent(intent);
        response.setConfidence(confidence);
        response.setSourceType(draft.sourceType);
        response.setCards(draft.cards);
        response.setActions(draft.actions);
        response.setNeedLogin(draft.needLogin);
        response.setNeedHandover(draft.needHandover);

        AiMessage assistant = buildAssistantMessage(session.getId(), userId, intent, confidence, draft);
        aiMessageService.save(assistant);
        response.setMessageId(assistant.getId());

        updateSessionSnapshot(session, message, response);
        return response;
    }

    @Override
    public List<AiSessionItemVO> listUserSessions(Long userId) {
        List<AiSession> sessions = aiSessionService.list(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getUserId, userId)
                        .orderByDesc(AiSession::getLastMessageTime)
                        .last("limit 30")
        );
        return sessions.stream().map(this::toSessionItem).collect(Collectors.toList());
    }

    @Override
    public AiSessionDetailVO getUserSessionDetail(Long sessionId, Long userId) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            throw new KineticSportsBindException("会话不存在");
        }
        return buildSessionDetail(session);
    }

    @Override
    public AiSessionDetailVO getSessionDetail(Long sessionId) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null) {
            throw new KineticSportsBindException("会话不存在");
        }
        return buildSessionDetail(session);
    }

    @Override
    public List<AiSessionItemVO> getAdminSessionHistory(Long sessionId) {
        AiSession current = aiSessionService.getById(sessionId);
        if (current == null) {
            return Collections.emptyList();
        }
        List<AiSession> sessions;
        if (current.getUserId() == null) {
            sessions = Collections.singletonList(current);
        } else {
            sessions = aiSessionService.list(new LambdaQueryWrapper<AiSession>()
                    .eq(AiSession::getUserId, current.getUserId())
                    .orderByDesc(AiSession::getLastMessageTime)
                    .orderByDesc(AiSession::getId));
        }
        return sessions.stream().map(this::toSessionItem).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitFeedback(Long sessionId, Long messageId, Integer rating, String comment, Long userId) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null || !Objects.equals(session.getUserId(), userId)) {
            throw new KineticSportsBindException("会话不存在");
        }
        AiMessage message = aiMessageService.getById(messageId);
        if (message == null || !Objects.equals(message.getSessionId(), sessionId)) {
            throw new KineticSportsBindException("消息不存在");
        }

        AiFeedback feedback = new AiFeedback();
        feedback.setSessionId(sessionId);
        feedback.setMessageId(messageId);
        feedback.setUserId(userId);
        feedback.setRating(Objects.equals(rating, 1) ? 1 : 0);
        feedback.setComment(comment);
        aiFeedbackService.save(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createHandover(Long sessionId, Long userId, String remark) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null) {
            throw new KineticSportsBindException("会话不存在");
        }
        if (userId != null && session.getUserId() != null && !Objects.equals(session.getUserId(), userId)) {
            throw new KineticSportsBindException("无权操作该会话");
        }

        long pendingCount = aiHandoverService.count(
                new LambdaQueryWrapper<AiHandover>()
                        .eq(AiHandover::getSessionId, sessionId)
                        .eq(AiHandover::getStatus, 0)
        );
        if (pendingCount == 0) {
            AiHandover handover = new AiHandover();
            handover.setSessionId(sessionId);
            handover.setUserId(session.getUserId());
            handover.setLatestQuestion(StringUtils.hasText(remark) ? remark : session.getLastQuestion());
            handover.setStatus(0);
            aiHandoverService.save(handover);
        }

        session.setNeedHandover(1);
        session.setStatus(SESSION_STATUS_WAIT_MANUAL);
        aiSessionService.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminReply(Long sessionId, String replyText, String adminName, boolean resolveAfterReply, boolean terminateAfterReply) {
        if (!StringUtils.hasText(replyText)) {
            throw new KineticSportsBindException("请输入回复内容");
        }
        terminateAfterReply = terminateAfterReply && !resolveAfterReply;
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null) {
            throw new KineticSportsBindException("会话不存在");
        }

        AiMessage aiMessage = new AiMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(session.getUserId());
        aiMessage.setRole("assistant");
        aiMessage.setReplyText(replyText.trim());
        aiMessage.setIntent("manual_service");
        aiMessage.setConfidence(BigDecimal.ONE);
        aiMessage.setCardsJson("[]");
        aiMessage.setActionsJson("[]");
        aiMessage.setSourceType("human");
        aiMessage.setHitRule(0);
        aiMessage.setNeedHandover(0);
        aiMessageService.save(aiMessage);

        session.setLastReply(replyText.trim());
        session.setLastIntent("manual_service");
        session.setLastMessageTime(LocalDateTime.now());
        session.setNeedHandover(0);
        session.setStatus(resolveAfterReply ? SESSION_STATUS_RESOLVED : (terminateAfterReply ? SESSION_STATUS_ENDED : SESSION_STATUS_PROCESSING));
        if (resolveAfterReply || terminateAfterReply) {
            session.setResolvedTime(LocalDateTime.now());
        }
        aiSessionService.updateById(session);

        if (resolveAfterReply) {
            aiMessageService.save(buildSystemAssistantMessage(sessionId, session.getUserId(), RESOLVED_SYSTEM_REPLY));
        }
        if (terminateAfterReply) {
            aiMessageService.save(buildSystemAssistantMessage(sessionId, session.getUserId(), ENDED_SYSTEM_REPLY));
        }

        List<AiHandover> pending = aiHandoverService.list(new LambdaQueryWrapper<AiHandover>()
                .eq(AiHandover::getSessionId, sessionId)
                .eq(AiHandover::getStatus, 0));
        for (AiHandover handover : pending) {
            handover.setStatus(1);
            handover.setAdminRemark(replyText.trim());
            handover.setHandledBy(adminName);
            handover.setHandledTime(LocalDateTime.now());
            aiHandoverService.updateById(handover);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveSession(Long sessionId, String operatorName, String remark) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null) {
            throw new KineticSportsBindException("会话不存在");
        }
        if (Objects.equals(session.getStatus(), SESSION_STATUS_RESOLVED)) {
            return;
        }

        String closeText = StringUtils.hasText(remark) ? remark.trim() : RESOLVED_SYSTEM_REPLY;
        aiMessageService.save(buildSystemAssistantMessage(sessionId, session.getUserId(), closeText));

        session.setLastReply(closeText);
        session.setLastIntent("session_resolved");
        session.setLastMessageTime(LocalDateTime.now());
        session.setNeedHandover(0);
        session.setStatus(SESSION_STATUS_RESOLVED);
        session.setResolvedTime(LocalDateTime.now());
        aiSessionService.updateById(session);

        List<AiHandover> pending = aiHandoverService.list(new LambdaQueryWrapper<AiHandover>()
                .eq(AiHandover::getSessionId, sessionId)
                .eq(AiHandover::getStatus, 0));
        for (AiHandover handover : pending) {
            handover.setStatus(1);
            handover.setAdminRemark(StringUtils.hasText(operatorName) ? operatorName + "已将本次问题标记为解决" : "后台已将本次问题标记为解决");
            handover.setHandledBy(operatorName);
            handover.setHandledTime(LocalDateTime.now());
            aiHandoverService.updateById(handover);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminateSession(Long sessionId, String operatorName, String remark) {
        AiSession session = aiSessionService.getById(sessionId);
        if (session == null) {
            throw new KineticSportsBindException("会话不存在");
        }
        if (Objects.equals(session.getStatus(), SESSION_STATUS_ENDED)) {
            return;
        }

        String closeText = StringUtils.hasText(remark) ? remark.trim() : ENDED_SYSTEM_REPLY;
        aiMessageService.save(buildSystemAssistantMessage(sessionId, session.getUserId(), closeText));

        session.setLastReply(closeText);
        session.setLastIntent("session_closed");
        session.setLastMessageTime(LocalDateTime.now());
        session.setNeedHandover(0);
        session.setStatus(SESSION_STATUS_ENDED);
        session.setResolvedTime(LocalDateTime.now());
        aiSessionService.updateById(session);

        List<AiHandover> pending = aiHandoverService.list(new LambdaQueryWrapper<AiHandover>()
                .eq(AiHandover::getSessionId, sessionId)
                .eq(AiHandover::getStatus, 0));
        for (AiHandover handover : pending) {
            handover.setStatus(1);
            handover.setAdminRemark(StringUtils.hasText(operatorName) ? operatorName + "结束了本轮咨询" : "后台结束了本轮咨询");
            handover.setHandledBy(operatorName);
            handover.setHandledTime(LocalDateTime.now());
            aiHandoverService.updateById(handover);
        }
    }

    @Override
    public Page<Map<String, Object>> getAdminSessionPage(Page<?> page, String keyword, String intent, Integer status, Integer needHandover) {
        List<AiSession> sessions = aiSessionService.list(
                new LambdaQueryWrapper<AiSession>()
                        .eq(StringUtils.hasText(intent), AiSession::getLastIntent, intent)
                        .eq(status != null, AiSession::getStatus, status)
                        .eq(needHandover != null, AiSession::getNeedHandover, needHandover)
                        .orderByDesc(AiSession::getLastMessageTime)
                        .orderByDesc(AiSession::getId)
        );

        Set<Long> allUserIds = sessions.stream()
                .map(AiSession::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> allUserMap = listUsersByIds(allUserIds);

        String trimmedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<AiSession> filteredSessions = sessions.stream()
                .filter(session -> matchesAdminKeyword(session, allUserMap.get(session.getUserId()), trimmedKeyword))
                .collect(Collectors.toList());

        LinkedHashMap<String, AiSession> latestSessionMap = new LinkedHashMap<>();
        Map<String, Long> sessionCountMap = new HashMap<>();
        for (AiSession session : filteredSessions) {
            String conversationKey = buildConversationKey(session.getUserId(), session.getId());
            latestSessionMap.putIfAbsent(conversationKey, session);
            sessionCountMap.merge(conversationKey, 1L, Long::sum);
        }

        List<AiSession> latestSessions = new ArrayList<>(latestSessionMap.values());
        int fromIndex = Math.max(0, (int) ((page.getCurrent() - 1) * page.getSize()));
        int toIndex = Math.min(latestSessions.size(), fromIndex + (int) page.getSize());
        List<AiSession> pagedSessions = fromIndex >= latestSessions.size()
                ? Collections.emptyList()
                : latestSessions.subList(fromIndex, toIndex);

        Set<Long> userIds = pagedSessions.stream()
                .map(AiSession::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = listUsersByIds(userIds);

        Page<Map<String, Object>> result = new Page<>(page.getCurrent(), page.getSize(), latestSessions.size());
        List<Map<String, Object>> records = pagedSessions.stream().map(session -> {
            Map<String, Object> item = new LinkedHashMap<>();
            User user = userMap.get(session.getUserId());
            String conversationKey = buildConversationKey(session.getUserId(), session.getId());
            item.put("id", conversationKey);
            item.put("conversationKey", conversationKey);
            item.put("latestSessionId", session.getId());
            item.put("title", session.getTitle());
            item.put("lastQuestion", session.getLastQuestion());
            item.put("lastReply", session.getLastReply());
            item.put("lastIntent", session.getLastIntent());
            item.put("status", session.getStatus());
            item.put("needHandover", session.getNeedHandover());
            item.put("lastMessageTime", session.getLastMessageTime());
            item.put("userId", session.getUserId());
            item.put("userNickName", user != null ? user.getNickName() : "游客");
            item.put("userAvatarUrl", user != null ? user.getAvatarUrl() : null);
            item.put("userPhone", user != null ? user.getPhone() : "-");
            item.put("sessionCount", sessionCountMap.getOrDefault(conversationKey, 1L));
            return item;
        }).collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    @Override
    public List<Map<String, Object>> getHandoverList(Integer status) {
        List<AiHandover> handovers = aiHandoverService.list(
                new LambdaQueryWrapper<AiHandover>()
                        .eq(status != null, AiHandover::getStatus, status)
                        .orderByDesc(AiHandover::getCreateTime)
        );
        Set<Long> sessionIds = handovers.stream()
                .map(AiHandover::getSessionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> userIds = handovers.stream()
                .map(AiHandover::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, AiSession> sessionMap = listSessionsByIds(sessionIds);
        Map<Long, User> userMap = listUsersByIds(userIds);

        return handovers.stream().map(handover -> {
            Map<String, Object> item = new LinkedHashMap<>();
            AiSession session = sessionMap.get(handover.getSessionId());
            User user = userMap.get(handover.getUserId());
            item.put("id", handover.getId());
            item.put("sessionId", handover.getSessionId());
            item.put("status", handover.getStatus());
            item.put("latestQuestion", handover.getLatestQuestion());
            item.put("adminRemark", handover.getAdminRemark());
            item.put("handledBy", handover.getHandledBy());
            item.put("handledTime", handover.getHandledTime());
            item.put("createTime", handover.getCreateTime());
            item.put("sessionTitle", session != null ? session.getTitle() : "-");
            item.put("userNickName", user != null ? user.getNickName() : "游客");
            item.put("userPhone", user != null ? user.getPhone() : "-");
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatsSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        long totalSessions = aiSessionService.count();
        long resolvedSessions = aiSessionService.count(new LambdaQueryWrapper<AiSession>().eq(AiSession::getStatus, 1));
        long handoverSessions = aiSessionService.count(new LambdaQueryWrapper<AiSession>().eq(AiSession::getNeedHandover, 1));
        long knowledgeCount = aiKnowledgeService.count(new LambdaQueryWrapper<AiKnowledge>().eq(AiKnowledge::getStatus, 1));
        long positiveFeedback = aiFeedbackService.count(new LambdaQueryWrapper<AiFeedback>().eq(AiFeedback::getRating, 1));
        long negativeFeedback = aiFeedbackService.count(new LambdaQueryWrapper<AiFeedback>().eq(AiFeedback::getRating, 0));

        List<Map<String, Object>> topIntents = aiSessionService.list().stream()
                .filter(item -> StringUtils.hasText(item.getLastIntent()))
                .collect(Collectors.groupingBy(AiSession::getLastIntent, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("intent", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        summary.put("totalSessions", totalSessions);
        summary.put("resolvedSessions", resolvedSessions);
        summary.put("handoverSessions", handoverSessions);
        summary.put("knowledgeCount", knowledgeCount);
        summary.put("positiveFeedback", positiveFeedback);
        summary.put("negativeFeedback", negativeFeedback);
        summary.put("topIntents", topIntents);
        summary.put("resolveRate", totalSessions == 0 ? BigDecimal.ZERO :
                BigDecimal.valueOf(resolvedSessions).divide(BigDecimal.valueOf(totalSessions), 2, RoundingMode.HALF_UP));
        return summary;
    }

    private AiSession prepareSession(Long sessionId, Long userId, String message) {
        AiSession session = sessionId == null ? null : aiSessionService.getById(sessionId);
        if (session != null) {
            if (userId != null && session.getUserId() != null && !Objects.equals(session.getUserId(), userId)) {
                throw new KineticSportsBindException("会话不存在");
            }
            if (Objects.equals(session.getStatus(), SESSION_STATUS_RESOLVED) || Objects.equals(session.getStatus(), SESSION_STATUS_ENDED)) {
                return createSession(userId, message);
            }
            if (session.getUserId() == null && userId != null) {
                session.setUserId(userId);
                aiSessionService.updateById(session);
            }
            return session;
        }

        return createSession(userId, message);
    }

    private AiMessage buildUserMessage(Long sessionId, Long userId, String message) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("user");
        aiMessage.setContent(message);
        aiMessage.setHitRule(0);
        aiMessage.setNeedHandover(0);
        return aiMessage;
    }

    private AiMessage buildAssistantMessage(Long sessionId, Long userId, String intent, BigDecimal confidence, ChatDraft draft) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setReplyText(draft.replyText);
        aiMessage.setIntent(intent);
        aiMessage.setConfidence(confidence);
        aiMessage.setCardsJson(writeJson(draft.cards));
        aiMessage.setActionsJson(writeJson(draft.actions));
        aiMessage.setSourceType(draft.sourceType);
        aiMessage.setHitRule(draft.hitRule ? 1 : 0);
        aiMessage.setNeedHandover(draft.needHandover ? 1 : 0);
        return aiMessage;
    }

    private AiMessage buildSystemAssistantMessage(Long sessionId, Long userId, String replyText) {
        AiMessage aiMessage = new AiMessage();
        aiMessage.setSessionId(sessionId);
        aiMessage.setUserId(userId);
        aiMessage.setRole("assistant");
        aiMessage.setReplyText(replyText);
        aiMessage.setIntent("session_closed");
        aiMessage.setConfidence(BigDecimal.ONE);
        aiMessage.setCardsJson("[]");
        aiMessage.setActionsJson("[]");
        aiMessage.setSourceType("system");
        aiMessage.setHitRule(1);
        aiMessage.setNeedHandover(0);
        return aiMessage;
    }

    private AiSession createSession(Long userId, String message) {
        AiSession created = new AiSession();
        created.setUserId(userId);
        created.setTitle(truncate(message, 18));
        created.setStatus(SESSION_STATUS_PROCESSING);
        created.setNeedHandover(0);
        created.setLastMessageTime(LocalDateTime.now());
        aiSessionService.save(created);
        return created;
    }

    private void updateSessionSnapshot(AiSession session, String question, AiChatResponse response) {
        session.setTitle(StringUtils.hasText(session.getTitle()) ? session.getTitle() : truncate(question, 18));
        session.setLastQuestion(question);
        session.setLastReply(response.getReplyText());
        session.setLastIntent(response.getIntent());
        session.setStatus(Boolean.TRUE.equals(response.getNeedHandover()) ? SESSION_STATUS_WAIT_MANUAL : session.getStatus());
        session.setNeedHandover(Boolean.TRUE.equals(response.getNeedHandover()) ? 1 : session.getNeedHandover());
        session.setLastMessageTime(LocalDateTime.now());
        aiSessionService.updateById(session);
    }

    private ChatDraft buildReply(String intent, String message, Long userId, Long sessionId) {
        return switch (intent) {
            case "course_recommend" -> buildCourseRecommendReply(message, userId);
            case "course_schedule_query" -> buildScheduleReply(message);
            case "product_recommend" -> buildProductReply(message, userId);
            case "order_query" -> buildOrderReply(userId, message);
            case "refund_help" -> buildRefundReply(userId);
            case "package_query" -> buildPackageReply(userId);
            case "coupon_query" -> buildCouponReply(userId, message);
            case "checkin_query" -> buildCheckinReply(userId);
            case "account_help" -> buildAccountReply(userId);
            case "manual_service" -> buildManualReply(userId, sessionId);
            default -> buildGeneralReply(message, userId);
        };
    }

    private ChatDraft buildCourseRecommendReply(String message, Long userId) {
        Integer type = null;
        if (message.contains("团课")) {
            type = 2;
        } else if (message.contains("私教") || message.contains("课包")) {
            type = 1;
        }
        List<Course> courses = courseService.list(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1)
                        .eq(type != null, Course::getType, type)
                        .orderByDesc(Course::getSales)
                        .last("limit 3")
        );

        ChatDraft draft = new ChatDraft();
        draft.cards = courses.stream().map(this::toCourseCard).collect(Collectors.toList());
        draft.actions.add(action("navigate", "查看课程", null, COURSE_ROUTE));
        List<AiKnowledge> knowledges = findKnowledge("course", message);

        String fallback = courses.isEmpty()
                ? "当前可推荐的课程还在整理中，建议稍后再看课程页，或者告诉我你更偏向私教课还是团课。"
                : "我先帮你挑了几门热度和口碑都不错的课程，适合先从上面的卡片里看看。如果你告诉我更偏好减脂、体能、康复还是专项训练，我还能继续细化推荐。";
        applyFinalReply(draft, "course_recommend", message, userId, knowledges, draft.cards, fallback);
        draft.sourceType = StringUtils.hasText(draft.modelReply) ? "model" : "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildScheduleReply(String message) {
        List<CourseSchedule> schedules = courseScheduleService.list(
                new LambdaQueryWrapper<CourseSchedule>()
                        .in(CourseSchedule::getStatus, 0, 1)
                        .gt(CourseSchedule::getScheduleDate, LocalDate.now())
                        .orderByAsc(CourseSchedule::getScheduleDate)
                        .last("limit 3")
        );
        Map<Long, Course> courseMap = listCoursesByIds(schedules.stream()
                .map(CourseSchedule::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        ChatDraft draft = new ChatDraft();
        draft.cards = schedules.stream().map(item -> toScheduleCard(item, courseMap.get(item.getCourseId()))).collect(Collectors.toList());
        draft.actions.add(action("navigate", "去看团课", null, COURSE_ROUTE));
        draft.replyText = schedules.isEmpty()
                ? "最近暂时没有可报名的团课场次，你可以先收藏课程页，等后台排课更新后再来查看。"
                : "我帮你查了最近可报名的团课场次，上面的卡片已经整理了时间、地点和剩余名额，挑合适的直接点进去就能看详情。";
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildProductReply(String message, Long userId) {
        List<Prod> prods = prodService.list(
                new LambdaQueryWrapper<Prod>()
                        .eq(Prod::getStatus, 1)
                        .orderByDesc(Prod::getSales)
                        .last("limit 3")
        );
        ChatDraft draft = new ChatDraft();
        draft.cards = prods.stream().map(this::toProductCard).collect(Collectors.toList());
        List<AiKnowledge> knowledges = findKnowledge("product", message);
        String fallback = prods.isEmpty()
                ? "当前商城商品还在补充中，你可以先去商城页看看最新上架内容。"
                : "我先给你挑了几款当前商城里比较热门的训练装备和补给，适合做入门购买参考。如果你想按减脂、力量或恢复场景来选，我也可以继续推荐。";
        applyFinalReply(draft, "product_recommend", message, userId, knowledges, draft.cards, fallback);
        draft.actions.add(action("navigate", "查看商城", null, "/pages/mall/mall"));
        draft.sourceType = StringUtils.hasText(draft.modelReply) ? "model" : "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildOrderReply(Long userId, String message) {
        if (userId == null) {
            return buildLoginRequiredReply("登录后我才能帮你查询课程订单、商品订单和退款状态。", COURSE_ORDER_ROUTE);
        }
        Integer orderType = null;
        if (message.contains("商品")) {
            orderType = 2;
        } else if (message.contains("课程")) {
            orderType = 1;
        }
        List<Order> orders = orderService.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .eq(orderType != null, Order::getOrderType, orderType)
                        .orderByDesc(Order::getCreateTime)
                        .last("limit 3")
        );

        Map<Long, String> itemNameMap = loadOrderItemNameMap(orders);
        ChatDraft draft = new ChatDraft();
        draft.cards = orders.stream().map(order -> toOrderCard(order, itemNameMap.get(order.getId()))).collect(Collectors.toList());
        if (orders.isEmpty()) {
            draft.replyText = "我暂时没有查到你的相关订单记录，如果你刚下单，可以稍后刷新订单页再看看。";
        } else {
            long pending = orders.stream().filter(item -> Objects.equals(item.getStatus(), 1)).count();
            long paid = orders.stream().filter(item -> Objects.equals(item.getStatus(), 2) || Objects.equals(item.getStatus(), 3)).count();
            draft.replyText = "我查到了你最近的订单记录。当前有 " + pending + " 笔待支付，" + paid + " 笔已支付待处理，具体信息已经整理在下方卡片里。";
        }
        draft.actions.add(action("navigate", "查看课程订单", null, COURSE_ORDER_ROUTE));
        draft.actions.add(action("navigate", "查看商品订单", null, PRODUCT_ORDER_ROUTE));
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildRefundReply(Long userId) {
        ChatDraft draft = new ChatDraft();
        draft.replyText = "目前 v1 版本支持在订单页发起退款申请。课程订单在已支付或待排课阶段可以申请，商品订单也可以在相应状态下提交退款原因，后台会在客服台进行审核处理。";
        draft.actions.add(action("navigate", "课程订单", null, COURSE_ORDER_ROUTE));
        draft.actions.add(action("navigate", "商品订单", null, PRODUCT_ORDER_ROUTE));

        if (userId != null) {
            List<Order> refundable = orderService.list(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getUserId, userId)
                            .in(Order::getStatus, 2, 3)
                            .orderByDesc(Order::getCreateTime)
                            .last("limit 2")
            );
            Map<Long, String> itemNameMap = loadOrderItemNameMap(refundable);
            draft.cards = refundable.stream().map(order -> toOrderCard(order, itemNameMap.get(order.getId()))).collect(Collectors.toList());
        }
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildPackageReply(Long userId) {
        if (userId == null) {
            return buildLoginRequiredReply("登录后我才能帮你查看课包剩余节数、有效期和最近消课记录。", PACKAGE_ROUTE);
        }

        List<UserCoursePackage> packages = userCoursePackageService.list(
                new LambdaQueryWrapper<UserCoursePackage>()
                        .eq(UserCoursePackage::getUserId, userId)
                        .orderByDesc(UserCoursePackage::getCreateTime)
                        .last("limit 3")
        );
        Map<Long, Course> courseMap = listCoursesByIds(packages.stream()
                .map(UserCoursePackage::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        ChatDraft draft = new ChatDraft();
        draft.cards = packages.stream().map(item -> toPackageCard(item, courseMap.get(item.getCourseId()))).collect(Collectors.toList());
        draft.replyText = packages.isEmpty()
                ? "我还没有查到你名下的课包记录，如果你刚完成购买，可以稍后到“我的课包”里刷新查看。"
                : "我已经帮你把最近的课包整理出来了，包含剩余节数和有效期。你也可以点“我的课包”继续查看详细消课记录。";
        draft.actions.add(action("navigate", "我的课包", null, PACKAGE_ROUTE));
        draft.actions.add(action("navigate", "上课记录", null, CHECKIN_ROUTE));
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildCouponReply(Long userId, String message) {
        if (userId == null) {
            ChatDraft draft = buildLoginRequiredReply("登录后我才能帮你筛选当前账号下可用的优惠券。", COUPON_ROUTE);
            draft.replyText = "如果你只是想了解规则，当前系统支持全场券、课程券和商品券三种范围，是否可用主要取决于有效期、使用门槛和订单类型。";
            return draft;
        }

        List<UserCoupon> userCoupons = userCouponService.list(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0)
                        .orderByDesc(UserCoupon::getCreateTime)
                        .last("limit 5")
        );
        Map<Long, Coupon> couponMap = listCouponsByIds(userCoupons.stream()
                .map(UserCoupon::getCouponId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        ChatDraft draft = new ChatDraft();
        draft.cards = userCoupons.stream()
                .map(item -> toCouponCard(item, couponMap.get(item.getCouponId())))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        draft.replyText = draft.cards.isEmpty()
                ? "我暂时没有查到你当前可用的优惠券。你可以去“我的优惠券”里看看是否有未使用或刚领取的券。"
                : "我已经帮你筛出了当前账号下可用的优惠券，上面的卡片里有适用范围、优惠方式和有效期。下单前记得看清门槛和适用品类。";
        draft.actions.add(action("navigate", "我的优惠券", null, COUPON_ROUTE));
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildCheckinReply(Long userId) {
        if (userId == null) {
            return buildLoginRequiredReply("登录后我才能帮你查看最近的上课签到和消课记录。", CHECKIN_ROUTE);
        }
        List<CourseCheckin> checkins = courseCheckinService.list(
                new LambdaQueryWrapper<CourseCheckin>()
                        .eq(CourseCheckin::getUserId, userId)
                        .orderByDesc(CourseCheckin::getCheckinTime)
                        .last("limit 5")
        );
        ChatDraft draft = new ChatDraft();
        if (checkins.isEmpty()) {
            draft.replyText = "我还没有查到你的签到记录，等首次上课或完成团课签到后，这里就会同步展示。";
        } else {
            long attended = checkins.stream().filter(item -> Objects.equals(item.getStatus(), 1)).count();
            draft.replyText = "我查到了你最近 " + checkins.size() + " 条上课记录，其中正常出勤 " + attended + " 次。你可以继续到“上课记录”页查看完整明细。";
        }
        draft.actions.add(action("navigate", "上课记录", null, CHECKIN_ROUTE));
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildAccountReply(Long userId) {
        ChatDraft draft = new ChatDraft();
        draft.replyText = "账号相关问题我可以帮你处理常见引导，比如绑定手机号、修改资料、设置密码或重新登录。如果你是首次下单，建议先确保手机号已绑定，避免影响订单和退款操作。";
        draft.actions.add(action("navigate", "个人中心", null, "/pages/profile/profile"));
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null && !StringUtils.hasText(user.getPhone())) {
                draft.replyText = "你当前还没有绑定手机号。为了顺利下单、接收通知和处理售后，建议先完成手机号绑定。";
                draft.actions.add(action("navigate", "去绑定手机号", null, BIND_PHONE_ROUTE));
            }
        } else {
            draft.actions.add(action("navigate", "去登录", null, LOGIN_ROUTE));
        }
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildManualReply(Long userId, Long sessionId) {
        createHandover(sessionId, userId, "用户请求人工客服");
        ChatDraft draft = new ChatDraft();
        draft.replyText = "我已经帮你提交人工处理申请，后台客服台会看到这条会话并跟进。你也可以继续补充问题细节，方便后续处理。";
        draft.needHandover = true;
        draft.actions.add(action("handover", "已转人工", "pending", null));
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private ChatDraft buildGeneralReply(String message, Long userId) {
        ChatDraft draft = new ChatDraft();
        List<AiKnowledge> knowledges = findKnowledge("faq", message);
        String fallback = !knowledges.isEmpty()
                ? knowledges.get(0).getContent()
                : "我可以帮你处理课程推荐、团课排期、订单状态、课包剩余节数、优惠券和退款说明这些问题。你也可以直接点快捷问题继续咨询。";
        applyFinalReply(draft, "general_faq", message, userId, knowledges, Collections.emptyList(), fallback);
        draft.actions.add(action("navigate", "看课程", null, COURSE_ROUTE));
        draft.actions.add(action("handover", "转人工", "manual_service", null));
        draft.sourceType = StringUtils.hasText(draft.modelReply) ? "model" : "fallback";
        draft.hitRule = !knowledges.isEmpty();
        return draft;
    }

    private ChatDraft buildLoginRequiredReply(String reply, String route) {
        ChatDraft draft = new ChatDraft();
        draft.replyText = reply;
        draft.needLogin = true;
        draft.actions.add(action("navigate", "去登录", null, LOGIN_ROUTE));
        if (StringUtils.hasText(route)) {
            draft.actions.add(action("retry", "登录后重试", route, route));
        }
        draft.sourceType = "rule";
        draft.hitRule = true;
        return draft;
    }

    private void applyFinalReply(ChatDraft draft, String intent, String message, Long userId, List<AiKnowledge> knowledges, List<AiCard> cards, String fallback) {
        String modelReply = aiModelClient.generateReply(intent, message, buildUserProfileSummary(userId), knowledges, cards);
        draft.modelReply = modelReply;
        draft.replyText = StringUtils.hasText(modelReply) ? modelReply : fallback;
    }

    private List<AiKnowledge> findKnowledge(String category, String message) {
        List<AiKnowledge> knowledges = aiKnowledgeService.list(
                new LambdaQueryWrapper<AiKnowledge>()
                        .eq(AiKnowledge::getStatus, 1)
                        .orderByDesc(AiKnowledge::getPriority)
        );
        if (knowledges.isEmpty()) {
            return defaultKnowledges(category, message);
        }

        List<AiKnowledge> matched = knowledges.stream()
                .filter(item -> !StringUtils.hasText(category) || category.equalsIgnoreCase(item.getCategory()) || "faq".equalsIgnoreCase(item.getCategory()))
                .filter(item -> matchKnowledge(item, message))
                .limit(3)
                .collect(Collectors.toList());
        return matched.isEmpty() ? defaultKnowledges(category, message) : matched;
    }

    private boolean matchKnowledge(AiKnowledge knowledge, String message) {
        if (!StringUtils.hasText(knowledge.getKeywords())) {
            return StringUtils.hasText(knowledge.getTitle()) && message.contains(knowledge.getTitle());
        }
        String[] keywords = knowledge.getKeywords().split("[,，]");
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && message.contains(keyword.trim())) {
                return true;
            }
        }
        return false;
    }

    private List<AiKnowledge> defaultKnowledges(String category, String message) {
        List<AiKnowledge> defaults = new ArrayList<>();
        if ("refund".equals(category) || message.contains("退款")) {
            defaults.add(mockKnowledge("退款说明", "refund", "退款,售后", "课程订单在已支付或待排课阶段可发起退款申请，商品订单也可在订单页提交退款原因，由后台统一审核。", 10));
        }
        if ("course".equals(category) || message.contains("课程") || message.contains("团课") || message.contains("私教")) {
            defaults.add(mockKnowledge("课程咨询", "course", "课程,团课,私教", "系统支持私教课包和团课两种课程形态，私教更适合长期进阶，团课更适合灵活报名和体验。", 8));
        }
        if ("product".equals(category) || message.contains("商品") || message.contains("装备")) {
            defaults.add(mockKnowledge("商城咨询", "product", "商品,装备,补剂", "商城支持训练装备、护具和补剂等商品浏览与下单，可结合课程场景做搭配推荐。", 8));
        }
        if ("faq".equals(category) || defaults.isEmpty()) {
            defaults.add(mockKnowledge("系统能力", "faq", "订单,课包,优惠券,退款", "我可以帮你查询订单状态、课包剩余节数、可用优惠券，也能做课程和商品推荐，还支持转人工处理。", 6));
        }
        return defaults;
    }

    private AiKnowledge mockKnowledge(String title, String category, String keywords, String content, Integer priority) {
        AiKnowledge knowledge = new AiKnowledge();
        knowledge.setTitle(title);
        knowledge.setCategory(category);
        knowledge.setKeywords(keywords);
        knowledge.setContent(content);
        knowledge.setPriority(priority);
        knowledge.setStatus(1);
        return knowledge;
    }

    private String buildUserProfileSummary(Long userId) {
        if (userId == null) {
            return "当前用户未登录，只能返回通用推荐和公开咨询结果。";
        }
        User user = userService.getById(userId);
        if (user == null) {
            return "用户不存在。";
        }
        long orderCount = orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
        long packageCount = userCoursePackageService.count(new LambdaQueryWrapper<UserCoursePackage>().eq(UserCoursePackage::getUserId, userId));
        long couponCount = userCouponService.count(new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId).eq(UserCoupon::getStatus, 0));
        return "昵称=" + Optional.ofNullable(user.getNickName()).orElse("用户")
                + "，手机号已绑定=" + StringUtils.hasText(user.getPhone())
                + "，订单数=" + orderCount
                + "，课包数=" + packageCount
                + "，未使用优惠券数=" + couponCount;
    }

    private String detectIntent(String message) {
        if (containsAny(message, "人工", "转人工", "人工客服")) return "manual_service";
        if (containsAny(message, "退款", "退费", "售后")) return "refund_help";
        if (containsAny(message, "课包", "剩几节", "剩余节数")) return "package_query";
        if (containsAny(message, "优惠券", "优惠", "折扣")) return "coupon_query";
        if (containsAny(message, "签到", "上课记录", "消课")) return "checkin_query";
        if (containsAny(message, "绑定手机号", "手机号", "密码", "登录", "账号")) return "account_help";
        if (containsAny(message, "订单", "支付", "发货", "已买")) return "order_query";
        if ((message.contains("团课") || message.contains("排课") || message.contains("名额"))
                && containsAny(message, "什么时候", "时间", "有名额", "场次", "最近")) return "course_schedule_query";
        if (containsAny(message, "推荐", "适合我", "买什么")
                && containsAny(message, "商品", "装备", "补剂")) return "product_recommend";
        if (containsAny(message, "推荐", "适合我", "报什么")
                && containsAny(message, "课程", "团课", "私教", "课包")) return "course_recommend";
        return "general_faq";
    }

    private BigDecimal estimateConfidence(String intent, String message) {
        if ("general_faq".equals(intent)) {
            return BigDecimal.valueOf(0.55);
        }
        int keywords = 0;
        if (containsAny(message, "订单", "退款", "课包", "优惠券", "团课", "私教", "签到", "账号", "人工")) {
            keywords++;
        }
        if (containsAny(message, "推荐", "查询", "最近", "还有", "剩余", "怎么")) {
            keywords++;
        }
        return BigDecimal.valueOf(Math.min(0.95, 0.65 + keywords * 0.1)).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean containsAny(String source, String... terms) {
        for (String term : terms) {
            if (source.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private AiSessionItemVO toSessionItem(AiSession session) {
        AiSessionItemVO itemVO = new AiSessionItemVO();
        itemVO.setSessionId(session.getId());
        itemVO.setTitle(session.getTitle());
        itemVO.setLastQuestion(session.getLastQuestion());
        itemVO.setLastReply(session.getLastReply());
        itemVO.setLastIntent(session.getLastIntent());
        itemVO.setStatus(session.getStatus());
        itemVO.setNeedHandover(session.getNeedHandover());
        itemVO.setLastMessageTime(session.getLastMessageTime());
        return itemVO;
    }

    private boolean matchesAdminKeyword(AiSession session, User user, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        return containsIgnoreCase(session.getTitle(), keyword)
                || containsIgnoreCase(session.getLastQuestion(), keyword)
                || containsIgnoreCase(session.getLastReply(), keyword)
                || containsIgnoreCase(user == null ? null : user.getNickName(), keyword)
                || containsIgnoreCase(user == null ? null : user.getPhone(), keyword);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return StringUtils.hasText(source) && source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private String buildConversationKey(Long userId, Long sessionId) {
        return userId != null ? "user_" + userId : "guest_" + sessionId;
    }

    private AiSessionDetailVO buildSessionDetail(AiSession session) {
        AiSessionDetailVO detailVO = new AiSessionDetailVO();
        detailVO.setSessionId(session.getId());
        detailVO.setTitle(session.getTitle());
        detailVO.setStatus(session.getStatus());
        detailVO.setNeedHandover(session.getNeedHandover());
        detailVO.setLastIntent(session.getLastIntent());

        List<AiMessage> messages = aiMessageService.list(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, session.getId())
                        .orderByAsc(AiMessage::getCreateTime)
        );
        detailVO.setMessages(messages.stream().map(this::toMessageVO).collect(Collectors.toList()));
        return detailVO;
    }

    private AiMessageVO toMessageVO(AiMessage message) {
        AiMessageVO vo = new AiMessageVO();
        vo.setId(message.getId());
        vo.setRole(message.getRole());
        vo.setContent(message.getContent());
        vo.setReplyText(message.getReplyText());
        vo.setIntent(message.getIntent());
        vo.setConfidence(message.getConfidence());
        vo.setSourceType(message.getSourceType());
        vo.setCards(readCards(message.getCardsJson()));
        vo.setActions(readActions(message.getActionsJson()));
        vo.setNeedHandover(Objects.equals(message.getNeedHandover(), 1));
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private Map<Long, String> loadOrderItemNameMap(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyMap();
        }
        return orderItemService.list(
                        new LambdaQueryWrapper<OrderItem>()
                                .in(OrderItem::getOrderId, orders.stream().map(Order::getId).collect(Collectors.toSet()))
                                .orderByAsc(OrderItem::getId)
                ).stream()
                .collect(Collectors.toMap(OrderItem::getOrderId, OrderItem::getItemName, (left, right) -> left));
    }

    private Map<Long, User> listUsersByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return userService.listByIds(ids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, AiSession> listSessionsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return aiSessionService.listByIds(ids).stream()
                .collect(Collectors.toMap(AiSession::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Course> listCoursesByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return courseService.listByIds(ids).stream()
                .collect(Collectors.toMap(Course::getId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, Coupon> listCouponsByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return couponService.listByIds(ids).stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity(), (left, right) -> left));
    }

    private AiCard toCourseCard(Course course) {
        AiCard card = new AiCard();
        card.setType("course");
        card.setId(String.valueOf(course.getId()));
        card.setTitle(course.getName());
        card.setSubtitle(Optional.ofNullable(course.getDescription()).orElse(course.getType() == 2 ? "团课" : "私教课包"));
        card.setImage(course.getPic());
        card.setPrice(formatPrice(course.getPrice()));
        card.setMeta((Objects.equals(course.getType(), 2) ? "团课" : "私教") + " · 销量 " + Optional.ofNullable(course.getSales()).orElse(0));
        card.setRoute(Objects.equals(course.getType(), 2)
                ? "/pages/course/course-detail-group?id=" + course.getId()
                : "/pages/course/course-detail-private?id=" + course.getId());
        return card;
    }

    private AiCard toScheduleCard(CourseSchedule schedule, Course course) {
        AiCard card = new AiCard();
        card.setType("course");
        card.setId(String.valueOf(schedule.getId()));
        card.setTitle(course != null ? course.getName() : "团课场次");
        card.setSubtitle("开课时间 " + formatDateTime(schedule.getStartTime()) + " · " + Optional.ofNullable(schedule.getLocation()).orElse("待定"));
        card.setPrice(course != null ? formatPrice(course.getPrice()) : null);
        card.setMeta("剩余名额 " + Math.max(0, Optional.ofNullable(schedule.getTotalSeats()).orElse(0) - Optional.ofNullable(schedule.getEnrolledSeats()).orElse(0)));
        if (course != null) {
            card.setImage(course.getPic());
            card.setRoute("/pages/course/course-detail-group?id=" + course.getId());
        }
        return card;
    }

    private AiCard toProductCard(Prod prod) {
        AiCard card = new AiCard();
        card.setType("product");
        card.setId(String.valueOf(prod.getId()));
        card.setTitle(prod.getName());
        card.setSubtitle(Optional.ofNullable(prod.getDescription()).orElse("训练装备推荐"));
        card.setImage(prod.getPic());
        card.setPrice(formatPrice(prod.getPrice()));
        card.setMeta("销量 " + Optional.ofNullable(prod.getSales()).orElse(0));
        card.setRoute("/pages/mall/product-detail?id=" + prod.getId());
        return card;
    }

    private AiCard toOrderCard(Order order, String itemName) {
        AiCard card = new AiCard();
        card.setType("order");
        card.setId(String.valueOf(order.getId()));
        card.setTitle(StringUtils.hasText(itemName) ? itemName : "订单 " + order.getOrderNumber());
        card.setSubtitle("订单号 " + order.getOrderNumber());
        card.setPrice(formatPrice(order.getActualAmount()));
        card.setMeta(statusText(order.getStatus()) + " · " + formatDateTime(order.getCreateTime()));
        card.setRoute(Objects.equals(order.getOrderType(), 2) ? PRODUCT_ORDER_ROUTE : COURSE_ORDER_ROUTE);
        return card;
    }

    private AiCard toPackageCard(UserCoursePackage pkg, Course course) {
        AiCard card = new AiCard();
        card.setType("package");
        card.setId(String.valueOf(pkg.getId()));
        card.setTitle(course != null ? course.getName() : "课程课包");
        card.setSubtitle("剩余 " + Math.max(0, Optional.ofNullable(pkg.getTotalLessons()).orElse(0) - Optional.ofNullable(pkg.getUsedLessons()).orElse(0)) + " 节");
        card.setPrice(course != null ? formatPrice(course.getPrice()) : null);
        card.setMeta("有效期至 " + formatDateTime(pkg.getExpireTime()));
        card.setRoute(PACKAGE_ROUTE);
        return card;
    }

    private AiCard toCouponCard(UserCoupon userCoupon, Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        AiCard card = new AiCard();
        card.setType("coupon");
        card.setId(String.valueOf(userCoupon.getId()));
        card.setTitle(coupon.getName());
        card.setSubtitle(couponScopeText(coupon.getScope()) + " · " + couponTypeText(coupon));
        card.setMeta("有效期至 " + formatDateTime(coupon.getEndTime()));
        card.setRoute(COUPON_ROUTE);
        return card;
    }

    private AiAction action(String type, String label, String value, String route) {
        AiAction action = new AiAction();
        action.setType(type);
        action.setLabel(label);
        action.setValue(value);
        action.setRoute(route);
        return action;
    }

    private String couponTypeText(Coupon coupon) {
        if (Objects.equals(coupon.getType(), 1)) {
            return "满 " + coupon.getMinAmount() + " 减 " + coupon.getDiscount();
        }
        if (Objects.equals(coupon.getType(), 2)) {
            return "满 " + coupon.getMinAmount() + " 打 " + coupon.getDiscountRatio().multiply(BigDecimal.TEN).stripTrailingZeros().toPlainString() + " 折";
        }
        return "无门槛立减 " + coupon.getDiscount();
    }

    private String couponScopeText(Integer scope) {
        return switch (scope == null ? 1 : scope) {
            case 2 -> "仅课程";
            case 3 -> "仅商品";
            default -> "全场通用";
        };
    }

    private String formatPrice(BigDecimal price) {
        return price == null ? null : "¥" + price.stripTrailingZeros().toPlainString();
    }

    private String formatDateTime(LocalDateTime time) {
        return time == null ? "-" : DATE_TIME_FORMATTER.format(time);
    }

    private String statusText(Integer status) {
        return switch (status == null ? 0 : status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "待处理";
            case 4 -> "已完成";
            case 5 -> "已取消";
            case 6 -> "退款中";
            case 7 -> "已退款";
            default -> "处理中";
        };
    }

    private String writeJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("写入 AI JSON 失败: {}", e.getMessage());
            return "[]";
        }
    }

    private List<AiCard> readCards(String cardsJson) {
        if (!StringUtils.hasText(cardsJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(cardsJson, new TypeReference<List<AiCard>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<AiAction> readActions(String actionsJson) {
        if (!StringUtils.hasText(actionsJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(actionsJson, new TypeReference<List<AiAction>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String truncate(String text, int maxLength) {
        if (!StringUtils.hasText(text) || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private static class ChatDraft {
        private String replyText;
        private String modelReply;
        private List<AiCard> cards = new ArrayList<>();
        private List<AiAction> actions = new ArrayList<>();
        private boolean needLogin;
        private boolean needHandover;
        private boolean hitRule;
        private String sourceType = "rule";
    }
}
