package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.CollectionItem;
import com.cardsgame.CardsGame.entity.UserAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionItemRepo extends JpaRepository<CollectionItem, Long> {

    List<CollectionItem> findByAlbum(UserAlbum album);
    Optional<CollectionItem> findByAlbumAndCard(UserAlbum album, Card card);
}
