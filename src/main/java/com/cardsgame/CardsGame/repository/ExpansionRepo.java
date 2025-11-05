package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.Expansion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpansionRepo extends JpaRepository<Expansion, Long> {

    Optional<Expansion> findByName(String name);
}
