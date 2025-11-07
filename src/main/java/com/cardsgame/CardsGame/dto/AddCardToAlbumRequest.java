package com.cardsgame.CardsGame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddCardToAlbumRequest {
    private Long cardId;
    private Integer quantity = 1;
}

