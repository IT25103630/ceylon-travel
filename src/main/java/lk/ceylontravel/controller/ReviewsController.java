package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.ReviewService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Review and Feedback Management
 * Member Name: Anuththara K. G. H.
 * Student ID: IT25103422
 * Role: Product Owner
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class ReviewsController {
    private final ReviewService reviewService;
    private final Access access;

    public ReviewsController(ReviewService reviewService, Access access) {
        this.reviewService = reviewService;
        this.access = access;
    }

    public record Review(
            Long booking_id,
            Long place_id,
            @Min(1) @Max(5) int rating,
            @NotBlank @Size(max = 2000) String comment
    ) {}

    @GetMapping("/api/public/reviews")
    public Object publicReviews(@RequestParam(required = false) Long guide, @RequestParam(required = false) Long place) {
        return reviewService.getPublishedReviews(guide, place);
    }

    @GetMapping("/api/reviews/mine")
    public Object mine(Principal p) {
        return reviewService.getReviewsByAuthor(access.id(p));
    }

    @PostMapping("/api/reviews")
    public Object add(Principal p, @Valid @RequestBody Review r) {
        long authorId = access.require(p, "TOURIST");
        long id = reviewService.createReview(authorId, r.booking_id(), r.place_id(), r.rating(), r.comment());
        return Map.of("id", id);
    }

    @PutMapping("/api/reviews/{id}")
    public void update(Principal p, @PathVariable long id, @Valid @RequestBody Review r) {
        reviewService.updateReview(id, access.id(p), r.rating(), r.comment());
    }

    @DeleteMapping("/api/reviews/{id}")
    public void delete(Principal p, @PathVariable long id) {
        reviewService.deleteReview(id, access.id(p), access.admin(p));
    }
}