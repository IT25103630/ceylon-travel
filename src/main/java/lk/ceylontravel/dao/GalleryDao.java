package lk.ceylontravel.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Gallery Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class GalleryDao {
    private final JdbcTemplate jdbc;

    public GalleryDao(JdbcTemplate jdbc) {
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

    public List<Map<String, Object>> findGalleryImages(Long placeId) {
        String sql = "SELECT i.*, u.name AS author_name, p.name AS place_name " +
                "FROM gallery_images i " +
                "JOIN users u ON u.id = i.user_id " +
                "JOIN places p ON p.id = i.place_id " +
                "WHERE p.status = 'APPROVED'";
        if (placeId == null) {
            return jdbc.queryForList(sql + " ORDER BY i.id DESC");
        } else {
            return jdbc.queryForList(sql + " AND i.place_id = ? ORDER BY i.id DESC", placeId);
        }
    }

    public Optional<Map<String, Object>> findGalleryImageById(long id) {
        var list = jdbc.queryForList("SELECT * FROM gallery_images WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long createGalleryImage(long userId, long placeId, String imageUrl, String caption) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO gallery_images(user_id, place_id, image_url, caption) VALUES(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, placeId);
            ps.setString(3, imageUrl);
            ps.setString(4, caption);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int deleteGalleryImage(long id) {
        return jdbc.update("DELETE FROM gallery_images WHERE id = ?", id);
    }
}
