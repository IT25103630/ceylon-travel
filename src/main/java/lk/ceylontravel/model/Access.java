package lk.ceylontravel.model;

import java.security.Principal;
import java.util.Map;
import lk.ceylontravel.dao.UserDao;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Access control and session helper
 */
@Component
public class Access {
    private final UserDao userDao;

    public Access(UserDao userDao) {
        this.userDao = userDao;
    }

    public Map<String, Object> user(Principal p) {
        if (p == null) throw new ResponseStatusException(UNAUTHORIZED, "Please sign in");
        var u = userDao.findByEmail(p.getName())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Account not found"));

        Object activeVal = u.get("active");
        boolean active = Boolean.TRUE.equals(activeVal) || "true".equalsIgnoreCase(String.valueOf(activeVal)) || "1".equals(String.valueOf(activeVal));

        if (!active) {
            throw new ResponseStatusException(FORBIDDEN, "Account disabled");
        }
        return u;
    }

    public long id(Principal p) {
        return ((Number) user(p).get("id")).longValue();
    }

    public long require(Principal p, String... roles) {
        var u = user(p);
        String currentRole = String.valueOf(u.get("role"));
        for (String role : roles) {
            if (role.equals(currentRole)) {
                return ((Number) u.get("id")).longValue();
            }
        }
        throw new ResponseStatusException(FORBIDDEN, "This action is not available for your account");
    }

    public boolean admin(Principal p) {
        return "ADMIN".equals(user(p).get("role"));
    }

    public void owner(Principal p, long ownerId) {
        if (id(p) != ownerId && !admin(p)) {
            throw new ResponseStatusException(FORBIDDEN, "You cannot change this record");
        }
    }
}