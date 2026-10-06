package lk.ceylontravel.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Function: Guide Calendar Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Repository
public class AvailabilityDao {
    private final JdbcTemplate jdbc;

    public AvailabilityDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> findAvailabilityByGuide(long guideId) {
        String sql = "SELECT a.available_date, " +
                "CASE WHEN EXISTS(" +
                "    SELECT 1 FROM bookings b " +
                "    WHERE b.guide_id = a.guide_id AND b.status = 'CONFIRMED' " +
                "    AND a.available_date BETWEEN b.start_date AND b.end_date" +
                ") THEN 'BOOKED' ELSE 'AVAILABLE' END AS status " +
                "FROM availability a " +
                "WHERE a.guide_id = ? AND a.available_date >= CURRENT_DATE " +
                "ORDER BY a.available_date";
        try {
            return jdbc.queryForList(sql, guideId);
        } catch (Exception e) {
            // Fallback for MSSQL (which requires CAST(GETDATE() AS DATE))
            String mssql = "SELECT a.available_date, " +
                    "CASE WHEN EXISTS(" +
                    "    SELECT 1 FROM bookings b " +
                    "    WHERE b.guide_id = a.guide_id AND b.status = 'CONFIRMED' " +
                    "    AND a.available_date BETWEEN b.start_date AND b.end_date" +
                    ") THEN 'BOOKED' ELSE 'AVAILABLE' END AS status " +
                    "FROM availability a " +
                    "WHERE a.guide_id = ? AND a.available_date >= CAST(GETDATE() AS DATE) " +
                    "ORDER BY a.available_date";
            return jdbc.queryForList(mssql, guideId);
        }
    }

    public boolean existsAvailability(long guideId, LocalDate date) {
        return !jdbc.queryForList("SELECT id FROM availability WHERE guide_id = ? AND available_date = ?", guideId, Date.valueOf(date)).isEmpty();
    }

    public int countAvailableDays(long guideId, LocalDate start, LocalDate end) {
        var list = jdbc.queryForList("SELECT id FROM availability WHERE guide_id = ? AND available_date BETWEEN ? AND ?",
                guideId, Date.valueOf(start), Date.valueOf(end));
        return list.size();
    }

    public int addAvailability(long guideId, LocalDate date) {
        if (existsAvailability(guideId, date)) return 0;
        return jdbc.update("INSERT INTO availability(guide_id, available_date) VALUES(?, ?)", guideId, Date.valueOf(date));
    }

    public int removeAvailability(long guideId, LocalDate date) {
        return jdbc.update("DELETE FROM availability WHERE guide_id = ? AND available_date = ?", guideId, Date.valueOf(date));
    }
}
