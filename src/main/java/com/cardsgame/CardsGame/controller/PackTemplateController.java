package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.PackTemplate;
import com.cardsgame.CardsGame.service.PackTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pack-templates")
@RequiredArgsConstructor
public class PackTemplateController {

    private final PackTemplateService packTemplateService;

    @PostMapping
    public ResponseEntity<PackTemplate> create(@RequestBody PackTemplate packTemplate){
        PackTemplate packTemplateCreated = packTemplateService.create(packTemplate);
        return ResponseEntity.created(URI.create("/api/pack-templates/" + packTemplateCreated.getId())).body(packTemplateCreated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackTemplate> getById(@PathVariable Long id){
        return packTemplateService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Pack Template non trovato con id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<PackTemplate>> list(){
        return ResponseEntity.ok(packTemplateService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PackTemplate> delete(@PathVariable Long id){
        packTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
