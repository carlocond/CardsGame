package com.cardsgame.CardsGame.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Data //Getter e Setter automatici
@NoArgsConstructor //Costruttore di default automatico
@AllArgsConstructor //Costruttore parametrizzato automatico
@Builder //Utilizzo del design pattern builder
@Table(name = "users")
public class User implements UserDetails {
/*
UserDatails è un interfaccia fornita da Spring che permette di salvare le informazioni relative all'user
garantendo maggiore sicurezza dei dati sensibili.
 */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fName;
    private String lName;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true) //orphanRemoval elimina completamente l'entità associata alla classe padre, quando viene eliminata
    @Builder.Default //Annotazione che permette di inizializzare i valori iniziali di default
    private Set<UserAlbum> albums = new HashSet<>(); //Set è un'interfaccia che permette di avere una collezione unica, HashSet è la classe che la implementa

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security si aspetta che i ruoli usino il prefisso 'ROLE_'
        // qui restituiamo quindi ad esempio 'ROLE_ADMIN' o 'ROLE_USER'
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
