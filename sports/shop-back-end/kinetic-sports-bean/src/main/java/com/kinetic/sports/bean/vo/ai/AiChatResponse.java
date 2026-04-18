package com.kinetic.sports.bean.vo.ai;

import com.kinetic.sports.bean.dto.ai.AiAction;
import com.kinetic.sports.bean.dto.ai.AiCard;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiChatResponse {

    private Long sessionId;

    private Long messageId;

    private String replyText;

    private String intent;

    private BigDecimal confidence;

    private String sourceType;

    private List<AiCard> cards = new ArrayList<>();

    private List<AiAction> actions = new ArrayList<>();

    private Boolean needLogin = false;

    private Boolean needHandover = false;
}
