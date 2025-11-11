package com.cardsgame.CardsGame.config;

import org.springframework.context.annotation.Configuration;

// Placeholder Jackson config – Hibernate module removed to avoid adding extra dependency.
// Entities already annotated with @JsonIgnoreProperties to prevent proxy serialization issues.
@Configuration
public class JacksonConfig {
    // Intentionally left empty.
}
