package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.ReportService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Emergency and Complaint Reporting Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class ReportsController {
    private final ReportService reportService;
    private final Access access;

    public ReportsController(ReportService reportService, Access access) {
        this.reportService = reportService;
        this.access = access;
    }

    public record Report(
            @NotNull @Pattern(regexp = "EMERGENCY|COMPLAINT|SUPPORT") String type,
            @NotBlank @Size(max = 150) String subject,
            @NotBlank @Size(max = 3000) String description,
            @NotNull @Size(max = 200) String location
    ) {}

    @GetMapping("/api/reports")
    public Object reports(Principal p) {
        return reportService.getUserReports(access.id(p));
    }

    @PostMapping("/api/reports")
    public Object add(Principal p, @Valid @RequestBody Report r) {
        long reportId = reportService.submitReport(access.id(p), r.type(), r.subject(), r.description(), r.location());
        return Map.of("id", reportId);
    }

    @GetMapping("/api/notifications")
    public Object notifications(Principal p) {
        return reportService.getNotifications(access.id(p));
    }

    @PostMapping("/api/notifications/read")
    public void read(Principal p) {
        reportService.markNotificationsAsRead(access.id(p));
    }
}