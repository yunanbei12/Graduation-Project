package com.kinetic.sports.bean.vo.ai;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiSessionItemVO {

    private Long sessionId;

    private String title;

    private String lastQuestion;

    private String lastReply;

    private String lastIntent;

    private Integer status;

    private Integer needHandover;

    private LocalDateTime lastMessageTime;
}
