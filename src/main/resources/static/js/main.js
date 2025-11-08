const API_BASE = "http://localhost:8081/api/v1";
const content = document.getElementById("content");
const navbar = document.getElementById("navbar");

let jwtToken = localStorage.getItem("jwtToken") || null;
let currentUser = null;

// ====== NAVBAR ======
function updateNavbar() {
  if (!jwtToken) {
    navbar.innerHTML = `
      <button id="btn-login">Login</button>
      <button id="btn-register">Registrati</button>
    `;
    document.getElementById("btn-login").onclick = showLogin;
    document.getElementById("btn-register").onclick = showRegister;
  } else {
    navbar.innerHTML = `
      <button id="btn-home">Home</button>
      <button id="btn-packs">Pacchetti</button>
      <button id="btn-album">Album</button>
      ${
        currentUser && currentUser.role === "ADMIN"
          ? `<button id="btn-admin">Admin</button>`
          : ""
      }
      <button id="btn-logout">Logout</button>
    `;
    document.getElementById("btn-home").onclick = showHome;
    document.getElementById("btn-packs").onclick = showPacks;
    document.getElementById("btn-album").onclick = showAlbum;
    if (currentUser && currentUser.role === "ADMIN") {
      document.getElementById("btn-admin").onclick = showAdminPanel;
    }
    document.getElementById("btn-logout").onclick = doLogout;
  }
}

// ====== UTILS ======
function showLoading(target = content) {
  target.innerHTML = `<div class="message">Caricamento...</div>`;
}

// ====== LOGIN / REGISTER ======
function showLogin() {
  content.innerHTML = `
    <h2>Accedi</h2>
    <form id="loginForm">
      <input type="email" id="email" placeholder="Email" required />
      <input type="password" id="password" placeholder="Password" required />
      <button class="button" type="submit">Entra</button>
    </form>
    <p class="message">Admin di test: admin@local.com / admin123</p>
  `;
  document.getElementById("loginForm").onsubmit = doLogin;
}

async function doLogin(e) {
  e.preventDefault();
  const email = document.getElementById("email").value;
  const password = document.getElementById("password").value;
  showLoading();

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!res.ok) throw new Error("Credenziali non valide");
    const data = await res.json();
    jwtToken = data.token;
    localStorage.setItem("jwtToken", jwtToken);
    await fetchCurrentUser(email);
    showHome();
    updateNavbar();
  } catch (err) {
    content.innerHTML = `<p class="message">Errore: ${err.message}</p>`;
  }
}

function showRegister() {
  content.innerHTML = `
    <h2>Registrati</h2>
    <form id="registerForm">
      <input id="fName" placeholder="Nome" required />
      <input id="lName" placeholder="Cognome" required />
      <input type="email" id="email" placeholder="Email" required />
      <input type="password" id="password" placeholder="Password" required />
      <button class="button" type="submit">Crea account</button>
    </form>
  `;
  document.getElementById("registerForm").onsubmit = doRegister;
}

async function doRegister(e) {
  e.preventDefault();
  const payload = {
    fName: document.getElementById("fName").value,
    lName: document.getElementById("lName").value,
    email: document.getElementById("email").value,
    password: document.getElementById("password").value,
  };

  showLoading();

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error("Errore di registrazione");
    const data = await res.json();
    jwtToken = data.token;
    localStorage.setItem("jwtToken", jwtToken);
    await fetchCurrentUser(payload.email);
    showHome();
    updateNavbar();
  } catch (err) {
    content.innerHTML = `<p class="message">${err.message}</p>`;
  }
}

// ====== USER & LOGOUT ======
async function fetchCurrentUser(email) {
  try {
    const res = await fetch("http://localhost:8081/api/users", {
      headers: { Authorization: "Bearer " + jwtToken },
    });
    const users = await res.json();
    currentUser = users.find((u) => u.email === email);
  } catch (e) {
    console.error(e);
  }
}

function doLogout() {
  jwtToken = null;
  currentUser = null;
  localStorage.removeItem("jwtToken");
  showLogin();
  updateNavbar();
}

// ====== HOME ======
function showHome() {
  content.innerHTML = `
    <div class="fade-in">
      <h2>Benvenuto, ${currentUser ? currentUser.fName : "Allenatore"}!</h2>
      <p>Colleziona le carte e diventa il miglior maestro Pokémon!</p>
      <img src="/images/pokeball.png" style="width:150px;margin-top:1rem;animation:spin 3s linear infinite;">
    </div>
  `;
}

// ====== PACKS ======
async function showPacks() {
  showLoading();
  try {
    const res = await fetch("http://localhost:8081/api/pack-templates", {
      headers: { Authorization: "Bearer " + jwtToken },
    });
    const packs = await res.json();

    content.innerHTML = `<h2>Pacchetti disponibili</h2>`;

    packs.forEach((p) => {
      const div = document.createElement("div");
      div.className = "card fade-in";
      div.innerHTML = `
        <img src="/images/bg.jpg" alt="Pack" />
        <h3>${p.name}</h3>
        <p>${p.expansion ? p.expansion.name : "Espansione sconosciuta"}</p>
        <button class="button open-pack-btn" data-id="${p.id}">Apri 🎴</button>
      `;
      content.appendChild(div);
    });

    document.querySelectorAll(".open-pack-btn").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-id");
        const cards = await openPack(id);
        showOpenedCards(cards);
      });
    });
  } catch (e) {
    content.innerHTML = `<p class="message">Errore caricamento pacchetti 😢</p>`;
  }
}

async function openPack(templateId) {
  const res = await fetch(
    `http://localhost:8081/api/pack-openings/${templateId}/open?userId=${currentUser.id}`,
    {
      method: "POST",
      headers: { Authorization: "Bearer " + jwtToken },
    }
  );
  return await res.json();
}

function showOpenedCards(cards) {
  content.innerHTML = `<h2>Hai trovato 🎉</h2>`;
  cards.forEach((card) => {
    const div = document.createElement("div");
    div.className = "card fade-in";
    div.innerHTML = `
      <img src="${card.imageUrl || "/images/bg.jpg"}" />
      <h3>${card.name}</h3>
      <p>${card.rarity}</p>
    `;
    content.appendChild(div);
  });
}

// ====== ALBUM ======
async function showAlbum() {
  showLoading();
  try {
    const res = await fetch(
      `http://localhost:8081/api/users/${currentUser.id}/albums`,
      {
        headers: { Authorization: "Bearer " + jwtToken },
      }
    );
    const albums = await res.json();

    content.innerHTML = `<h2>I tuoi album</h2>`;
    albums.forEach((a) => {
      const div = document.createElement("div");
      div.className = "card fade-in";
      div.innerHTML = `
        <h3>${a.name}</h3>
        <p>${a.items.length} carte</p>
      `;
      content.appendChild(div);
    });
  } catch (e) {
    content.innerHTML = `<p class="message">Errore caricamento album</p>`;
  }
}

// ====== ADMIN PANEL ======
async function showAdminPanel() {
  content.innerHTML = `
    <h2>Dashboard Admin 🛠️</h2>
    <div class="admin-panel">
      <button class="button" id="btn-stats">Statistiche</button>
      <button class="button" id="btn-add-card">Crea Carta</button>
      <button class="button" id="btn-add-pack">Crea Pacchetto</button>
    </div>
    <div id="adminContent" class="fade-in"></div>
  `;

  document.getElementById("btn-stats").onclick = showAdminStats;
  document.getElementById("btn-add-card").onclick = showCardCreator;
  document.getElementById("btn-add-pack").onclick = showPackCreator;

  showAdminStats();
}

async function showAdminStats() {
  const adminContent = document.getElementById("adminContent");
  showLoading(adminContent);

  try {
    const res = await fetch(`http://localhost:8081/api/admin/statistics`, {
      headers: { Authorization: "Bearer " + jwtToken },
    });
    const stats = await res.json();

    const totalCards = stats.totalCards || 0;
    const totalPacks = stats.totalPackTemplates || 0;
    const sum = totalCards + totalPacks || 1;

    const cardsRatio = (totalCards / sum) * 100;
    const packsRatio = (totalPacks / sum) * 100;

    adminContent.innerHTML = `
      <div class="stat-grid">
        <div class="stat-card">
          <h3>Carte totali</h3>
          <p>${totalCards}</p>
        </div>
        <div class="stat-card">
          <h3>Pack template</h3>
          <p>${totalPacks}</p>
        </div>
      </div>

      <div class="chart-wrapper">
        <h3>Distribuzione Carte / Pack</h3>
        <div class="chart-bar-container">
          <div class="chart-bar" id="bar-cards">
            <span>${Math.round(cardsRatio)}%</span>
          </div>
          <div class="chart-bar" id="bar-packs">
            <span>${Math.round(packsRatio)}%</span>
          </div>
        </div>
        <div class="chart-labels">
          <div class="chart-label">Carte</div>
          <div class="chart-label">Pack</div>
        </div>
      </div>
    `;

    setTimeout(() => {
      document.getElementById("bar-cards").classList.add("animate");
      document.getElementById("bar-packs").classList.add("animate");
    }, 50);
  } catch (e) {
    adminContent.innerHTML = `<p class="message">Errore nel caricamento statistiche</p>`;
  }
}

function showCardCreator() {
  const adminContent = document.getElementById("adminContent");
  adminContent.innerHTML = `
    <h2>Crea nuova carta Pokémon</h2>
    <form id="createCardForm">
      <input id="name" placeholder="Nome carta" required />
      <input id="description" placeholder="Descrizione" />
      <input id="imageUrl" placeholder="URL immagine" />
      <input id="type" placeholder="Tipo (es. ELECTRIC)" />
      <input id="expansionId" placeholder="ID espansione" required />
      <select id="rarity">
        <option value="COMMON">COMMON</option>
        <option value="UNCOMMON">UNCOMMON</option>
        <option value="RARE">RARE</option>
        <option value="ULTRA_RARE">ULTRA_RARE</option>
      </select>
      <button class="button" type="submit">Crea</button>
    </form>
  `;

  document.getElementById("createCardForm").onsubmit = async (e) => {
    e.preventDefault();
    const payload = {
      name: document.getElementById("name").value,
      description: document.getElementById("description").value,
      type: document.getElementById("type").value || "NORMAL",
      rarity: document.getElementById("rarity").value,
      expansion: { id: parseInt(document.getElementById("expansionId").value) },
      imageUrl: document.getElementById("imageUrl").value,
    };

    try {
      const res = await fetch(`http://localhost:8081/api/admin/cards`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + jwtToken,
        },
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error("Errore nella creazione");
      alert("Carta creata con successo!");
      showAdminStats();
    } catch {
      alert("Errore durante la creazione della carta");
    }
  };
}

function showPackCreator() {
  const adminContent = document.getElementById("adminContent");
  adminContent.innerHTML = `
    <h2>Crea nuovo Pack Template</h2>
    <form id="createPackForm">
      <input id="name" placeholder="Nome pacchetto" required />
      <input id="expansionId" placeholder="ID espansione" required />
      <button class="button" type="submit">Crea</button>
    </form>
  `;

  document.getElementById("createPackForm").onsubmit = async (e) => {
    e.preventDefault();
    const payload = {
      name: document.getElementById("name").value,
      expansion: { id: parseInt(document.getElementById("expansionId").value) },
    };

    try {
      const res = await fetch(
        `http://localhost:8081/api/admin/pack-templates`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer " + jwtToken,
          },
          body: JSON.stringify(payload),
        }
      );
      if (!res.ok) throw new Error("Errore");
      alert("Pack Template creato!");
      showAdminStats();
    } catch {
      alert("Errore durante la creazione del pacchetto");
    }
  };
}

// ====== INIT ======
updateNavbar();
if (jwtToken) {
  showHome();
} else {
  showLogin();
}
