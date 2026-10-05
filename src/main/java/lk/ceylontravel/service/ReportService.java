package lk.ceylontravel.service;

import java.util.*;
import lk.ceylontravel.dao.ReportDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Emergency and Complaint Reporting Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class ReportService {
    private final ReportDao reportDao;
    private final Events events;

    public ReportService(ReportDao reportDao, Events events) {
        this.reportDao = reportDao;
        this.events = events;
    }

    public List<Map<String, Object>> getUserReports(long userId) {
        return reportDao.findReportsByUserId(userId);
    }

    public Map<String, Object> getReportById(long id) {
        return reportDao.findReportById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Report not found"));
    }

    @Transactional
    public long submitReport(long userId, String type, String subject, String description, String location) {
        long reportId = reportDao.createReport(userId, type, subject, description, location);
        for (var admin : reportDao.findActiveAdmins()) {
            long adminId = ((Number) admin.get("id")).longValue();
            events.notify(adminId, type + ": " + subject);
        }
        return reportId;
    }

    @Transactional
    public void handleReport(long reportId, String status, String response, long adminId) {
        var report = getReportById(reportId);
        if ("RESOLVED".equals(status) && (response == null || response.isBlank())) {
            throw new ResponseStatusException(BAD_REQUEST, "Add a response before resolving the report");
        }

        reportDao.handleReport(reportId, status, response, adminId);
        long reporterId = ((Number) report.get("user_id")).longValue();
        events.notify(reporterId, "Report #" + reportId + " is now " + status.toLowerCase());
    }

    public List<Map<String, Object>> getNotifications(long userId) {
        return reportDao.findNotifications(userId);
    }

    public void markNotificationsAsRead(long userId) {
        reportDao.markNotificationsRead(userId);
    }

    public List<Map<String, Object>> getAllReportsForAdmin() {
        return reportDao.findAllForAdmin();
    }
}
