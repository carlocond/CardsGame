package com.cardsgame.CardsGame.repository;

import com.cardsgame.CardsGame.entity.User;
import com.cardsgame.CardsGame.entity.UserAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAlbumRepo extends JpaRepository<UserAlbum, Long> {

    List<UserAlbum> findByUser(User user);
}
