package com.sportedu.backend.teaching.dto;

import java.math.BigDecimal;
import java.util.List;

public record CoachResponse(
    Long coachId,
    String coachName,
    String phone,
    String idCardNo,
    Integer gender,
    List<String> sportItems,
    String introduction,
    BigDecimal hourlyRate,
    List<String> availableTimes,
    Integer status
) {
}
