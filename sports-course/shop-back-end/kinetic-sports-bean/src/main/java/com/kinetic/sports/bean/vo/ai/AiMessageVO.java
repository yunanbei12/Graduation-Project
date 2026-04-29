package com.kinetic.sports.bean.vo.ai;

import com.kinetic.sports.bean.dto.ai.AiAction;
import com.kinetic.sports.bean.dto.ai.AiCard;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiMessageVO {

    private Long id;

    private String role;

    private String content;

    private String replyText;

    private String intent;

    private BigDecimal confidence;

    private String sourceType;

    private List<AiCard> cards = new ArrayList<>();

    private List<AiAction> actions = new ArrayList<>();

    private Boolean needHandover = false;

    private LocalDateTime createTime;
}
