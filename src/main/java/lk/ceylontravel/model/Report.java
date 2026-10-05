package lk.ceylontravel.model;

import java.time.LocalDateTime;

/**
 * Function: Gallery and Emergency Management (Emergency/Complaint Reporting)
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class Report {
    private Long id;
    private Long userId;
    private String type; // 'EMERGENCY', 'COMPLAINT', 'SUPPORT'
    private String subject;
    private String description;
    private String location;
    private String status; // 'OPEN', 'IN_PROGRESS', 'RESOLVED'
    private String response;
    private Long handledBy;
    private LocalDateTime createdAt;
    private String authorName;

    public Report() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Long getHandledBy() { return handledBy; }
    public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
