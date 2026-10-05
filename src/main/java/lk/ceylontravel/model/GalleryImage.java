package lk.ceylontravel.model;

import java.time.LocalDateTime;

/**
 * Function: Gallery and Emergency Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class GalleryImage {
    private Long id;
    private Long userId;
    private Long placeId;
    private String imageUrl;
    private String caption;
    private LocalDateTime createdAt;
    private String authorName;
    private String placeName;

    public GalleryImage() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
}
