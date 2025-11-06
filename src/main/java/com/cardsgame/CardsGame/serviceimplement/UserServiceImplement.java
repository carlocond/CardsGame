package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.entity.User;
import com.cardsgame.CardsGame.repository.UserRepo;
import com.cardsgame.CardsGame.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImplement implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(User user){
        if (user.getPassword() != null) { //Controlla che la password esista
            //Se esiste, la cripta
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        //Salva l'utente
        return userRepo.save(user);
    }

    @Override
    public Optional<User> findById(Long id){
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email){
        return userRepo.findByEmail(email);
    }

    @Override
    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Override
    public void delete(Long id){
        userRepo.deleteById(id);
    }
}
