// ==================== CONFIG & STATO ====================

const API_BASE = "/api";

const state = {
  token: null,
  user: null, // {id, fName, lName, email, role}
  openedPack: [],
  keptCards: [],
  albums: [],
  packTemplates: [],
};

// ==================== ELEMENTI UI DI BASE ====================

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
    navLogin.style.display = "inline-block";
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

  state.token = res.token;
  if (!state.token) {
    throw new Error("Token JWT non trovato nella risposta");
  }

  // recupero utente dal backend usando email
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

// ==================== LOGIN / REGISTER VIEW ====================

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
      return sum + a.items.length;
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
          <p class="text-muted">
            Album: <strong>${albumCount}</strong><br/>
            Carte totali (approx): <strong>${totalCards}</strong>
          </p>
        </div>
        ${
          isAdmin()
            ? `<div class="card">
                 <div class="card-header"><div class="card-title">Amministrazione rapida</div></div>
                 <p class="text-muted">Gestisci utenti, carte e pacchetti.</p>
                 <button id="home-admin-btn" class="btn btn-sm">Vai al pannello Admin</button>
               </div>`
            : ""
        }
      </div>
    `);

    document.getElementById("home-open-pack").onclick = renderPacksView;
    const btnAdmin = document.getElementById("home-admin-btn");
    if (btnAdmin) btnAdmin.onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore nel caricamento della home: " + err.message);
  }
}

// ==================== PACKS VIEW (USER) ====================

async function renderPacksView() {
  if (!isAuthenticated()) return renderLoginView();

  try {
    showLoading("Caricamento pacchetti disponibili...");
    const templates = await apiFetch("/pack-templates");
    state.packTemplates = templates || [];

    if (!templates || templates.length === 0) {
      render(`
        <div class="card fade-in">
          <div class="card-header"><div class="card-title">Pacchetti</div></div>
          <p class="text-muted">Nessun pacchetto disponibile al momento.</p>
          <button id="back-home" class="btn">⬅ Torna alla Home</button>
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
          <div class="card">
            <div class="card-header">
              <div class="card-title">${p.name}</div>
            </div>
            <p class="text-muted">
              ${p.expansion ? p.expansion.name : "Espansione sconosciuta"}
            </p>
            <button class="btn btn-sm btn-secondary" data-pack="${p.id}">
              Apri questo pacchetto 🎴
            </button>
          </div>
        `
          )
          .join("")}
      </div>
      <button id="back-home" class="btn" style="margin-top:0.8rem;">⬅ Torna alla Home</button>
    `);

    document.getElementById("back-home").onclick = renderHomeView;

    document.querySelectorAll("[data-pack]").forEach((btn) => {
      btn.onclick = async () => {
        const id = btn.getAttribute("data-pack");
        await openPackAndShow(id);
      };
    });
  } catch (err) {
    showError("Errore caricamento pacchetti: " + err.message);
  }
}

async function openPackAndShow(templateId) {
  try {
    showLoading("Apertura pacchetto...");
    const cards = await apiFetch(`/pack-openings/${templateId}/open?userId=${state.user.id}`, {
      method: "POST",
    });
    state.openedPack = cards || [];
    state.keptCards = [...state.openedPack];
    renderOpenedPack();
  } catch (err) {
    showError("Errore apertura pacchetto: " + err.message);
  }
}

function renderOpenedPack() {
  if (!state.openedPack.length) return;

  const cardsHtml = state.openedPack
    .map((card) => {
      const kept = !!state.keptCards.find((c) => c.id === card.id);
      return `
      <div class="card">
        <div class="card-header">
          <div class="card-title">${card.name}</div>
          <div>${rarityBadge(card.rarity)}</div>
        </div>
        <p class="text-muted">${card.type || ""}</p>
        <button class="btn btn-sm btn-ghost toggle-keep" data-id="${card.id}">
          ${kept ? "Scarta" : "Tienila"}
        </button>
      </div>
    `;
    })
    .join("");

  render(`
    <div class="card fade-in">
      <div class="card-header">
        <div class="card-title">Pacchetto aperto!</div>
        <div class="card-subtitle">Seleziona quali carte tenere</div>
      </div>
      <div class="grid grid-3">
        ${cardsHtml}
      </div>
      <button id="btn-save-cards" class="btn" style="margin-top:0.6rem;">
        Salva carte tenute in un album 💾
      </button>
      <button id="back-packs" class="btn btn-secondary" style="margin-top:0.4rem;">
        ⬅ Torna ai pacchetti
      </button>
    </div>
  `);

  document.querySelectorAll(".toggle-keep").forEach((btn) => {
    btn.onclick = () => {
      const id = Number(btn.getAttribute("data-id"));
      const idx = state.keptCards.findIndex((c) => c.id === id);
      if (idx >= 0) {
        state.keptCards.splice(idx, 1);
        btn.textContent = "Tienila";
      } else {
        const card = state.openedPack.find((c) => c.id === id);
        if (card) state.keptCards.push(card);
        btn.textContent = "Scarta";
      }
    };
  });

  document.getElementById("btn-save-cards").onclick = renderSaveCardsToAlbumDialog;
  document.getElementById("back-packs").onclick = renderPacksView;
}

async function renderSaveCardsToAlbumDialog() {
  if (!state.keptCards.length) {
    alert("Non hai selezionato nessuna carta da tenere.");
    return;
  }

  try {
    const albums = await apiFetch(`/users/${state.user.id}/albums`);
    state.albums = albums || [];

    const options = state.albums
      .map((a) => `<option value="${a.id}">${a.name}</option>`)
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header">
          <div class="card-title">Salva carte in un album</div>
          <div class="card-subtitle">Carte selezionate: ${state.keptCards.length}</div>
        </div>
        ${
          state.albums.length
            ? `<div class="form-group">
                 <label>Seleziona album esistente</label>
                 <select id="album-select">${options}</select>
               </div>`
            : `<p class="text-muted">Non hai ancora album. Creane uno nuovo.</p>`
        }
        <div class="form-group">
          <label>Oppure crea nuovo album</label>
          <input id="new-album-name" placeholder="Nome nuovo album" />
        </div>
        <button id="btn-confirm-save" class="btn btn-sm">Conferma salvataggio</button>
      </div>
    `);

    document.getElementById("btn-confirm-save").onclick = async () => {
      let albumId = null;
      const newName = document.getElementById("new-album-name").value.trim();

      if (newName) {
        const created = await apiFetch(`/users/${state.user.id}/albums`, {
          method: "POST",
          body: JSON.stringify({ name: newName }),
        });
        albumId = created.id;
      } else if (state.albums.length) {
        albumId = Number(document.getElementById("album-select").value);
      }

      if (!albumId) {
        alert("Seleziona un album o inserisci il nome di un nuovo album.");
        return;
      }

      const cardIds = state.keptCards.map((c) => c.id);

      await apiFetch(`/users/${state.user.id}/albums/${albumId}/add-cards`, {
        method: "POST",
        body: JSON.stringify({ cardIds }),
      });

      alert("Carte salvate correttamente nell'album!");
      renderAlbumsView();
    };
  } catch (err) {
    showError("Errore salvataggio carte: " + err.message);
  }
}

// ==================== ALBUMS VIEW ====================

async function renderAlbumsView() {
  if (!isAuthenticated()) return renderLoginView();
  showLoading("Caricamento album...");

  try {
    const albums = await apiFetch(`/users/${state.user.id}/albums`);
    state.albums = albums || [];

    if (!albums.length) {
      render(`
        <div class="card fade-in">
          <div class="card-header"><div class="card-title">Nessun album</div></div>
          <p class="text-muted">Apri qualche pacchetto e salva le carte per creare un album!</p>
          <button id="back-home" class="btn">⬅ Torna alla Home</button>
        </div>
      `);
      document.getElementById("back-home").onclick = renderHomeView;
      return;
    }

    const rows = albums
      .map((a) => {
        const count = a.items ? a.items.length : 0;
        return `
          <tr class="album-row" data-id="${a.id}">
            <td>${a.id}</td>
            <td>${a.name}</td>
            <td>${count}</td>
          </tr>`;
      })
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">I tuoi album</div></div>
        <table class="table">
          <thead>
            <tr><th>ID</th><th>Nome</th><th>Carte</th></tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>
        <p class="text-muted" style="margin-top:0.4rem;">Clicca su una riga per vedere il dettaglio.</p>
        <button id="back-home" class="btn" style="margin-top:0.6rem;">⬅ Torna alla Home</button>
      </div>
    `);

    document.getElementById("back-home").onclick = renderHomeView;
    document.querySelectorAll(".album-row").forEach((tr) => {
      tr.onclick = () => {
        const id = tr.getAttribute("data-id");
        renderAlbumDetailView(id);
      };
    });
  } catch (err) {
    showError("Errore caricamento album: " + err.message);
  }
}

async function renderAlbumDetailView(albumId) {
  showLoading("Caricamento dettagli album...");
  try {
    const album = await apiFetch(`/users/${state.user.id}/albums/${albumId}`);
    const items = album.items || [];

    const cardsHtml = items
      .map((it) => {
        const c = it.card || it; // in caso ritorni direttamente la card
        return `
        <div class="card">
          <div class="card-header">
            <div class="card-title">${c.name}</div>
            <div>${rarityBadge(c.rarity)}</div>
          </div>
          <p class="text-muted">${c.type || ""}</p>
        </div>
      `;
      })
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header">
          <div class="card-title">Album: ${album.name}</div>
          <div class="card-subtitle">ID: ${album.id}</div>
        </div>
        <div class="grid grid-3">
          ${cardsHtml || "<p class='text-muted'>Nessuna carta in questo album.</p>"}
        </div>
        <button id="back-albums" class="btn" style="margin-top:0.8rem;">⬅ Torna agli album</button>
      </div>
    `);

    document.getElementById("back-albums").onclick = renderAlbumsView;
  } catch (err) {
    showError("Errore dettagli album: " + err.message);
  }
}

// ==================== ADMIN VIEW ====================

async function renderAdminDashboardView() {
  if (!isAdmin()) {
    return showError("Accesso negato - solo admin");
  }

  showLoading("Caricamento pannello admin...");

  try {
    const stats = await apiFetch("/admin/statistics");

    render(`
      <div class="grid grid-3 fade-in">
        <div class="card">
          <div class="card-header"><div class="card-title">Statistiche</div></div>
          <p class="text-muted">
            ${Object.entries(stats)
              .map(([k, v]) => `${k}: <strong>${v}</strong>`)
              .join("<br/>")}
          </p>
        </div>
        <div class="card">
          <div class="card-header"><div class="card-title">Gestione carte</div></div>
          <button id="admin-cards" class="btn btn-sm">🃏 Gestisci carte</button>
        </div>
        <div class="card">
          <div class="card-header"><div class="card-title">Gestione pacchetti / utenti</div></div>
          <button id="admin-packs" class="btn btn-sm" style="margin-right:0.4rem;">🎴 Pack template</button>
          <button id="admin-users" class="btn btn-sm">👥 Utenti</button>
        </div>
      </div>
    `);

    document.getElementById("admin-cards").onclick = renderAdminCardsView;
    document.getElementById("admin-packs").onclick = renderAdminPacksView;
    document.getElementById("admin-users").onclick = renderAdminUsersView;
  } catch (err) {
    showError("Errore dashboard admin: " + err.message);
  }
}

// --- Admin: carte ---

async function renderAdminCardsView() {
  if (!isAdmin()) return showError("Accesso negato");
  showLoading("Caricamento carte...");

  try {
    const cards = await apiFetch("/cards");
    const expansions = await apiFetch("/expansions");

    const rows = cards
      .map(
        (c) => `
      <tr>
        <td>${c.id}</td>
        <td>${c.name}</td>
        <td>${c.rarity}</td>
        <td>${c.expansion ? c.expansion.name : ""}</td>
        <td><button class="btn btn-sm btn-danger btn-del-card" data-id="${c.id}">Elimina</button></td>
      </tr>`
      )
      .join("");

    const expOptions = expansions
      .map((e) => `<option value="${e.id}">${e.name}</option>`)
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Gestione carte</div></div>
        <table class="table">
          <thead><tr><th>ID</th><th>Nome</th><th>Rarità</th><th>Espansione</th><th>Azioni</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>

        <hr style="margin:0.8rem 0; border-color:#4b5563;"/>

        <div class="card-title" style="margin-bottom:0.4rem;">Aggiungi nuova carta</div>
        <div class="grid grid-2">
          <div>
            <div class="form-group"><label>Nome</label><input id="card-name"/></div>
            <div class="form-group"><label>Descrizione</label><input id="card-desc"/></div>
            <div class="form-group"><label>Tipo</label><input id="card-type" placeholder="Es. ELECTRIC"/></div>
          </div>
          <div>
            <div class="form-group">
              <label>Rarità</label>
              <select id="card-rarity">
                <option value="COMMON">COMMON</option>
                <option value="UNCOMMON">UNCOMMON</option>
                <option value="RARE">RARE</option>
                <option value="ULTRA_RARE">ULTRA_RARE</option>
              </select>
            </div>
            <div class="form-group">
              <label>Espansione</label>
              <select id="card-expansion">${expOptions}</select>
            </div>
            <div class="form-group">
              <label>URL immagine (opzionale)</label>
              <input id="card-image"/>
            </div>
          </div>
        </div>
        <button id="btn-add-card" class="btn btn-sm" style="margin-top:0.4rem;">Aggiungi carta</button>
        <button id="back-admin" class="btn btn-secondary btn-sm" style="margin-top:0.4rem;">⬅ Torna al pannello admin</button>
      </div>
    `);

    document.querySelectorAll(".btn-del-card").forEach((btn) => {
      btn.onclick = async () => {
        const id = btn.getAttribute("data-id");
        if (!confirm("Eliminare la carta " + id + "?")) return;
        await apiFetch(`/admin/cards/${id}`, { method: "DELETE" });
        renderAdminCardsView();
      };
    });

    document.getElementById("btn-add-card").onclick = async () => {
      const name = document.getElementById("card-name").value.trim();
      const description = document.getElementById("card-desc").value.trim();
      const type = document.getElementById("card-type").value.trim();
      const rarity = document.getElementById("card-rarity").value;
      const expId = Number(document.getElementById("card-expansion").value);
      const imageUrl = document.getElementById("card-image").value.trim();

      if (!name || !rarity || !expId) {
        alert("Nome, rarità ed espansione sono obbligatori.");
        return;
      }

      await apiFetch("/admin/cards", {
        method: "POST",
        body: JSON.stringify({
          name,
          description,
          type,
          rarity,
          expansion: { id: expId },
          imageUrl: imageUrl || null,
        }),
      });

      renderAdminCardsView();
    };

    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore gestione carte: " + err.message);
  }
}

// --- Admin: pack template ---

async function renderAdminPacksView() {
  if (!isAdmin()) return showError("Accesso negato");
  showLoading("Caricamento pack template...");

  try {
    const packs = await apiFetch("/pack-templates");
    const expansions = await apiFetch("/expansions");

    const rows = packs
      .map(
        (p) => `
      <tr>
        <td>${p.id}</td>
        <td>${p.name}</td>
        <td>${p.expansion ? p.expansion.name : ""}</td>
        <td><button class="btn btn-sm btn-danger btn-del-pack" data-id="${p.id}">Elimina</button></td>
      </tr>`
      )
      .join("");

    const expOptions = expansions
      .map((e) => `<option value="${e.id}">${e.name}</option>`)
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Gestione pack template</div></div>
        <table class="table">
          <thead><tr><th>ID</th><th>Nome</th><th>Espansione</th><th>Azioni</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>

        <hr style="margin:0.8rem 0; border-color:#4b5563;"/>

        <div class="card-title" style="margin-bottom:0.4rem;">Crea nuovo pack template</div>
        <div class="grid grid-2">
          <div class="form-group">
            <label>Nome pack</label>
            <input id="pack-name"/>
          </div>
          <div class="form-group">
            <label>Espansione</label>
            <select id="pack-expansion">${expOptions}</select>
          </div>
        </div>
        <p class="text-muted">Gli slot del pack sono definiti lato backend.</p>
        <button id="btn-add-pack" class="btn btn-sm" style="margin-top:0.4rem;">Crea pack</button>
        <button id="back-admin" class="btn btn-secondary btn-sm" style="margin-top:0.4rem;">⬅ Torna al pannello admin</button>
      </div>
    `);

    document.querySelectorAll(".btn-del-pack").forEach((btn) => {
      btn.onclick = async () => {
        const id = btn.getAttribute("data-id");
        if (!confirm("Eliminare il pack template " + id + "?")) return;
        await apiFetch(`/admin/pack-templates/${id}`, { method: "DELETE" });
        renderAdminPacksView();
      };
    });

    document.getElementById("btn-add-pack").onclick = async () => {
      const name = document.getElementById("pack-name").value.trim();
      const expId = Number(document.getElementById("pack-expansion").value);
      if (!name || !expId) {
        alert("Compila nome pack ed espansione.");
        return;
      }

      await apiFetch("/admin/pack-templates", {
        method: "POST",
        body: JSON.stringify({
          name,
          expansion: { id: expId },
        }),
      });

      renderAdminPacksView();
    };

    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore gestione pack template: " + err.message);
  }
}

// --- Admin: utenti ---

async function renderAdminUsersView() {
  if (!isAdmin()) return showError("Accesso negato");
  showLoading("Caricamento utenti...");

  try {
    const users = await apiFetch("/users");
    const rows = users
      .map(
        (u) => `
      <tr>
        <td>${u.id}</td>
        <td>${u.fName} ${u.lName}</td>
        <td>${u.email}</td>
        <td>${u.role}</td>
        <td><button class="btn btn-sm btn-danger btn-del-user" data-id="${u.id}">Elimina</button></td>
      </tr>`
      )
      .join("");

    render(`
      <div class="card fade-in">
        <div class="card-header"><div class="card-title">Utenti registrati</div></div>
        <table class="table">
          <thead><tr><th>ID</th><th>Nome</th><th>Email</th><th>Ruolo</th><th>Azioni</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
        <button id="back-admin" class="btn btn-secondary btn-sm" style="margin-top:0.4rem;">⬅ Torna al pannello admin</button>
      </div>
    `);

    document.querySelectorAll(".btn-del-user").forEach((btn) => {
      btn.onclick = async () => {
        const id = btn.getAttribute("data-id");
        if (!confirm("Eliminare l'utente " + id + "?")) return;
        await apiFetch(`/users/${id}`, { method: "DELETE" });
        renderAdminUsersView();
      };
    });

    document.getElementById("back-admin").onclick = renderAdminDashboardView;
  } catch (err) {
    showError("Errore caricamento utenti: " + err.message);
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
