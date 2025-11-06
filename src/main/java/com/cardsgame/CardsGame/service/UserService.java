package com.cardsgame.CardsGame.service;

import com.cardsgame.CardsGame.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User create (User user);
    Optional<User> findById (Long id);
    Optional<User> findByEmail (String email);
    List<User> findAll();
    void delete(Long id);
}
