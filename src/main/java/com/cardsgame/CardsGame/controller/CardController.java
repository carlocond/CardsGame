package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<Card> create(@RequestBody Card card){
        Card cardCreated = cardService.create(card);
        return ResponseEntity.created(URI.create("/api/cards/" + cardCreated.getId())).body(cardCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getById(@PathVariable Long id){
        return cardService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Carta non trovata con id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<Card>> list(){
        return ResponseEntity.ok(cardService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Card> delete(@PathVariable Long id){
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
