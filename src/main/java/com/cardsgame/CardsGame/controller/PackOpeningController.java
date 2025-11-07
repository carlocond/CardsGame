package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.service.PackOpeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pack-openings")
@RequiredArgsConstructor
public class PackOpeningController {
    private final PackOpeningService packOpeningService;

    @PostMapping("/{packTemplateId}/open")
    public ResponseEntity<List<Card>> openPack(
            @PathVariable Long packTemplateId,
            @RequestParam Long userId) { //RequestParam serve per indicare il valore da passare nella query
        List<Card> openedPack = packOpeningService.openPack(userId, packTemplateId);
        return ResponseEntity.ok(openedPack);
    }
}
