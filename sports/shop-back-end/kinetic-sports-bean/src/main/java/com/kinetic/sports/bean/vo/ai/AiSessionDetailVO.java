package com.kinetic.sports.bean.vo.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiSessionDetailVO {

    private Long sessionId;

    private String title;

    private Integer status;

    private Integer needHandover;

    private String lastIntent;

    private List<AiMessageVO> messages = new ArrayList<>();
}
