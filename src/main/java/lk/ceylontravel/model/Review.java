package lk.ceylontravel.model;

import java.time.LocalDateTime;

/**
 * Function: Review and Feedback Management
 * Member Name: Anuththara K. G. H.
 * Student ID: IT25103422
 * Role: Product Owner
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class Review {
    private Long id;
    private Long authorId;
    private Long bookingId;
    private Long placeId;
    private int rating;
    private String comment;
    private String status; // 'PENDING', 'PUBLISHED', 'REJECTED'
    private LocalDateTime createdAt;
    private String authorName;
    private Long guideId;
    private String targetName;

    public Review() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public Long getGuideId() { return guideId; }
    public void setGuideId(Long guideId) { this.guideId = guideId; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
}
