package lk.ceylontravel.service;

import java.util.*;
import lk.ceylontravel.dao.GalleryDao;
import lk.ceylontravel.dao.PlaceDao;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Gallery Management
 * Member Name: Anushan R.
 * Student ID: IT25101716
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class GalleryService {
    private final GalleryDao galleryDao;
    private final PlaceDao placeDao;

    public GalleryService(GalleryDao galleryDao, PlaceDao placeDao) {
        this.galleryDao = galleryDao;
        this.placeDao = placeDao;
    }

    public List<Map<String, Object>> getGalleryImages(Long placeId) {
        return galleryDao.findGalleryImages(placeId);
    }

    public long uploadPhoto(long userId, long placeId, String imageUrl, String caption) {
        placeDao.findApprovedPlaceById(placeId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Place not found or not approved"));
        return galleryDao.createGalleryImage(userId, placeId, imageUrl, caption);
    }

    public void deletePhoto(long id, long userId, boolean isAdmin) {
        var image = galleryDao.findGalleryImageById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        long ownerId = ((Number) image.get("user_id")).longValue();
        if (ownerId != userId && !isAdmin) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot delete this photo");
        }
        galleryDao.deleteGalleryImage(id);
    }
}
