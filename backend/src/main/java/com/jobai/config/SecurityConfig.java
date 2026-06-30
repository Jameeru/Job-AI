package com.jobai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * <p>The application is stateless (JWT / Firebase tokens). CSRF is disabled
 * because the API is consumed by a React SPA using Authorization headers —
 * not cookies.
 *
 * <p>Public endpoints: /api/health, /api/docs/**, /swagger-ui/**, /actuator/health
 * Everything else requires a valid Firebase JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;
    private final CorsConfig corsConfig;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter, CorsConfig corsConfig) {
        this.firebaseAuthFilter = firebaseAuthFilter;
        this.corsConfig = corsConfig;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Stateless REST API — no sessions
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CSRF disabled (SPA uses Authorization header, not cookies)
            .csrf(AbstractHttpConfigurer::disable)

            // CORS using our CorsConfigurationSource bean
            .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/health",
                    "/actuator/health",
                    "/api/docs/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // Add Firebase token filter before Spring's default auth filter
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .build();
    }
}
