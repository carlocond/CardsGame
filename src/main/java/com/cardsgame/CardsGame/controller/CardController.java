package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.service.CardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // Logger aggiunto per tracciare le richieste che arrivano al controller
    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    @PostMapping
    public ResponseEntity<Card> create(@RequestBody Card card){
        log.info("POST /api/cards chiamato con body name={}", card.getName()); // log semplice per debug
        Card cardCreated = cardService.create(card);
        return ResponseEntity.created(URI.create("/api/cards/" + cardCreated.getId())).body(cardCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id){
        log.info("GET /api/cards/{} chiamato", id); // log per capire se la richiesta arriva
        return cardService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Carta non trovata con id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<Card>> list(){
        log.info("GET /api/cards chiamato per lista"); // log per debug
        return ResponseEntity.ok(cardService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Card> delete(@PathVariable Long id){
        log.info("DELETE /api/cards/{} chiamato", id); // log per debug
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
