package com.cardsgame.CardsGame.controller;

import com.cardsgame.CardsGame.entity.User;
import com.cardsgame.CardsGame.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController //indica che questa classe è un controller
@RequestMapping("/api/users") //percorso di tutte le richieste
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping //postmapping serve per fare una richiesta HTTP di tipo POST
    public ResponseEntity<User> create(@RequestBody User user){//RequestBody serve a indicare che il tipo di dati che verranno inseriti sono di tipo User
        User userCreated = userService.create(user);
        return ResponseEntity.created(URI.create("/api/users/" + userCreated.getId())).body(userCreated);
        //URI sarebbe l'indirizzo della risorsa creata, in questo caso /api/users, seguito dall'id dell'utente creato
    }

    @GetMapping("/{id}") //getmapping serve per fare una richiesta HTTP di tipo GET
    public ResponseEntity<User> getById(@PathVariable Long id){//PathVariable serve a indicare quale valore verrà inserito nell'URL
        return userService.findById(id)//cerca l'utente
                .map(ResponseEntity::ok) //.map è un metodo che trasforma l'oggettto User in un ResponseEntity
                .orElseThrow(() -> new RuntimeException("Utente non trovato con id: " + id)); //Se l'utente non esiste viene lanciato l'errore
    }

    @GetMapping
    public ResponseEntity<List<User>> list(){
        return ResponseEntity.ok(userService.findAll()); //Restituisce tutti gli studenti
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> delete(@PathVariable Long id){
        userService.delete(id);
        return ResponseEntity.noContent().build(); //Restituisce una risposta senza contenuto
    }

}
