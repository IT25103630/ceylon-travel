package lk.ceylontravel.service;

import java.util.*;
import lk.ceylontravel.dao.BookingDao;
import lk.ceylontravel.dao.PlaceDao;
import lk.ceylontravel.dao.ReviewDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Review and Feedback Management
 * Member Name: Anuththara K. G. H.
 * Student ID: IT25103422
 * Role: Product Owner
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class ReviewService {
    private final ReviewDao reviewDao;
    private final BookingDao bookingDao;
    private final PlaceDao placeDao;

    public ReviewService(ReviewDao reviewDao, BookingDao bookingDao, PlaceDao placeDao) {
        this.reviewDao = reviewDao;
        this.bookingDao = bookingDao;
        this.placeDao = placeDao;
    }

    private String moderateContent(String comment) {
        if (comment.toLowerCase().matches("(?s).*\\b(fuck|shit|bitch)\\b.*")) {
            return "PENDING";
        }
        return "PUBLISHED";
    }

    public List<Map<String, Object>> getPublishedReviews(Long guideId, Long placeId) {
        return reviewDao.findPublishedReviews(guideId, placeId);
    }

    public List<Map<String, Object>> getReviewsByAuthor(long authorId) {
        return reviewDao.findReviewsByAuthor(authorId);
    }

    public Map<String, Object> getReviewById(long id) {
        return reviewDao.findReviewById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Review not found"));
    }

    @Transactional
    public long createReview(long authorId, Long bookingId, Long placeId, int rating, String comment) {
        if ((bookingId == null) == (placeId == null)) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose a booking or a place");
        }

        if (bookingId != null) {
            var booking = bookingDao.findBookingById(bookingId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Booking not found"));
            long touristId = ((Number) booking.get("tourist_id")).longValue();
            String status = booking.get("status").toString();
            if (touristId != authorId || !"COMPLETED".equals(status)) {
                throw new ResponseStatusException(NOT_FOUND, "Review eligible for completed bookings only");
            }
        } else {
            placeDao.findApprovedPlaceById(placeId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Place not found"));
            // Check completed trip to this place
            boolean hasCompleted = bookingDao.findBookingsByUserId(authorId).stream().anyMatch(b ->
                    placeId.equals(((Number) b.get("place_id")).longValue()) &&
                    "COMPLETED".equals(b.get("status")));
            if (!hasCompleted) {
                throw new ResponseStatusException(CONFLICT, "You can review a place after a completed visit booked here");
            }
            // Check for duplicate place review
            boolean hasReviewed = reviewDao.findReviewsByAuthor(authorId).stream().anyMatch(r ->
                    r.get("place_id") != null && placeId.equals(((Number) r.get("place_id")).longValue()));
            if (hasReviewed) {
                throw new ResponseStatusException(CONFLICT, "You already reviewed this place");
            }
        }

        String status = moderateContent(comment);
        return reviewDao.createReview(authorId, bookingId, placeId, rating, comment.trim(), status);
    }

    @Transactional
    public void updateReview(long id, long userId, int rating, String comment) {
        var review = getReviewById(id);
        long authorId = ((Number) review.get("author_id")).longValue();
        if (authorId != userId) {
            throw new ResponseStatusException(FORBIDDEN, "Only the author can edit a review");
        }
        String status = moderateContent(comment);
        reviewDao.updateReview(id, rating, comment.trim(), status);
    }

    @Transactional
    public void deleteReview(long id, long userId, boolean isAdmin) {
        var review = getReviewById(id);
        long authorId = ((Number) review.get("author_id")).longValue();
        if (authorId != userId && !isAdmin) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot delete this review");
        }
        reviewDao.deleteReview(id);
    }

    public void moderateReview(long id, String status) {
        reviewDao.moderateReview(id, status);
    }

    public List<Map<String, Object>> getAllReviewsForAdmin() {
        return reviewDao.findAllForAdmin();
    }
}
