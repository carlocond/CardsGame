package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.service.ExpansionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/expansions")
@RequiredArgsConstructor
public class ExpansionController {

    private final ExpansionService expansionService;

    @PostMapping
    public ResponseEntity<Expansion> create(@RequestBody Expansion expansion){
        Expansion expansionCreated = expansionService.create(expansion);
        return ResponseEntity.created(URI.create("/api/expansions/" + expansionCreated.getId())).body(expansionCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expansion> getById(@PathVariable Long id){
        return expansionService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Espansione non trovata con id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<Expansion>> list(){
        return ResponseEntity.ok(expansionService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Expansion> delete(@PathVariable Long id){
        expansionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
