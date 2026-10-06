package lk.ceylontravel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Places Management
 * Member Name: Fernando M. G. D. W.
 * Student ID: IT25101548
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class PlaceDao {
    private final JdbcTemplate jdbc;

    public PlaceDao(JdbcTemplate jdbc) {
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

    public List<Map<String, Object>> findApprovedPlaces(String q, String category) {
        String sql = "SELECT p.*, u.name AS submitted_name, " +
                "(SELECT AVG(r.rating) FROM reviews r WHERE r.place_id = p.id AND r.status = 'PUBLISHED') AS rating " +
                "FROM places p " +
                "JOIN users u ON u.id = p.submitted_by " +
                "WHERE p.status = 'APPROVED' AND (p.name LIKE ? OR p.location LIKE ?) AND (? = '' OR p.category = ?) " +
                "ORDER BY p.id";
        return jdbc.queryForList(sql, "%" + q + "%", "%" + q + "%", category, category);
    }

    public Optional<Map<String, Object>> findPlaceById(long id) {
        var list = jdbc.queryForList("SELECT * FROM places WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Map<String, Object>> findApprovedPlaceById(long id) {
        var list = jdbc.queryForList("SELECT * FROM places WHERE id = ? AND status = 'APPROVED'", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Map<String, Object>> findPlacesBySubmittedBy(long userId) {
        return jdbc.queryForList("SELECT * FROM places WHERE submitted_by = ? ORDER BY id DESC", userId);
    }

    public long createPlace(long submittedBy, String name, String description, String location, String category, String tips, String imageUrl) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO places(submitted_by, name, description, location, category, tips, image_url) VALUES(?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, submittedBy);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, location);
            ps.setString(5, category);
            ps.setString(6, tips);
            ps.setString(7, imageUrl);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int updatePlace(long id, String name, String description, String location, String category, String tips, String imageUrl) {
        return jdbc.update(
                "UPDATE places SET name = ?, description = ?, location = ?, category = ?, tips = ?, image_url = ?, status = 'PENDING', reviewed_by = NULL, reviewed_at = NULL WHERE id = ?",
                name, description, location, category, tips, imageUrl, id);
    }

    public int archivePlace(long id) {
        return jdbc.update("UPDATE places SET status = 'ARCHIVED' WHERE id = ?", id);
    }

    public int approveOrReject(long id, String status, long adminId) {
        return jdbc.update("UPDATE places SET status = ?, reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?",
                status, adminId, id);
    }

    public List<Map<String, Object>> findAllForAdmin() {
        String sql = "SELECT p.*, u.name AS submitted_name FROM places p JOIN users u ON u.id = p.submitted_by WHERE p.status <> 'ARCHIVED' ORDER BY p.id DESC";
        return jdbc.queryForList(sql);
    }
}
