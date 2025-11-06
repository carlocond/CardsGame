package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.Expansion;

import java.util.List;
import java.util.Optional;

public interface ExpansionService {
    Expansion create(Expansion expansion);
    Optional<Expansion> findById(Long id);
    List<Expansion> findAll();
    void delete(Long id);
}
