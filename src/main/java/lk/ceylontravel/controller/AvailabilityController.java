package lk.ceylontravel.controller;

import java.security.Principal;
import java.time.LocalDate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.AvailabilityService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Guide Calendar Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class AvailabilityController {
    private final AvailabilityService availabilityService;
    private final Access access;

    public AvailabilityController(AvailabilityService availabilityService, Access access) {
        this.availabilityService = availabilityService;
        this.access = access;
    }

    public record Slot(@NotNull LocalDate date) {}

    @GetMapping("/api/public/guides/{id}/availability")
    public Object calendar(@PathVariable long id) {
        return availabilityService.getGuideAvailability(id);
    }

    @PostMapping("/api/availability")
    public void add(Principal p, @Valid @RequestBody Slot r) {
        long guideId = access.require(p, "GUIDE");
        availabilityService.setAvailability(guideId, r.date(), true);
    }

    @DeleteMapping("/api/availability/{date}")
    public void remove(Principal p, @PathVariable LocalDate date) {
        long guideId = access.require(p, "GUIDE");
        availabilityService.setAvailability(guideId, date, false);
    }
}
