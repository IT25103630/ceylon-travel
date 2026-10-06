package lk.ceylontravel.service;

import java.time.LocalDate;
import java.util.*;
import lk.ceylontravel.dao.AvailabilityDao;
import lk.ceylontravel.dao.BookingDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Guide Calendar Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class AvailabilityService {
    private final AvailabilityDao availabilityDao;
    private final BookingDao bookingDao;

    public AvailabilityService(AvailabilityDao availabilityDao, BookingDao bookingDao) {
        this.availabilityDao = availabilityDao;
        this.bookingDao = bookingDao;
    }

    public List<Map<String, Object>> getGuideAvailability(long guideId) {
        return availabilityDao.findAvailabilityByGuide(guideId);
    }

    @Transactional
    public void setAvailability(long guideId, LocalDate date, boolean add) {
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose today or a future date");
        }

        if (add) {
            availabilityDao.addAvailability(guideId, date);
        } else {
            if (bookingDao.hasConflictingBooking(guideId, date, date)) {
                throw new ResponseStatusException(CONFLICT, "Cancel or reschedule the confirmed booking first");
            }
            // Check for pending bookings on this date
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
