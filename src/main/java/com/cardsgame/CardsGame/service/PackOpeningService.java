package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.Card;

import java.util.List;

public interface PackOpeningService {
    List<Card> openPack(Long userId, Long packTemplateId);
}