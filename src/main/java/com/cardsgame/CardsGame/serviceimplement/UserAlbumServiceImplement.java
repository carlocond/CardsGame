package com.cardsgame.CardsGame.serviceimplement;

import com.cardsgame.CardsGame.dto.AddCardToAlbumRequest;
import com.cardsgame.CardsGame.dto.AddCardsToAlbumRequest;
import com.cardsgame.CardsGame.entity.Card;
import com.cardsgame.CardsGame.entity.CollectionItem;
import com.cardsgame.CardsGame.entity.User;
import com.cardsgame.CardsGame.entity.UserAlbum;
import com.cardsgame.CardsGame.repository.CardRepo;
import com.cardsgame.CardsGame.repository.CollectionItemRepo;
import com.cardsgame.CardsGame.repository.UserAlbumRepo;
import com.cardsgame.CardsGame.repository.UserRepo;
import com.cardsgame.CardsGame.service.UserAlbumService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAlbumServiceImplement implements UserAlbumService {

    private final UserRepo userRepo;
    private final UserAlbumRepo userAlbumRepo;
    private final CardRepo cardRepo;
    private final CollectionItemRepo collectionItemRepo;

    @Override
    public UserAlbum createAlbum(Long userId, UserAlbum album) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User non trovato"));
        album.setUser(user);
        return userAlbumRepo.save(album);
    }

    @Override
    public List<UserAlbum> listAlbums(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User non trovato"));
        return userAlbumRepo.findByUser(user);
    }

    @Override
    public Optional<UserAlbum> getAlbum(Long userId, Long albumId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User non trovato"));
        return userAlbumRepo.findById(albumId)
                .filter(a -> a.getUser().getId().equals(user.getId()));
    }

    @Override
    public void addCardToAlbum(Long userId, Long albumId, AddCardToAlbumRequest request) {
        UserAlbum album = userAlbumRepo.findById(albumId).orElseThrow(() -> new RuntimeException("Album non trovato"));
        if (!album.getUser().getId().equals(userId)) throw new RuntimeException("Permesso negato");

        Card card = cardRepo.findById(request.getCardId()).orElseThrow(() -> new RuntimeException("Carta non trovata"));
        CollectionItem existing = collectionItemRepo.findByAlbumAndCard(album, card).orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + (request.getQuantity() == null ? 1 : request.getQuantity()));
            collectionItemRepo.save(existing);
        } else {
            CollectionItem item = CollectionItem.builder()
                    .album(album)
                    .card(card)
                    .quantity(request.getQuantity() == null ? 1 : request.getQuantity())
                    .build();
            collectionItemRepo.save(item);
            album.getItems().add(item);
            userAlbumRepo.save(album);
        }
    }

    @Override
    public void addCardsToAlbum(Long userId, Long albumId, AddCardsToAlbumRequest request) {
        for (Long cardId : request.getCardIds()){
            addCardToAlbum(userId, albumId, new AddCardToAlbumRequest(cardId, 1));
        }
    }
}

