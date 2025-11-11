package com.cardsgame.CardsGame.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "collection_items", uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "card_id"}))
//I constraint si assicurano che non ci possano essere più carte uguali nello stesso album
public class CollectionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private UserAlbum album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    private Integer quantity;
}
