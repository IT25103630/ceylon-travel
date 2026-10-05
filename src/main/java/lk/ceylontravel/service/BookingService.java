package lk.ceylontravel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import lk.ceylontravel.controller.BookingController.Request;
import lk.ceylontravel.dao.AvailabilityDao;
import lk.ceylontravel.dao.BookingDao;
import lk.ceylontravel.dao.PlaceDao;
import lk.ceylontravel.dao.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Booking Management
 * Member Name: Samarawickrama S. J. D.
 * Student ID: IT25102542
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class BookingService {
    private final BookingDao bookingDao;
    private final AvailabilityDao availabilityDao;
    private final UserDao userDao;
    private final PlaceDao placeDao;
    private final Events events;

    public BookingService(BookingDao bookingDao, AvailabilityDao availabilityDao,
                          UserDao userDao, PlaceDao placeDao, Events events) {
        this.bookingDao = bookingDao;
        this.availabilityDao = availabilityDao;
        this.userDao = userDao;
        this.placeDao = placeDao;
        this.events = events;
    }

    public List<Map<String, Object>> getUserBookings(long userId) {
        return bookingDao.findBookingsByUserId(userId);
    }

    private void checkGuideFree(long guideId, LocalDate start, LocalDate end) {
        if (bookingDao.hasConflictingBooking(guideId, start, end)) {
            throw new ResponseStatusException(CONFLICT, "These dates already have a confirmed booking");
        }
        int availableCount = availabilityDao.countAvailableDays(guideId, start, end);
        long requiredDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (availableCount != requiredDays) {
            throw new ResponseStatusException(CONFLICT, "The guide is not available on every selected date");
        }
    }

    @Transactional
    public long create(long touristId, Request r) {
        if (r.start_date().isBefore(LocalDate.now()) || r.end_date().isBefore(r.start_date()) ||
                ChronoUnit.DAYS.between(r.start_date(), r.end_date()) > 29) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose a future trip lasting 1 to 30 days");
        }

        var guide = userDao.findGuideProfile(r.guide_id())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Guide not found"));

        Object verifiedVal = guide.get("verified");
        boolean verified = Boolean.TRUE.equals(verifiedVal) || "1".equals(String.valueOf(verifiedVal)) || "true".equalsIgnoreCase(String.valueOf(verifiedVal));
        if (!verified) {
            throw new ResponseStatusException(CONFLICT, "Guide is not verified");
        }

        var user = userDao.findById(r.guide_id())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Guide user not found"));
        Object activeVal = user.get("active");
        boolean active = Boolean.TRUE.equals(activeVal) || "1".equals(String.valueOf(activeVal)) || "true".equalsIgnoreCase(String.valueOf(activeVal));
        if (!active) {
            throw new ResponseStatusException(CONFLICT, "Guide account is disabled");
        }

        placeDao.findApprovedPlaceById(r.place_id())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Destination not found or not approved"));

        checkGuideFree(r.guide_id(), r.start_date(), r.end_date());

        if (bookingDao.hasDuplicateRequest(touristId, r.guide_id(), r.start_date(), r.end_date())) {
            throw new ResponseStatusException(CONFLICT, "You already requested this trip");
        }

        long days = ChronoUnit.DAYS.between(r.start_date(), r.end_date()) + 1;
        BigDecimal dailyRate = new BigDecimal(guide.get("daily_rate").toString());
        BigDecimal total = dailyRate.multiply(BigDecimal.valueOf(days));

        long id = bookingDao.createBooking(touristId, r.guide_id(), r.place_id(),
                r.start_date(), r.end_date(), r.guests(), r.details(), total);

        events.notify(r.guide_id(), "New booking request #" + id);
        return id;
    }

    @Transactional
    public void status(long actorId, long bookingId, String newStatus) {
        var booking = bookingDao.findBookingById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking not found"));

        long guideId = ((Number) booking.get("guide_id")).longValue();
        long touristId = ((Number) booking.get("tourist_id")).longValue();

        boolean isGuide = (guideId == actorId);
        boolean isTourist = (touristId == actorId);

        if (!isGuide && !isTourist) {
            throw new ResponseStatusException(FORBIDDEN, "This is not your booking");
        }

        String oldStatus = booking.get("status").toString();
        boolean allowed = (isGuide && oldStatus.equals("PENDING") && (newStatus.equals("CONFIRMED") || newStatus.equals("REJECTED")))
                || ((isGuide || isTourist) && (oldStatus.equals("PENDING") || oldStatus.equals("CONFIRMED")) && newStatus.equals("CANCELLED"))
                || (isGuide && oldStatus.equals("CONFIRMED") && newStatus.equals("COMPLETED"));

        if (!allowed) {
            throw new ResponseStatusException(CONFLICT, "This booking status change is not allowed");
        }

        var start = LocalDate.parse(booking.get("start_date").toString());
        var end = LocalDate.parse(booking.get("end_date").toString());

        if (newStatus.equals("CONFIRMED")) {
            if (start.isBefore(LocalDate.now())) {
                throw new ResponseStatusException(CONFLICT, "This trip date has passed");
            }
            checkGuideFree(actorId, start, end);
        }

        if (newStatus.equals("COMPLETED") && end.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(CONFLICT, "Complete the booking after the trip ends");
        }

        bookingDao.updateStatus(bookingId, newStatus);
        long notifyTarget = isGuide ? touristId : guideId;
        events.notify(notifyTarget, "Booking #" + bookingId + " is now " + newStatus.toLowerCase());
    }

    // Proxy for backward compatibility if needed by tests
    @Transactional
    public void availability(long guideId, LocalDate date, boolean add) {
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose today or a future date");
        }
        if (add) {
            availabilityDao.addAvailability(guideId, date);
        } else {
            if (bookingDao.hasConflictingBooking(guideId, date, date)) {
                throw new ResponseStatusException(CONFLICT, "Cancel or reschedule the confirmed booking first");
            }
            if (bookingDao.findBookingsByUserId(guideId).stream().anyMatch(b ->
                    "PENDING".equals(b.get("status")) &&
                    !LocalDate.parse(b.get("start_date").toString()).isAfter(date) &&
                    !LocalDate.parse(b.get("end_date").toString()).isBefore(date))) {
                throw new ResponseStatusException(CONFLICT, "Resolve pending requests for this date first");
            }
            availabilityDao.removeAvailability(guideId, date);
        }
    }
}