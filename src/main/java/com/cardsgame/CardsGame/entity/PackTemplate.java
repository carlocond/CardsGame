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
@Table(name = "pack_templates")
public class PackTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    //Relazione con espansione, da cui verranno pescate le carte del pacchetto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expansion_id")
    private Expansion expansion;

    //Posizione delle carte all'interno del pacchetto per determinare la rarità
    @OneToMany(mappedBy = "packTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PackSlot> packSlots = new HashSet<>();
}
