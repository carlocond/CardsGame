package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.PackSlot;
import com.cardsgame.CardsGame.entity.PackTemplate;
import com.cardsgame.CardsGame.repository.CardRepo;
import com.cardsgame.CardsGame.repository.PackTemplateRepo;
import com.cardsgame.CardsGame.service.PackOpeningService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PackOpeningServiceImplement implements PackOpeningService {

    private final PackTemplateRepo packTemplateRepo;
    private final CardRepo cardRepo;
    private final Random rnd = new Random();

    @Override
    public List<Card> openPack(Long userId, Long packTemplateId){
        PackTemplate template = packTemplateRepo.findById(packTemplateId)
                .orElseThrow(() -> new RuntimeException("Pack template non trovato"));
        //Ottiene gli slot del pacchetto
        Set<PackSlot> slots = template.getPackSlots();
        if(slots == null){ //Controlla se gli slot sono null
            try {
                //Prova a chiamare il metodo getPackSlots tramite reflection
                //Reflection permette di ispezionare e manipolare classi metodi e campi a runtime.
                slots = (Set<PackSlot>) template.getClass().getMethod("getPackSlots").invoke(template);
            } catch (Exception e) {
                slots = Collections.emptySet();
            }
        }

        // Fallback: se non ci sono slot definiti, genera un pacchetto standard di 10 carte
        if (slots == null || slots.isEmpty()) {
            int defaultSize = 10;
            // prova a usare l'espansione del template come pool
            Long expansionId = template.getExpansion() != null ? template.getExpansion().getId() : null;
            List<Card> pool = (expansionId != null) ? cardRepo.findByExpansionId(expansionId) : cardRepo.findAll();
            if (pool == null || pool.isEmpty()) return Collections.emptyList();
            List<Card> fallbackResult = new ArrayList<>();
            for (int i = 0; i < defaultSize; i++) {
                fallbackResult.add(pool.get(rnd.nextInt(pool.size())));
            }
            return fallbackResult;
        }

        List<Card> allCards = cardRepo.findAll();
        List<Card> result = new ArrayList<>();

        //Scorre gli slot del pacchetto
        for (PackSlot slot : slots){
            //Filtra le carte che corrispondono alla rarità e all'espansione dello slot
            List<Card> candidates;
            if (slot.getExpansion() != null) {
                // use findByExpansionId to avoid possible proxy equality issues
                candidates = cardRepo.findByExpansionId(slot.getExpansion().getId());
            } else {
                candidates = allCards;
            }
            candidates = candidates.stream()
                    .filter(c -> c.getRarity() == slot.getRarity())
                    .collect(Collectors.toList()); //Raccoglie il tutto in una lista

            //Controllo se non ci sono corrispondenti
            if (candidates.isEmpty()){
                candidates = allCards.stream()
                        .filter(c -> c.getRarity() == slot.getRarity())
                        .collect(Collectors.toList());
            }

            //Controlla se è ancora vuoto
            if (candidates.isEmpty()){
                candidates = new ArrayList<>(allCards);
            }
            //Sceglie una carta a caso tra i candidati
            if (!candidates.isEmpty()){
                Card chosen = candidates.get(rnd.nextInt(candidates.size()));
                result.add(chosen); //Aggiunge la carta scelta al risultato
            }
        }
        //Restituisce la lista delle carte ottenute dall'apertura del pacchetto
        return result;
    }
}
