package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.dto.CreateCardRequest;
import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.PackTemplate;

import java.util.Map;


public interface AdminService {

    PackTemplate createPackTemplate(PackTemplate packTemplate);
    PackTemplate updatePackTemplate(Long id, PackTemplate packTemplate);
    void deletePackTemplate(Long id);
    Card addCard(CreateCardRequest card);
    void removeCard(Long id);
    Map<String, Object> getStatistics();
}
