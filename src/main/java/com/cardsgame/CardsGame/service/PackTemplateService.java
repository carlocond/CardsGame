package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.PackTemplate;

import java.util.List;
import java.util.Optional;

public interface PackTemplateService {
    PackTemplate create (PackTemplate packTemplate);
    Optional<PackTemplate> findById(Long id);
    List<PackTemplate> findAll();
    void delete(Long id);
}
