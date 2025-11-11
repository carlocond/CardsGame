package com.cardsgame.CardsGame.config;

import com.cardsgame.CardsGame.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component //Questa classe verrà trattata come un bean
@RequiredArgsConstructor //Crea costruttori per ogni variabile final
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /*
    OncePerRequestFilter è una classe che garantisce che un filtro venga eseguito una sola volta per ogni richiesta HTTP
     */
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(
           @NonNull HttpServletRequest request,
           @NonNull HttpServletResponse response,
           @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // Log base: path e metodo per vedere cosa arriva
        String path = request.getRequestURI();
        log.debug("JwtAuthFilter: request {} {}", request.getMethod(), path);

        // Check del token JWT
        final String authHeader = request.getHeader("Authorization");
        log.debug("JwtAuthFilter: Authorization header present: {}", (authHeader != null));

        final String jwt;
        final String userEmail;

        if (path.startsWith("/api/v1/auth") ||
                path.startsWith("/api/cards") ||
                path.startsWith("/api/pack-templates") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.equals("/") ||
                path.endsWith(".html") ||
                path.endsWith(".ico") ||
                path.startsWith("/api/debug") ||
                path.equals("/error")) {
            // Queste rotte vengono trattate come pubbliche e quindi skip del parsing del token
            log.debug("JwtAuthFilter: Public path - skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            log.debug("JwtAuthFilter: No Bearer token present - continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7); //La posizione 7 equivale allo spazio lasciato in "Bearer*"
        log.debug("JwtAuthFilter: raw token starts with: {}", jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt);
        try {
            userEmail = jwtService.extractUserEmail(jwt);
            log.debug("JwtAuthFilter: extracted userEmail from token: {}", userEmail);
        } catch (Exception ex) {
            log.warn("JwtAuthFilter: Failed to extract user email from token: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            boolean valid = jwtService.isTokenValid(jwt, userDetails);
            log.debug("JwtAuthFilter: token valid status: {} for user {}", valid, userEmail);
            if (valid) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("JwtAuthFilter: Authentication set for user {}", userEmail);
            }
        }
        filterChain.doFilter(request, response);
    }


}
