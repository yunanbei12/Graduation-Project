package com.kinetic.sports.bean.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BatchScheduleCreateCommand {

    private Long courseId;

    private Long coachId;

    private List<LocalDate> scheduleDates;

    private Integer totalSeats;
}
