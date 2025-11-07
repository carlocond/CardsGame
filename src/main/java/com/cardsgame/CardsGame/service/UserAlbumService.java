package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.UserAlbum;
import com.cardsgame.CardsGame.dto.AddCardToAlbumRequest;
import com.cardsgame.CardsGame.dto.AddCardsToAlbumRequest;

import java.util.List;
import java.util.Optional;

public interface UserAlbumService {
    UserAlbum createAlbum(Long userId, UserAlbum album);
    List<UserAlbum> listAlbums(Long userId);
    Optional<UserAlbum> getAlbum(Long userId, Long albumId);
    void addCardToAlbum(Long userId, Long albumId, AddCardToAlbumRequest request);
    void addCardsToAlbum(Long userId, Long albumId, AddCardsToAlbumRequest request);
}

