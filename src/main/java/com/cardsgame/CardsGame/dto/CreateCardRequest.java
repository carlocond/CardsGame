package com.cardsgame.CardsGame.dto;

import com.cardsgame.CardsGame.entity.Expansion;
import com.cardsgame.CardsGame.entity.Rarity;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequest {
    /*
    Questo DTO viene utilizzato per creare una nuova carta tramite l'AdminService.
    DTO sta per data transfer object, ed è un oggetto che trasporta dati tra processi,
     */
    private String name;
    private String description;
    private String type;
    private Rarity rarity;
    private Expansion expansion;
    private String imageUrl;

}
