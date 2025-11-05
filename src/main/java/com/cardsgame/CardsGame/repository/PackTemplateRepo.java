package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.entity.PackTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackTemplateRepo extends JpaRepository<PackTemplate, Long> {

    List<PackTemplate> findByExpansion(Expansion expansion);
}
