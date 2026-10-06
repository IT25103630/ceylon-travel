package lk.ceylontravel.model;

import java.time.LocalDateTime;

/**
 * Function: Places Management
 * Member Name: Fernando M. G. D. W.
 * Student ID: IT25101548
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class Place {
    private Long id;
    private Long submittedBy;
    private String submittedName;
    private String name;
    private String description;
    private String location;
    private String category; // 'Heritage', 'Nature', 'Beach', 'Adventure', 'City'
    private String tips;
    private String imageUrl;
    private String status; // 'PENDING', 'APPROVED', 'REJECTED', 'ARCHIVED'
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private Double rating;

    public Place() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long submittedBy) { this.submittedBy = submittedBy; }

    public String getSubmittedName() { return submittedName; }
    public void setSubmittedName(String submittedName) { this.submittedName = submittedName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Long reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
