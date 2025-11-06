package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.entity.Rarity;

import java.util.List;
import java.util.Optional;

public interface CardService {
    Card create(Card card);
    Optional<Card> findById(Long id);
    List<Card> findByExpansion(Expansion expansion);
    List<Card> findAll();
    void delete(Long id);
}
