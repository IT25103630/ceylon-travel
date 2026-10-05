package lk.ceylontravel.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.BookingService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Booking Management
 * Member Name: Samarawickrama S. J. D.
 * Student ID: IT25102542
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class BookingController {
    private final BookingService bookingService;
    private final Access access;

    public BookingController(BookingService bookingService, Access access) {
        this.bookingService = bookingService;
        this.access = access;
    }

    public record Request(
            @Positive long guide_id,
            @Positive long place_id,
            @NotNull LocalDate start_date,
            @NotNull LocalDate end_date,
            @Min(1) @Max(20) int guests,
            @NotNull @Size(max = 2000) String details
    ) {}

    public record Status(
            @NotNull @Pattern(regexp = "CONFIRMED|REJECTED|CANCELLED|COMPLETED") String status
    ) {}

    @GetMapping("/api/bookings")
    public Object bookings(Principal p) {
        long id = access.id(p);
        return bookingService.getUserBookings(id);
    }

    @PostMapping("/api/bookings")
    public Object book(Principal p, @Valid @RequestBody Request r) {
        long touristId = access.require(p, "TOURIST");
        long bookingId = bookingService.create(touristId, r);
        return Map.of("id", bookingId);
    }

    @PatchMapping("/api/bookings/{id}")
    public void status(Principal p, @PathVariable long id, @Valid @RequestBody Status r) {
        bookingService.status(access.id(p), id, r.status());
    }
}