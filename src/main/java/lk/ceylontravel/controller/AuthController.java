package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.UserService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

/**
 * Function: User Registration and Authentication Management
 * Member Name: Vidanage S. H.
 * Student ID: IT25100632
 * Role: Developer
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final Access access;

    public AuthController(UserService userService, Access access) {
        this.userService = userService;
        this.access = access;
    }

    public record Registration(
            @NotBlank @Size(max = 100) String name,
            @Email @NotBlank @Size(max = 180) String email,
            @Size(min = 8, max = 64) @NotNull String password,
            @Pattern(regexp = "TOURIST|GUIDE|COMMUNITY") @NotNull String role
    ) {}

    public record Profile(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 30) @NotNull String phone
    ) {}

    @GetMapping("/csrf")
    public Object csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "header", token.getHeaderName());
    }

    @GetMapping("/me")
    public Object me(Principal p) {
        return p == null ? Map.of() : access.user(p);
    }

    @PostMapping("/register")
    public Object register(@Valid @RequestBody Registration r) {
        long id = userService.registerUser(r.name(), r.email(), r.password(), r.role());
        return Map.of("id", id);
    }

    @PutMapping("/profile")
    public Object profile(Principal p, @Valid @RequestBody Profile r) {
        long userId = access.id(p);
        return userService.updateProfile(userId, r.name(), r.phone());
    }
}