package com.cardsgame.CardsGame.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expansions")
public class Expansion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "expansion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Card> cards = new HashSet<>();
}
