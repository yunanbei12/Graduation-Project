package com.kinetic.sports.api.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.kinetic.sports.bean.dto.ai.AiChatRequest;
import com.kinetic.sports.bean.vo.ai.AiChatResponse;
import com.kinetic.sports.bean.vo.ai.AiSessionDetailVO;
import com.kinetic.sports.bean.vo.ai.AiSessionItemVO;
import com.kinetic.sports.common.response.ServerResponseEntity;
import com.kinetic.sports.service.ai.AiCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ApiAiController {

    private final AiCustomerService aiCustomerService;

    //发送消息并获取 AI 回复
    @PostMapping("/chat")
    public ServerResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ServerResponseEntity.success(aiCustomerService.chat(request, userId));
    }

    //查询用户历史会话
    @GetMapping("/session/list")
    public ServerResponseEntity<List<AiSessionItemVO>> sessionList() {
        if (!StpUtil.isLogin()) {
            return ServerResponseEntity.fail(401, "请先登录");
        }
        return ServerResponseEntity.success(aiCustomerService.listUserSessions(StpUtil.getLoginIdAsLong()));
    }

    //查看会话详情
    @GetMapping("/session/{id}")
    public ServerResponseEntity<AiSessionDetailVO> sessionDetail(@PathVariable Long id,
                                                                 @RequestParam(required = false) String guestToken) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ServerResponseEntity.success(aiCustomerService.getClientSessionDetail(id, userId, guestToken));
    }

    //提交回复反馈
    @PostMapping("/session/{id}/feedback")
    public ServerResponseEntity<Void> feedback(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        if (!StpUtil.isLogin()) {
            return ServerResponseEntity.fail(401, "请先登录");
        }
        Long messageId = params.get("messageId") == null ? null : Long.parseLong(String.valueOf(params.get("messageId")));
        Integer rating = params.get("rating") == null ? 1 : Integer.parseInt(String.valueOf(params.get("rating")));
        String comment = params.get("comment") == null ? null : String.valueOf(params.get("comment"));
        aiCustomerService.submitFeedback(id, messageId, rating, comment, StpUtil.getLoginIdAsLong());
        return ServerResponseEntity.success();
    }

    //请求转人工
    @PostMapping("/session/{id}/handover")
    public ServerResponseEntity<Void> handover(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params) {
        String remark = params == null || params.get("remark") == null ? null : String.valueOf(params.get("remark"));
        String guestToken = params == null || params.get("guestToken") == null ? null : String.valueOf(params.get("guestToken"));
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        aiCustomerService.createHandover(id, userId, guestToken, remark);
        return ServerResponseEntity.success();
    }
}
