package com.xripp.backend.filter;

import com.xripp.backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/v3/auth/login")
                || uri.contains("/v3/api-docs")
                || uri.contains("/swagger-ui")
                || uri.contains("/doc.html");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7).trim();
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    Long partnerId = jwtUtil.getPartnerIdFromToken(token);

                    if (role == null || role.isBlank()) {
                        role = "member";
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    com.xripp.backend.security.SecurityContextHolder.setContext(
                            new com.xripp.backend.security.SecurityContextHolder.UserContext(
                                    userId, username, role.toLowerCase(), partnerId
                            )
                    );
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            com.xripp.backend.security.SecurityContextHolder.clearContext();
        }
    }
}
