package lk.ceylontravel.service;

import java.math.BigDecimal;
import java.util.*;
import lk.ceylontravel.dao.UserDao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: User & Authentication Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@Service
public class UserService {
    private final UserDao userDao;
    private final PasswordEncoder encoder;
    private final Events events;

    public UserService(UserDao userDao, PasswordEncoder encoder, Events events) {
        this.userDao = userDao;
        this.encoder = encoder;
        this.events = events;
    }

    public Optional<Map<String, Object>> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    public Map<String, Object> getUserById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    @Transactional
    public long registerUser(String name, String email, String password, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(BAD_REQUEST, "Cannot register as ADMIN");
        }
        if (userDao.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        String encodedPassword = encoder.encode(password);
        long userId = userDao.createUser(name, email, encodedPassword, role);

        if ("GUIDE".equalsIgnoreCase(role)) {
            userDao.createGuideProfile(userId, "", "Sri Lanka", BigDecimal.ZERO);
        }

        return userId;
    }

    public Map<String, Object> updateProfile(long id, String name, String phone) {
        userDao.updateProfile(id, name, phone);
        return getUserById(id);
    }

    public Map<String, Object> getGuideProfile(long userId) {
        return userDao.findGuideProfile(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Guide profile not found"));
    }

    public List<Map<String, Object>> searchGuides(String query) {
        return userDao.findVerifiedGuides(query);
    }

    @Transactional
    public void updateGuideProfile(long userId, String bio, String location, BigDecimal dailyRate, String imageUrl, String languagesStr) {
        var languages = Arrays.stream(languagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (languages.isEmpty() || languages.stream().anyMatch(s -> s.length() > 50)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid languages specified");
        }

        userDao.updateGuideProfile(userId, bio, location, dailyRate, imageUrl, languages);
    }

    public void setGuideVerified(long userId, boolean verified) {
        var guide = getGuideProfile(userId);
        if (verified) {
            String bio = guide.get("bio") != null ? guide.get("bio").toString() : "";
            String imageUrl = guide.get("image_url") != null ? guide.get("image_url").toString() : "";
            BigDecimal rate = new BigDecimal(guide.get("daily_rate").toString());
            if (bio.isBlank() || imageUrl.isBlank() || rate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(CONFLICT, "The guide must complete their profile, photo, and daily rate first");
            }
        }
        userDao.setGuideVerified(userId, verified);
        events.notify(userId, verified ? "Your guide profile has been verified" : "Your guide verification was removed");
    }

    public void setUserActive(long userId, boolean active) {
        var user = getUserById(userId);
        if ("ADMIN".equals(user.get("role"))) {
            throw new ResponseStatusException(CONFLICT, "Admin accounts cannot be disabled here");
        }
        userDao.setUserActive(userId, active);
    }

    public List<Map<String, Object>> getAllUsersForAdmin() {
        return userDao.findAllUsers();
    }
}
