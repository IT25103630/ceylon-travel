package lk.ceylontravel.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: User and Guide Profile Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class UserDao {
    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
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

    public Optional<Map<String, Object>> findByEmail(String email) {
        var list = jdbc.queryForList("SELECT * FROM users WHERE email = ?", email.toLowerCase().trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<Map<String, Object>> findById(long id) {
        var list = jdbc.queryForList("SELECT id, name, email, role, phone, active FROM users WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createUser(String name, String email, String passwordHash, String role) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(name, email, password_hash, role) VALUES(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, role);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int updateProfile(long id, String name, String phone) {
        return jdbc.update("UPDATE users SET name = ?, phone = ? WHERE id = ?", name.trim(), phone, id);
    }

    public int setUserActive(long id, boolean active) {
        return jdbc.update("UPDATE users SET active = ? WHERE id = ?", active ? 1 : 0, id);
    }

    public List<Map<String, Object>> findAllUsers() {
        String sql = "SELECT u.id, u.name, u.email, u.role, u.active, g.verified " +
                "FROM users u " +
                "LEFT JOIN guide_profiles g ON g.user_id = u.id " +
                "ORDER BY u.id";
        return jdbc.queryForList(sql);
    }

    public Optional<Map<String, Object>> findGuideProfile(long userId) {
        String sql = "SELECT g.*, u.name, " +
                "(SELECT AVG(r.rating) FROM reviews r JOIN bookings b ON b.id = r.booking_id WHERE b.guide_id = g.user_id AND r.status = 'PUBLISHED') AS rating, " +
                "(SELECT COUNT(*) FROM reviews r JOIN bookings b ON b.id = r.booking_id WHERE b.guide_id = g.user_id AND r.status = 'PUBLISHED') AS review_count " +
                "FROM guide_profiles g " +
                "JOIN users u ON u.id = g.user_id " +
                "WHERE g.user_id = ?";
        var list = jdbc.queryForList(sql, userId);
        if (list.isEmpty()) return Optional.empty();
        var row = new HashMap<>(list.get(0));
        row.put("languages", getLanguagesString(userId));
        return Optional.of(row);
    }

    public List<Map<String, Object>> findVerifiedGuides(String query) {
        String sql = "SELECT g.*, u.name, " +
                "(SELECT AVG(r.rating) FROM reviews r JOIN bookings b ON b.id = r.booking_id WHERE b.guide_id = g.user_id AND r.status = 'PUBLISHED') AS rating, " +
                "(SELECT COUNT(*) FROM reviews r JOIN bookings b ON b.id = r.booking_id WHERE b.guide_id = g.user_id AND r.status = 'PUBLISHED') AS review_count " +
                "FROM guide_profiles g " +
                "JOIN users u ON u.id = g.user_id " +
                "WHERE g.verified = 1 AND u.active = 1 " +
                "AND (u.name LIKE ? OR g.location LIKE ? OR EXISTS(SELECT 1 FROM guide_languages l WHERE l.guide_id = g.user_id AND l.language LIKE ?)) " +
                "ORDER BY g.user_id";
        var rows = jdbc.queryForList(sql, "%" + query + "%", "%" + query + "%", "%" + query + "%");
        List<Map<String, Object>> result = new ArrayList<>();
        for (var row : rows) {
            var map = new HashMap<>(row);
            long gid = ((Number) map.get("user_id")).longValue();
            map.put("languages", getLanguagesString(gid));
            result.add(map);
        }
        return result;
    }

    public String getLanguagesString(long guideId) {
        var langs = jdbc.queryForList("SELECT language FROM guide_languages WHERE guide_id = ? ORDER BY language", String.class, guideId);
        return String.join(", ", langs);
    }

    public void createGuideProfile(long userId, String bio, String location, BigDecimal dailyRate) {
        jdbc.update("INSERT INTO guide_profiles(user_id, bio, location, daily_rate) VALUES(?, ?, ?, ?)",
                userId, bio, location, dailyRate);
        jdbc.update("INSERT INTO guide_languages(guide_id, language) VALUES(?, ?)", userId, "English");
    }

    public void updateGuideProfile(long userId, String bio, String location, BigDecimal dailyRate, String imageUrl, List<String> languages) {
        jdbc.update("UPDATE guide_profiles SET bio = ?, location = ?, daily_rate = ?, image_url = ? WHERE user_id = ?",
                bio, location, dailyRate, imageUrl, userId);
        jdbc.update("DELETE FROM guide_languages WHERE guide_id = ?", userId);
        for (String lang : languages) {
            jdbc.update("INSERT INTO guide_languages(guide_id, language) VALUES(?, ?)", userId, lang);
        }
    }

    public int setGuideVerified(long userId, boolean verified) {
        return jdbc.update("UPDATE guide_profiles SET verified = ? WHERE user_id = ?", verified ? 1 : 0, userId);
    }
}
