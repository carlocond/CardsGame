# CardsGame

> Gioco di carte collezionabili ispirato al mondo Pokémon, sviluppato in **Spring Boot (Java)** con integrazione frontend statico.  
> Gestione utenti, ruoli (USER / ADMIN), pacchetti, album e statistiche globali.

---

## ⚙️ Stack Tecnologico

### Backend
- **Java 17+**
- **Spring Boot 3**
  - Spring Web
  - Spring Security (JWT)
  - Spring Data JPA (Hibernate)
- **MySQL** (o H2 per testing)
- **Maven** per il build
- **Lombok** per boilerplate ridotto

### Frontend (Statico)
- HTML5, CSS3, JavaScript (vanilla)
- Animazioni leggere e stile “Pokémon”
- Servito direttamente da Spring Boot (`src/main/resources/static`)

---

## 🚀 Funzionalità Principali

### 👤 Utente (ROLE_USER)
- Registrazione e login tramite JWT  
- Visualizza espansioni e carte collezionabili  
- Apre pacchetti (pack opening) e riceve carte casuali  
- Gestisce i propri album (crea, visualizza, aggiunge carte)

### 🛠️ Amministratore (ROLE_ADMIN)
- Gestione completa delle carte (`/api/admin/cards`)  
- Gestione dei template dei pacchetti (`/api/admin/pack-templates`)  
- Statistiche globali (`/api/admin/statistics`)

### 🎨 Frontend
- Interfaccia moderna e colorata in stile Pokémon  
- Navigazione: Home, Pacchetti, Album, Admin  
- Animazioni fluide con transizioni e hover sugli elementi  
- Integrazione diretta con le API Spring Boot (porta 8081)

---

## 📂 Struttura del Progetto

CardsGame/
│
├── src/
│ ├── main/
│ │ ├── java/com/cardsGame/
│ │ │ ├── controllers/ → Controller REST (User, Admin, Auth, Cards)
│ │ │ ├── models/ → Entità JPA (User, Card, Album, etc.)
│ │ │ ├── services/ → Business logic
│ │ │ ├── repositories/ → Spring Data JPA
│ │ │ └── security/ → Configurazione JWT & Authentication
│ │ └── resources/
│ │ ├── static/ → Frontend statico servito da Spring Boot
│ │ │ ├── index.html
│ │ │ ├── style.css
│ │ │ ├── main.js
│ │ │ └── assets/
│ │ │ ├── bg.jpg
│ │ │ └── pokeball.png
│ │ └── application.yml → Configurazione DB e sicurezza
│ └── test/ → Test unitari
│
├── pom.xml
└── README.md

---

## 🧩 Endpoints Principali

### 🔐 Autenticazione
| Metodo | Endpoint | Descrizione |
|:-------|:----------|:------------|
| `POST` | `/api/v1/auth/register` | Registrazione utente |
| `POST` | `/api/v1/auth/login` | Login con JWT |

---

### 🧑‍🎮 Utente
| Metodo | Endpoint | Descrizione |
|:-------|:----------|:------------|
| `GET` | `/api/cards` | Lista carte |
| `GET` | `/api/pack-templates` | Lista template pacchetti |
| `POST` | `/api/pack-openings/{packTemplateId}/open?userId={id}` | Apre un pacchetto |
| `GET` | `/api/users/{userId}/albums` | Visualizza album utente |
| `POST` | `/api/users/{userId}/albums` | Crea nuovo album |
| `POST` | `/api/users/{userId}/albums/{albumId}/add-cards` | Aggiunge carte all’album |

---

### 🧑‍💼 Admin
| Metodo | Endpoint | Descrizione |
|:-------|:----------|:------------|
| `POST` | `/api/admin/cards` | Crea una nuova carta |
| `DELETE` | `/api/admin/cards/{id}` | Elimina carta |
| `POST` | `/api/admin/pack-templates` | Crea un nuovo pack template |
| `DELETE` | `/api/admin/pack-templates/{id}` | Elimina un pack template |
| `GET` | `/api/admin/statistics` | Visualizza statistiche globali |

---

## 🧠 Ruoli e Credenziali

- **Admin di default:**  
email: admin@local.com
password: admin123
(Creato automaticamente dal `DataLoader` all’avvio)

---
(Creato automaticamente dal `DataLoader` all’avvio)

---

## 🖥️ Come eseguire il progetto

### 1️⃣ Configura il database
Nel file `src/main/resources/application.properties`:

```properties
# === DATABASE ===
spring.datasource.url=jdbc:mysql://localhost:3306/cardsdb
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# === JPA ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# === SERVER ===
server.port=8081

#Avvia il programma
mvn spring-boot:run

#Apri il browser con il link:
http://localhost:8081/
