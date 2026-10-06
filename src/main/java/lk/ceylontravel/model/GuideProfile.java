package lk.ceylontravel.model;

import java.math.BigDecimal;

/**
 * Function: Guide Profile Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class GuideProfile {
    private Long userId;
    private String name;
    private String bio;
    private String location;
    private BigDecimal dailyRate;
    private boolean verified;
    private String imageUrl;
    private String languages;
    private Double rating;
    private Integer reviewCount;

    public GuideProfile() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLanguages() { return languages; }
    public void setLanguages(String languages) { this.languages = languages; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
}
