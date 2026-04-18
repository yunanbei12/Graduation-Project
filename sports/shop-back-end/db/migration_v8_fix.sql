-- v8 fix: 修复 course_schedule 表字段允许 NULL
ALTER TABLE `course_schedule`
    MODIFY COLUMN `start_time` DATETIME NULL COMMENT '兼容旧数据，新数据用 schedule_date',
    MODIFY COLUMN `end_time` DATETIME NULL COMMENT '兼容旧数据',
    MODIFY COLUMN `location` VARCHAR(200) NULL COMMENT '兼容旧数据，地点现归一化到课程模板';
