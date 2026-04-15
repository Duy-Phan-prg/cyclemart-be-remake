package com.example.cyclemartberemake.security;

import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println(" JWT Filter - Path: " + path);

        // Skip JWT completely for all auth endpoints
        if (path.startsWith("/api/auth/") && !path.equals("/api/auth/me")) {
            System.out.println(" Skipping JWT for: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        System.out.println(" Auth header: " + (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(" No valid auth header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println(" Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

        boolean isValid = jwtService.isValid(token);
        System.out.println(" Token valid: " + isValid);

        if (!isValid) {
            System.out.println(" Invalid token");
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        System.out.println(" Email from token: " + email);

        Users user = userRepository.findByEmail(email).orElse(null);
        System.out.println(" User found: " + (user != null ? user.getEmail() : "null"));

        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println(" Authentication set for: " + user.getEmail());
        }

        filterChain.doFilter(request, response);
    }
}