package com.cardsgame.CardsGame;

import com.cardsgame.CardsGame.entity.*;
import com.cardsgame.CardsGame.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    /*
    Il DataLoader è un componente che viene eseguito per inserire i dati all'interno del database;
    CommandLineRunner è l'interfaccia che permette l'esecuzione al momento dell'avvio
     */

    private static final Logger log = (Logger) LoggerFactory.getLogger(DataLoader.class);
    //Il logger viene usato per stampare i messaggi nella console
    //LoggerFactory è una classe che crea istanze di Logger


    //Connessione alle repository
    private final UserRepo userRepo;
    private final ExpansionRepo expansionRepo;
    private final CardRepo cardRepo;
    private final PackTemplateRepo packTemplateRepo;
    private final PackSlotRepo packSlotRepo;
    private final PasswordEncoder passwordEncoder; //Codifica delle password

    //Override del metodo run di CommandLineRunner
    //Run è il metodo che viene eseguito all'avvio dell'app
    @Override
    public void run(String ... args) throws Exception{
        //Controlla se esiste l'admin
        if (userRepo.findByEmail("admin@local.com").isPresent()){
            log.info("Dati inseriti gia esistenti.");
            return;
        }
        //Creazione dell'admin con password codificata
        User admin = User.builder()
                .fName("Admin")
                .lName("Admin")
                .email("admin@local.com")
                .password("admin123")
                .role(Role.ADMIN)
                .build();
        userRepo.save(admin);
        log.info("Dati utente inseriti correttamente.");

        //Creazione di espansioni
        Expansion expansion1 = Expansion.builder()
                .name("Base set")
                .description("Espansione di esempio 1")
                .cards(new HashSet<>())
                .build();
        Expansion expansion2 = Expansion.builder()
                .name("Shiny Stars")
                .description("Espansione di esempio 2")
                .cards(new HashSet<>())
                .build();
        Expansion expansion3 = Expansion.builder()
                .name("Legends")
                .description("Espansione di esempio 3")
                .cards(new HashSet<>())
                .build();

        expansionRepo.saveAll(List.of(expansion1, expansion2, expansion3));
        log.info("Espansioni create correttamente.");

        //Aggiunta delle carte alle espansioni

        createCard("Pikachu", Rarity.COMMON, expansion1, "url/pikachu.png", "Elettrico base");
        createCard("Bulbasaur", Rarity.COMMON, expansion1, "url/bulbasaur.png", "Erba base");
        createCard("Charmander", Rarity.UNCOMMON, expansion1, "url/charmander.png", "Fuoco non comune");
        createCard("Mewtwo", Rarity.RARE, expansion1, "url/mewtwo.png", "Psico raro");

        createCard("Shiny Gyarados", Rarity.ULTRA_RARE, expansion2, "url/gyarados.png", "Shiny ultra raro");
        createCard("Eevee", Rarity.COMMON, expansion2, "url/eevee.png", "Evoluzioni multiple");
        createCard("Vaporeon", Rarity.UNCOMMON, expansion2, "url/vaporeon.png", "Evoluzione non comune");

        createCard("Zapdos", Rarity.RARE, expansion3, "url/zapdos.png", "Leggendario raro");
        createCard("Articuno", Rarity.RARE, expansion3, "url/articuno.png", "Leggendario raro");
        createCard("Moltres", Rarity.ULTRA_RARE, expansion3, "url/moltres.png", "Leggendario ultra raro");

        log.info("Carte create correttamente.");
    }

    //Metodo per creare e salvare le carte

    private void createCard(String name, Rarity rarity, Expansion expansion, String imageUrl, String description){
        Card c = Card.builder()
                .name(name)
                .rarity(rarity)
                .expansion(expansion)
                .imageUrl(imageUrl)
                .description(description)
                .build();
        cardRepo.save(c);
        //Aggiunta della carta all'espansione in memoria
        if (expansion.getCards() == null) expansion.setCards(new HashSet<>());
        expansion.getCards().add(c);
        expansionRepo.save(expansion);
    }

    //Metodo che crea un PackTemplate con slot casuali con limite di 10 carte per pack

    private void createRandPackTemplate(String name, int requestedSlotCount, Map<Rarity, Integer> rarityWeights){
        //Impostazione di 10 slot massimi
        int slotCount = Math.min(requestedSlotCount, 10);

        //Utilizzo di tutte le espansioni esistenti
        List<Expansion> expansions = expansionRepo.findAll();

        if (expansions.isEmpty()){ //Controlla se ci sono espansioni per creare il template
            log.info("Non è stato possibile creare un pack template: Nessuna espansione trovata.");
            return;
        }

        //Creazione del template
        PackTemplate template = PackTemplate.builder()
                .name(name)
                .packSlots(new HashSet<>())
                .build();
        packTemplateRepo.save(template);

        //Creazione di una lista pesata: il peso determina la percentuale di uscita di una specifica rarità della carta esempio(Comune 70%, Raro 20%, ecc..)
        List<Rarity> weightedRarities = new ArrayList<>();
        for (Map.Entry<Rarity, Integer> entry : rarityWeights.entrySet()){ //Scorre tutte le rarità e i loro pesi presenti nella mappa(Chiave Rarità, Valore Peso)
            int weight = Math.max(0, entry.getValue()); //Controllo che il peso sia sempre positivo
            for (int i = 0; i < weight; i++) weightedRarities.add(entry.getKey()); //Aggiunta della rarità in base al peso
        }
        //Controlla se la lista è vuota e in caso aggiunge tutte le rarità
        if (weightedRarities.isEmpty()){
            weightedRarities = List.of(Rarity.values()).stream().collect(Collectors.toCollection(ArrayList::new));
        }

        Random rnd = new Random(); //Creazione di un generatore di numeri random

        //Creazione degli slot e salvataggio
        for (int i = 0; i <= slotCount; i++){
            //Rarità casuale in base al peso
            Rarity chosenRarity = weightedRarities.get(rnd.nextInt(weightedRarities.size()));
            //Espansione casuale
            Expansion chosenExpansion = expansions.get(rnd.nextInt(expansions.size()));

            //Creazione slot e salvataggio
            PackSlot slot = PackSlot.builder()
                    .position(i)
                    .rarity(chosenRarity)
                    .expansion(chosenExpansion)
                    .packTemplate(template)
                    .build();
            packSlotRepo.save(slot);

            //Salvataggio in memoria
            if (template.getPackSlots() == null) template.setPackSlots(new HashSet<>());
            template.getPackSlots().add(slot);
        }

        //Salvataggio del template aggiornato
        packTemplateRepo.save(template);
        log.info("Template casuale:" + name + "\nCon " + slotCount +" slot, creato correttamente");
    }

}
