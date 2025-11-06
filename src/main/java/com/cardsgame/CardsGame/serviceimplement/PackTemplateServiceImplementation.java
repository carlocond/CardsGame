package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.entity.PackTemplate;
import com.cardsgame.CardsGame.repository.PackTemplateRepo;
import com.cardsgame.CardsGame.service.PackTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PackTemplateServiceImplementation implements PackTemplateService {
    private final PackTemplateRepo packTemplateRepo;

    @Override
    public PackTemplate create(PackTemplate packTemplate) {
        return packTemplateRepo.save(packTemplate);
    }

    @Override
    public Optional<PackTemplate> findById(Long id) {
        return packTemplateRepo.findById(id);
    }

    @Override
    public List<PackTemplate> findAll(){
        return  packTemplateRepo.findAll();
    }

    @Override
    public void delete(Long id){
        packTemplateRepo.deleteById(id);
    }
}
