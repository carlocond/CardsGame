package com.cardsgame.CardsGame;

import com.cardsgame.CardsGame.entity.*;
import com.cardsgame.CardsGame.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    /*
    Il DataLoader viene eseguito all'avvio dell'applicazione per popolare dati di esempio.
    Ho reso il caricamento idempotente: se qualcosa è già presente non viene duplicato,
    ma le entità mancanti verranno create. Questo evita che un check su admin impedisca
    la creazione di altri dati utili (espansioni, carte, pack template).
     */

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepo userRepo;
    private final ExpansionRepo expansionRepo;
    private final CardRepo cardRepo;
    private final PackTemplateRepo packTemplateRepo;
    private final PackSlotRepo packSlotRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String ... args) throws Exception{
        // 1) assicurati che l'admin esista
        if (userRepo.findByEmail("admin@local.com").isEmpty()){
            User admin = User.builder()
                    .fName("Admin")
                    .lName("Admin")
                    .email("admin@local.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepo.save(admin);
            log.info("Utente admin inserito correttamente.");
        } else {
            log.info("Utente admin già presente -> non creato");
        }

        // 2) crea espansioni solo se non ce ne sono
        if (expansionRepo.count() == 0) {
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

            // Aggiunta delle carte alle espansioni
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
        } else {
            log.info("Espansioni già presenti -> non create");
        }

        // 3) crea pack template di esempio solo se non ce ne sono
        if (packTemplateRepo.count() == 0) {
            Map<Rarity, Integer> weights = new HashMap<>();
            weights.put(Rarity.COMMON, 70);
            weights.put(Rarity.UNCOMMON, 20);
            weights.put(Rarity.RARE, 9);
            weights.put(Rarity.ULTRA_RARE, 1);

            createRandPackTemplate("Starter Pack 1", 10, weights);
        } else {
            log.info("Pack template già presenti -> non creati");
        }
    }

    private void createCard(String name, Rarity rarity, Expansion expansion, String imageUrl, String description){
        // Verifica se la carta esiste già (per nome + expansion)
        boolean exists = cardRepo.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name)
                        && c.getExpansion() != null
                        && c.getExpansion().getName().equalsIgnoreCase(expansion.getName()));
        if (exists) return;

        Card c = Card.builder()
                .name(name)
                .rarity(rarity)
                .expansion(expansion)
                .imageUrl(imageUrl)
                .description(description)
                .build();
        cardRepo.save(c);
        // Aggiunta della carta all'espansione in memoria
        if (expansion.getCards() == null) expansion.setCards(new HashSet<>());
        expansion.getCards().add(c);
        expansionRepo.save(expansion);
    }

    private void createRandPackTemplate(String name, int requestedSlotCount, Map<Rarity, Integer> rarityWeights){
        int slotCount = Math.min(requestedSlotCount, 10);
        List<Expansion> expansions = expansionRepo.findAll();

        if (expansions.isEmpty()){
            log.info("Non è stato possibile creare un pack template: Nessuna espansione trovata.");
            return;
        }

        PackTemplate template = PackTemplate.builder()
                .name(name)
                .packSlots(new HashSet<>())
                .build();
        packTemplateRepo.save(template);

        List<Rarity> weightedRarities = new ArrayList<>();
        for (Map.Entry<Rarity, Integer> entry : rarityWeights.entrySet()){
            int weight = Math.max(0, entry.getValue());
            for (int i = 0; i < weight; i++) weightedRarities.add(entry.getKey());
        }
        if (weightedRarities.isEmpty()){
            weightedRarities = List.of(Rarity.values()).stream().collect(Collectors.toCollection(ArrayList::new));
        }

        Random rnd = new Random();

        for (int i = 0; i < slotCount; i++){
            Rarity chosenRarity = weightedRarities.get(rnd.nextInt(weightedRarities.size()));
            Expansion chosenExpansion = expansions.get(rnd.nextInt(expansions.size()));

            PackSlot slot = PackSlot.builder()
                    .position(i)
                    .rarity(chosenRarity)
                    .expansion(chosenExpansion)
                    .packTemplate(template)
                    .build();
            packSlotRepo.save(slot);

            if (template.getPackSlots() == null) template.setPackSlots(new HashSet<>());
            template.getPackSlots().add(slot);
        }

        packTemplateRepo.save(template);
        log.info("Template casuale: {} con {} slot, creato correttamente", name, slotCount);
    }

}
