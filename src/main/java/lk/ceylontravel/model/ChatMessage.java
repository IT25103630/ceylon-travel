package lk.ceylontravel.model;

import java.time.LocalDateTime;

/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering (SLIIT)
 */
public class ChatMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String body;
    private boolean edited;
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(Long id, Long conversationId, Long senderId, String senderName, String body, boolean edited, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.body = body;
        this.edited = edited;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
