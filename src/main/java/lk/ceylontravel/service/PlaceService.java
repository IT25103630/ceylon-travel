package lk.ceylontravel.service;

import java.util.*;
import lk.ceylontravel.dao.PlaceDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Places Management
 * Member Name: Fernando M. G. D. W.
 * Student ID: IT25101548
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class PlaceService {
    private final PlaceDao placeDao;
    private final Events events;

    public PlaceService(PlaceDao placeDao, Events events) {
        this.placeDao = placeDao;
        this.events = events;
    }

    public List<Map<String, Object>> searchApprovedPlaces(String query, String category) {
        return placeDao.findApprovedPlaces(query, category);
    }

    public Map<String, Object> getApprovedPlace(long id) {
        return placeDao.findApprovedPlaceById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Place not found"));
    }

    public Map<String, Object> getPlaceById(long id) {
        return placeDao.findPlaceById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Place not found"));
    }

    public List<Map<String, Object>> getPlacesBySubmitter(long userId) {
        return placeDao.findPlacesBySubmittedBy(userId);
    }

    public long submitPlace(long submittedBy, String name, String description, String location, String category, String tips, String imageUrl) {
        return placeDao.createPlace(submittedBy, name, description, location, category, tips, imageUrl);
    }

    @Transactional
    public void updatePlace(long id, long userId, boolean isAdmin, String name, String description, String location, String category, String tips, String imageUrl) {
        var place = getPlaceById(id);
        long ownerId = ((Number) place.get("submitted_by")).longValue();
        if (ownerId != userId && !isAdmin) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot change this place");
        }
        if ("ARCHIVED".equals(place.get("status"))) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot edit archived place");
        }

        placeDao.updatePlace(id, name, description, location, category, tips, imageUrl);
    }

    @Transactional
    public void archivePlace(long id, long userId, boolean isAdmin) {
        var place = getPlaceById(id);
        long ownerId = ((Number) place.get("submitted_by")).longValue();
        if (ownerId != userId && !isAdmin) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot change this place");
        }
        placeDao.archivePlace(id);
    }

    @Transactional
    public void reviewPlaceSubmission(long id, String status, long adminId) {
        var place = getPlaceById(id);
        placeDao.approveOrReject(id, status, adminId);
        long submitterId = ((Number) place.get("submitted_by")).longValue();
        events.notify(submitterId, "Your place " + place.get("name") + " was " + status.toLowerCase());
    }

    public List<Map<String, Object>> getAllPlacesForAdmin() {
        return placeDao.findAllForAdmin();
    }
}
