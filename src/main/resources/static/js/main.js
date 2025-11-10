// ==================== CONFIG & STATO ====================

const API_BASE = "http://localhost:8081/api";

const state = {
  token: null,
  user: null,
  openedPack: [],
  keptCards: [],
  albums: [],
  packTemplates: [],
};

// ==================== ELEMENTI BASE ====================

const view = document.getElementById("view");
const navHome = document.getElementById("nav-home");
const navPacks = document.getElementById("nav-packs");
const navAlbums = document.getElementById("nav-albums");
const navAdmin = document.getElementById("nav-admin");
const navLogin = document.getElementById("nav-login");
const navLogout = document.getElementById("nav-logout");
const navUserLabel = document.getElementById("nav-user-label");

// ==================== UTILS ====================

function render(html) {
  view.innerHTML = html;
}

function showLoading(msg = "Caricamento...") {
  render(`<p class="text-muted">${msg}</p>`);
}

function showError(msg) {
  render(`<p class="text-error">${msg}</p>`);
}

function rarityBadge(rarity) {
  if (!rarity) return "";
  const r = rarity.toUpperCase();
  const map = {
    COMMON: "badge-common",
    UNCOMMON: "badge-uncommon",
    RARE: "badge-rare",
    ULTRA_RARE: "badge-ultra",
  };
  const cls = map[r] || "badge-common";
  return `<span class="badge ${cls}">${r}</span>`;
}

function authHeader() {
  return state.token ? { Authorization: `Bearer ${state.token}` } : {};
}

async function apiFetch(path, options = {}) {
  const headers = {
    ...(options.headers || {}),
    ...authHeader(),
  };
  if (options.body && !headers["Content-Type"]) {
    headers["Content-Type"] = "application/json";
  }

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (res.status === 401) {
    logout(true);
    throw new Error("Token scaduto o non valido");
  }
  if (res.status === 403) {
    throw new Error("Accesso negato - permessi insufficienti");
  }

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || res.statusText);
  }

  if (res.status === 204) return null;
  return res.json();
}

function saveAuthToStorage() {
  if (state.token && state.user) {
    localStorage.setItem("jwtToken", state.token);
    localStorage.setItem("userData", JSON.stringify(state.user));
  } else {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userData");
  }
}

function loadAuthFromStorage() {
  const token = localStorage.getItem("jwtToken");
  const userRaw = localStorage.getItem("userData");
  if (token && userRaw) {
    try {
      state.token = token;
      state.user = JSON.parse(userRaw);
    } catch {
      state.token = null;
      state.user = null;
    }
  }
}

function isAuthenticated() {
  return !!(state.token && state.user);
}

function isAdmin() {
  return isAuthenticated() && state.user.role === "ADMIN";
}

function updateNav() {
  if (isAuthenticated()) {
    navUserLabel.textContent = `${state.user.fName} (${state.user.role})`;
    navLogin.style.display = "none";
    navLogout.style.display = "inline-block";
  } else {
    navUserLabel.textContent = "";
    navLogin.style.display = "inline-block";
    navLogout.style.display = "none";
  }
  navAdmin.style.display = isAdmin() ? "inline-block" : "none";
}

// ==================== AUTH ====================

async function login(email, password) {
  const body = { email, password };
  const res = await apiFetch("/v1/auth/login", {
    method: "POST",
    body: JSON.stringify(body),
    headers: { "Content-Type": "application/json" },
  });

  state.token = res.token || res.jwt || res.accessToken;
  if (!state.token) {
    throw new Error("Token JWT non trovato nella risposta");
  }

  const users = await apiFetch("/users");
  const me = users.find((u) => u.email === email);
  if (!me) {
    throw new Error("Utente non trovato dopo login");
  }
  state.user = me;
  saveAuthToStorage();
  updateNav();
}

function logout(silent = false) {
  state.token = null;
  state.user = null;
  state.openedPack = [];
  state.keptCards = [];
  state.albums = [];
  state.packTemplates = [];
  saveAuthToStorage();
  updateNav();
  if (!silent) {
    renderLoginView();
  }
}

// ==================== LOGIN / REGISTER ====================

function renderLoginView(message) {
  const msg = message
    ? `<p class="text-error" style="margin-bottom:0.5rem;">${message}</p>`
    : "";

  render(`
    <div class="login-container fade-in">
      <div class="card login-card">
        <div class="card-header">
          <div class="card-title">Accedi a PokéCards</div>
          <div class="card-subtitle">Usa il tuo account (ADMIN o USER)</div>
        </div>
        ${msg}
        <div class="form-group">
          <label>Email</label>
          <input id="login-email" type="email" placeholder="es. admin@local.com" />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input id="login-password" type="password" placeholder="Password" />
        </div>
        <button id="btn-login-submit" class="btn" style="width:100%; margin-top:0.5rem;">Login</button>
        <p class="text-muted" style="margin-top:0.5rem;">
          Non hai un account?
          <button id="btn-show-register" class="btn btn-ghost btn-sm">Registrati</button>
        </p>
      </div>
    </div>
  `);

  document.getElementById("btn-login-submit").onclick = async () => {
    const email = document.getElementById("login-email").value.trim();
    const password = document.getElementById("login-password").value.trim();
    if (!email || !password) {
      renderLoginView("Inserisci email e password.");
      return;
    }
    try {
      showLoading("Accesso in corso...");
      await login(email, password);
      renderHomeView();
    } catch (err) {
      renderLoginView(`Errore login: ${err.message}`);
    }
  };

  document.getElementById("btn-show-register").onclick = renderRegisterView;
}

function renderRegisterView() {
  render(`
    <div class="login-container fade-in">
      <div class="card login-card">
        <div class="card-title">Crea un nuovo account</div>
        <div class="form-group"><label>Nome</label><input id="reg-fname" /></div>
        <div class="form-group"><label>Cognome</label><input id="reg-lname" /></div>
        <div class="form-group"><label>Email</label><input id="reg-email" type="email" /></div>
        <div class="form-group"><label>Password</label><input id="reg-password" type="password" /></div>
        <button id="btn-register" class="btn" style="width:100%;">Registrati</button>
        <p class="text-muted" style="margin-top:0.5rem;">
          Hai già un account?
          <button id="btn-back-login" class="btn btn-ghost btn-sm">Accedi</button>
        </p>
      </div>
    </div>
  `);

  document.getElementById("btn-back-login").onclick = renderLoginView;
  document.getElementById("btn-register").onclick = async () => {
    const body = {
      fName: document.getElementById("reg-fname").value.trim(),
      lName: document.getElementById("reg-lname").value.trim(),
      email: document.getElementById("reg-email").value.trim(),
      password: document.getElementById("reg-password").value.trim(),
    };
    try {
      showLoading("Registrazione in corso...");
      await apiFetch("/v1/auth/register", {
        method: "POST",
        body: JSON.stringify(body),
        headers: { "Content-Type": "application/json" },
      });
      renderLoginView("Registrazione completata, ora effettua il login.");
    } catch (err) {
      renderRegisterView();
      showError("Errore registrazione: " + err.message);
    }
  };
}

// ==================== HOME VIEW ====================

async function renderHomeView() {
  if (!isAuthenticated()) return renderLoginView();

  try {
    const albums = await apiFetch(`/users/${state.user.id}/albums`);
    state.albums = albums || [];

    const albumCount = state.albums.length;
    const totalCards = state.albums.reduce((sum, a) => {
      if (!a.items) return sum;
      return sum + a.items.reduce((s, it) => s + (it.quantity || 0), 0);
    }, 0);

    render(`
      <div class="grid grid-2 fade-in">
        <div class="card">
          <div class="card-header">
            <div class="card-title">Ciao, ${state.user.fName}!</div>
            <div class="card-subtitle">Ruolo: ${state.user.role}</div>
          </div>
          <p class="text-muted">Benvenuto nel mondo PokéCards!</p>
          <button id="home-open-pack" class="btn btn-secondary btn-sm">Apri subito un pacchetto 🎴</button>
        </div>
        <div class="card">
          <div class="card-header"><div class="card-title">Statistiche utente</div></div>
          <p class="text-muted">Album: <strong>${albumCount}</strong><br/>Carte totali: <strong>${totalCards}</strong></p>
        </div>
        ${isAdmin()
          ? `<div class="card"><div class="card-header"><div class="card-title">Amministrazione rapida</div></div><p class="text-muted">Gestisci utenti e carte.</p><button id="home-admin-btn" class="btn btn-sm">Vai al pannello Admin</button></div>`
          : ""}
      </div>
    `);

    document.getElementById("home-open-pack").onclick = renderPacksView;
    const btnAdmin = document.getElementById("home-admin-btn");
    if (btnAdmin) btnAdmin.onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore nel caricamento della home: " + err.message);
  }
}

// ==================== PACKS, ALBUMS, ADMIN ====================

// ==================== PACKS VIEW ====================

async function renderPacksView() {
  if (!isAuthenticated()) return renderLoginView();

  try {
    showLoading("Caricamento pacchetti disponibili...");
    const templates = await apiFetch("/pack-templates");
    state.packTemplates = templates;

    if (!templates || templates.length === 0) {
      render(`
        <div class="card fade-in">
          <div class="card-header">
            <div class="card-title">Pacchetti</div>
          </div>
          <p class="text-muted">Nessun pacchetto disponibile al momento.</p>
          <button class="btn" id="back-home">⬅ Torna alla Home</button>
        </div>
      `);
      document.getElementById("back-home").onclick = renderHomeView;
      return;
    }

    render(`
      <div class="grid grid-3 fade-in">
        ${templates
          .map(
            (p) => `
          <div class="card pack-card">
            <div class="card-header">
              <div class="card-title">${p.name}</div>
            </div>
            <p class="text-muted">${p.description || ""}</p>
            <button class="btn btn-sm" data-pack="${p.id}">Apri questo pacchetto</button>
          </div>
        `
          )
          .join("")}
      </div>
      <button id="back-home" class="btn" style="margin-top:1rem;">⬅ Torna alla Home</button>
    `);

    document.getElementById("back-home").onclick = renderHomeView;

    document.querySelectorAll("[data-pack]").forEach((btn) => {
      btn.onclick = async () => {
        const id = btn.getAttribute("data-pack");
        try {
          showLoading("Apertura pacchetto...");
          const cards = await apiFetch(`/packs/open/${id}`, { method: "POST" });
          state.openedPack = cards;
          renderOpenedPackView(cards);
        } catch (err) {
          showError("Errore apertura pacchetto: " + err.message);
        }
      };
    });
  } catch (err) {
    showError("Errore caricamento pacchetti: " + err.message);
  }
}

function renderOpenedPackView(cards) {
  render(`
    <div class="fade-in">
      <div class="card-header">
        <div class="card-title">Pacchetto Aperto!</div>
      </div>
      <div class="grid grid-3">
        ${cards
          .map(
            (c, i) => `
          <div class="card small-card">
            <div class="card-header">
              <div class="card-title">${c.name}</div>
            </div>
            <p>${rarityBadge(c.rarity)}</p>
            <p class="text-muted">${c.type}</p>
            <div class="form-group">
              <input type="checkbox" id="keep-${i}" data-index="${i}" checked> Tieni
            </div>
          </div>
        `
          )
          .join("")}
      </div>
      <button id="btn-save-pack" class="btn">Salva le carte selezionate nell'album</button>
      <button id="back-packs" class="btn btn-secondary">⬅ Torna ai Pacchetti</button>
    </div>
  `);

  document.getElementById("btn-save-pack").onclick = saveOpenedPack;
  document.getElementById("back-packs").onclick = renderPacksView;
}

async function saveOpenedPack() {
  const kept = [];
  document.querySelectorAll("[data-index]").forEach((el) => {
    if (el.checked) kept.push(state.openedPack[parseInt(el.getAttribute("data-index"))]);
  });
  if (kept.length === 0) return showError("Seleziona almeno una carta da salvare!");

  try {
    showLoading("Salvataggio in corso...");
    const res = await apiFetch(`/users/${state.user.id}/albums`, { method: "GET" });
    let album = res && res.length > 0 ? res[0] : null;
    if (!album) {
      album = await apiFetch(`/users/${state.user.id}/albums`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name: `${state.user.fName} Album` }),
      });
    }

    await apiFetch(`/albums/${album.id}/cards`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(kept),
    });

    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Salvataggio completato ✅</div></div>
        <p class="text-muted">Le carte selezionate sono state aggiunte al tuo album.</p>
        <button class="btn" id="go-album">Vai al mio Album</button>
      </div>
    `);
    document.getElementById("go-album").onclick = renderAlbumsView;
  } catch (err) {
    showError("Errore salvataggio carte: " + err.message);
  }
}

// ==================== ALBUMS VIEW ====================

async function renderAlbumsView() {
  if (!isAuthenticated()) return renderLoginView();

  try {
    showLoading("Caricamento album...");
    const albums = await apiFetch(`/users/${state.user.id}/albums`);
    state.albums = albums;

    if (!albums || albums.length === 0) {
      render(`
        <div class="card fade-in">
          <div class="card-header"><div class="card-title">Nessun album</div></div>
          <p class="text-muted">Non hai ancora album. Creane uno per iniziare la tua collezione!</p>
          <button id="btn-create-album" class="btn">Crea album</button>
          <button id="back-home" class="btn btn-secondary">⬅ Torna alla Home</button>
        </div>
      `);
      document.getElementById("back-home").onclick = renderHomeView;
      document.getElementById("btn-create-album").onclick = async () => {
        await apiFetch(`/users/${state.user.id}/albums`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ name: `${state.user.fName} Album` }),
        });
        renderAlbumsView();
      };
      return;
    }

    const album = albums[0];
    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">${album.name}</div></div>
        <div class="grid grid-3">
          ${album.items
            .map(
              (c) => `
            <div class="card small-card">
              <div class="card-header"><div class="card-title">${c.name}</div></div>
              <p>${rarityBadge(c.rarity)}</p>
              <p class="text-muted">${c.type}</p>
            </div>
          `
            )
            .join("")}
        </div>
        <button id="back-home" class="btn">⬅ Torna alla Home</button>
      </div>
    `);
    document.getElementById("back-home").onclick = renderHomeView;
  } catch (err) {
    showError("Errore caricamento album: " + err.message);
  }
}

// ==================== ADMIN DASHBOARD ====================

async function renderAdminDashboardView() {
  if (!isAdmin()) {
    return showError("Accesso negato - solo admin");
  }

  render(`
    <div class="fade-in">
      <div class="card">
        <div class="card-header">
          <div class="card-title">Pannello Amministratore</div>
        </div>
        <p class="text-muted">Gestisci utenti, pacchetti e carte Pokémon.</p>
        <div class="grid grid-3">
          <button id="btn-users" class="btn">👥 Utenti</button>
          <button id="btn-cards" class="btn">🃏 Carte</button>
          <button id="btn-packs" class="btn">🎴 Pacchetti</button>
        </div>
        <button id="back-home" class="btn btn-secondary" style="margin-top:1rem;">⬅ Torna alla Home</button>
      </div>
      <div id="admin-content"></div>
    </div>
  `);

  document.getElementById("back-home").onclick = renderHomeView;
  document.getElementById("btn-users").onclick = renderAdminUsers;
  document.getElementById("btn-cards").onclick = renderAdminCards;
  document.getElementById("btn-packs").onclick = renderAdminPacks;
}

// ---- sottosezioni admin ----

async function renderAdminUsers() {
  try {
    showLoading("Caricamento utenti...");
    const users = await apiFetch("/v1/users");
    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Utenti Registrati</div></div>
        <table class="table">
          <tr><th>ID</th><th>Nome</th><th>Email</th><th>Ruolo</th></tr>
          ${users.map((u) => `<tr><td>${u.id}</td><td>${u.fName}</td><td>${u.email}</td><td>${u.role}</td></tr>`).join("")}
        </table>
        <button id="back-admin" class="btn btn-secondary">⬅ Torna al pannello Admin</button>
      </div>
    `);
    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore caricamento utenti: " + err.message);
  }
}

async function renderAdminCards() {
  try {
    showLoading("Caricamento carte...");
    const cards = await apiFetch("/v1/cards");
    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Tutte le Carte</div></div>
        <div class="grid grid-3">
          ${cards
            .map(
              (c) => `
            <div class="card small-card">
              <div class="card-header"><div class="card-title">${c.name}</div></div>
              <p>${rarityBadge(c.rarity)}</p>
              <p class="text-muted">${c.type}</p>
            </div>
          `
            )
            .join("")}
        </div>
        <button id="back-admin" class="btn btn-secondary">⬅ Torna al pannello Admin</button>
      </div>
    `);
    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore gestione carte: " + err.message);
  }
}

async function renderAdminPacks() {
  try {
    showLoading("Caricamento pacchetti...");
    const packs = await apiFetch("/pack-templates");
    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Pacchetti Disponibili</div></div>
        <table class="table">
          <tr><th>ID</th><th>Nome</th><th>Descrizione</th></tr>
          ${packs.map((p) => `<tr><td>${p.id}</td><td>${p.name}</td><td>${p.description || ""}</td></tr>`).join("")}
        </table>
        <button id="back-admin" class="btn btn-secondary">⬅ Torna al pannello Admin</button>
      </div>
    `);
    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore gestione pack template: " + err.message);
  }
}


// ==================== INIT ====================

function init() {
  loadAuthFromStorage();
  updateNav();

  navHome.onclick = () => (isAuthenticated() ? renderHomeView() : renderLoginView());
  navPacks.onclick = () => (isAuthenticated() ? renderPacksView() : renderLoginView());
  navAlbums.onclick = () => (isAuthenticated() ? renderAlbumsView() : renderLoginView());
  navAdmin.onclick = () => (isAuthenticated() ? renderAdminDashboardView() : renderLoginView());
  navLogin.onclick = () => renderLoginView();
  navLogout.onclick = () => logout(false);

  if (!isAuthenticated()) renderLoginView();
  else renderHomeView();
}

window.addEventListener("DOMContentLoaded", init);
