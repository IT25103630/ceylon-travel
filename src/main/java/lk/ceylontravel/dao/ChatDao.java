package lk.ceylontravel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import lk.ceylontravel.model.ChatMessage;
import lk.ceylontravel.model.Conversation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class ChatDao {
    private final JdbcTemplate jdbc;

    public ChatDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private long extractGeneratedId(GeneratedKeyHolder keyHolder) {
        var keys = keyHolder.getKeys();
        if (keys != null) {
            for (String key : List.of("id", "ID", "GENERATED_KEY")) {
                if (keys.containsKey(key) && keys.get(key) != null) {
                    return ((Number) keys.get(key)).longValue();
                }
            }
        }
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public List<Map<String, Object>> findConversationsByUserId(long userId) {
        String sql = "SELECT c.*, t.name AS tourist_name, g.name AS guide_name, " +
                "(SELECT body FROM messages m WHERE m.conversation_id = c.id ORDER BY m.id DESC LIMIT 1) AS last_message " +
                "FROM conversations c " +
                "JOIN users t ON t.id = c.tourist_id " +
                "JOIN users g ON g.id = c.guide_id " +
                "WHERE c.tourist_id = ? OR c.guide_id = ? " +
                "ORDER BY c.id DESC";
        // To support both SQL Server (TOP 1) and H2/MySQL (LIMIT 1), we can query without TOP/LIMIT inside subquery or use row query
        try {
            return jdbc.queryForList(sql, userId, userId);
        } catch (Exception e) {
            // SQL Server fallback
            String sqlMssql = "SELECT c.*, t.name AS tourist_name, g.name AS guide_name, " +
                    "(SELECT TOP 1 body FROM messages m WHERE m.conversation_id = c.id ORDER BY m.id DESC) AS last_message " +
                    "FROM conversations c " +
                    "JOIN users t ON t.id = c.tourist_id " +
                    "JOIN users g ON g.id = c.guide_id " +
                    "WHERE c.tourist_id = ? OR c.guide_id = ? " +
                    "ORDER BY c.id DESC";
            return jdbc.queryForList(sqlMssql, userId, userId);
        }
    }

    public Optional<Map<String, Object>> findConversationById(long id) {
        var list = jdbc.queryForList("SELECT * FROM conversations WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Map<String, Object>> findConversationBetween(long touristId, long guideId) {
        var list = jdbc.queryForList("SELECT * FROM conversations WHERE tourist_id = ? AND guide_id = ?", touristId, guideId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createConversation(long touristId, long guideId) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO conversations(tourist_id, guide_id) VALUES(?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, touristId);
            ps.setLong(2, guideId);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public List<Map<String, Object>> findMessagesByConversationId(long conversationId) {
        return jdbc.queryForList(
                "SELECT m.*, u.name AS sender_name " +
                "FROM messages m " +
                "JOIN users u ON u.id = m.sender_id " +
                "WHERE m.conversation_id = ? " +
                "ORDER BY m.id", conversationId);
    }

    public Optional<Map<String, Object>> findMessageById(long messageId) {
        var list = jdbc.queryForList("SELECT * FROM messages WHERE id = ?", messageId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createMessage(long conversationId, long senderId, String body) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO messages(conversation_id, sender_id, body) VALUES(?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, conversationId);
            ps.setLong(2, senderId);
            ps.setString(3, body);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int updateMessage(long messageId, String body) {
        return jdbc.update("UPDATE messages SET body = ?, edited = 1 WHERE id = ?", body, messageId);
    }

    public int deleteMessage(long messageId) {
        return jdbc.update("DELETE FROM messages WHERE id = ?", messageId);
    }
}
