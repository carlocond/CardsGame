package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.dto.AddCardToAlbumRequest;
import com.cardsgame.CardsGame.dto.AddCardsToAlbumRequest;
import com.cardsgame.CardsGame.entity.UserAlbum;
import com.cardsgame.CardsGame.service.UserAlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/albums")
@RequiredArgsConstructor
public class UserAlbumController {

    private final UserAlbumService userAlbumService;

    @PostMapping
    public ResponseEntity<UserAlbum> createAlbum(@PathVariable Long userId, @RequestBody UserAlbum album){
        UserAlbum created = userAlbumService.createAlbum(userId, album);
        return ResponseEntity.created(URI.create("/api/users/" + userId + "/albums/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UserAlbum>> listAlbums(@PathVariable Long userId){
        return ResponseEntity.ok(userAlbumService.listAlbums(userId));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<UserAlbum> getAlbum(@PathVariable Long userId, @PathVariable Long albumId){
        return userAlbumService.getAlbum(userId, albumId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("Album non trovato o permesso negato"));
    }

    @PostMapping("/{albumId}/items")
    public ResponseEntity<Void> addCardToAlbum(@PathVariable Long userId, @PathVariable Long albumId, @RequestBody AddCardToAlbumRequest request){
        userAlbumService.addCardToAlbum(userId, albumId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{albumId}/add-cards")
    public ResponseEntity<Void> addCardsToAlbum(@PathVariable Long userId, @PathVariable Long albumId, @RequestBody AddCardsToAlbumRequest request){
        userAlbumService.addCardsToAlbum(userId, albumId, request);
        return ResponseEntity.ok().build();
    }
}
