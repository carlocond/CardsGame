package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.User;
import com.cardsgame.CardsGame.repository.UserRepo;
import com.cardsgame.CardsGame.service.PackOpeningService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pack-openings")
@RequiredArgsConstructor
public class PackOpeningController {
    private final PackOpeningService packOpeningService;
    private final UserRepo userRepo;

    private static final Logger log = LoggerFactory.getLogger(PackOpeningController.class);

    @PostMapping("/{packTemplateId}/open")
    public ResponseEntity<List<Card>> openPack(
            @PathVariable Long packTemplateId,
            @RequestParam(required = false) Long userId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String principal = auth.getName();
        log.debug("PackOpening: principal={} roles={}", principal, auth.getAuthorities());

        // determina l'id dell'utente effettivo se non lo trova, usa l'id dell'utente autenticato
        User effectiveUser = null;
        if (userId != null) {
            //Se lo trova, controlla i permessi
            User requestedUser = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Utente non trovato"));
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && !principal.equalsIgnoreCase(requestedUser.getEmail())) {
                log.warn("PackOpening: access denied for principal {} trying to open pack for user {}", principal, userId);
                return ResponseEntity.status(403).build();
            }
            effectiveUser = requestedUser;
        } else {
            // id non trovato usa quello dell'utente autenticato
            effectiveUser = userRepo.findByEmail(principal).orElseThrow(() -> new RuntimeException("Utente autenticato non trovato"));
        }

        List<Card> openedPack = packOpeningService.openPack(effectiveUser.getId(), packTemplateId);
        return ResponseEntity.ok(openedPack);
    }
}
