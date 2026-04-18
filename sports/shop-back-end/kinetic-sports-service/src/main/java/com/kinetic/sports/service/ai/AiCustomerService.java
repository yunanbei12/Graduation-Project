package com.kinetic.sports.service.ai;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kinetic.sports.bean.dto.ai.AiChatRequest;
import com.kinetic.sports.bean.vo.ai.AiChatResponse;
import com.kinetic.sports.bean.vo.ai.AiSessionDetailVO;
import com.kinetic.sports.bean.vo.ai.AiSessionItemVO;

import java.util.List;
import java.util.Map;

public interface AiCustomerService {

    AiChatResponse chat(AiChatRequest request, Long userId);

    List<AiSessionItemVO> listUserSessions(Long userId);

    AiSessionDetailVO getUserSessionDetail(Long sessionId, Long userId);

    AiSessionDetailVO getSessionDetail(Long sessionId);

    List<AiSessionItemVO> getAdminSessionHistory(Long sessionId);

    void submitFeedback(Long sessionId, Long messageId, Integer rating, String comment, Long userId);

    void createHandover(Long sessionId, Long userId, String remark);

    void adminReply(Long sessionId, String replyText, String adminName, boolean resolveAfterReply, boolean terminateAfterReply);

    void resolveSession(Long sessionId, String operatorName, String remark);

    void terminateSession(Long sessionId, String operatorName, String remark);

    Page<Map<String, Object>> getAdminSessionPage(Page<?> page, String keyword, String intent, Integer status, Integer needHandover);

    List<Map<String, Object>> getHandoverList(Integer status);

    Map<String, Object> getStatsSummary();
}
