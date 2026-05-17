package com.kinetic.sports.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.model.*;
import com.kinetic.sports.bean.vo.ai.AiAdminUserVO;
import com.kinetic.sports.bean.vo.ai.AiSessionDetailVO;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.*;
import com.kinetic.sports.service.ai.AiCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
public class AiAdminController {

    private final AiCustomerService aiCustomerService;
    private final AiSessionService aiSessionService;
    private final AiKnowledgeService aiKnowledgeService;
    private final AiHandoverService aiHandoverService;
    private final AiFeedbackService aiFeedbackService;
    private final UserService userService;
    private final SysUserService sysUserService;


    @GetMapping("/session/list")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<Page<Map<String, Object>>> sessionList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer needHandover) {
        Page<Map<String, Object>> page = aiCustomerService.getAdminSessionPage(new Page<>(pageNum, pageSize), keyword, intent, status, needHandover);
        return ServerResponseEntity.success(page);
    }


    @GetMapping("/session/{id}")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<Map<String, Object>> sessionDetail(@PathVariable Long id) {
        AiSession session = aiSessionService.getById(id);
        AiSessionDetailVO detail = aiCustomerService.getSessionDetail(id);
        User user = session != null && session.getUserId() != null ? userService.getById(session.getUserId()) : null;
        AiAdminUserVO safeUser = buildAdminUser(user);
        List<AiHandover> handovers = aiHandoverService.list(new LambdaQueryWrapper<AiHandover>()
                .eq(AiHandover::getSessionId, id)
                .orderByDesc(AiHandover::getCreateTime));
        List<AiFeedback> feedbacks = aiFeedbackService.list(new LambdaQueryWrapper<AiFeedback>()
                .eq(AiFeedback::getSessionId, id)
                .orderByDesc(AiFeedback::getCreateTime));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session", session);
        result.put("detail", detail);
        result.put("user", safeUser);
        result.put("conversationKey", session != null && session.getUserId() != null ? "user_" + session.getUserId() : "guest_" + id);
        result.put("sessionHistory", aiCustomerService.getAdminSessionHistory(id));
        result.put("handovers", handovers);
        result.put("feedbacks", feedbacks);
        return ServerResponseEntity.success(result);
    }


    @PutMapping("/session/{id}/status")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<Void> updateSessionStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        AiSession session = aiSessionService.getById(id);
        if (session == null) {
            return ServerResponseEntity.fail("会话不存在");
        }
        Long adminUserId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserService.getById(adminUserId);
        String adminName = sysUser != null && sysUser.getNickName() != null ? sysUser.getNickName() : "管理员";
        Integer status = Integer.parseInt(String.valueOf(params.getOrDefault("status", session.getStatus())));
        Integer needHandover = params.get("needHandover") == null ? session.getNeedHandover() : Integer.parseInt(String.valueOf(params.get("needHandover")));
        String remark = params.get("remark") == null ? null : String.valueOf(params.get("remark"));
        if (status == 1) {
            aiCustomerService.resolveSession(id, adminName, remark);
            return ServerResponseEntity.success();
        }
        if (status == 3) {
            aiCustomerService.terminateSession(id, adminName, remark);
            return ServerResponseEntity.success();
        }
        if (status != 2) {
            needHandover = 0;
        }
        session.setStatus(status);
        session.setNeedHandover(needHandover);
        aiSessionService.updateById(session);
        if (needHandover != null && needHandover == 0) {
            List<AiHandover> pending = aiHandoverService.list(new LambdaQueryWrapper<AiHandover>()
                    .eq(AiHandover::getSessionId, id)
                    .eq(AiHandover::getStatus, 0));
            for (AiHandover handover : pending) {
                handover.setStatus(1);
                handover.setAdminRemark(remark != null && !remark.isBlank() ? remark : "后台已调整会话状态并结束本次人工处理");
                handover.setHandledBy(adminName);
                handover.setHandledTime(LocalDateTime.now());
                aiHandoverService.updateById(handover);
            }
        }
        return ServerResponseEntity.success();
    }

    @PostMapping("/session/{id}/reply")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<Void> reply(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        String replyText = params.get("replyText") == null ? null : String.valueOf(params.get("replyText"));
        boolean resolveAfterReply = Boolean.parseBoolean(String.valueOf(params.getOrDefault("resolveAfterReply", false)));
        boolean terminateAfterReply = Boolean.parseBoolean(String.valueOf(params.getOrDefault("terminateAfterReply", false)));
        Long adminUserId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserService.getById(adminUserId);
        String adminName = sysUser != null && sysUser.getNickName() != null ? sysUser.getNickName() : "管理员";
        aiCustomerService.adminReply(id, replyText, adminName, resolveAfterReply, terminateAfterReply);
        return ServerResponseEntity.success();
    }

    @PostMapping("/session/{id}/terminate")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<Void> terminate(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        Long adminUserId = StpUtil.getLoginIdAsLong();
        SysUser sysUser = sysUserService.getById(adminUserId);
        String adminName = sysUser != null && sysUser.getNickName() != null ? sysUser.getNickName() : "管理员";
        String remark = params == null || params.get("remark") == null ? null : String.valueOf(params.get("remark"));
        aiCustomerService.terminateSession(id, adminName, remark);
        return ServerResponseEntity.success();
    }

    @GetMapping("/handover/list")
    @SaCheckPermission("ai:session")
    public ServerResponseEntity<List<Map<String, Object>>> handoverList(@RequestParam(required = false) Integer status) {
        return ServerResponseEntity.success(aiCustomerService.getHandoverList(status));
    }

    @GetMapping("/knowledge/list")
    @SaCheckPermission("ai:knowledge")
    public ServerResponseEntity<Page<AiKnowledge>> knowledgeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        Page<AiKnowledge> page = aiKnowledgeService.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiKnowledge>()
                        .and(keyword != null && !keyword.isBlank(), wrapper -> wrapper
                                .like(AiKnowledge::getTitle, keyword)
                                .or()
                                .like(AiKnowledge::getKeywords, keyword)
                                .or()
                                .like(AiKnowledge::getContent, keyword))
                        .eq(category != null && !category.isBlank(), AiKnowledge::getCategory, category)
                        .eq(status != null, AiKnowledge::getStatus, status)
                        .orderByDesc(AiKnowledge::getPriority)
                        .orderByDesc(AiKnowledge::getCreateTime));
        return ServerResponseEntity.success(page);
    }

    @PostMapping("/knowledge")
    @SaCheckPermission("ai:knowledge")
    public ServerResponseEntity<Void> createKnowledge(@RequestBody AiKnowledge knowledge) {
        aiKnowledgeService.save(knowledge);
        return ServerResponseEntity.success();
    }

    @PutMapping("/knowledge")
    @SaCheckPermission("ai:knowledge")
    public ServerResponseEntity<Void> updateKnowledge(@RequestBody AiKnowledge knowledge) {
        aiKnowledgeService.updateById(knowledge);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/knowledge/{id}")
    @SaCheckPermission("ai:knowledge")
    public ServerResponseEntity<Void> deleteKnowledge(@PathVariable Long id) {
        aiKnowledgeService.removeById(id);
        return ServerResponseEntity.success();
    }

    @GetMapping("/stats/summary")
    @SaCheckPermission("ai:stats")
    public ServerResponseEntity<Map<String, Object>> statsSummary() {
        return ServerResponseEntity.success(aiCustomerService.getStatsSummary());
    }

    private AiAdminUserVO buildAdminUser(User user) {
        if (user == null) {
            return null;
        }
        AiAdminUserVO result = new AiAdminUserVO();
        result.setId(user.getId());
        result.setNickName(user.getNickName());
        result.setAvatarUrl(user.getAvatarUrl());
        result.setPhone(user.getPhone());
        return result;
    }
}
