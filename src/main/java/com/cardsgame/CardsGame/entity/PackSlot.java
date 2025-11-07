package com.cardsgame.CardsGame.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pack_slots")
public class PackSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Posizione della carta
    private Integer position;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pack_template_id")
    private PackTemplate packTemplate;

    // Espansione associata allo slot (singola Expansion) — mapping corretto è ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expansion_id")
    private Expansion expansion;
}
