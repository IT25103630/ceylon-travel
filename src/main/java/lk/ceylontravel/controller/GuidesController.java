package lk.ceylontravel.controller;

import java.math.BigDecimal;
import java.security.Principal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Guide Profile Discovery and Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class GuidesController {
    private final UserService userService;
    private final Access access;

    public GuidesController(UserService userService, Access access) {
        this.userService = userService;
        this.access = access;
    }

    public record Profile(
            @NotBlank @Size(max = 2000) String bio,
            @NotBlank @Size(max = 100) String location,
            @NotBlank @Size(max = 200) String languages,
            @NotNull @DecimalMin("0.01") @DecimalMax("1000000") BigDecimal daily_rate,
            @NotBlank @Size(max = 500) String image_url
    ) {}

    @GetMapping("/api/public/guides")
    public Object guides(@RequestParam(defaultValue = "") String q) {
        return userService.searchGuides(q);
    }

    @GetMapping("/api/public/guides/{id}")
    public Object guide(@PathVariable long id) {
        return userService.getGuideProfile(id);
    }

    @GetMapping("/api/guides/profile")
    public Object profile(Principal p) {
        return userService.getGuideProfile(access.require(p, "GUIDE"));
    }

    @PutMapping("/api/guides/profile")
    public void update(Principal p, @Valid @RequestBody Profile r) {
        PlacesController.image(r.image_url());
        long guideId = access.require(p, "GUIDE");
        userService.updateGuideProfile(guideId, r.bio(), r.location(), r.daily_rate(), r.image_url(), r.languages());
    }
}