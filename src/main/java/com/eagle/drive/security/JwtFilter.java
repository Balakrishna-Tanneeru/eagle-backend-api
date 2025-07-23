package com.eagle.drive.security;

import com.eagle.drive.model.User;
import com.eagle.drive.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return List.of(
                "/api/auth/login",
                "/api/auth/signup",
                "/swagger-ui",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/h2-console"
        ).stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtUtil.extractUsername(token);
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent() && jwtUtil.validateToken(token, email)) {
                User user = userOpt.get();

                String decodedRole = jwtUtil.extractRole(token);
                System.out.println("✅ Decoded JWT Email: " + email);
                System.out.println("✅ Decoded JWT Role: " + decodedRole);
                System.out.println("🔍 User Role from DB: " + user.getRole());


                // Map role to Spring Security authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());


                System.out.println("✅ Granted Authority: " + authority.getAuthority());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("✅ Authentication set in context for user: " + user.getEmail());
            } else {
                System.out.println("⛔ Invalid token or user not found for email: " + email);
            }
        }

        filterChain.doFilter(request, response);
    }
}
