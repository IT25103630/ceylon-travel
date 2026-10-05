package lk.ceylontravel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Review and Feedback Management
 * Member Name: Anuththara K. G. H.
 * Student ID: IT25103422
 * Role: Product Owner
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class ReviewDao {
    private final JdbcTemplate jdbc;

    public ReviewDao(JdbcTemplate jdbc) {
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

    private static final String BASE_SELECT = "SELECT r.*, u.name AS author_name, b.guide_id, COALESCE(p.name, g.name) AS target_name " +
            "FROM reviews r " +
            "JOIN users u ON u.id = r.author_id " +
            "LEFT JOIN bookings b ON b.id = r.booking_id " +
            "LEFT JOIN users g ON g.id = b.guide_id " +
            "LEFT JOIN places p ON p.id = r.place_id ";

    public List<Map<String, Object>> findPublishedReviews(Long guideId, Long placeId) {
        if (guideId != null) {
            return jdbc.queryForList(BASE_SELECT + "WHERE r.status = 'PUBLISHED' AND b.guide_id = ? ORDER BY r.id DESC", guideId);
        }
        if (placeId != null) {
            return jdbc.queryForList(BASE_SELECT + "WHERE r.status = 'PUBLISHED' AND r.place_id = ? ORDER BY r.id DESC", placeId);
        }
        String sql = BASE_SELECT + "WHERE r.status = 'PUBLISHED' ORDER BY r.id DESC";
        try {
            return jdbc.queryForList(sql + " LIMIT 50");
        } catch (Exception e) {
            return jdbc.queryForList("SELECT TOP 50 r.*, u.name AS author_name, b.guide_id, COALESCE(p.name, g.name) AS target_name " +
                    "FROM reviews r JOIN users u ON u.id = r.author_id LEFT JOIN bookings b ON b.id = r.booking_id " +
                    "LEFT JOIN users g ON g.id = b.guide_id LEFT JOIN places p ON p.id = r.place_id WHERE r.status = 'PUBLISHED' ORDER BY r.id DESC");
        }
    }

    public List<Map<String, Object>> findReviewsByAuthor(long authorId) {
        return jdbc.queryForList(BASE_SELECT + "WHERE r.author_id = ? ORDER BY r.id DESC", authorId);
    }

    public Optional<Map<String, Object>> findReviewById(long id) {
        var list = jdbc.queryForList("SELECT * FROM reviews WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createReview(long authorId, Long bookingId, Long placeId, int rating, String comment, String status) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reviews(author_id, booking_id, place_id, rating, comment, status) VALUES(?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, authorId);
            if (bookingId != null) ps.setLong(2, bookingId); else ps.setNull(2, java.sql.Types.BIGINT);
            if (placeId != null) ps.setLong(3, placeId); else ps.setNull(3, java.sql.Types.BIGINT);
            ps.setInt(4, rating);
            ps.setString(5, comment);
            ps.setString(6, status);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int updateReview(long id, int rating, String comment, String status) {
        return jdbc.update("UPDATE reviews SET rating = ?, comment = ?, status = ? WHERE id = ?", rating, comment, status, id);
    }

    public int deleteReview(long id) {
        return jdbc.update("DELETE FROM reviews WHERE id = ?", id);
    }

    public int moderateReview(long id, String status) {
        return jdbc.update("UPDATE reviews SET status = ? WHERE id = ?", status, id);
    }

    public List<Map<String, Object>> findAllForAdmin() {
        String sql = "SELECT r.*, u.name AS author_name FROM reviews r JOIN users u ON u.id = r.author_id ORDER BY r.id DESC";
        return jdbc.queryForList(sql);
    }
}
