package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.entity.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepo extends JpaRepository<Card, Long> {

    List<Card> findByExpansion(Expansion expansion);
    List<Card> findByRarirty(Rarity rarity);
    Optional<Card> findByNameAndExpansion(String name, Expansion expansion);
}
