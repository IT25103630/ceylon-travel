package lk.ceylontravel.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Booking Management
 * Member Name: Samarawickrama S. J. D.
 * Student ID: IT25102542
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class BookingDao {
    private final JdbcTemplate jdbc;

    public BookingDao(JdbcTemplate jdbc) {
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

    public List<Map<String, Object>> findBookingsByUserId(long userId) {
        String sql = "SELECT b.*, t.name AS tourist_name, g.name AS guide_name, p.name AS place_name, " +
                "(SELECT r.id FROM reviews r WHERE r.booking_id = b.id) AS review_id " +
                "FROM bookings b " +
                "JOIN users t ON t.id = b.tourist_id " +
                "JOIN users g ON g.id = b.guide_id " +
                "JOIN places p ON p.id = b.place_id " +
                "WHERE b.tourist_id = ? OR b.guide_id = ? " +
                "ORDER BY b.id DESC";
        return jdbc.queryForList(sql, userId, userId);
    }

    public Optional<Map<String, Object>> findBookingById(long id) {
        var list = jdbc.queryForList("SELECT * FROM bookings WHERE id = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean hasConflictingBooking(long guideId, LocalDate start, LocalDate end) {
        String sql = "SELECT id FROM bookings WHERE guide_id = ? AND status = 'CONFIRMED' AND start_date <= ? AND end_date >= ?";
        return !jdbc.queryForList(sql, guideId, Date.valueOf(end), Date.valueOf(start)).isEmpty();
    }

    public boolean hasDuplicateRequest(long touristId, long guideId, LocalDate start, LocalDate end) {
        String sql = "SELECT id FROM bookings WHERE tourist_id = ? AND guide_id = ? AND start_date = ? AND end_date = ? AND status IN ('PENDING', 'CONFIRMED')";
        return !jdbc.queryForList(sql, touristId, guideId, Date.valueOf(start), Date.valueOf(end)).isEmpty();
    }

    public long createBooking(long touristId, long guideId, long placeId, LocalDate start, LocalDate end, int guests, String details, BigDecimal totalPrice) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bookings(tourist_id, guide_id, place_id, start_date, end_date, guests, details, total_price) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, touristId);
            ps.setLong(2, guideId);
            ps.setLong(3, placeId);
            ps.setDate(4, Date.valueOf(start));
            ps.setDate(5, Date.valueOf(end));
            ps.setInt(6, guests);
            ps.setString(7, details);
            ps.setBigDecimal(8, totalPrice);
            return ps;
        }, keyHolder);
        return extractGeneratedId(keyHolder);
    }

    public int updateStatus(long id, String status) {
        return jdbc.update("UPDATE bookings SET status = ? WHERE id = ?", status, id);
    }

    public List<Map<String, Object>> findAllForAdmin() {
        String sql = "SELECT b.*, u.name AS tourist_name, g.name AS guide_name " +
                "FROM bookings b " +
                "JOIN users u ON u.id = b.tourist_id " +
                "JOIN users g ON g.id = b.guide_id " +
                "ORDER BY b.id DESC";
        return jdbc.queryForList(sql);
    }
}
