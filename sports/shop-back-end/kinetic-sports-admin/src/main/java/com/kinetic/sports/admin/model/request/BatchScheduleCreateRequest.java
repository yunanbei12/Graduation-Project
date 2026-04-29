package com.kinetic.sports.admin.model.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchScheduleCreateRequest {

    private Long courseId;

    private Long coachId;

    private List<LocalDate> scheduleDates;

    private Integer totalSeats;
}
