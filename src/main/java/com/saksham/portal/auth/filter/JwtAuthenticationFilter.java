package com.saksham.portal.auth.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saksham.portal.auth.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Authentication Filter - The Missing Piece!
 * 
 * This filter intercepts EVERY request and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Sets authentication in SecurityContext
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No JWT token found, continue with filter chain
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT token (remove "Bearer " prefix)
        String jwt = authHeader.substring(7);
        Long userId;

        try {
            // 3. Extract username from token
            userId = jwtUtil.extractUserId(jwt);
        } catch (Exception e) {
            // Invalid token format
            filterChain.doFilter(request, response);
            return;
        }

        // 4. If username exists and no authentication is set
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            try {
                // 5. Extract role from token
                String role = jwtUtil.extractRole(jwt);
                
                // 6. Validate token
                if (jwtUtil.isTokenValid(jwt, userId)) {
                    
                    // 7. Create authorities from role
                    List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );
                    
                    // 8. Create authentication token with authorities
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userId,
                            null, // No credentials needed for JWT
                            authorities // Use authorities from token
                        );
                    
                    // 9. Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 10. Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Token parsing failed, continue without authentication
            }
        }

        // 10. Continue with filter chain
        filterChain.doFilter(request, response);
    }
}