package com.sportedu.backend.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("courses")
public class Course {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String courseName;
    private String courseCode;
    private Integer courseType;
    private String sportType;
    private String coverUrl;
    private String description;
    private String detailImages;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer lessonCount;
    private Integer validityDays;
    private Integer isDoorToDoor;
    private String serviceAreas;
    private String fixedScheduleDesc;
    private String fixedLocation;
    private Integer maxParticipants;
    private Integer groupSuccessCount;
    private Integer operationWeight;
    private Integer status;
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Integer getCourseType() {
        return courseType;
    }

    public void setCourseType(Integer courseType) {
        this.courseType = courseType;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailImages() {
        return detailImages;
    }

    public void setDetailImages(String detailImages) {
        this.detailImages = detailImages;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(Integer lessonCount) {
        this.lessonCount = lessonCount;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
    }

    public Integer getIsDoorToDoor() {
        return isDoorToDoor;
    }

    public void setIsDoorToDoor(Integer isDoorToDoor) {
        this.isDoorToDoor = isDoorToDoor;
    }

    public String getServiceAreas() {
        return serviceAreas;
    }

    public void setServiceAreas(String serviceAreas) {
        this.serviceAreas = serviceAreas;
    }

    public String getFixedScheduleDesc() {
        return fixedScheduleDesc;
    }

    public void setFixedScheduleDesc(String fixedScheduleDesc) {
        this.fixedScheduleDesc = fixedScheduleDesc;
    }

    public String getFixedLocation() {
        return fixedLocation;
    }

    public void setFixedLocation(String fixedLocation) {
        this.fixedLocation = fixedLocation;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getGroupSuccessCount() {
        return groupSuccessCount;
    }

    public void setGroupSuccessCount(Integer groupSuccessCount) {
        this.groupSuccessCount = groupSuccessCount;
    }

    public Integer getOperationWeight() {
        return operationWeight;
    }

    public void setOperationWeight(Integer operationWeight) {
        this.operationWeight = operationWeight;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
