package com.kinetic.sports.bean.dto.ai;

import lombok.Data;

@Data
public class AiChatRequest {

    private Long sessionId;

    private String guestToken;

    private String message;
}
