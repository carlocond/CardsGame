package com.cardsgame.CardsGame.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) //Abilita l'uso di @PreAuthorize sui metodi
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // Disabilitiamo CSRF per semplicità in questo progetto (front-end JS usa token)
                .csrf(csrf -> csrf.disable())
                // Configurazione CORS: permettiamo chiamate dal nostro front-end
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Permettiamo le preflight OPTIONS per CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Risorse pubbliche statiche e pagine
                        .requestMatchers(
                                "/", "/index.html", "/admin.html",
                                "/favicon.ico",
                                "/css/**", "/js/**", "/images/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        // Permettiamo le GET pubbliche per le entità che vogliamo rendere leggibili senza login
                        // Aggiungiamo sia il path base che i sotto-percorsi per evitare mismatch nelle route
                        .requestMatchers(HttpMethod.GET, "/api/cards", "/api/cards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pack-templates", "/api/pack-templates/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/expansions", "/api/expansions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/debug", "/api/debug/**").permitAll() // endpoint di debug

                        // rotte admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // il resto richiede autenticazione
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                // Aggiungiamo il filtro JWT DOPO l'AnonymousAuthenticationFilter per evitare di interferire con le richieste anonime
                .addFilterAfter(jwtAuthFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }

    // NOTA: Il bean PasswordEncoder viene fornito in `ApplicationConfig`.
    // Non lo ridefiniamo qui per evitare conflitti di bean all'avvio.

    // Configurazione CORS permissiva per lo sviluppo locale
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permettiamo le origini locali. Se vuoi restringere in produzione, metti gli host esatti.
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
