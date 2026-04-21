// Smooth scroll
document.querySelectorAll('a[href^="#"], a[href^="index.html#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        const targetId = this.getAttribute('href').split('#')[1];
        const target = document.getElementById(targetId);
        if (target) {
            e.preventDefault();
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});

console.log('Website TUB Projeto carregado com sucesso!');

// Botão "Começar"
const btnComecar = document.querySelector('.btn3');
if (btnComecar) {
    btnComecar.addEventListener('click', function() {
        alert('Conseguiste');
    });
}

///////////////////////////////MAPA DE BRAGA///////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    verificarAcessos();

    const mapContainer = document.getElementById("map");
    if (mapContainer) {
        const map = L.map("map").setView([41.5454, -8.4265], 13);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors"
        }).addTo(map);

        let marcadores = [];
        let stopsGuardados = [];

        function limparMarcadores() {
            marcadores.forEach(marcador => map.removeLayer(marcador));
            marcadores = [];
        }

        async function carregarStops() {
            try {
                const resposta = await fetch("/api/stops");
                const stops = await resposta.json();
                stopsGuardados = stops;
                limparMarcadores();

                stops.forEach(stop => {
                    if (stop.latitude != null && stop.longitude != null) {
                        const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
                        let popupContent = `<strong>${stop.name}</strong>`;

                        if (utilizadorGuardado) {
                            const utilizador = JSON.parse(utilizadorGuardado);
                            if (utilizador.role === 'ADMIN') {
                                popupContent += `
                                    <br>Latitude: ${stop.latitude}
                                    <br>Longitude: ${stop.longitude}
                                    <br><br>
                                    <button onclick="preencherFormularioParagem(${stop.id}, '${stop.name.replace(/'/g, "\\'")}', ${stop.latitude}, ${stop.longitude})">Editar</button>
                                    <button onclick="apagarParagem(${stop.id})">Apagar</button>
                                `;
                            }
                        }

                        const marcador = L.marker([stop.latitude, stop.longitude])
                            .addTo(map)
                            .bindPopup(popupContent);
                        marcadores.push(marcador);
                    }
                });
            } catch (erro) {
                console.error("Erro ao carregar stops do mapa:", erro);
            }
        }

        function pesquisarParagem() {
            const input = document.getElementById("searchStop");
            if (!input) return;
            const termo = input.value.trim().toLowerCase();
            if (!termo) { alert("Escreve o nome de uma paragem."); return; }

            const stopEncontrado = stopsGuardados.find(stop =>
                stop.name &&
                stop.latitude != null &&
                stop.longitude != null &&
                stop.name.toLowerCase().includes(termo)
            );

            if (stopEncontrado) {
                map.setView([stopEncontrado.latitude, stopEncontrado.longitude], 16);
                L.popup()
                    .setLatLng([stopEncontrado.latitude, stopEncontrado.longitude])
                    .setContent(`<strong>${stopEncontrado.name}</strong>`)
                    .openOn(map);
            } else {
                alert("Paragem não encontrada.");
            }
        }

        carregarStops();
        setInterval(carregarStops, 5000);

        const botaoPesquisa = document.getElementById("searchBtn");
        const inputPesquisa = document.getElementById("searchStop");
        if (botaoPesquisa) botaoPesquisa.addEventListener("click", pesquisarParagem);
        if (inputPesquisa) inputPesquisa.addEventListener("keydown", function (e) {
            if (e.key === "Enter") pesquisarParagem();
        });
    }
});
/////////////////////////////////////////////FIM MAPA DE BRAGA//////////////////////////////////////////

///////////////////////// GESTÃO DE ACESSOS E LOGIN ///////////////////////////

async function validarLogin() {
    const emailInput = document.getElementById('emailInput');
    if (!emailInput) return;

    const email = emailInput.value;
    let ficheiroParaLer = "";

    if (email === 'admin@tub.pt') {
        ficheiroParaLer = 'teste_admin.json';
    } else if (email === 'supervisor@tub.pt') {
        ficheiroParaLer = 'teste_supervisor.json';
    } else if (email === 'user@tub.pt') {
        ficheiroParaLer = 'teste_utilizador.json';
    } else {
        document.getElementById('erro-msg').style.display = 'block';
        return;
    }

    try {
        const resposta = await fetch(ficheiroParaLer);
        const dadosUtilizador = await resposta.json();
        localStorage.setItem('utilizadorAtivo', JSON.stringify(dadosUtilizador));
        document.getElementById('erro-msg').style.display = 'none';
        window.location.href = 'mapa.html';
    } catch (erro) {
        console.error("Erro ao ler o ficheiro JSON:", erro);
        alert("Erro no login! Confirma se os ficheiros de teste estão na pasta.");
    }
}

function fazerLogout() {
    localStorage.removeItem('utilizadorAtivo');
    window.location.href = 'index.html';
}

function verificarAcessos() {
    const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
    const navLogin         = document.getElementById('nav-login');
    const navLogout        = document.getElementById('nav-logout');
    const navAlarmes       = document.getElementById('nav-alarmes');
    const navMonitorizacao = document.getElementById('nav-monitorizacao');
    const navVeiculos      = document.getElementById('nav-veiculos');
    const navRotas         = document.getElementById('nav-rotas');

    if (utilizadorGuardado) {
        if (navLogin)  navLogin.style.display  = 'none';
        if (navLogout) navLogout.style.display = 'inline-block';

        const utilizador = JSON.parse(utilizadorGuardado);

        if (utilizador.role === 'ADMIN' || utilizador.role === 'SUPERVISOR') {
            if (navAlarmes) navAlarmes.style.display = 'inline-block';
        }
        if (utilizador.role === 'ADMIN') {
            if (navMonitorizacao) navMonitorizacao.style.display = 'inline-block';
            if (navVeiculos) navVeiculos.style.display = 'inline-block';
            if (navRotas) navRotas.style.display = 'inline-block';
        }
    } else {
        if (navLogin)         navLogin.style.display         = 'inline-block';
        if (navLogout)        navLogout.style.display        = 'none';
        if (navAlarmes)       navAlarmes.style.display       = 'none';
        if (navMonitorizacao) navMonitorizacao.style.display = 'none';
        if (navVeiculos)      navVeiculos.style.display      = 'none';
        if (navRotas)         navRotas.style.display         = 'none';
    }
}

///////////////////// Funções admin — paragens /////////////////////
function preencherFormularioParagem(id, name, latitude, longitude) {
    document.getElementById("stopId").value        = id;
    document.getElementById("stopName").value      = name;
    document.getElementById("stopLatitude").value  = latitude;
    document.getElementById("stopLongitude").value = longitude;
}

function limparFormularioParagem() {
    document.getElementById("stopId").value        = "";
    document.getElementById("stopName").value      = "";
    document.getElementById("stopLatitude").value  = "";
    document.getElementById("stopLongitude").value = "";
}

async function adicionarOuEditarParagem() {
    const id        = document.getElementById("stopId").value;
    const name      = document.getElementById("stopName").value.trim();
    const latitude  = parseFloat(document.getElementById("stopLatitude").value);
    const longitude = parseFloat(document.getElementById("stopLongitude").value);

    if (!name || isNaN(latitude) || isNaN(longitude)) {
        alert("Preenche nome, latitude e longitude.");
        return;
    }

    const payload = { name, latitude, longitude };
    const url    = id ? `/api/stops/${id}` : "/api/stops";
    const method = id ? "PUT" : "POST";

    try {
        const resposta = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (resposta.ok) {
            alert(id ? "Paragem atualizada com sucesso." : "Paragem adicionada com sucesso.");
            limparFormularioParagem();
            location.reload();
        } else {
            alert("Erro ao guardar a paragem.");
        }
    } catch (erro) {
        console.error("Erro ao guardar paragem:", erro);
        alert("Erro de ligação ao servidor.");
    }
}

async function apagarParagem(id) {
    if (!confirm("Tens a certeza que queres apagar esta paragem?")) return;

    try {
        const resposta = await fetch(`/api/stops/${id}`, { method: "DELETE" });
        if (resposta.ok) {
            alert("Paragem apagada com sucesso.");
            location.reload();
        } else {
            alert("Erro ao apagar paragem.");
        }
    } catch (erro) {
        console.error("Erro ao apagar paragem:", erro);
        alert("Erro de ligação ao servidor.");
    }
}

///////////////////// Navegação por secções (index.html) /////////////////////

const SECCOES_INDEX = ['home', 'about', 'contact', 'alarmes'];

function mostrarSecao(idAlvo) {
    SECCOES_INDEX.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });

    const seccaoAtiva = document.getElementById(idAlvo);
    if (seccaoAtiva) {
        seccaoAtiva.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (idAlvo === 'alarmes') carregarAlarmes();
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const eIndexPage = !!document.getElementById('home');
    if (!eIndexPage) return;
    ['home', 'about', 'contact'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'block';
    });
});

///////////////////// ALARMES /////////////////////

function getRoleAtivo() {
    const u = localStorage.getItem('utilizadorAtivo');
    if (!u) return null;
    return JSON.parse(u).role;
}

function getUsernameAtivo() {
    const u = localStorage.getItem('utilizadorAtivo');
    if (!u) return null;
    return JSON.parse(u).nome;
}

async function carregarAlarmes() {
    const role = getRoleAtivo();
    if (role === 'SUPERVISOR') {
        await carregarAlarmesParaSupervisor();
    } else if (role === 'ADMIN') {
        await carregarAlarmesParaAdmin();
    }
}

// ── SUPERVISOR ──────────────────────────────────────

async function carregarAlarmesParaSupervisor() {
    try {
        const [resActive, resPending] = await Promise.all([
            fetch('/api/alerts/active'),
            fetch('/api/alerts/pending')
        ]);
        const active  = await resActive.json();
        const pending = await resPending.json();

        const todos = [...active, ...pending];
        document.getElementById('count-high').innerText   = todos.filter(a => a.severity === 'HIGH').length;
        document.getElementById('count-medium').innerText = todos.filter(a => a.severity === 'MEDIUM').length;
        document.getElementById('count-low').innerText    = todos.filter(a => a.severity === 'LOW').length;

        const body = document.getElementById('alerts-body');
        body.innerHTML = '';

        if (todos.length === 0) {
            body.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--muted); padding:2rem;">Sem alertas pendentes.</td></tr>`;
            return;
        }

        [...pending, ...active].forEach(alert => {
            const isPending = alert.status === 'PENDING';
            const motivoHtml = isPending && alert.inconsistentReason
                ? `<div style="margin-top:0.4rem; padding:0.5rem 0.75rem; background:rgba(231,76,60,0.1); border-left:3px solid var(--danger); border-radius:4px; font-size:0.82rem; color:#ff8f85;">
                       <strong>Motivo devolvido:</strong> ${alert.inconsistentReason}
                   </div>`
                : '';

            const badgeStatus = isPending
                ? `<span style="background:rgba(231,76,60,0.15);color:var(--danger);padding:0.2rem 0.6rem;border-radius:4px;font-size:0.78rem;font-weight:700;">DEVOLVIDO</span>`
                : `<span style="background:rgba(52,152,219,0.15);color:var(--info);padding:0.2rem 0.6rem;border-radius:4px;font-size:0.78rem;font-weight:700;">NOVO</span>`;

            body.innerHTML += `
                <tr>
                    <td>${alert.id}</td>
                    <td>${alert.type ?? '—'}</td>
                    <td class="severity-${(alert.severity ?? '').toLowerCase()}">${alert.severity ?? '—'}</td>
                    <td>${badgeStatus}${motivoHtml}</td>
                    <td>
                        <button onclick="supervisorAceitarAlerta(${alert.id})" class="btn-acao">✅ Aceitar</button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        console.error("Erro ao carregar alarmes (supervisor):", err);
    }
}

async function supervisorAceitarAlerta(id) {
    const username = getUsernameAtivo();
    try {
        await fetch(`/api/alerts/${id}/accept?username=${encodeURIComponent(username)}`, { method: 'POST' });
        await carregarAlarmes();
    } catch (err) {
        console.error("Erro ao aceitar alerta:", err);
    }
}

// ── ADMIN ────────────────────────────────────────────

async function carregarAlarmesParaAdmin() {
    try {
        const resAccepted = await fetch('/api/alerts/accepted');
        const alerts = await resAccepted.json();

        // CORRIGIDO: contadores calculados a partir dos alertas ACCEPTED, não do /stats global
        document.getElementById('count-high').innerText   = alerts.filter(a => a.severity === 'HIGH').length;
        document.getElementById('count-medium').innerText = alerts.filter(a => a.severity === 'MEDIUM').length;
        document.getElementById('count-low').innerText    = alerts.filter(a => a.severity === 'LOW').length;

        const body = document.getElementById('alerts-body');
        body.innerHTML = '';

        if (alerts.length === 0) {
            body.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--muted); padding:2rem;">Sem alertas aceites pelo supervisor.</td></tr>`;
            return;
        }

        alerts.forEach(alert => {
            body.innerHTML += `
                <tr>
                    <td>${alert.id}</td>
                    <td>${alert.type ?? '—'}</td>
                    <td class="severity-${(alert.severity ?? '').toLowerCase()}">${alert.severity ?? '—'}</td>
                    <td>${alert.status}</td>
                    <td>
                        <button onclick="adminResolverAlerta(${alert.id})" class="btn-acao" title="Concluído">✅</button>
                        <button onclick="abrirModalInconsistente(${alert.id})" class="btn-acao" title="Inconsistente">⚠️</button>
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        console.error("Erro ao carregar alarmes (admin):", err);
    }
}

async function adminResolverAlerta(id) {
    const username = getUsernameAtivo();
    try {
        await fetch(`/api/alerts/${id}/resolve?username=${encodeURIComponent(username)}`, { method: 'POST' });
        await carregarAlarmes();
    } catch (err) {
        console.error("Erro ao resolver alerta:", err);
    }
}

// ── MODAL INCONSISTENTE ──────────────────────────────

function abrirModalInconsistente(alertId) {
    const anterior = document.getElementById('modal-inconsistente');
    if (anterior) anterior.remove();

    const modal = document.createElement('div');
    modal.id = 'modal-inconsistente';
    modal.style.cssText = `
        position: fixed; inset: 0; z-index: 9999;
        background: rgba(7,14,22,0.85);
        display: flex; align-items: center; justify-content: center;
    `;

    modal.innerHTML = `
        <div style="
            background: var(--navy-light);
            border: 1px solid rgba(245,166,35,0.2);
            border-radius: 12px;
            padding: 2rem 2.5rem;
            width: 100%;
            max-width: 460px;
            box-shadow: 0 8px 40px rgba(0,0,0,0.5);
            animation: fadeUp 0.2s ease both;
        ">
            <h3 style="font-family:var(--font-display); color:var(--amber); font-size:1.2rem; margin-bottom:0.5rem;">
                Marcar como Inconsistente
            </h3>
            <p style="color:var(--muted); font-size:0.88rem; margin-bottom:1.2rem;">
                Escreve o motivo. O supervisor irá receber este alerta de volta com a tua justificação.
            </p>
            <textarea
                id="motivo-inconsistente"
                placeholder="Descreve o motivo de inconsistência..."
                style="
                    width:100%; min-height:110px; resize:vertical;
                    background:var(--navy); border:1px solid rgba(255,255,255,0.1);
                    border-radius:8px; color:var(--white);
                    font-family:var(--font-body); font-size:0.93rem;
                    padding:0.75rem 1rem; outline:none;
                    transition: border-color 0.2s;
                "
                onfocus="this.style.borderColor='var(--amber)'"
                onblur="this.style.borderColor='rgba(255,255,255,0.1)'"
            ></textarea>
            <div style="display:flex; gap:0.75rem; margin-top:1.2rem; justify-content:flex-end;">
                <button
                    onclick="fecharModalInconsistente()"
                    style="
                        background:transparent; border:1px solid rgba(255,255,255,0.15);
                        color:var(--muted); border-radius:7px;
                        font-family:var(--font-display); font-size:0.82rem;
                        font-weight:600; letter-spacing:0.05em; text-transform:uppercase;
                        padding:0.6rem 1.2rem; cursor:pointer;
                    "
                >Cancelar</button>
                <button
                    onclick="adminMarcarInconsistente(${alertId})"
                    style="
                        background:var(--danger); border:none; color:white;
                        border-radius:7px; font-family:var(--font-display);
                        font-size:0.82rem; font-weight:700; letter-spacing:0.05em;
                        text-transform:uppercase; padding:0.6rem 1.4rem; cursor:pointer;
                    "
                >Enviar</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    document.getElementById('motivo-inconsistente').focus();
}

function fecharModalInconsistente() {
    const modal = document.getElementById('modal-inconsistente');
    if (modal) modal.remove();
}

async function adminMarcarInconsistente(id) {
    const motivo = document.getElementById('motivo-inconsistente').value.trim();
    if (!motivo) {
        document.getElementById('motivo-inconsistente').style.borderColor = 'var(--danger)';
        return;
    }

    const username = getUsernameAtivo();
    try {
        await fetch(
            `/api/alerts/${id}/inconsistent?username=${encodeURIComponent(username)}&reason=${encodeURIComponent(motivo)}`,
            { method: 'POST' }
        );
        fecharModalInconsistente();
        await carregarAlarmes();
    } catch (err) {
        console.error("Erro ao marcar inconsistente:", err);
    }
}