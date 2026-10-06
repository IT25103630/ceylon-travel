package lk.ceylontravel.controller;

import lk.ceylontravel.model.Access;
import lk.ceylontravel.repository.Store;
import java.security.Principal;
import jakarta.validation.Valid;
import jakarta.validation.constraints  .*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/**
 * Function: Places Management
 * Member Name: Fernando M.G.D.W.
 * Student ID: IT25101548
 * Course: SE2030 - Software Engineering (SLIIT)
 */


@RestController
public class PlacesController {
    private final Store db;
    private final Access access;

    public PlacesController(Store db,Access access) {
        this.db=db;
        this.access=access;
    }

    public record Place(@NotBlank @Size(max=150) String name,@NotBlank @Size(max=4000) String description,
        @NotBlank @Size(max=150) String location,@Pattern(regexp="Heritage|Nature|Beach|Adventure|City") @NotNull String category,
        @NotNull @Size(max=1500) String tips,@NotBlank @Size(max=500) String image_url) {}

    static void image(String url) {
        if(!url.matches("/media/[a-f0-9-]+\\.png")&&!url.matches("/images/[a-z0-9-]+\\.jpg"))
            throw new ResponseStatusException(BAD_REQUEST,"Upload a JPG or PNG image first");
    }

    @GetMapping("/api/public/places") Object places(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String category) {
        return db.list("SELECT p.*,u.name AS submitted_name,(SELECT AVG(r.rating) FROM reviews r WHERE r.place_id=p.id AND r.status='PUBLISHED') AS rating FROM places p JOIN users u ON u.id=p.submitted_by WHERE p.status='APPROVED' AND (p.name LIKE ? OR p.location LIKE ?) AND (?='' OR p.category=?) ORDER BY p.id", "%"+q+"%","%"+q+"%",category,category);
    }

    @GetMapping("/api/public/places/{id}") Object place(@PathVariable long id) {
        return db.one("SELECT * FROM places WHERE id=? AND status='APPROVED'",id);
    }

    @GetMapping("/api/places/mine") Object mine(Principal p) {
        return db.list("SELECT * FROM places WHERE submitted_by=? ORDER BY id DESC",access.id(p));
    }

    @PostMapping("/api/places") Object add(Principal p,@Valid @RequestBody Place r) {
        image(r.image_url());
        return java.util.Map.of("id",db.insert("INSERT INTO places(submitted_by,name,description,location,category,tips,image_url) VALUES(?,?,?,?,?,?,?)",access.id(p),r.name(),r.description(),r.location(),r.category(),r.tips(),r.image_url()));
    }

    @PutMapping("/api/places/{id}") Object edit(Principal p,@PathVariable long id,@Valid @RequestBody Place r) {
        var place=db.one("SELECT * FROM places WHERE id=? AND status<>'ARCHIVED'",id);access.owner(p,Store.id(place,"submitted_by"));image(r.image_url());
        db.update("UPDATE places SET name=?,description=?,location=?,category=?,tips=?,image_url=?,status='PENDING',reviewed_by=NULL,reviewed_at=NULL WHERE id=?",r.name(),r.description(),r.location(),r.category(),r.tips(),r.image_url(),id);
        return java.util.Map.of("ok",true);
    }

    @DeleteMapping("/api/places/{id}") void remove(Principal p,@PathVariable long id) {
        access.owner(p,Store.id(db.one("SELECT * FROM places WHERE id=?",id),"submitted_by"));
        db.update("UPDATE places SET status='ARCHIVED' WHERE id=?",id);
    }
}
