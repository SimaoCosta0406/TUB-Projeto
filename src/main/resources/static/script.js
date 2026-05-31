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

        const painelIcon = L.icon({
            iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
            shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
            iconSize: [35, 55],
            iconAnchor: [17, 55],
            popupAnchor: [1, -45],
            shadowSize: [55, 55]
        });
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
                        popupContent += `<br><br><button onclick="mostrarTabelaVeiculosPorParagem(${stop.id}, '${stop.name.replace(/'/g, "\\'")}')">Ver veículos</button>`;

                        const marcador = L.marker([stop.latitude, stop.longitude], { icon: painelIcon })
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
                    .setContent(`<strong>${stopEncontrado.name}</strong><br><br><button onclick="mostrarTabelaVeiculosPorParagem(${stopEncontrado.id}, '${stopEncontrado.name.replace(/'/g, "\\'")}')">Ver veículos</button>`)
                    .openOn(map);
                mostrarTabelaVeiculosPorParagem(stopEncontrado.id, stopEncontrado.name);
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
    } else if (email === 'worker@tub.pt') {
        ficheiroParaLer = 'teste_worker.json';
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

        // Redirecionar conforme o role
        if (dadosUtilizador.role === 'WORKER') {
            window.location.href = 'worker.html';
        } else if (dadosUtilizador.role === 'SUPERVISOR') {
            window.location.href = 'supervisor.html';
        } else {
            window.location.href = 'mapa.html';
        }
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
    const navAdminUsers = document.getElementById('nav-admin-users');
    garantirItemNavbar('nav-paineis', 'Painéis', 'paineis-login.html', 'nav-login');
    garantirItemNavbar('nav-alarmes', 'Alarmes', 'javascript:void(0)', 'nav-login', "window.location.href='index.html#alarmes'");

    const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
    const navLogin         = document.getElementById('nav-login');
    const navLogout        = document.getElementById('nav-logout');
    const navAlarmes       = document.getElementById('nav-alarmes');
    const navPaineis       = document.getElementById('nav-paineis');
    const navMonitorizacao = document.getElementById('nav-monitorizacao');
    const navVeiculos      = document.getElementById('nav-veiculos');
    const navRotas         = document.getElementById('nav-rotas');
    const navEnviarAlarme  = document.getElementById('nav-enviar-alarme');
    const navHome          = document.querySelector('nav .nav-links a[href="index.html"]')?.closest('li');
    const navMapa          = document.querySelector('nav .nav-links a[href="mapa.html"]')?.closest('li');

    if (utilizadorGuardado) {
        if (navLogin)  navLogin.style.display  = 'none';
        if (navLogout) navLogout.style.display = 'inline-block';

        const utilizador = JSON.parse(utilizadorGuardado);

        if (utilizador.role === 'SUPERVISOR') {
            if (!window.location.pathname.endsWith('supervisor.html')) {
                window.location.href = 'supervisor.html';
                return;
            }
        }

        if (utilizador.role === 'ADMIN') {
            if (navAlarmes)       navAlarmes.style.display       = 'inline-block';
            if (navPaineis)       navPaineis.style.display       = 'inline-block';
            if (navMonitorizacao) navMonitorizacao.style.display = 'inline-block';
            if (navVeiculos)      navVeiculos.style.display      = 'inline-block';
            if (navRotas)         navRotas.style.display         = 'inline-block';
        }
        if (utilizador.role === 'ADMIN') {
            if (navAdminUsers) navAdminUsers.style.display = 'inline-block';
        } else {
            if (navAdminUsers) navAdminUsers.style.display = 'none';
        }

        if (utilizador.role === 'WORKER') {
            if (navEnviarAlarme) navEnviarAlarme.style.display = 'inline-block';
        }

        if (utilizador.role === 'SUPERVISOR') {
            if (navAlarmes) navAlarmes.style.display = 'inline-block';
            if (navPaineis) navPaineis.style.display = 'none';
            if (navHome)    navHome.style.display    = 'none';
            if (navMapa)    navMapa.style.display    = 'none';
            if (navMonitorizacao) navMonitorizacao.style.display = 'none';
            if (navVeiculos)      navVeiculos.style.display      = 'none';
            if (navRotas)         navRotas.style.display         = 'none';
            if (navEnviarAlarme)  navEnviarAlarme.style.display  = 'none';
        }
    } else {
        if (navLogin)         navLogin.style.display         = 'inline-block';
        if (navLogout)        navLogout.style.display        = 'none';
        if (navAlarmes)       navAlarmes.style.display       = 'none';
        if (navPaineis)       navPaineis.style.display       = 'none';
        if (navMonitorizacao) navMonitorizacao.style.display = 'none';
        if (navVeiculos)      navVeiculos.style.display      = 'none';
        if (navRotas)         navRotas.style.display         = 'none';
        if (navEnviarAlarme)  navEnviarAlarme.style.display  = 'none';
    }
}

function garantirItemNavbar(id, texto, href, beforeId, onclick) {
    if (document.getElementById(id)) return;

    const navLinks = document.querySelector('nav .nav-links');
    if (!navLinks) return;

    const item = document.createElement('li');
    item.id = id;
    item.style.display = 'none';

    const link = document.createElement('a');
    link.href = href;
    link.textContent = texto;
    if (onclick) link.setAttribute('onclick', onclick);

    item.appendChild(link);

    const before = beforeId ? document.getElementById(beforeId) : null;
    if (before) {
        navLinks.insertBefore(item, before);
    } else {
        navLinks.appendChild(item);
    }
}

document.addEventListener("DOMContentLoaded", function () {
    verificarAcessos();
});

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

///////////////////// Veículos por paragem /////////////////////

async function carregarTabelaVeiculosPorParagem(stopId) {
    const section   = document.getElementById('stopsVehiclesSection');
    const container = document.getElementById('stops-vehicles-list');
    if (!container || !section) return;
    container.innerHTML = `<div style="color: var(--muted);">A carregar veículos...</div>`;
    section.style.display = 'block';

    try {
        const resposta = await fetch(`/api/stops/${stopId}/vehicles`);
        const vehicles = await resposta.json();

        if (!vehicles || vehicles.length === 0) {
            container.innerHTML = `<div style="color: var(--muted);">Nenhum veículo atribuído a esta paragem.</div>`;
            return;
        }

        const table = document.createElement('table');
        table.style.width = '100%';
        table.style.borderCollapse = 'collapse';
        table.style.marginTop = '1rem';
        table.innerHTML = `
            <thead>
                <tr>
                    <th style="text-align:left; padding:8px;">Placa</th>
                    <th style="text-align:left; padding:8px;">Modelo</th>
                    <th style="text-align:left; padding:8px;">Estado</th>
                    <th style="text-align:left; padding:8px;">Capacidade</th>
                </tr>
            </thead>
            <tbody></tbody>
        `;
        const tbody = table.querySelector('tbody');
        vehicles.forEach(v => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="padding:8px; border-top:1px solid rgba(255,255,255,0.06);">${v.plate}</td>
                <td style="padding:8px; border-top:1px solid rgba(255,255,255,0.06);">${v.model ?? '—'}</td>
                <td style="padding:8px; border-top:1px solid rgba(255,255,255,0.06);">${v.status ?? '—'}</td>
                <td style="padding:8px; border-top:1px solid rgba(255,255,255,0.06);">${v.capacity != null ? v.capacity : '—'}</td>
            `;
            tbody.appendChild(tr);
        });
        container.innerHTML = '';
        container.appendChild(table);
    } catch (err) {
        console.error('Erro ao carregar veículos por paragem:', err);
        container.innerHTML = `<div style="color: var(--danger);">Erro ao carregar veículos.</div>`;
    }
}

function mostrarTabelaVeiculosPorParagem(id, stopName) {
    const title   = document.getElementById('stopVehiclesTitle');
    const section = document.getElementById('stopsVehiclesSection');
    if (title)   title.innerText = `Veículos na paragem: ${stopName}`;
    if (section) section.style.display = 'block';
    carregarTabelaVeiculosPorParagem(id);
}

///////////////////// Navegação por secções (index.html) /////////////////////

const SECCOES_INDEX = ['home', 'about', 'contact', 'alarmes', 'enviar-alarme'];

function mostrarSecao(idAlvo) {
    SECCOES_INDEX.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });

    const seccaoAtiva = document.getElementById(idAlvo);
    if (seccaoAtiva) {
        seccaoAtiva.style.display = 'block';
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (idAlvo === 'alarmes')       carregarAlarmes();
        if (idAlvo === 'enviar-alarme') carregarMeusAlarmes();
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const eIndexPage = !!document.getElementById('home');
    if (!eIndexPage) return;

    const utilizadorGuardado = localStorage.getItem('utilizadorAtivo');
    if (utilizadorGuardado) {
        const utilizador = JSON.parse(utilizadorGuardado);
        // Supervisor não devia estar aqui — redireciona
        if (utilizador.role === 'SUPERVISOR') {
            window.location.href = 'supervisor.html';
            return;
        }
        // Worker vai para secção de envio
        if (utilizador.role === 'WORKER') {
            mostrarSecao('enviar-alarme');
            return;
        }
    }

    ['home', 'about', 'contact'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'block';
    });

    const hashSection = window.location.hash.replace('#', '');
    if (hashSection && SECCOES_INDEX.includes(hashSection)) {
        mostrarSecao(hashSection);
    }
});

///////////////////// WORKER — Enviar Alarme /////////////////////

async function workerEnviarAlarme() {
    const tipo       = document.getElementById('alarme-tipo').value;
    const severidade = document.getElementById('alarme-severidade').value;
    const source     = document.getElementById('alarme-source').value.trim();
    const descricao  = document.getElementById('alarme-descricao').value.trim();
    const feedback   = document.getElementById('alarme-feedback');

    if (!tipo) { mostrarFeedback(feedback, 'Por favor, seleciona o tipo de alarme.', 'error'); return; }
    if (!descricao) { mostrarFeedback(feedback, 'Por favor, escreve uma descrição.', 'error'); return; }

    const username = getUsernameAtivo();
    try {
        const params = new URLSearchParams({ username, type: tipo, description: descricao, severity: severidade, source });
        const resposta = await fetch(`/api/alerts/submit?${params}`, { method: 'POST' });
        if (resposta.ok) {
            mostrarFeedback(feedback, '✅ Alarme enviado com sucesso! O supervisor irá rever em breve.', 'success');
            document.getElementById('alarme-tipo').value = '';
            document.getElementById('alarme-severidade').value = 'MEDIUM';
            document.getElementById('alarme-source').value = '';
            document.getElementById('alarme-descricao').value = '';
            await carregarMeusAlarmes();
        } else {
            mostrarFeedback(feedback, '❌ Erro ao enviar o alarme. Tenta novamente.', 'error');
        }
    } catch (err) {
        console.error("Erro ao enviar alarme:", err);
        mostrarFeedback(feedback, '❌ Erro de ligação ao servidor.', 'error');
    }
}

function mostrarFeedback(el, mensagem, tipo) {
    el.style.display    = 'block';
    el.style.padding    = '0.75rem 1rem';
    el.style.borderRadius = '8px';
    el.style.fontSize   = '0.9rem';
    el.style.fontWeight = '500';
    if (tipo === 'success') {
        el.style.background = 'rgba(46,204,113,0.12)';
        el.style.border     = '1px solid rgba(46,204,113,0.3)';
        el.style.color      = '#2ecc71';
    } else {
        el.style.background = 'rgba(231,76,60,0.12)';
        el.style.border     = '1px solid rgba(231,76,60,0.3)';
        el.style.color      = '#ff8f85';
    }
    el.textContent = mensagem;
    setTimeout(() => { el.style.display = 'none'; }, 5000);
}

async function carregarMeusAlarmes() {
    const username = getUsernameAtivo();
    if (!username) return;
    const body = document.getElementById('my-alerts-body');
    if (!body) return;

    try {
        const resposta = await fetch(`/api/alerts/my?username=${encodeURIComponent(username)}`);
        const alertas  = await resposta.json();
        body.innerHTML = '';

        if (alertas.length === 0) {
            body.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--muted); padding:2rem;">Ainda não enviaste nenhum alarme.</td></tr>`;
            return;
        }

        alertas.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        alertas.forEach(alerta => {
            const data       = alerta.createdAt ? new Date(alerta.createdAt).toLocaleString('pt-PT') : '—';
            const statusInfo = traduzirStatus(alerta.status);
            body.innerHTML += `
                <tr>
                    <td>${alerta.id}</td>
                    <td>${alerta.type ?? '—'}</td>
                    <td class="severity-${(alerta.severity ?? '').toLowerCase()}">${alerta.severity ?? '—'}</td>
                    <td>${alerta.source || '—'}</td>
                    <td><span style="background:${statusInfo.bg}; color:${statusInfo.color}; padding:0.2rem 0.6rem; border-radius:4px; font-size:0.78rem; font-weight:700;">${statusInfo.label}</span></td>
                    <td style="color:var(--muted); font-size:0.85rem;">${data}</td>
                </tr>
            `;
        });
    } catch (err) {
        console.error("Erro ao carregar meus alarmes:", err);
        body.innerHTML = `<tr><td colspan="6" style="text-align:center; color:var(--danger); padding:2rem;">Erro ao carregar histórico.</td></tr>`;
    }
}

function traduzirStatus(status) {
    switch (status) {
        case 'ACTIVE':   return { label: 'Aguarda supervisor',  bg: 'rgba(52,152,219,0.15)',  color: 'var(--info)' };
        case 'PENDING':  return { label: 'Devolvido',           bg: 'rgba(231,76,60,0.15)',   color: 'var(--danger)' };
        case 'ACCEPTED': return { label: 'Em análise (Admin)',  bg: 'rgba(243,156,18,0.15)',  color: 'var(--warning)' };
        case 'RESOLVED': return { label: 'Resolvido',           bg: 'rgba(46,204,113,0.15)', color: 'var(--success)' };
        default:         return { label: status,                bg: 'rgba(255,255,255,0.07)', color: 'var(--muted)' };
    }
}

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
    if (role === 'SUPERVISOR') await carregarAlarmesParaSupervisor();
    else if (role === 'ADMIN') await carregarAlarmesParaAdmin();
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
            body.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--muted); padding:2rem;">Sem alertas para rever.</td></tr>`;
            return;
        }

        [...pending, ...active].forEach(alert => {
            const isPending  = alert.status === 'PENDING';
            const motivoHtml = isPending && alert.inconsistentReason
                ? `<div style="margin-top:0.4rem; padding:0.5rem 0.75rem; background:rgba(231,76,60,0.1); border-left:3px solid var(--danger); border-radius:4px; font-size:0.82rem; color:#ff8f85;">
                       <strong>Motivo devolvido:</strong> ${alert.inconsistentReason}
                   </div>`
                : '';
            const workerHtml = alert.createdBy
                ? `<div style="font-size:0.78rem; color:var(--muted); margin-top:0.3rem;">Enviado por: <strong>${alert.createdBy}</strong></div>`
                : '';
            const badgeStatus = isPending
                ? `<span style="background:rgba(231,76,60,0.15);color:var(--danger);padding:0.2rem 0.6rem;border-radius:4px;font-size:0.78rem;font-weight:700;">DEVOLVIDO</span>`
                : `<span style="background:rgba(52,152,219,0.15);color:var(--info);padding:0.2rem 0.6rem;border-radius:4px;font-size:0.78rem;font-weight:700;">NOVO</span>`;

            body.innerHTML += `
                <tr>
                    <td>${alert.id}</td>
                    <td>${alert.type ?? '—'}${workerHtml}</td>
                    <td class="severity-${(alert.severity ?? '').toLowerCase()}">${alert.severity ?? '—'}</td>
                    <td>${badgeStatus}${motivoHtml}</td>
                    <td>
                        <button onclick="supervisorAceitarAlerta(${alert.id})" class="btn-acao">✅ Aceitar</button>
                        <button onclick="abrirModalEstadoAcoes(${alert.id})" class="btn-acao">⚙️ Processar</button>
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
        await carregarAlarmesParaSupervisor();
    } catch (err) {
        console.error("Erro ao aceitar alerta:", err);
    }
}

// ── MODAL ESTADO E AÇÕES (SUPERVISOR) ──────────────────

async function abrirModalEstadoAcoes(alertId) {
    const anterior = document.getElementById('modal-estado-acoes');
    if (anterior) anterior.remove();

    const modal = document.createElement('div');
    modal.id = 'modal-estado-acoes';
    modal.style.cssText = `
        position: fixed; inset: 0; z-index: 9999;
        background: rgba(7,14,22,0.85);
        display: flex; align-items: center; justify-content: center;
    `;

    let opcoes = {};
    try {
        const res = await fetch('/api/alerts/options');
        if (res.ok) opcoes = await res.json();
    } catch (e) {
        console.error("Erro ao carregar opções:", e);
    }

    const acoes  = opcoes.acoes || [];
    const estados = ['ACTIVE', 'PENDING', 'ACCEPTED', 'RESOLVED'];

    const acoesBotoes  = acoes.map(acao => `
        <label style="display: flex; align-items: center; margin-bottom: 0.6rem; cursor: pointer;">
            <input type="checkbox" value="${acao}" style="margin-right: 0.5rem; cursor: pointer;">
            <span style="color: var(--white); font-size: 0.9rem;">${acao}</span>
        </label>
    `).join('');

    const estadosBotoes = estados.map(e => `<option value="${e}">${e}</option>`).join('');

    modal.innerHTML = `
        <div style="
            background: var(--navy-light);
            border: 1px solid rgba(245,166,35,0.2);
            border-radius: 12px;
            padding: 2rem 2.5rem;
            width: 100%; max-width: 520px;
            box-shadow: 0 8px 40px rgba(0,0,0,0.5);
            animation: fadeUp 0.2s ease both;
            max-height: 90vh; overflow-y: auto;
        ">
            <h3 style="font-family:var(--font-display); color:var(--amber); font-size:1.2rem; margin-bottom:1rem;">
                ⚙️ Estado e Ações Recomendadas
            </h3>
            <div style="margin-bottom: 1.5rem;">
                <label style="display: block; font-weight: 600; margin-bottom: 0.5rem; color: var(--white); font-size: 0.95rem;">Estado do Alarme</label>
                <select id="novo-estado" style="width:100%; padding:0.75rem 1rem; background:var(--navy); border:1px solid rgba(255,255,255,0.1); border-radius:8px; color:var(--white); font-family:var(--font-body); font-size:0.95rem; outline:none;">
                    ${estadosBotoes}
                </select>
            </div>
            <div style="margin-bottom: 1.5rem;">
                <label style="display: block; font-weight: 600; margin-bottom: 0.75rem; color: var(--white); font-size: 0.95rem;">Ações Recomendadas ao Motorista</label>
                <div style="background:var(--navy); border:1px solid rgba(255,255,255,0.1); border-radius:8px; padding:1rem; max-height:250px; overflow-y:auto;">
                    ${acoesBotoes || '<span style="color:var(--muted);">Sem ações disponíveis.</span>'}
                </div>
            </div>
            <div style="display:flex; gap:0.75rem; justify-content:flex-end;">
                <button onclick="fecharModalEstadoAcoes()" style="background:transparent; border:1px solid rgba(255,255,255,0.15); color:var(--muted); border-radius:7px; font-family:var(--font-display); font-size:0.82rem; font-weight:600; letter-spacing:0.05em; text-transform:uppercase; padding:0.6rem 1.2rem; cursor:pointer;">Cancelar</button>
                <button onclick="enviarEstadoEAcoes(${alertId})" style="background:var(--info); border:none; color:white; border-radius:7px; font-family:var(--font-display); font-size:0.82rem; font-weight:700; letter-spacing:0.05em; text-transform:uppercase; padding:0.6rem 1.4rem; cursor:pointer;">Enviar</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

function fecharModalEstadoAcoes() {
    const modal = document.getElementById('modal-estado-acoes');
    if (modal) modal.remove();
}

async function enviarEstadoEAcoes(alertId) {
    const novoEstado = document.getElementById('novo-estado').value;
    if (!novoEstado) { alert('Seleciona um estado para o alarme.'); return; }

    const checkboxes = document.querySelectorAll('#modal-estado-acoes input[type="checkbox"]:checked');
    const acoesSelecionadas = Array.from(checkboxes).map(cb => cb.value);
    const username = getUsernameAtivo();

    const payload = {
        newStatus: novoEstado,
        recommendedActionsJson: JSON.stringify(acoesSelecionadas),
        username
    };

    try {
        const resposta = await fetch(`/api/alerts/${alertId}/update-state-and-actions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (resposta.ok) {
            fecharModalEstadoAcoes();
            await carregarAlarmesParaSupervisor();
        } else {
            alert('Erro ao atualizar o alarme: ' + resposta.status);
        }
    } catch (erro) {
        console.error('Erro:', erro);
        alert('Erro ao enviar as alterações.');
    }
}

// ── ADMIN ────────────────────────────────────────────

async function carregarAlarmesParaAdmin() {
    try {
        const resAccepted = await fetch('/api/alerts/accepted');
        const alerts = await resAccepted.json();

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
            const workerHtml = alert.createdBy
                ? `<div style="font-size:0.78rem; color:var(--muted); margin-top:0.3rem;">Enviado por: <strong>${alert.createdBy}</strong></div>`
                : '';
            body.innerHTML += `
                <tr>
                    <td>${alert.id}</td>
                    <td>${alert.type ?? '—'}${workerHtml}</td>
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
        <div style="background:var(--navy-light); border:1px solid rgba(245,166,35,0.2); border-radius:12px; padding:2rem 2.5rem; width:100%; max-width:460px; box-shadow:0 8px 40px rgba(0,0,0,0.5); animation:fadeUp 0.2s ease both;">
            <h3 style="font-family:var(--font-display); color:var(--amber); font-size:1.2rem; margin-bottom:0.5rem;">Marcar como Inconsistente</h3>
            <p style="color:var(--muted); font-size:0.88rem; margin-bottom:1.2rem;">Escreve o motivo. O supervisor irá receber este alerta de volta com a tua justificação.</p>
            <textarea id="motivo-inconsistente" placeholder="Descreve o motivo de inconsistência..." style="width:100%; min-height:110px; resize:vertical; background:var(--navy); border:1px solid rgba(255,255,255,0.1); border-radius:8px; color:var(--white); font-family:var(--font-body); font-size:0.93rem; padding:0.75rem 1rem; outline:none;" onfocus="this.style.borderColor='var(--amber)'" onblur="this.style.borderColor='rgba(255,255,255,0.1)'"></textarea>
            <div style="display:flex; gap:0.75rem; margin-top:1.2rem; justify-content:flex-end;">
                <button onclick="fecharModalInconsistente()" style="background:transparent; border:1px solid rgba(255,255,255,0.15); color:var(--muted); border-radius:7px; font-family:var(--font-display); font-size:0.82rem; font-weight:600; letter-spacing:0.05em; text-transform:uppercase; padding:0.6rem 1.2rem; cursor:pointer;">Cancelar</button>
                <button onclick="adminMarcarInconsistente(${alertId})" style="background:var(--danger); border:none; color:white; border-radius:7px; font-family:var(--font-display); font-size:0.82rem; font-weight:700; letter-spacing:0.05em; text-transform:uppercase; padding:0.6rem 1.4rem; cursor:pointer;">Enviar</button>
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
        await fetch(`/api/alerts/${id}/inconsistent?username=${encodeURIComponent(username)}&reason=${encodeURIComponent(motivo)}`, { method: 'POST' });
        fecharModalInconsistente();
        await carregarAlarmes();
    } catch (err) {
        console.error("Erro ao marcar inconsistente:", err);
    }
}
function exportarRelatorioPDF() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    // Seleciona o conteúdo do relatório
    const reportContent = document.getElementById("reportSummary").innerText + "\n";

    // Adiciona conteúdo ao PDF
    doc.text(reportContent, 10, 10);

    // Se quiser incluir a tabela, você pode iterar pelas linhas da tabela
    const table = document.getElementById("table-reports");
    if (table) {
        let startY = 20;
        Array.from(table.rows).forEach(row => {
            const rowText = Array.from(row.cells).map(cell => cell.innerText).join(" | ");
            doc.text(rowText, 10, startY);
            startY += 10;
        });
    }

    // Salvar PDF
    doc.save("relatorio.pdf");
}