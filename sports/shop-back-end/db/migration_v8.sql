-- v8: 团课时间段迁移
-- 1. course 表：添加时间段和地点字段（团课专用）
ALTER TABLE `course`
    ADD COLUMN `start_hour` VARCHAR(8) DEFAULT NULL COMMENT '团课每天开始时间 如 16:00',
    ADD COLUMN `end_hour` VARCHAR(8) DEFAULT NULL COMMENT '团课每天结束时间 如 17:00',
    ADD COLUMN `location` VARCHAR(200) DEFAULT NULL COMMENT '团课上课地点';

-- 2. course_schedule 表：添加 schedule_date 字，并将 start_time/end_time/location 改为允许 NULL
ALTER TABLE `course_schedule`
    ADD COLUMN `schedule_date` DATE DEFAULT NULL COMMENT '排课日期（团课用，时间段从课程模板读取）',
    MODIFY COLUMN `start_time` DATETIME NULL COMMENT '兼容旧数据，新数据用 schedule_date',
    MODIFY COLUMN `end_time` DATETIME NULL COMMENT '兼容旧数据',
    MODIFY COLUMN `location` VARCHAR(200) NULL COMMENT '兼容旧数据，地点现归一化到课程模板';

-- 3. 将已有排课数据的 start_time 日期部分回填到 schedule_date
UPDATE `course_schedule` SET `schedule_date` = DATE(`start_time`) WHERE `schedule_date` IS NULL;
