package com.sportedu.backend.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("user_profiles")
public class UserProfile {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Integer preferredType;
    private BigDecimal priceRangeMin;
    private BigDecimal priceRangeMax;
    private String preferredLocation;
    private String sportPreferences;
    private String timePreference;
    private Integer priceSensitiveLevel;
    private Integer purchaseCount;
    private Integer profileSource;
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPreferredType() {
        return preferredType;
    }

    public void setPreferredType(Integer preferredType) {
        this.preferredType = preferredType;
    }

    public BigDecimal getPriceRangeMin() {
        return priceRangeMin;
    }

    public void setPriceRangeMin(BigDecimal priceRangeMin) {
        this.priceRangeMin = priceRangeMin;
    }

    public BigDecimal getPriceRangeMax() {
        return priceRangeMax;
    }

    public void setPriceRangeMax(BigDecimal priceRangeMax) {
        this.priceRangeMax = priceRangeMax;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public String getSportPreferences() {
        return sportPreferences;
    }

    public void setSportPreferences(String sportPreferences) {
        this.sportPreferences = sportPreferences;
    }

    public String getTimePreference() {
        return timePreference;
    }

    public void setTimePreference(String timePreference) {
        this.timePreference = timePreference;
    }

    public Integer getPriceSensitiveLevel() {
        return priceSensitiveLevel;
    }

    public void setPriceSensitiveLevel(Integer priceSensitiveLevel) {
        this.priceSensitiveLevel = priceSensitiveLevel;
    }

    public Integer getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(Integer purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public Integer getProfileSource() {
        return profileSource;
    }

    public void setProfileSource(Integer profileSource) {
        this.profileSource = profileSource;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
