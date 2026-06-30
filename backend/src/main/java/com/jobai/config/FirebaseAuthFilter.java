package com.jobai.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Extracts the Firebase JWT from the {@code Authorization: Bearer <token>} header,
 * verifies it server-side using the Firebase Admin SDK, and populates the
 * Spring Security context with the authenticated user.
 *
 * <p>This filter runs once per request. If no token is present or the token
 * is invalid, the request proceeds without authentication (Spring Security
 * will then reject protected endpoints with 401).
 */
@Slf4j
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token != null) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

                // Build Spring Security authentication object
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        decodedToken,               // principal = FirebaseToken
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Firebase token verified for UID: {}", decodedToken.getUid());

            } catch (FirebaseAuthException e) {
                log.warn("Invalid Firebase token: {} — {}", e.getAuthErrorCode(), e.getMessage());
                // Do not set authentication; Spring Security will handle 401
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT from the Authorization header, or returns null
     * if the header is absent or malformed.
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
