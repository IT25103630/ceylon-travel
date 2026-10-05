package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.GalleryService;
import org.springframework.web.bind.annotation.*;

/**
 * Function: Gallery Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class GalleryController {
    private final GalleryService galleryService;
    private final Access access;

    public GalleryController(GalleryService galleryService, Access access) {
        this.galleryService = galleryService;
        this.access = access;
    }

    public record Photo(
            @Positive long place_id,
            @NotBlank @Size(max = 500) String image_url,
            @NotBlank @Size(max = 300) String caption
    ) {}

    @GetMapping("/api/public/gallery")
    public Object gallery(@RequestParam(required = false) Long place) {
        return galleryService.getGalleryImages(place);
    }

    @PostMapping("/api/gallery")
    public Object upload(Principal p, @Valid @RequestBody Photo r) {
        long userId = access.require(p, "TOURIST", "GUIDE");
        PlacesController.image(r.image_url());
        long photoId = galleryService.uploadPhoto(userId, r.place_id(), r.image_url(), r.caption());
        return Map.of("id", photoId);
    }

    @DeleteMapping("/api/gallery/{id}")
    public void remove(Principal p, @PathVariable long id) {
        galleryService.deletePhoto(id, access.id(p), access.admin(p));
    }
}