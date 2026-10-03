package lk.ceylontravel.service;

import java.util.*;
import lk.ceylontravel.dao.ChatDao;
import lk.ceylontravel.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class ChatService {
    private final ChatDao chatDao;
    private final UserDao userDao;
    private final Events events;

    public ChatService(ChatDao chatDao, UserDao userDao, Events events) {
        this.chatDao = chatDao;
        this.userDao = userDao;
        this.events = events;
    }

    public List<Map<String, Object>> getUserConversations(long userId) {
        return chatDao.findConversationsByUserId(userId);
    }

    public Map<String, Object> getConversationForUser(long conversationId, long userId) {
        var room = chatDao.findConversationById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Conversation not found"));
        long touristId = ((Number) room.get("tourist_id")).longValue();
        long guideId = ((Number) room.get("guide_id")).longValue();
        if (userId != touristId && userId != guideId) {
            throw new ResponseStatusException(FORBIDDEN, "Private conversation");
        }
        return room;
    }

    @Transactional
    public long startConversation(long touristId, long guideId, String initialMessage) {
        var guideProfile = userDao.findGuideProfile(guideId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Guide not found"));

        Object verifiedVal = guideProfile.get("verified");
        boolean verified = Boolean.TRUE.equals(verifiedVal) || "1".equals(String.valueOf(verifiedVal)) || "true".equalsIgnoreCase(String.valueOf(verifiedVal));
        if (!verified) {
            throw new ResponseStatusException(CONFLICT, "Guide is not verified");
        }

        var existing = chatDao.findConversationBetween(touristId, guideId);
        long conversationId = existing.map(c -> ((Number) c.get("id")).longValue())
                .orElseGet(() -> chatDao.createConversation(touristId, guideId));

        chatDao.createMessage(conversationId, touristId, initialMessage.trim());
        events.notify(guideId, "You have a new message");
        events.changed(guideId, "chat");

        return conversationId;
    }

    public List<Map<String, Object>> getMessages(long conversationId, long userId) {
        getConversationForUser(conversationId, userId);
        return chatDao.findMessagesByConversationId(conversationId);
    }

    @Transactional
    public long sendMessage(long conversationId, long senderId, String body) {
        var room = getConversationForUser(conversationId, senderId);
        long messageId = chatDao.createMessage(conversationId, senderId, body.trim());

        long touristId = ((Number) room.get("tourist_id")).longValue();
        long guideId = ((Number) room.get("guide_id")).longValue();
        events.changed(touristId, "chat");
        events.changed(guideId, "chat");

        return messageId;
    }

    @Transactional
    public void editMessage(long messageId, long userId, String newBody) {
        var msg = chatDao.findMessageById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));
        long senderId = ((Number) msg.get("sender_id")).longValue();
        if (senderId != userId) {
            throw new ResponseStatusException(FORBIDDEN, "Only the sender can change a message");
        }

        chatDao.updateMessage(messageId, newBody.trim());

        long convId = ((Number) msg.get("conversation_id")).longValue();
        var room = chatDao.findConversationById(convId).orElse(null);
        if (room != null) {
            events.changed(((Number) room.get("tourist_id")).longValue(), "chat");
            events.changed(((Number) room.get("guide_id")).longValue(), "chat");
        }
    }

    @Transactional
    public void deleteMessage(long messageId, long userId) {
        var msg = chatDao.findMessageById(messageId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Message not found"));
        long senderId = ((Number) msg.get("sender_id")).longValue();
        if (senderId != userId) {
            throw new ResponseStatusException(FORBIDDEN, "Only the sender can change a message");
        }

        long convId = ((Number) msg.get("conversation_id")).longValue();
        chatDao.deleteMessage(messageId);

        var room = chatDao.findConversationById(convId).orElse(null);
        if (room != null) {
            events.changed(((Number) room.get("tourist_id")).longValue(), "chat");
            events.changed(((Number) room.get("guide_id")).longValue(), "chat");
        }
    }
}
