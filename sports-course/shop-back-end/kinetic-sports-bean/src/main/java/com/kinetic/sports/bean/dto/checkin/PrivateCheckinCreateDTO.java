package com.kinetic.sports.bean.dto.checkin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateCheckinCreateDTO {

    private Long userId;

    private Long courseId;

    private Long coachId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkinTime;

    private String remark;
}
