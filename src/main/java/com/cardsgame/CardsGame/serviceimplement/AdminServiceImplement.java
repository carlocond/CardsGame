package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.dto.CreateCardRequest;
import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.PackTemplate;
import com.cardsgame.CardsGame.repository.CardRepo;
import com.cardsgame.CardsGame.repository.PackTemplateRepo;
import com.cardsgame.CardsGame.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImplement implements AdminService {

    private final PackTemplateRepo packTemplateRepo;
    private final CardRepo cardRepo;

    @Override
    public PackTemplate createPackTemplate(PackTemplate packTemplate) {
        return packTemplateRepo.save(packTemplate);
    }

    @Override
    public PackTemplate updatePackTemplate(Long id, PackTemplate packTemplate){
        PackTemplate existingPackTemplate = packTemplateRepo.findById(id) //Controlla se esiste il template
                .orElseThrow(() -> new RuntimeException("Pack Template non trovato con id: " + id));
        existingPackTemplate.setName(packTemplate.getName()); //Aggiorna i campi necessari
        return packTemplateRepo.save(existingPackTemplate); //Salva le modifiche
    }

    @Override
    public Card addCard(CreateCardRequest cardRequest) {
        Card card = new Card();
        card.setName(cardRequest.getName());
        card.setDescription(cardRequest.getDescription());
        card.setRarity(cardRequest.getRarity());
        card.setExpansion(cardRequest.getExpansion());
        card.setImageUrl(cardRequest.getImageUrl());
        return cardRepo.save(card);
    }

    @Override
    public void deletePackTemplate(Long id) {
        if(!packTemplateRepo.existsById(id)){
            throw new RuntimeException("Pack Template non trovato con id: " + id);
        }
        packTemplateRepo.deleteById(id);
    }

    @Override
    public void removeCard(Long id) {
        if(!cardRepo.existsById(id)){
            throw new RuntimeException("Carta non trovata con id: " + id);
        }
        cardRepo.deleteById(id);
    }

    @Override
    public Map<String, Object> getStatistics(){
        Map<String, Object> stats = new HashMap<>(); //Crea una mappa per le statistiche delle carte
        stats.put("totalCards", cardRepo.count()); //Conta il numero totale di carte
        stats.put("totalPackTemplates", packTemplateRepo.count()); //Conta il numero totale di pack template
        return stats;
    }
}
