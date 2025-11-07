package com.cardsgame.CardsGame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCardsToAlbumRequest {
    private List<Long> cardIds;
}

