package lk.ceylontravel.config;

import lk.ceylontravel.dao.UserDao;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwords() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetails(UserDao userDao) {
        return email -> {
            var opt = userDao.findByEmail(email.toLowerCase().trim());
            if (opt.isEmpty()) {
                throw new UsernameNotFoundException("Unknown account: " + email);
            }
            var u = opt.get();
            Object activeVal = u.get("active");
            boolean isActive = Boolean.TRUE.equals(activeVal) || "1".equals(String.valueOf(activeVal)) || "true".equalsIgnoreCase(String.valueOf(activeVal));

            return User.withUsername(u.get("email").toString())
                    .password(u.get("password_hash").toString())
                    .roles(u.get("role").toString())
                    .disabled(!isActive)
                    .build();
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/csrf", "/api/auth/register", "/api/auth/me", "/api/public/**").permitAll()
                        .requestMatchers("/api/**").authenticated().anyRequest().permitAll())
                .formLogin(f -> f.loginProcessingUrl("/api/auth/login")
                        .successHandler((q, r, a) -> {
                            r.setContentType("application/json");
                            r.getWriter().write("{\"ok\":true}");
                        })
                        .failureHandler((q, r, e) -> {
                            r.setStatus(401);
                            r.setContentType("application/json");
                            r.getWriter().write("{\"message\":\"Email or password is incorrect\"}");
                        }))
                .logout(l -> l.logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((q, r, a) -> r.setStatus(204)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((q, r, x) -> r.sendError(401))
                        .accessDeniedHandler((q, r, x) -> r.sendError(403))).build();
    }
}