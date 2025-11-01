package com.cardsgame.CardsGame.config;

import com.cardsgame.CardsGame.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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

    @Override
    protected void doFilterInternal(
           @NonNull HttpServletRequest request,
           @NonNull HttpServletResponse response,
           @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        //Check del token JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7); //La posizione 7 equivale allo spazio lasciato in "Bearer*"
        userEmail = jwtService.extractUserEmail(jwt);
    }


}
