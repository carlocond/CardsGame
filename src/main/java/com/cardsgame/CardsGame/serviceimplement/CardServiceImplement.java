package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.entity.Rarity;
import com.cardsgame.CardsGame.repository.CardRepo;
import com.cardsgame.CardsGame.service.CardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CardServiceImplement implements CardService {

    private final CardRepo cardRepo;

    @Override
    public Card create(Card card){
        return cardRepo.save(card);
    }

    @Override
    public Optional<Card> findById(Long id) {
        return cardRepo.findById(id);
    }

    @Override
    public List<Card> findByExpansion(Expansion expansion) {
        return cardRepo.findAll().stream() //trova le carte
                //filtra in base all'espansione e controlla che non sia null
                .filter(c -> c.getExpansion() != null &&
                        //Controlla che l'id dell'espansione della carta sia uguale a quello inserito
                        c.getExpansion().getId().equals(expansion.getId()))
                //Inserisce in una lista
                .collect(Collectors.toList());
    }

    @Override
    public List<Card> findAll() {
        return cardRepo.findAll();
    }

    @Override
    public void delete(Long id) {
        cardRepo.deleteById(id);
    }
}
