package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.dao.BookingDao;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.PlaceService;
import lk.ceylontravel.service.ReportService;
import lk.ceylontravel.service.ReviewService;
import lk.ceylontravel.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Platform Administration and Moderation
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;
    private final PlaceService placeService;
    private final ReviewService reviewService;
    private final ReportService reportService;
    private final BookingDao bookingDao;
    private final Access access;

    public AdminController(UserService userService, PlaceService placeService,
                           ReviewService reviewService, ReportService reportService,
                           BookingDao bookingDao, Access access) {
        this.userService = userService;
        this.placeService = placeService;
        this.reviewService = reviewService;
        this.reportService = reportService;
        this.bookingDao = bookingDao;
        this.access = access;
    }

    public record Approval(@NotNull @Pattern(regexp = "APPROVED|REJECTED") String status) {}
    public record ReviewStatus(@NotNull @Pattern(regexp = "PUBLISHED|REJECTED") String status) {}
    public record Handling(@NotNull @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED") String status, @NotNull @Size(max = 2000) String response) {}
    public record Enabled(boolean enabled) {}

    @GetMapping("/overview")
    public Object overview(Principal p) {
        access.require(p, "ADMIN");
        return Map.of(
                "users", userService.getAllUsersForAdmin(),
                "places", placeService.getAllPlacesForAdmin(),
                "reports", reportService.getAllReportsForAdmin(),
                "reviews", reviewService.getAllReviewsForAdmin(),
                "bookings", bookingDao.findAllForAdmin()
        );
    }

    @PatchMapping("/places/{id}")
    public void place(Principal p, @PathVariable long id, @Valid @RequestBody Approval r) {
        long adminId = access.require(p, "ADMIN");
        placeService.reviewPlaceSubmission(id, r.status(), adminId);
    }

    @PatchMapping("/guides/{id}")
    public void guide(Principal p, @PathVariable long id, @RequestBody Enabled r) {
        access.require(p, "ADMIN");
        userService.setGuideVerified(id, r.enabled());
    }

    @PatchMapping("/users/{id}")
    public void user(Principal p, @PathVariable long id, @RequestBody Enabled r) {
        access.require(p, "ADMIN");
        userService.setUserActive(id, r.enabled());
    }

    @PatchMapping("/reviews/{id}")
    public void review(Principal p, @PathVariable long id, @Valid @RequestBody ReviewStatus r) {
        access.require(p, "ADMIN");
        reviewService.moderateReview(id, r.status());
    }

    @PatchMapping("/reports/{id}")
    public void report(Principal p, @PathVariable long id, @Valid @RequestBody Handling r) {
        long adminId = access.require(p, "ADMIN");
        reportService.handleReport(id, r.status(), r.response(), adminId);
    }
}