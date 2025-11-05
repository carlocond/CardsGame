package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.PackSlot;
import com.cardsgame.CardsGame.entity.PackTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackSlotRepo extends JpaRepository<PackSlot, Long> {

    List<PackSlot> findByPackTemplate(PackTemplate packTemplate);
}
