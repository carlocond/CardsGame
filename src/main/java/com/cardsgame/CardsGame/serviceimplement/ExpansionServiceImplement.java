package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.repository.ExpansionRepo;
import com.cardsgame.CardsGame.service.ExpansionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpansionServiceImplement implements ExpansionService {
    private final ExpansionRepo expansionRepo;

    @Override
    public Expansion create(Expansion expansion){
        return expansionRepo.save(expansion);
    }

    @Override
    public Optional<Expansion> findById(Long id){
        return expansionRepo.findById(id);
    }

    @Override
    public List<Expansion> findAll(){
        return expansionRepo.findAll();
    }

    @Override
    public void delete(Long id){
        expansionRepo.deleteById(id);
    }
}
