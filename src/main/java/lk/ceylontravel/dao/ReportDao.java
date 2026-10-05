package lk.ceylontravel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Emergency and Complaint Reporting Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class ReportDao {
    private final JdbcTemplate jdbc;

    public ReportDao(JdbcTemplate jdbc) {
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

    public List<Map<String, Object>> findReportsByUserId(long userId) {
        return jdbc.queryForList("SELECT * FROM reports WHERE user_id = ? ORDER BY id DESC", userId);
    }

    public Optional<Map<String, Object>> findReportById(long id) {
        var list = jdbc.queryForList("SELECT * FROM reports WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createReport(long userId, String type, String subject, String description, String location) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reports(user_id, type, subject, description, location) VALUES(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, type);
            ps.setString(3, subject);
            ps.setString(4, description);
            ps.setString(5, location);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int handleReport(long id, String status, String response, long adminId) {
        return jdbc.update("UPDATE reports SET status = ?, response = ?, handled_by = ? WHERE id = ?",
                status, response, adminId, id);
    }

    public int deleteReport(long id) {
        return jdbc.update("DELETE FROM reports WHERE id = ?", id);
    }

    public List<Map<String, Object>> findAllForAdmin() {
        String sql = "SELECT r.*, u.name AS author_name FROM reports r JOIN users u ON u.id = r.user_id ORDER BY r.id DESC";
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> findNotifications(long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC";
        try {
            return jdbc.queryForList(sql + " LIMIT 50", userId);
        } catch (Exception e) {
            return jdbc.queryForList("SELECT TOP 50 * FROM notifications WHERE user_id = ? ORDER BY id DESC", userId);
        }
    }

    public void createNotification(long userId, String text) {
        jdbc.update("INSERT INTO notifications(user_id, text) VALUES(?, ?)", userId, text);
    }

    public int markNotificationsRead(long userId) {
        return jdbc.update("UPDATE notifications SET is_read = 1 WHERE user_id = ?", userId);
    }

    public List<Map<String, Object>> findActiveAdmins() {
        return jdbc.queryForList("SELECT id FROM users WHERE role = 'ADMIN' AND active = 1");
    }
}
